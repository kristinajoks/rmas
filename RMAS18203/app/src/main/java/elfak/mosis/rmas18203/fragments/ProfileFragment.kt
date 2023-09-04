
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import elfak.mosis.rmas18203.activities.MainActivity
import elfak.mosis.rmas18203.data.User
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

                        val items = it.booksRead.values.toTypedArray()
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)

                        Log.d("nebitno",  "ProfileFragment: ${items}")
                        binding.listView.adapter = adapter
                        val pp = it.points

                        binding.addBookButton.setOnClickListener {
                            if(!binding.bookNameEditText.text.isNullOrEmpty()){
                                val bookName = binding.bookNameEditText.text.toString()
                                Log.d("nebitno",  "ProfileFragment: ${bookName}")

                                if(!items.contains(bookName)){
                                    return@setOnClickListener
                                }
                                Log.d("nebitno",  "ProfileFragment: ${bookName}")
                                userReference.child("booksTaken").child(bookName).removeValue()
                                userReference.child("booksRead").child(bookName).setValue(bookName)
                                userReference.child("points").setValue(pp!!.plus(3))
                                binding.bookNameEditText.text.clear()
                            }
                        }
//
//                        binding.editProfileButton.setOnClickListener {
//                            //OPEN THE DIALOG HERE
//                        }

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

        binding.logOffButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(fragmentContext, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
