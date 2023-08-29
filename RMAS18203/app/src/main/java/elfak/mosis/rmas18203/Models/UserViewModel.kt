package elfak.mosis.rmas18203.Models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import elfak.mosis.rmas18203.Repository.UserRepository

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

}