package elfak.mosis.rmas18203.data

data class Place(var name: String = "",
                 var type: String="",
                 var description: String="",
                 var creatorID: Number=0,
                 var longitude: Double=0.0,
                 var latitude: Double=0.0,
                 var dateCreated: String="",
                 var timeCreated : Long=0,
                 var comments: ArrayList<String> = ArrayList(),
                 var ratings: ArrayList<Int> = ArrayList(),
                 var id: String="")

//Mozda treba ubaciti listu dogadjaja/ listu knjiga koje su vezani za tu lokaciju
//i mozda bih izbacila ID, jer je to vec u bazi