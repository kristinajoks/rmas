package elfak.mosis.rmas18203.fragments

import PlacePurposeAdapter
import PlaceTypeAdapter
import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import elfak.mosis.rmas18203.R
import elfak.mosis.rmas18203.data.*
import elfak.mosis.rmas18203.databinding.FragmentMapBinding
import elfak.mosis.rmas18203.models.PlaceViewModel
import elfak.mosis.rmas18203.models.UserViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.*


class MapFragment : Fragment() {

    private var _binding : FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView
    private  val placeViewModel: PlaceViewModel by activityViewModels() //viewModels()//by activityViewModels() bi trebalo ali nece
    private var centerMe : Boolean = false
    private lateinit var firebaseAuth: FirebaseAuth
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var editName : TextInputEditText
    private lateinit var editLatitude : TextInputEditText
    private lateinit var editLongitude : TextInputEditText
    private lateinit var editDescription : TextInputEditText
    private lateinit var editType : Spinner
    private lateinit var editPurpose : Spinner
    private lateinit var addButton : Button
    private lateinit var cancelButton : Button
    private lateinit var searchText : EditText
    private lateinit var searchButton: Button
    private lateinit var filterButton : Button
    private lateinit var clearButton: Button

    var completedTasks = 0
    var compl = false
    private var userLocation: Location? = Location("initial")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var ctx: Context? = getActivity()?.getApplicationContext()
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        searchText =  requireView().findViewById(R.id.searchBar)
        filterButton = requireView().findViewById(R.id.filterButton)
        searchButton = requireView().findViewById(R.id.searchButton)
        clearButton = requireView().findViewById(R.id.clearButton)
        clearButton.visibility = View.GONE

        searchButton.setOnClickListener{
            searchForMarker(searchText.text.toString())
            searchText.text.clear()
        }

        filterButton.setOnClickListener {
            filterButtonListener()
        }

        clearButton.setOnClickListener {
            observePlaces()
            clearButton.visibility = View.GONE
        }

        map = requireView().findViewById(R.id.mapView)
        map.setMultiTouchControls(true)

        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else{
            //fetching + setting markers
            placeViewModel.fetchPlaces()
            observePlaces()
        }

        map.controller.setZoom(17.0)

        if(!centerMe) {
            var startPoint = GeoPoint(43.3209, 21.8958)
            map.controller.setCenter(startPoint)
        }

        //


    }

    private fun filterButtonListener() {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView: View = inflater.inflate(R.layout.dialog_filter, null)

        val autorLayout : LinearLayout = dialogView.findViewById(R.id.autorLayout)
        val tipLayout: LinearLayout = dialogView.findViewById(R.id.tipLayout)
        val svrhaLayout: LinearLayout = dialogView.findViewById(R.id.svrhaLayout)
        val datumOd : EditText = dialogView.findViewById(R.id.datumOd)
        val datumDo : EditText = dialogView.findViewById(R.id.datumDo)
        val vremeOd : EditText = dialogView.findViewById(R.id.vremeOd)
        val vremeDo : EditText = dialogView.findViewById(R.id.vremeDo)
        val radijus : EditText = dialogView.findViewById(R.id.radijus)
        val poslInterakcija : CheckBox = dialogView.findViewById(R.id.poslInterakcija)

        //autor
//        userViewModel.allUsers.observe(viewLifecycleOwner, { users ->
//            for (user in users) {
//                val checkBox = CheckBox(requireContext())
//                checkBox.text = "${user.firstName} ${user.lastName}"
//                checkBox.tag = "${user.firstName} ${user.lastName}"
//                autorLayout.addView(checkBox)
//            }
//        })
        autorLayout.visibility = View.GONE

        //tip
        for (placeType in PlaceType.values()) {
            val checkBox = CheckBox(requireContext())
            checkBox.text = placeType.toString()
            checkBox.tag = placeType.toString()
            tipLayout.addView(checkBox)
        }

        //svrha
        for (placePurpose in PlacePurpose.values()) {
            val checkBox = CheckBox(requireContext())
            checkBox.text = placePurpose.toString()
            checkBox.tag = placePurpose.toString()
            svrhaLayout.addView(checkBox)
        }

        //datum
        datumOd.setOnClickListener {
            showDatePickerDialog(datumOd)
        }

        datumDo.setOnClickListener {
            showDatePickerDialog(datumDo)
        }
        
        //vreme
        vremeOd.setOnClickListener {
            showTimePickerDialog(vremeOd)
        }
        
        vremeDo.setOnClickListener {
            showTimePickerDialog(vremeDo)
        }

        //preuzimanje vrednosti iz dijaloga
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        builder.setPositiveButton("Apply") { dialog, which ->

            val selectedAuthors = mutableListOf<String>()
            val selectedTypes = mutableListOf<String>()
            val selectedPurposes = mutableListOf<String>()

            // Capture selected authors
            for (i in 0 until autorLayout.childCount) {
                val view = autorLayout.getChildAt(i)
                Log.d("nebitno",  "Da vidimo dokle ide: ${view.tag}")
                if (view is CheckBox && view.isChecked) {
                    selectedAuthors.add(view.text.toString())
                }
            }

            for (placeType in PlaceType.values()) {
                val checkBox = dialogView.findViewWithTag<CheckBox>(placeType.toString())
                if (checkBox != null){
                    if(checkBox.isChecked) {
                        selectedTypes.add(placeType.toString())
                    }
                }
            }

            for (placePurpose in PlacePurpose.values()) {
                val checkBox = dialogView.findViewWithTag<CheckBox>(placePurpose.toString())
                if (checkBox != null)
                {
                    if(checkBox.isChecked)
                    {
                        selectedPurposes.add(placePurpose.toString())
                    }
                }
            }

            // Capture selected dates and times
            val startDate = datumOd.text.toString()
            val endDate = datumDo.text.toString()
            val startTime = vremeOd.text.toString()
            val endTime = vremeDo.text.toString()


            // Capture radius value
            val radijusValue = radijus.text.toString()
            var radiusValue = if (radijusValue.isNullOrEmpty()) {
                0.0
            } else {
                radijusValue.toDouble()
            }
            Log.d("nebitno",  "rad: $radiusValue")


            if(!radijusValue.isNullOrEmpty() && !radijusValue.matches(Regex("[0-9.]+"))){
                Toast.makeText(activity, "Radijus mora biti broj", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Capture the last interaction checkbox
            val isLastInteractionSelected = poslInterakcija.isChecked

            // Create FilterOptions instance
            var filterOptions = FilterOptions(
                selectedAuthors,
                selectedTypes,
                selectedPurposes,
                startDate,
                endDate,
                startTime,
                endTime,
                radiusValue.toDouble(),
                isLastInteractionSelected
            )

            filterPlaces(filterOptions)

            //mozda treba ocistiti polja

            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun filterPlaces(filterOptions: FilterOptions) {
    if(filterOptions.selectedAuthors.isEmpty() && filterOptions.selectedTypes.isEmpty() && filterOptions.selectedPurposes.isEmpty() && filterOptions.startDate.isNullOrEmpty() && filterOptions.endDate.isNullOrEmpty() && filterOptions.startTime.isNullOrEmpty() && filterOptions.endTime.isNullOrEmpty() && filterOptions.radius.equals(0.0) && !filterOptions.isLastInteractionSelected){
            Toast.makeText(activity, "Neophodno je popuniti bar jedno polje", Toast.LENGTH_SHORT).show()
            return
        }

        var optionsSelectedNum = 0
        var filteredPlaces : ArrayList<Place> = ArrayList()


//        if (!filterOptions.selectedAuthors.isEmpty()) {
//            //
//            optionsSelectedNum++
//
//            val totalAuthorTasks = filterOptions.selectedAuthors.count() //
//            for (selectedAuthor in filterOptions.selectedAuthors) {
//                val firstName = selectedAuthor.split(" ")[0]
//                val lastName = selectedAuthor.split(" ")[1]
//
////                userViewModel.getUserByName(firstName, lastName).observe(viewLifecycleOwner) { userId ->
////                    userId?.let {
////                        if (!userId.isEmpty()) {
////                            Log.d("nebitno",  "MapFragment: $userId")
////                            val places: ArrayList<Place> = placeViewModel.getPlacesByCreatorID(userId)
////                            for (p in places) {
////                                Log.d("nebitno",  "MapFragment: ${p.name}")
////                                if (!filteredPlaces.contains(p)) {
////                                    filteredPlaces += p
////                                    Log.d("nebitno",  "MapFragment: ${filteredPlaces.count()}")
////                                }
////                            }
////                        }
////                    }
////                }
//                val places = placeViewModel.getPlacesByCreatorName(firstName, lastName)
//                for (p in places) {
//                    Log.d("nebitno", "MapFragment: ${p.name}")
//                    if (!filteredPlaces.contains(p)) {
//                        filteredPlaces += p
//                        Log.d("nebitno", "MapFragment: ${filteredPlaces.count()}")
//                    }
//                }
//
//
//
//                //checkAllTasksCompleted(filteredPlaces, totalAuthorTasks)
//            }
//        }



        if(!filterOptions.selectedTypes.isEmpty()){
            optionsSelectedNum++

            for(selectedType in filterOptions.selectedTypes) {
                Log.d("nebitno",  "selectedtyopes1: ${filteredPlaces.count()}")

                var places : ArrayList<Place> = placeViewModel.getPlacesByType(selectedType)
                Log.d("nebitno",  "types2 ret: ${places.count()}")

                if(optionsSelectedNum > 1 && !filteredPlaces.isEmpty()){
                    //ako je broj opcija veci od jedan, onda se filtriraju vec filtrirani
                    filteredPlaces = filteredPlaces.filter { places.contains(it) } as ArrayList<Place>
                }
                else {
                    for (p in places) {
                        if (!filteredPlaces.contains(p))
                            filteredPlaces += p
                        Log.d("nebitno", "types3: ${filteredPlaces.count()}")

                    }
                }
            }
        }

        Log.d("nebitno",  "tip purp: ${filteredPlaces.count()}")

        if(!filterOptions.selectedPurposes.isEmpty()){
            optionsSelectedNum++

            for(selectedPurpose in filterOptions.selectedPurposes) {
                Log.d("nebitno",  "purp1: ${filteredPlaces.count()}")

                var places : ArrayList<Place> = placeViewModel.getPlacesByPurpose(selectedPurpose)
                Log.d("nebitno",  "pur2 ret: ${places.count()}")

                if(optionsSelectedNum > 1 && !filteredPlaces.isEmpty()){
                    //ako je broj opcija veci od jedan, onda se filtriraju vec filtrirani
                    filteredPlaces = filteredPlaces.filter { places.contains(it) } as ArrayList<Place>
                }
                else {
                    for (p in places) {
                        if (!filteredPlaces.contains(p))
                            filteredPlaces += p
                        Log.d("nebitno", "pur3: ${filteredPlaces.count()}")

                    }
                }
            }
        }

        Log.d("nebitno",  "purp dat: ${filteredPlaces.count()}")

        if(!filterOptions.startDate.isNullOrEmpty() && !filterOptions.endDate.isNullOrEmpty() || !filterOptions.startTime.isNullOrEmpty() && !filterOptions.endTime.isNullOrEmpty()){
            optionsSelectedNum++

            var places : ArrayList<Place> = placeViewModel.getPlacesByDT(filterOptions.startDate.toString(), filterOptions.endDate.toString(), filterOptions.startTime.toString(), filterOptions.endTime.toString())
            Log.d("nebitno", filterOptions.startDate.toString() + filterOptions.endDate.toString() + filterOptions.startTime.toString() + filterOptions.endTime.toString())
            Log.d("nebitno",  "dat1: ${filteredPlaces.count()}")
            Log.d("nebitno",  "dat2 ret: ${places.count()}")

            if(optionsSelectedNum > 1 && !filteredPlaces.isEmpty()){
                //ako je broj opcija veci od jedan, onda se filtriraju vec filtrirani
                filteredPlaces = filteredPlaces.filter { places.contains(it) } as ArrayList<Place>
            }
            else {
                for (p in places) {
                    if (!filteredPlaces.contains(p))
                        filteredPlaces += p
                    Log.d("nebitno", "dat3: ${filteredPlaces.count()}")

                }
            }
        }

        if(!filterOptions.radius.equals(0.0)){
            optionsSelectedNum++

            if(userLocation != null){
                val lat = userLocation!!.latitude
                val lng = userLocation!!.longitude


                var places: ArrayList<Place> =
                    placeViewModel.getPlacesByRadius(lat, lng, filterOptions.radius)

                Log.d("nebitno", "rad1: ${filteredPlaces.count()}")
                Log.d("nebitno", "rad2 ret: ${places.count()}")

                if(optionsSelectedNum > 1 && !filteredPlaces.isEmpty()){
                    //ako je broj opcija veci od jedan, onda se filtriraju vec filtrirani
                    filteredPlaces = filteredPlaces.filter { places.contains(it) } as ArrayList<Place>
                }
                else {
                    for (p in places) {
                        if (!filteredPlaces.contains(p))
                            filteredPlaces += p
                        Log.d("nebitno", "rad3: ${filteredPlaces.count()}")

                    }
                }
            }
        }

        Log.d("nebitno",  "dat posl: ${filteredPlaces.count()}")

        if(filterOptions.isLastInteractionSelected){
            Log.d("nebitno",  "posl1: ${filteredPlaces.count()}")

            firebaseAuth = FirebaseAuth.getInstance()
            var user = firebaseAuth.currentUser
            if(user != null){
                var place : Place = placeViewModel.getPlaceByLastVisitedID(user.uid)
                Log.d("nebitno",  "posl2 ret: ${place}, ${user.uid}")

                if(optionsSelectedNum > 1 && !filteredPlaces.isEmpty()){
                    //ako je broj opcija veci od jedan, onda se filtriraju vec filtrirani
                    filteredPlaces = filteredPlaces.filter { it == place } as ArrayList<Place>
                }
                else if(!place.name.isNullOrEmpty() && !filteredPlaces.contains(place)){
//                    filteredPlaces += place
                    filteredPlaces.clear()
                    filteredPlaces.add(place)
                    Log.d("nebitno",  "posl3: ${filteredPlaces.count()}")

                }
            }
        }

        if(filteredPlaces.isEmpty()){
            Toast.makeText(activity, "Broj mesta: ${filteredPlaces.count()}", Toast.LENGTH_SHORT).show()

            Toast.makeText(activity, "Nema rezultata", Toast.LENGTH_SHORT).show()
            observePlaces()
            return
        }

        map.overlays.clear()

        setMyLocationOverlay()

        for(place in filteredPlaces){
            val marker = Marker(map)
            marker.position = GeoPoint(place.latitude, place.longitude)
            marker.title = place.name
            marker.snippet = place.description

            //marker.icon = resources.getDrawable(R.drawable.img) //proba

            map.overlays.add(marker)
        }


        setOnMapClickOverlay()
        Toast.makeText(activity, "Rezultati su prikazani", Toast.LENGTH_SHORT).show()
        clearButton.visibility = View.VISIBLE

        map.invalidate()
//        val transaction = requireFragmentManager().beginTransaction()
//        transaction.replace(R.id.map_fragment, MapFragment())
//        transaction.addToBackStack(null)
//        transaction.commit()
    }

    private fun showTimePickerDialog(vremeOd: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val selectedTime = "$selectedHour:$selectedMinute"
                vremeOd.setText(selectedTime)
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun checkAllTasksCompleted(filteredPlaces: ArrayList<Place>, totalTasks: Int) {
        completedTasks++
        if (completedTasks == totalTasks) {
            compl = true
            // All tasks are completed, you can now process filteredPlaces
            if (filteredPlaces.isEmpty()) {
                Toast.makeText(activity, "Nema rezultata", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Broj mesta: ${filteredPlaces.count()}", Toast.LENGTH_SHORT).show()
                // Process and display filteredPlaces here
            }
            completedTasks = 0
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val selectedDate = "$formattedDay.$formattedMonth.$selectedYear"
                editText.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun searchForMarker(name: String) {
        if(!map.overlays.isEmpty()){
            for(overlay in map.overlays){
                if(overlay is Marker){
                    if(overlay.title == name){
                        map.controller.setCenter(overlay.position)
                        map.controller.setZoom(17.0)
                    }
                }
            }
        }
    }


    private fun observePlaces(){
            placeViewModel.placesLiveData.observe(viewLifecycleOwner, {places->

                map.overlays.clear()
                setMyLocationOverlay()

                for(place in places){
                    val marker = Marker(map)
                    marker.position = GeoPoint(place.latitude, place.longitude)
                    marker.title = place.name
                    marker.snippet = place.description

                    //marker.icon = resources.getDrawable(R.drawable.img) //proba

                    val infoWindow = CustomInfoWindow(marker,
                        requireContext(),
                    place.dateCreated,
                    place.purpose,
                    place.rating,
                    map)

                    marker.infoWindow = infoWindow

                    map.overlays.add(marker)
                }

                //
                setOnMapClickOverlay()
                //
                map.invalidate()

            })
    }

    private var currentInfoWindow: InfoWindow? = null
    private fun setOnMapClickOverlay() {
        //revise
        map.overlays.add(object : org.osmdroid.views.overlay.Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                val projection = mapView?.projection
                val location = GeoPoint(projection?.fromPixels(e!!.x.toInt(), e!!.y.toInt()))

                var infow = false

                if(currentInfoWindow != null)
                    currentInfoWindow?.close()

                val distanceTreshold = 0.1
                val corrMarker = placeViewModel.findMarkerLocation(location, distanceTreshold, map)
                if(corrMarker != null) {
                    infow = true

                    val dialogV : View = layoutInflater.inflate(R.layout.dialog_pin, null)
                    val dialogBuilder = AlertDialog.Builder(requireContext())
                        .setView(dialogV)
                    val dial = dialogBuilder.create()

                    val btnPinDelete = dialogV.findViewById<Button>(R.id.btnPinDelete)
                    val btnPinRate = dialogV.findViewById<Button>(R.id.btnPinRate)
                    val btnPinInfo = dialogV.findViewById<Button>(R.id.btnPinInfo)

                    val plc = placeViewModel.getPlaceByLatLng(corrMarker.position.latitude, corrMarker.position.longitude)

                    firebaseAuth = FirebaseAuth.getInstance()

                    if(plc.creatorID == firebaseAuth.currentUser?.uid){
                        btnPinDelete.visibility = View.VISIBLE
                    }
                    else{
                        btnPinDelete.visibility = View.GONE
                    }

                    btnPinDelete.setOnClickListener {
                        placeViewModel.deletePlace(plc)
                        dial.dismiss()
                    }

                    btnPinInfo.setOnClickListener {
                        dial.dismiss()
                        corrMarker?.showInfoWindow()
                        currentInfoWindow = corrMarker?.infoWindow
                    }


                    btnPinRate.setOnClickListener {
                        dial.dismiss()

                        val dialogVw: View = layoutInflater.inflate(R.layout.dialog_pin_rate, null)
                        val dialogB = AlertDialog.Builder(requireContext())
                            .setView(dialogVw)
                        val dRate = dialogB.create()
                        dRate.show()

                        val checkBoxBook = dialogVw.findViewById<CheckBox>(R.id.checkBoxBook)
                        val checkBoxEvent = dialogVw.findViewById<CheckBox>(R.id.checkBoxEvent)
                        val bookLayout = dialogVw.findViewById<LinearLayout>(R.id.knjigaImeLayout)

                        bookLayout.visibility = View.GONE

                        if(plc.purpose == PlacePurpose.Pozajmica_knjiga.toString()) {
                            checkBoxBook.visibility = View.VISIBLE
                            checkBoxEvent.visibility = View.GONE
                        }
                        else if(plc.type == PlacePurpose.Literarni_događaji.toString()) {
                            checkBoxBook.visibility = View.GONE
                            checkBoxEvent.visibility = View.VISIBLE
                        }
                        else{
                            checkBoxBook.visibility = View.VISIBLE
                            checkBoxEvent.visibility = View.VISIBLE
                        }

                        checkBoxBook.setOnClickListener{
                            if(checkBoxBook.isChecked) {
                                bookLayout.visibility = View.VISIBLE
                            }
                            else{
                                bookLayout.visibility = View.GONE
                            }
                        }

                        val bookName = dialogVw.findViewById<EditText>(R.id.knjigaImeEditText)

                        val submitBtn = dialogVw.findViewById<Button>(R.id.submitBtn)
                        submitBtn.setOnClickListener {
                            // Retrieve the RatingBar
                            val ratingBar = dialogVw.findViewById<RatingBar>(R.id.ratingBar)
                            val rating = ratingBar.rating.toDouble()

                            // Retrieve the comment EditText
                            val commentEditText = dialogVw.findViewById<EditText>(R.id.commentEditText)
                            val comment = commentEditText.text.toString()

                            // Retrieve the CheckBoxes
                            val hasBorrowedBook = checkBoxBook.isChecked

                            val hasAttendedEvent = checkBoxEvent.isChecked

                            // Now you can use these values as needed, e.g., pass them to your ViewModel or perform other actions.

                            if(rating == 0.0 && comment.isEmpty() && !hasBorrowedBook && !hasAttendedEvent){
                                Toast.makeText(activity, "Morate popuniti barem jedno polje", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }



                            if(rating != 0.0){
                                placeViewModel.addRatingToRatings(rating, plc, firebaseAuth.currentUser?.uid.toString())
                                userViewModel.addPointsToUser(1, firebaseAuth.currentUser?.uid.toString(), plc.name)
                            }

                            if(comment.isNotEmpty()){
                                placeViewModel.addComment(comment, plc, firebaseAuth.currentUser?.uid.toString())
                                userViewModel.addPointsToUser(2, firebaseAuth.currentUser?.uid.toString(), plc.name)
                            }

                            if(hasBorrowedBook){

                                val book = bookName.text.toString()

                                if(!book.isNullOrEmpty()){
                                    Log.d("knjiga",  "MapFragment: ${book}")
                                    userViewModel.addBorrowedBook(book, firebaseAuth.currentUser?.uid.toString())
                                    userViewModel.addPointsToUser(3, firebaseAuth.currentUser?.uid.toString(), plc.name)
                                }
                            }

                            if(hasAttendedEvent){
                                userViewModel.addPointsToUser(5, firebaseAuth.currentUser?.uid.toString(), plc.name)
                            }

                            // Dismiss the dialog
                            dRate.dismiss()
                        }
                    }

                    dial.show()
                }

                if(infow)
                    return true //resicu soon

                val dialogView: View = layoutInflater.inflate(R.layout.dialog_pin_add_info, null)

                //preuzimanje polja iz dijaloga
                editLatitude = dialogView.findViewById(R.id.obj_latitude)
                editName = dialogView.findViewById(R.id.obj_name)
                editDescription = dialogView.findViewById(R.id.obj_desc)
                editLongitude = dialogView.findViewById(R.id.obj_longitude)
                editType = dialogView.findViewById(R.id.objType_spinner)
                editPurpose = dialogView.findViewById(R.id.objPurpose_spinner)

                addButton = dialogView.findViewById(R.id.btnAddInfoObj)
                cancelButton = dialogView.findViewById(R.id.btnCancelInfoObj)


                val typeAdapter = PlaceTypeAdapter(requireContext())
                val purposeAdapter = PlacePurposeAdapter(requireContext())

                editType.adapter = typeAdapter
                editPurpose.adapter = purposeAdapter

                val dialogBuilder = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                val dialog = dialogBuilder.create()
                dialog.show()

                addButton.setOnClickListener{
                    storeLocationFromForm(editName.text.toString(),
                        editType.selectedItem.toString(),
                        editDescription.text.toString(),
                        editPurpose.selectedItem.toString(),
                        editLongitude.text.toString().toDouble(),
                        editLatitude.text.toString().toDouble())

                    dialog.dismiss()
                }

                cancelButton.setOnClickListener{
                    dialog.dismiss()
                }
                //

                editLatitude.setText(location.latitude.toString())
                editLongitude.setText(location.longitude.toString())
                return true
            }
        })
    }

    private fun storeLocationFromForm(name : String, type: String, description: String, purpose: String, longitude: Double, latitude: Double) {
        if(validateForm(name, type, description, purpose, longitude, latitude)){
            firebaseAuth = FirebaseAuth.getInstance()
            val uid = firebaseAuth.currentUser?.uid

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentDate= Date()

            val place = Place(name, type, description, purpose, uid, "", longitude, latitude, dateFormat.format(currentDate), timeFormat.format(currentDate), HashMap<String,String>(), 0,0.0)

            if(placeViewModel.savePlace(place)) {

                editName.text?.clear()
                editDescription.text?.clear()
                editLatitude.text?.clear()
                editLongitude.text?.clear()
                editType.setSelection(0)
                editPurpose.setSelection(0)

                Toast.makeText(activity, "Podaci su sačuvani", Toast.LENGTH_SHORT).show()

            }
            else{
                Toast.makeText(activity, "Podaci nisu sačuvani", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(name : String, type: String, description: String, purpose: String, longitude: Double, latitude: Double) : Boolean {
        if(name.isEmpty() || type.isEmpty() || description.isEmpty() || purpose.isEmpty() || longitude == 0.0 || latitude == 0.0){
            Toast.makeText(activity, "Neophodno je popuniti sva polja", Toast.LENGTH_SHORT).show()
            return false
        }
        else{
            return true
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        isGranted: Boolean ->
        if(isGranted)
        {
            setMyLocationOverlay()
            setOnMapClickOverlay()
        }
    }

    private fun setMyLocationOverlay()
    {
        val fragmentManager = requireFragmentManager()
        val mapFragment = fragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?

        if(activity==null || mapFragment?.isVisible == false)
            return

        var myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(activity), map)
        myLocationOverlay.enableMyLocation()


        myLocationOverlay?.runOnFirstFix {
            val location = myLocationOverlay.myLocation
            if (location != null) // && mapFragment != null
            {
                userLocation?.latitude = location?.latitude ?: 0.0
                userLocation?.longitude = location?.longitude ?: 0.0

                requireActivity().runOnUiThread {
                    map.controller.setCenter(GeoPoint(location.latitude, location.longitude))
                    centerMe = true
                }
            }
        }

        map.overlays.add(myLocationOverlay)
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

}