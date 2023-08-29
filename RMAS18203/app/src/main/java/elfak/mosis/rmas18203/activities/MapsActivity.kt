package elfak.mosis.rmas18203.activities

import android.Manifest
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import elfak.mosis.rmas18203.R
import elfak.mosis.rmas18203.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener, AdapterView.OnItemSelectedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson
    private lateinit var markerList: MutableList<LatLng>

    private

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val PREFERENCE_NAME = "MapPins"
        private const val PREFERENCE_KEY_MARKERS = "markers"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)
        gson = Gson()
        markerList = mutableListOf()


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

        loadSavedMarkers() // Load saved markers when the map is ready
        setUpMap()
    }

    private fun setUpMap() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)

                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLatLong,
                        15f
                    )
                )
            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
        //mMap.addMarker(markerOption

        if (::lastLocation.isInitialized && currentLatLong != getCurrentLocationLatLng()) {
            mMap.addMarker(markerOptions)
            markerList.add(currentLatLong) // Add the marker to the list
            //saveMarkers() // Save the updated list of markers
        }
        else{
            mMap.addMarker(markerOptions)
        }
        saveMarkers()
    }

    override fun onMarkerClick(marker: Marker) : Boolean {

        //uzeti u obzir da onaj koji je napravio pin moze da ga izbrise i radi ovo nanize, a ostali odmah vide info

        val dialogView: View = layoutInflater.inflate(R.layout.dialog_pin, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        val btnPinDel = dialogView.findViewById<Button>(R.id.btnPinDelete)
        val btnPinInfo = dialogView.findViewById<Button>(R.id.btnPinInfo)

        btnPinDel.setOnClickListener {

            marker.remove()
            markerList.remove(marker.position)
            saveMarkers()

            dialog.dismiss()

            mMap.clear()
            placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            loadSavedMarkers()
        }

        btnPinInfo.setOnClickListener {
            dialog.dismiss()

            //val pinInfo = getPinInfo()
            val pinInfo = marker.tag as? PinInfo

            if (pinInfo != null) {
                // Retrieve the desired properties from the pinInfo object
                val option1 = pinInfo.option1
                val option2 = pinInfo.option2
                val inputText = pinInfo.inputText

                val infoString = "Tip objekta: $option1, Opis: $inputText, Tip aktivnosti: $option2"

                // Set the retrieved pin information as the title of the marker
                marker.title = infoString

                // Show the InfoWindow
                marker.showInfoWindow()
            }
        }

        return true
    }

    private fun getCurrentLocationLatLng(): LatLng {
        return if (::lastLocation.isInitialized) {
            LatLng(lastLocation.latitude, lastLocation.longitude)
        } else {
            LatLng(0.0, 0.0) // Default location if lastLocation is not initialized
        }
    }

    override fun onMapClick(latLng: LatLng) {
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_touch, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)


        val dialog = dialogBuilder.create()
        dialog.show()

        val btnPinLoc = dialogView.findViewById<Button>(R.id.btnPinLocation)

        btnPinLoc.setOnClickListener {

            //otvaranje dialog_pin_info
            pinLocListener(latLng)
            dialog.dismiss()

           //placeMarkerOnMap(latLng) //type 1
            //dialog.dismiss()
        }
    }

    //unosenje informacija o objektu
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        val selectedIdem = parent.getItemAtPosition(pos)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    private fun pinLocListener(latLng: LatLng) {
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_pin_add_info, null)
        val spinner1: Spinner = dialogView.findViewById(R.id.objType_spinner)
        val spinner2: Spinner = dialogView.findViewById(R.id.objPurpose_spinner)
        val textInput: EditText = dialogView.findViewById(R.id.obj_desc)

        val button: Button = dialogView.findViewById(R.id.btnAddInfoObj)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        button.setOnClickListener {
            val selectedOption1 = spinner1.selectedItem.toString()
            val selectedOption2 = spinner2.selectedItem.toString()
            val inputText = textInput.text.toString()

            // Create a marker and set its properties
            val markerOptions = MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            val marker = mMap.addMarker(markerOptions)


            // Save the pin information and associate it with the marker
            val pinInfo = PinInfo(selectedOption1, selectedOption2, inputText)
            marker?.tag = pinInfo

            // Dismiss the dialog
            dialog.dismiss()
        }
    }

    data class PinInfo(
        val option1: String,
        val option2: String,
        val inputText: String
    )


    private fun savePinInfo(option1: String, option2: String, inputText: String) {
        val pinInfo = "$option1, $option2, $inputText"

        // Save the pin information using SharedPreferences
        val sharedPreferences = getSharedPreferences("PinInfo", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("pin", pinInfo)
        editor.apply()
    }

    private fun getPinInfo(): String {
        // Retrieve the saved pin information using SharedPreferences
        val sharedPreferences = getSharedPreferences("PinInfo", MODE_PRIVATE)
        return sharedPreferences.getString("pin", "") ?: ""
    }

    //unosenje markera na dodir mape
    private fun saveMarkers() {
        val markerJson = gson.toJson(markerList)
        val editor = sharedPreferences.edit()
        editor.putString(PREFERENCE_KEY_MARKERS, markerJson)
        editor.apply()
    }

    private fun loadSavedMarkers() {
        val markerJson = sharedPreferences.getString(PREFERENCE_KEY_MARKERS, null)
        if (markerJson != null) {
            val markerType = object : TypeToken<List<LatLng>>() {}.type
            val savedMarkers = gson.fromJson<List<LatLng>>(markerJson, markerType)
            markerList.addAll(savedMarkers)

            for (markerLocation in savedMarkers) {
                placeMarkerOnMap(markerLocation)
            }
        }
    }

    private fun deleteAllMarkers() {
        markerList.clear() // Clear the marker list

        // Remove all markers from the map
        mMap.clear()

        // Clear the saved markers from SharedPreferences
        val editor = sharedPreferences.edit()
        editor.remove(PREFERENCE_KEY_MARKERS)
        editor.apply()
    }

}