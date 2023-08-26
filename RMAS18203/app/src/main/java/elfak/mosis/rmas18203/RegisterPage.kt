package elfak.mosis.rmas18203
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import elfak.mosis.rmas18203.databinding.ActivityRegisterPageBinding
import java.io.ByteArrayOutputStream

class RegisterPage : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPageBinding
    private  lateinit var  firebaseAuth : FirebaseAuth
    private lateinit var databaseRef : DatabaseReference

    var profileImg:String? = ""
    private var CAMERA_REQUEST_CODE = 1

    //
    lateinit var storage: FirebaseStorage
    private lateinit var image: ImageView
   // private lateinit var galleryImage : ActivityResultLauncher<String>

    private var storageReff = Firebase.storage
    private lateinit var uri: Uri
    //


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //deo za cuvanje slike
        //njihovo
//        storage = Firebase.storage

        // Create a storage reference from our app
//        var storageRef = storage.reference

        image = findViewById(R.id.profile_image)
        storageReff = FirebaseStorage.getInstance()



        //storage tryout

        firebaseAuth = FirebaseAuth.getInstance()


        binding.selectPhotoButton.setOnClickListener{
            showOptionsDialog()
        }

        binding.signIn.setOnClickListener {
            val intent = Intent(this@RegisterPage, MainActivity::class.java)
            startActivity(intent)
            finish()
        }



        binding.signUp.setOnClickListener {
            val name = binding.name.text.toString()
            val surname =binding.surname.text.toString()
            val number = binding.phonenum.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val passRep = binding.passwordRepeated.text.toString()


           databaseRef = FirebaseDatabase.getInstance().getReference("users")
            val user = User(email,name, surname, number, profileImg)
            val databaseReference = FirebaseDatabase.getInstance().reference
            val id = databaseReference.push().key

            databaseRef.child(id.toString()).setValue(user).addOnSuccessListener {
                binding.email.text?.clear()
                binding.password.text?.clear()
                binding.passwordRepeated.text?.clear()
                binding.name.text?.clear()
                binding.surname.text?.clear()
                binding.phonenum.text?.clear()

                profileImg = ""
                Toast.makeText(this, "Podaci su sačuvani", Toast.LENGTH_SHORT).show() //ovo me nervira pomerice se posle


            }.addOnFailureListener{
                Toast.makeText(this, "Podaci nisu sačuvani", Toast.LENGTH_SHORT).show()
            }

            //added
            if(validateForm(name, surname, number, email, password, passRep)){
                storageReff.getReference("images").child(System.currentTimeMillis().toString())
                    .putFile(uri)
                    .addOnSuccessListener { task ->
                        task.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener {
                                val userId = FirebaseAuth.getInstance().currentUser!!.uid

                                val mapImage = mapOf(
                                    "url" to it.toString()
                                )

                                val databaseReference = FirebaseDatabase.getInstance().getReference("userImages")
                                databaseReference.child(userId).setValue(mapImage)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Uspešno", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener{error ->
                                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                                    }
                            }
                    }
            }
            //

            if(validateForm(name, surname, number, email, password, passRep)) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val userID = user?.uid
                            if (userID != null) {
                                val userRef = databaseRef.child("users").child(userID)
                                userRef.child("email").setValue(email)
                                userRef.child("name").setValue(name)
                                userRef.child("surname").setValue(surname)
                                userRef.child("phone").setValue(number)

                                Toast.makeText(this, "Uspešna registracija", Toast.LENGTH_SHORT)
                                    .show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
            }
        }
    }

    private fun validateForm(name:String, surname: String, number: String, email: String, password: String, passRep: String) : Boolean
    {
        if (name.isEmpty()) {
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
        else if (!number.startsWith("+3816") || !number.startsWith("06") && !Patterns.PHONE.matcher(number).matches()) {
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
//            var myfileintent =Intent(Intent.ACTION_GET_CONTENT)
//            myfileintent.setType("image/*")
//            launcher.launch(myfileintent)

            val galleryImage = registerForActivityResult(
                ActivityResultContracts.GetContent(),
                {
                    image.setImageURI(it)
                    uri= it!!
                })

            galleryImage.launch("image/*")
//
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

    private fun camera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    val uri = getImageUri(bitmap)
                    binding.profileImage.load(uri) {
                        crossfade(true)
                        crossfade(1000)
                    }
                    val inputStream = contentResolver.openInputStream(uri!!)
                    val myBitmap = BitmapFactory.decodeStream(inputStream)
                    val stream = ByteArrayOutputStream()
                    myBitmap.compress(
                        Bitmap.CompressFormat.PNG, 100, stream
                    )
                    val bytes = stream.toByteArray()
                    profileImg = android.util.Base64.encode(bytes, android.util.Base64.DEFAULT).toString()
                    binding.profileImage.setImageBitmap(myBitmap)
                    inputStream!!.close()
                    Toast.makeText(this, "Slika je odabrana!", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    //trebalo bi da mi ne treba
    private fun getImageUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    //trebalo bi da je ok
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