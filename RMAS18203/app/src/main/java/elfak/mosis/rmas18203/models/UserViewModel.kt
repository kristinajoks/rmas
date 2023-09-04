package elfak.mosis.rmas18203.models

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
        }

        return resultLiveData
    }

    fun getPlaceIdByN(name: String) : LiveData<String?> {
        val resultLiveData = MutableLiveData<String?>()
        val placeViewModel = PlaceViewModel()
        placeViewModel.getPlaceIdByName(name) { placeId: String? ->
            resultLiveData.postValue(placeId)
        }

        return resultLiveData
    }

    fun addPointsToUser(points: Int, uid: String, placeName: String){
        getPlaceIdByN(placeName).observeForever { placeID ->
            repository.addPointsToUser(points, uid, placeID!!)
            addLastVisitedID(placeName, uid)
        }
    }

    fun addPointsBook(points: Int, uid: String){
        repository.addPointsBook(points, uid)
    }

    fun addBorrowedBook(book: String, uid: String) {
        repository.addBorrowedBook(book, uid)
    }

    fun addReadBook(book: String, uid: String) {
        repository.addReadBook(book, uid)
    }

    fun addLastVisitedID(placeName: String, uid: String) {
        repository.addLastVisitedID(placeName, uid)
    }

}