package elfak.mosis.rmas18203.Activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import elfak.mosis.rmas18203.Models.User
import elfak.mosis.rmas18203.R
import elfak.mosis.rmas18203.databinding.ActivityRegisterPageBinding
import java.io.ByteArrayOutputStream

class RegisterPage : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var image: ImageView
    private lateinit var galleryImage: ActivityResultLauncher<String>



    private var profileImg: String? = ""
    private var CAMERA_REQUEST_CODE = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        image = binding.profileImage
//        binding.signUp.isEnabled = false

        galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            {
                image.setImageURI(it)
                selectedImageUri = it
            })

        binding.selectPhotoButton.setOnClickListener {
            showOptionsDialog()
        }

        binding.signIn.setOnClickListener {
            val intent = Intent(this@RegisterPage, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.signUp.setOnClickListener {
            val name = binding.name.text.toString()
            val surname = binding.surname.text.toString()
            val number = binding.phonenum.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val passRep = binding.passwordRepeated.text.toString()

            if (validateForm(name, surname, number, email, password, passRep)) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val userID = user?.uid
                            if (userID != null) {
                                uploadProfileImageToStorage(userID)
                                saveUserDataToDatabase(userID, name, surname, email, number)

                                Toast.makeText(this, "Uspešna registracija", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }
    }

    private fun validateForm(name:String, surname: String, number: String, email: String, password: String, passRep: String) : Boolean
    {
        if (selectedImageUri == null) {
            Toast.makeText(this@RegisterPage, "Morate odabrati sliku pre nego što se prijavite.", Toast.LENGTH_SHORT).show()
            return false
        } //dodato iznad
        else if (name.isEmpty()) {
            Toast.makeText(this@RegisterPage, "Uneti ime", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (surname.isEmpty()) {
            Toast.makeText(this@RegisterPage, "Uneti prezime", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (number.isEmpty()) {
            Toast.makeText(this@RegisterPage, "Uneti broj telefona", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (!number.startsWith("+3816") && !number.startsWith("06")) {
            Toast.makeText(this@RegisterPage, "Uneti validan broj telefona", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (email.isEmpty()) {
            Toast.makeText(this@RegisterPage, "Uneti Email", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this@RegisterPage, "Uneti validan Email", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (password.isEmpty()) {
            Toast.makeText(this@RegisterPage, "Uneti šifru", Toast.LENGTH_SHORT).show()
            return false
        }
        else if ( password.length < 6) {
            Toast.makeText(this@RegisterPage, "Šifra mora biti dužine minimum 6 karaktera", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(password.compareTo(passRep) != 0)
        {
            Toast.makeText(this@RegisterPage, "Šifra i ponovljena šifra se razlikuju", Toast.LENGTH_SHORT).show()
            return false
        }
        else return true
    }

    private fun showOptionsDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_photo, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        val btnCaptureImage = dialogView.findViewById<Button>(R.id.btnCaptureImage)
        btnCaptureImage.setOnClickListener {
            cameraCheckPermission()
            dialog.dismiss()
        }



        val btnInsertFromGallery = dialogView.findViewById<Button>(R.id.btnInsertFromGallery)
        btnInsertFromGallery.setOnClickListener {
            galleryImage.launch("image/*")
            dialog.dismiss()

        }
    }

    private fun cameraCheckPermission() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
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
            })
            .onSameThread()
            .check()
    }

    private fun camera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    selectedImageUri = getImageUri(bitmap)
                    binding.profileImage.load(selectedImageUri) {
                        crossfade(true)
                        crossfade(1000)
                    }
                    Toast.makeText(this, "Slika je odabrana!", Toast.LENGTH_SHORT).show()

//                    binding.signUp.isEnabled = true //
                }
            }
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("Izgleda ste isključili dozvole potrebne za ovu funkciju. Možete ih uključiti u podešavanjima aplikacije.")
            .setPositiveButton("Idi u podešavanja:") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (exc: ActivityNotFoundException) {
                    exc.printStackTrace()
                }
            }
            .setNegativeButton("Poništi") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun uploadProfileImageToStorage(userID: String) {
        if (selectedImageUri != null) {
            val imageRef = storageRef.child("profile_images/$userID.jpg")

            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
                        databaseReference.child(userID).child("profileImg").setValue(uri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Postavljanje slike neuspešno: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserDataToDatabase(userID: String, name: String, surname: String, email: String, number: String) {
        val user = User(email, name, surname, number, profileImg)

        if (selectedImageUri != null) { //
            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            databaseReference.child(userID).setValue(user)
                .addOnSuccessListener {
                    binding.email.text?.clear()
                    binding.password.text?.clear()
                    binding.passwordRepeated.text?.clear()
                    binding.name.text?.clear()
                    binding.surname.text?.clear()
                    binding.phonenum.text?.clear()
                    profileImg = ""
                    Toast.makeText(this, "Podaci su sačuvani", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Podaci nisu sačuvani", Toast.LENGTH_SHORT).show()
                }

        }
        else{
            Toast.makeText(this, "Morate odabrati sliku pre nego što se prijavite.", Toast.LENGTH_SHORT).show()
        }
    }
}
