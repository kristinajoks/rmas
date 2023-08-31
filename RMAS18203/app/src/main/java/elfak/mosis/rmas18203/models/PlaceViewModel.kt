package elfak.mosis.rmas18203.models

import androidx.lifecycle.ViewModel
import elfak.mosis.rmas18203.data.Place

class PlaceViewModel : ViewModel() {
    var placesList : ArrayList<Place> = ArrayList<Place>()


    fun addPlace(place: Place) {
        placesList.add(place)
    }

    fun removePlace(place: Place) {
        placesList.remove(place)
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

    fun getPlaces(startDate: String, endDate: String, startTime: String, endTime: String) : ArrayList<Place> {
        var places : ArrayList<Place> = ArrayList<Place>()
        for (place in placesList) {
            if (place.dateCreated.compareTo(startDate) >= 0 && place.dateCreated.compareTo(endDate) <= 0 && place.timeCreated.toString().compareTo(startTime) >= 0 && place.timeCreated.toString().compareTo(endTime) <= 0) {
                places.add(place)
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
        return places
    }

    fun getLastVisitedPlace(userID : Number) : Place {
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
            if (distance(latitude, longitude, place.latitude, place.longitude) <= radius) {
                places.add(place)
            }
        }
        return places
    }

    //revise
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

}