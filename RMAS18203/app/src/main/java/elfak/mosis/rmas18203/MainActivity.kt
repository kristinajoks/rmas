package elfak.mosis.rmas18203

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var signIn: Button
    private lateinit var signUp: TextView
    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        signIn = findViewById(R.id.sign_in)
        signUp = findViewById(R.id.sign_up)

        signUp.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterPage::class.java)
            startActivity(intent)
            finish()
        }

        signIn.setOnClickListener {
            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this@MainActivity, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}