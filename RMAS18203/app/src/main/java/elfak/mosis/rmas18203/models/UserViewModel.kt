package elfak.mosis.rmas18203.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import elfak.mosis.rmas18203.data.User
import elfak.mosis.rmas18203.repository.UserRepository

class UserViewModel : ViewModel() {

    private val repository : UserRepository
    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers : LiveData<List<User>> = _allUsers

    init{
        repository = UserRepository().getInstance()
        repository.loadUsers(_allUsers)
    }

    fun getRepository(): UserRepository {
        return repository
    }

    fun fetchAllUsers() {
        repository.loadUsers(_allUsers)
    }

    fun getUserByName(firstName: String, lastName: String): LiveData<String?> {
        val resultLiveData = MutableLiveData<String?>()

        repository.getUserIdByName(firstName, lastName) { userId: String? ->
            resultLiveData.postValue(userId)
            Log.d("nebitno",  "UserViewModel: ${userId}")
        }

        return resultLiveData
    }

}