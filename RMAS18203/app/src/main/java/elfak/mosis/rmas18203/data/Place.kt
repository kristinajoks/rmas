package elfak.mosis.rmas18203.data

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem

data class Place(var name: String = "",
                 var type: String= PlaceType.Biblioteka.toString(),
                 var description: String="",
                 var purpose: String= PlacePurpose.Pozajmica_knjiga.toString(),
                 var creatorID: String?="",
                 var lastVisitedID: String?="",
                 var longitude: Double=0.0,
                 var latitude: Double=0.0,
                 var dateCreated: String="",
                 var timeCreated : String="",
                 var comments: HashMap<String,String> = HashMap(),
                 var ratingNum: Int = 0,
                 var rating: Double=0.0
                 ) : OverlayItem(name, description, GeoPoint(latitude, longitude))


// Enum class for Place types
enum class PlaceType {
    Kafić,
    Biblioteka
}

// Enum class for Place purposes
enum class PlacePurpose {
    Pozajmica_knjiga,
    Literarni_događaji,
    Oba
}

data class FilterOptions(
    val selectedAuthors: List<String>,
    val selectedTypes: List<String>,
    val selectedPurposes: List<String>,
    val startDate: String?,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val radius: Double,
    val isLastInteractionSelected: Boolean
)
//Mozda treba ubaciti listu dogadjaja/ listu knjiga koje su vezani za tu lokaciju
//i mozda bih izbacila ID, jer je to vec u bazi


//var comments: ArrayList<String> = ArrayList(),
//                 var ratings: ArrayList<Int> = ArrayList()
//jer nmz da se serijalizuje