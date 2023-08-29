package elfak.mosis.rmas18203.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import elfak.mosis.rmas18203.Fragments.LeaderboardFragment
import elfak.mosis.rmas18203.Fragments.MapFragment
import elfak.mosis.rmas18203.Fragments.PlacesFragment
import elfak.mosis.rmas18203.R
import elfak.mosis.rmas18203.databinding.ActivityUserProfileBinding

//razmislicemo
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)


        setContentView(binding.root)

        binding.bottomNavigationView2.setOnItemSelectedListener { menuItem ->
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
                    //razmislicemo
                    //replaceFragment(ProfileFragment())
                    val intent = Intent(this@UserProfileActivity, UserProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout2, fragment)
        fragmentTransaction.commit()
    }
}