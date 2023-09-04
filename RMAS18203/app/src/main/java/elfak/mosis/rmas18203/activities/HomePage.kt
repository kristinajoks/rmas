package elfak.mosis.rmas18203.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
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

class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //proba
        val navigation: BottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        setupWithNavController(navigation, navController)


        binding.bottomNavigationView.setItemIconTintList(null)

        val userId = Firebase.auth.currentUser?.uid

        // preuzimanje url-a slike iz baze
        if (userId != null) {
            getUserProfilePictureUrl(userId) { profilePictureUrl ->
                if (profilePictureUrl != null) {
                    val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    val menuItem: MenuItem = navView.menu.findItem(R.id.profileFragment)

                    // ucitavanje slike uz glide
                    Glide.with(this)
                        .load(profilePictureUrl)
                        .override(48, 48)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .circleCrop()
                        .placeholder(R.drawable.round_account_circle_24)
                        .error(R.drawable.round_account_circle_24)
                        .into(object : CustomTarget<Drawable>() {
                            override fun onLoadCleared(placeholder: Drawable?) {
                               //
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {

                                menuItem.icon = resource
                                Log.d(
                                    "HomePage",
                                    "onResourceReady() - profilePictureUrl: $profilePictureUrl"
                                )
                            }
                        })
                }
            }
        }
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