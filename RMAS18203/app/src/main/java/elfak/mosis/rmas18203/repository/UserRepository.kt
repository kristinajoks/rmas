package elfak.mosis.rmas18203.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import elfak.mosis.rmas18203.data.User
import elfak.mosis.rmas18203.models.PlaceViewModel

class UserRepository {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("users")


    @Volatile private var INSTANCE : UserRepository ?= null


    fun getInstance() : UserRepository {

        return INSTANCE ?: synchronized(this) {
            val instance = UserRepository()
            INSTANCE = instance
            instance
        }
    }

    fun loadUsers(userList: LiveData<List<User>>){
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val _userList : List<User> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(User::class.java)!!
                    }

                    (userList as MutableLiveData).postValue(_userList)

                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("UserRepository", "loadUsers: ${error.message}")
            }
        })
    }

    fun getUserIdByName(firstName: String, lastName: String, callback: (String?) -> Unit) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val userId = snapshot.children
                        .firstOrNull { dataSnapshot ->
                            val user = dataSnapshot.getValue(User::class.java)
                            user?.firstName == firstName && user?.lastName == lastName
                        }?.key

                    Log.d("nebitno",  "UserRepository: $userId")

                    callback(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("UserRepository", "getUserIdByName: ${error.message}")
                callback(null)
            }
        })
    }

    fun addPointsToUser(points: Int, uid: String, placeID: String) {
        val user = getUserById(uid) { user ->
            user?.let {
                val newPoints = user.points?.plus(points)
                databaseReference.child(uid).child("points").setValue(newPoints)
                databaseReference.child(uid).child("lastVisitedID").setValue(placeID)
                val placeViewModel = PlaceViewModel()
                placeViewModel.addLastUser(placeID, uid)
            }
        }
    }

    fun getUserById(uid: String, callback: (User?) -> Unit) {
        databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val user = snapshot.getValue(User::class.java)
                    callback(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("UserRepository", "getUserById: ${error.message}")
                callback(null)
            }
        })
    }

    fun addBorrowedBook(book: String, uid: String) {
        getUserById(uid) { user ->
            user?.let {
                Log.d("UserRepository", "addBorrowedBook: list ${user.booksTaken}, br el ${user.booksTaken.size}, knjiga ${book}")
                databaseReference.child(uid).child("booksTaken").child(book).setValue(book)
            }
        }
    }

    fun addReadBook(book: String, uid: String) {
        getUserById(uid) { user ->
            user?.let {
                databaseReference.child(uid).child("booksRead").child(book).setValue(book)
                databaseReference.child(uid).child("booksTaken").child(book).removeValue()
            }
        }
    }

    fun addLastVisitedID(placeName: String, uid: String) {
        val placeViewModel = PlaceViewModel()
        placeViewModel.getPlaceIdByName(placeName) { placeId: String? ->
            databaseReference.child(uid).child("lastVisitedID").setValue(placeId)
            placeViewModel.addLastUser(placeId!!, uid)
        }
    }

    fun addPointsBook(points: Int, uid: String) {
        val user = getUserById(uid) { user ->
            user?.let {
                val newPoints = user.points?.plus(points)
                databaseReference.child(uid).child("points").setValue(newPoints)
            }
        }
    }


}