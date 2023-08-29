package elfak.mosis.rmas18203.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import elfak.mosis.rmas18203.models.User

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
}