package elfak.mosis.rmas18203.data

data class User(val username:String?=null,
                val firstName:String?= null,
                val lastName:String?= null,
                val phoneNumber: String?=null,
                val profileImg: String?="",
                val points: Int?=0, //odavde dodato
                val lastVisitedID: String?=null,
                val myPlaces : HashMap<String,String> = HashMap(),
                val booksTaken : HashMap<String,String> = HashMap(),
                val booksRead: HashMap<String,String> = HashMap())

//                val myPlaces : ArrayList<String> = ArrayList(),
//                val booksTaken : ArrayList<String> = ArrayList(),
//                val booksRead: ArrayList<String> = ArrayList())
