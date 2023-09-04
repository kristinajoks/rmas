package elfak.mosis.rmas18203.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import elfak.mosis.rmas18203.adapter.FirebasePlace
import elfak.mosis.rmas18203.adapter.toFirebasePlace
import elfak.mosis.rmas18203.adapter.toPlace
import elfak.mosis.rmas18203.data.Place
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat

class PlaceViewModel : ViewModel() {
    var placesList : ArrayList<Place> = ArrayList<Place>()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("places")
    val placesLiveData: LiveData<List<Place>> = MutableLiveData()

    fun savePlace(place: Place) : Boolean {
        val firebasePlace = place.toFirebasePlace()

        var ret : Boolean = true //proba
        val newPlaceRef = databaseReference.push()
        newPlaceRef.setValue(firebasePlace) //ovde zamenjeno
            .addOnSuccessListener {
                ret = true
            }

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(place.creatorID.toString())
        userRef.child("myPlaces").child(newPlaceRef.key.toString()).setValue(newPlaceRef.key.toString()).addOnFailureListener{
            ret = false //caskom zakomm
        }

        return ret
    }


    //mora da se izmene fje jer mora id place a ne name
    fun addRatingToRatings(rating: Double, place: Place, uid: String){

        var newRating = 0.0
        val newRatingNum = 1 + place.ratingNum

        if(place.ratingNum > 0) {
            newRating = (place.rating * place.ratingNum + rating) / newRatingNum
        }
        else{
            newRating = rating
        }

        getPlaceIdByName(place.name){placeId: String? ->
            if(placeId != null){
                databaseReference.child(placeId).child("rating").setValue(newRating)
                databaseReference.child(placeId).child("ratingNum").setValue(newRatingNum)
            }
        }
    }

    fun addComment(comment: String, place: Place, uid: String){
        getPlaceIdByName(place.name){placeId: String? ->
            if(placeId != null){
                databaseReference.child(placeId).child("comments").child(uid).setValue(comment)
            }
        }
    }

    fun getPlaceIdByName(name: String, callback: (String?) -> Unit) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val placeId = snapshot.children
                        .firstOrNull { dataSnapshot ->
                            val place = dataSnapshot.getValue(Place::class.java)
                            place?.name == name
                        }?.key


                    callback(placeId)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }


    fun fetchPlaces(){

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val places = ArrayList<Place>()

                for (placeSnapshot in snapshot.children) {
                    // Deserialize the DataSnapshot into a Place object
                    val fPlace = placeSnapshot.getValue(FirebasePlace::class.java) //i ovde izmenjeno
                    val place = fPlace?.toPlace()
                    place?.let {
                        places.add(it)
                    }
                }

                // Update your placesList with the fetched data
                placesList.clear()
                placesList.addAll(places)

                // Notify any observers that the data has changed
                (placesLiveData as MutableLiveData).value = placesList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("PlaceViewModel", "fetchPlaces() failed: ${error.message}")
            }
        })
    }

    fun addPlace(place: Place) {
        placesList.add(place)
    }

    fun removePlace(place: Place) {
        placesList.remove(place)
    }

    fun deletePlace(place: Place){
        databaseReference.child(place.name).removeValue()
    }

    fun getPlaceByName(name: String): Place? {
        for (place in placesList) {
            if (place.name == name) {
                return place
            }
        }
        return null
    }

    fun getPlacesByDateTime(dateCreated: String, timeCreated: String) : ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            if (place.dateCreated == dateCreated && place.timeCreated.toString() == timeCreated) {
                places.add(place)
            }
        }
        return places
    }

    fun getPlacesByDT(startDate: String, endDate: String, startTime: String, endTime: String) : ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            if(!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty() && !startTime.isNullOrEmpty() && !endTime.isNullOrEmpty()){
                 if (place.dateCreated.compareTo(startDate) >= 0 && place.dateCreated.compareTo(endDate) <= 0 && place.timeCreated.toString().compareTo(startTime) >= 0 && place.timeCreated.toString().compareTo(endTime) <= 0) {
                    places.add(place)
                }
            }
            else if(!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()){

                val dateFormat = SimpleDateFormat("dd.MM.yyyy")
                val dateCr = dateFormat.parse(place.dateCreated)
                val dateSt = dateFormat.parse(startDate)
                val dateEn = dateFormat.parse(endDate)

                if(dateCr.compareTo(dateSt) >= 0 && dateCr.compareTo(dateEn) <= 0){
                    places.add(place)
                }
            }
            else if(!startTime.isNullOrEmpty() && !endTime.isNullOrEmpty()){
                if (place.timeCreated.toString().compareTo(startTime) >= 0 && place.timeCreated.toString().compareTo(endTime) <= 0) {
                    places.add(place)
                }
            }
        }
        return places
    }

    fun getPlacesByType(type: String) : ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            if (place.type == type) {
                places.add(place)
            }
        }

        return places
    }

    fun getPlacesByName(name: String) : ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            if (place.name.contains(name)) {
                places.add(place)
            }
        }
        return places
    }

    fun getPlacesByCreatorID(creatorID: String) : ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            if (place.creatorID.toString() == creatorID) {
                places.add(place)
            }
        }
        Log.d("nebitno", "getPlacesByCreatorID: ${places.size}")
        return places
    }

    fun getPlacesByCreatorName(firstName: String, lastName: String) : ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()

        val userViewModel = UserViewModel()
        userViewModel.getUserByName(firstName, lastName).observeForever { uid ->
            places = getPlacesByCreatorID(uid.toString())
        }
        return places

    }

    fun getLastVisitedPlace(userID : String) : Place {
        var place : Place = Place()
        for (p in placesList) {
            if (p.creatorID == userID) {
                place = p
            }
        }
        return place
    }

    fun getPlacesByDescription(desc : String) : ArrayList<Place>{
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            if (place.description.contains(desc)) {
                places.add(place)
            }
        }
        return places
    }

    //revise
    fun getPlacesByRadius(latitude: Double, longitude: Double, radius: Double) : ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            var d : Double = distance(latitude, longitude, place.latitude, place.longitude)
            if (d <= radius ) {
                Log.d("nebitno", "getPlacesByRadius: ${place.name}, ${d}, ${radius}")
                places.add(place)
            }
        }
        return places
    }


    private fun distance(latitude: Double, longitude: Double, latitude1: Double, longitude1: Double): Double {
        val radius = 6371
        val dLat = Math.toRadians(latitude1 - latitude)
        val dLon = Math.toRadians(longitude1 - longitude)
        val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(latitude1))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = radius * c
        return distance
    }

    fun findMarkerLocation(location: GeoPoint, distanceTreshold: Double, map: MapView) : Marker?
    {
        for(place in placesLiveData.value.orEmpty()){
            val distance = distance(location.latitude, location.longitude, place.latitude, place.longitude)
            if(distance <= distanceTreshold){
                val corrMarker = findCorrespondingMarker(place, map)
                return corrMarker
            }
        }
        return null
    }

    private fun findCorrespondingMarker(place: Place, map: MapView): Marker? {
        for(ov in map.overlays){
            if(ov is Marker){
                val marker = ov as Marker
                if(marker.position.latitude == place.latitude &&
                    marker.position.longitude == place.longitude){
                    return marker
                }
            }
        }
        return null
    }

    fun getPlacesByPurpose(purpose: String): ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            if (place.purpose == purpose) {
                places.add(place)
            }
        }

        return places
    }

    fun getPlaceByLastVisitedID(id: String): Place {
        var place : Place = Place()
        Log.d("nebitno", "getPlaceByLastVisitedID: ${id}")
        for (p in placesList) {
            if (p.lastVisitedID == id) {
                place = p
            }
        }
        return place
    }

    fun getPlaceByLatLng(latitude: Double, longitude: Double): Place {
        var place : Place = Place()
        for (p in placesList) {
            if (p.latitude == latitude && p.longitude == longitude) {
                place = p
            }
        }
        return place
    }

    fun addLastUser(placeID: String, uid: String) {
        databaseReference.child(placeID).child("lastVisitedID").setValue(uid)
    }

}