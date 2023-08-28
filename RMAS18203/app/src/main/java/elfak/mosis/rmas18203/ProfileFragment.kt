package elfak.mosis.rmas18203

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import elfak.mosis.rmas18203.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding : FragmentProfileBinding//    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentProfileBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser

        user?.let {
            val name = it.displayName
            val email = it.email
            val number = it.phoneNumber

        }
    }

}
