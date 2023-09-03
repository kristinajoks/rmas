package elfak.mosis.rmas18203.adapter

import elfak.mosis.rmas18203.data.Place

// DataConversionUtils.kt

data class FirebasePlace(
    var name: String = "",
    var type: String = "",
    var description: String = "",
    var purpose: String = "",
    var creatorID: String? = "",
    var lastVisitedID: String?="",
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var dateCreated: String = "",
    var timeCreated: String = "",
    var comments: HashMap<String,String> = HashMap(),
    var ratings: HashMap<String, Number> = HashMap()
)

fun Place.toFirebasePlace(): FirebasePlace {
    return FirebasePlace(
        this.name,
        this.type,
        this.description,
        this.purpose,
        this.creatorID,
        this.lastVisitedID,
        this.longitude,
        this.latitude,
        this.dateCreated,
        this.timeCreated,
        this.comments,
        this.ratings
    )
}

fun FirebasePlace.toPlace(): Place {
    return Place(
        this.name,
        this.type,
        this.description,
        this.purpose,
        this.creatorID,
        this.lastVisitedID,
        this.longitude,
        this.latitude,
        this.dateCreated,
        this.timeCreated,
        this.comments,
        this.ratings
    )
}
