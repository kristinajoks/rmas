package elfak.mosis.rmas18203

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import elfak.mosis.rmas18203.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUp.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterPage::class.java)
            startActivity(intent)
            finish()
        }

        binding.signIn.setOnClickListener {
            val email=binding.email.text.toString()
            val password=binding.password.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this@MainActivity, "Uneti Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this@MainActivity, "Uneti šifru", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Prijava uspešna", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, HomePage::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this@MainActivity, "Autentifikacija neuspešna", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}