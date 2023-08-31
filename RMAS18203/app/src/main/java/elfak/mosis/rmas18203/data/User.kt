package elfak.mosis.rmas18203.data

data class User(val username:String?=null,
                val firstName:String?= null,
                val lastName:String?= null,
                val phoneNumber: String?=null,
                val profileImg: String?="",
                val points: Int?=0,
                val lastVisited: Place?=null,
                val myPlaces : ArrayList<Place> = ArrayList())
