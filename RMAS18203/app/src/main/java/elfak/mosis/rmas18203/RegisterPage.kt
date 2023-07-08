package elfak.mosis.rmas18203

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import elfak.mosis.rmas18203.databinding.ActivityRegisterPageBinding

class RegisterPage : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextPassRep : TextInputEditText
    private lateinit var signUp: Button
    private lateinit var signIn: TextView
    // private lateinit var profileImage: ImageView
    private lateinit var selectPhotoButton: Button

    private lateinit var binding: ActivityRegisterPageBinding
    private var CAMERA_REQUEST_CODE = 1
    private  var GALLERY_REQUEST_CODE = 2


    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        binding = ActivityRegisterPageBinding.inflate(layoutInflater)
        //setContentView(binding.root)


        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        editTextPassRep = findViewById(R.id.passwordRepeated)
        signIn = findViewById(R.id.sign_in)
        signUp = findViewById(R.id.sign_up)
       // profileImage = findViewById(R.id.profile_image)
        selectPhotoButton = findViewById(R.id.select_photo_button)



        signIn.setOnClickListener {
            val intent = Intent(this@RegisterPage, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        selectPhotoButton.setOnClickListener{
            showOptionsDialog()
        }

        signUp.setOnClickListener {
            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()
            val passRep : String = editTextPassRep.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this@RegisterPage, "Uneti Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this@RegisterPage, "Uneti šifru", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(password.compareTo(passRep) != 0)
            {
                Toast.makeText(this@RegisterPage, "Šifra i ponovljena šifra se razlikuju", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                        task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@RegisterPage, "Registration Successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterPage, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else
                    {
                        Toast.makeText(this@RegisterPage, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                }

        }

    }


    private fun showOptionsDialog() {
        val dialogView : View = layoutInflater.inflate(R.layout.dialog_photo, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        val btnCaptureImage = dialogView.findViewById<Button>(R.id.btnCaptureImage)
        btnCaptureImage.setOnClickListener {
            // Handle capture image option

            cameraCheckPermission()

            dialog.dismiss()
        }

        val btnInsertFromGallery = dialogView.findViewById<Button>(R.id.btnInsertFromGallery)
        btnInsertFromGallery.setOnClickListener {
            // Handle insert from gallery option
            dialog.dismiss()
        }
    }

    private fun cameraCheckPermission() {

        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(
                object: MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if(report.areAllPermissionsGranted()){
                                camera()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRotationalDialogForPermission()
                    }
                }
            ).onSameThread().check()

    }

    private fun camera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                CAMERA_REQUEST_CODE -> {

                    val bitmap = data?.extras?.get("data") as Bitmap
                    binding.profileImage.load(data?.data){
                        crossfade(true)
                        crossfade(1000)
                    transformations(CircleCropTransformation())
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    binding.profileImage.load(data?.data){
                        crossfade(true)
                        crossfade(1000)
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }
    }

    private fun showRotationalDialogForPermission(){
        AlertDialog.Builder(this)
            .setMessage("Izgleda ste isključili dozvole potrebne za ovu funckiju. Možete ih uključiti u podešavanjima aplikacije.")
            .setPositiveButton("Idi u podešavanja:"){_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch(exc: ActivityNotFoundException){
                    exc.printStackTrace()
                }
            }
            .setNegativeButton("Poništi"){dialog,_ ->
                dialog.dismiss()
            }.show()

        }


}