package elfak.mosis.rmas18203.Models

data class User(val username:String?=null,
                val firstName:String?= null,
                val lastName:String?= null,
                val phoneNumber: String?=null,
                val profileImg: String?="",
                val points: Int?=0)
