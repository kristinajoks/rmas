
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import elfak.mosis.rmas18203.Models.User
import elfak.mosis.rmas18203.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userReference: DatabaseReference
    private lateinit var binding : FragmentProfileBinding

    private lateinit var fragmentContext: Context
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
        userReference = database.getReference("users").child(auth.currentUser?.uid ?: "")

        // Load user data from the Realtime Database
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)

                    // Update UI with user data
                    user?.let {
                        binding.firstNameTextView.text = it.firstName
                        binding.lastNameTextView.text = it.lastName
                        binding.usernameTextView.text = it.username
                        binding.pointsTextView.text = "Points: ${it.points}"

                        // Load profile image using Glide
                        Glide.with(fragmentContext)
                            .load(it.profileImg)
                            .into(binding.profileImageView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ProfileFragment", "Greška pri pribavljanju podataka: ${error.message}")
            }
        })

        // Set up the log-off button
        binding.logOffButton.setOnClickListener {
            // Sign out the user and navigate to the login screen
            auth.signOut()
            // Add your navigation logic here, e.g., navigate to the login fragment/activity
        }
    }
}
