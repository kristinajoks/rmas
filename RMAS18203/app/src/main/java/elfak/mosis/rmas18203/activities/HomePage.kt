package elfak.mosis.rmas18203.activities

import ProfileFragment
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import elfak.mosis.rmas18203.R
import elfak.mosis.rmas18203.databinding.ActivityHomePageBinding
import elfak.mosis.rmas18203.fragments.LeaderboardFragment
import elfak.mosis.rmas18203.fragments.MapFragment
import elfak.mosis.rmas18203.fragments.PlacesFragment

class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(MapFragment())

        binding.bottomNavigationView.setItemIconTintList(null)

        // Retrieve the current user's ID (assuming Firebase Authentication is used)
        val userId = Firebase.auth.currentUser?.uid

        // Add the code for retrieving the user's profile picture URL here
        if (userId != null) {
            getUserProfilePictureUrl(userId) { profilePictureUrl ->
                if (profilePictureUrl != null) {
                    val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    val menuItem: MenuItem = navView.menu.findItem(R.id.profile)

                    // Load the image using Glide (ensure you have the Glide dependency in your project)
                    Glide.with(this)
                        .load(profilePictureUrl)
                        .override(48,48)
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Clear cache
                        .circleCrop() // You can add this for a circular profile picture
                        .placeholder(R.drawable.round_account_circle_24)
                        .error(R.drawable.round_account_circle_24)
                        .into(object : CustomTarget<Drawable>() {
                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Handle the case where the image load is cleared
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                // Set the loaded image as the icon for the "Profile" menu item
                                menuItem.icon = resource
                                Log.d("HomePage", "onResourceReady() - profilePictureUrl: $profilePictureUrl")
                            }
                        })
                }
            }
        }


        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    replaceFragment(MapFragment())
                    true
                }
                R.id.places -> {
                    replaceFragment(PlacesFragment())
                    true
                }
                R.id.leaderboard -> {
                    replaceFragment(LeaderboardFragment())
                    true
                }
                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun getUserProfilePictureUrl(userId: String, callback: (String?) -> Unit) {
        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)

        userRef.child("profileImg").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profilePictureUrl = snapshot.getValue(String::class.java)
                callback(profilePictureUrl)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.d("HomePage", "getUserProfilePictureUrl() failed: ${error.message}")
                callback(null)
            }
        })
    }

}