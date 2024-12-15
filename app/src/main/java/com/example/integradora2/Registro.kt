package com.example.integradora2
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.IOException
import android.util.Base64
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.integradora2.databinding.ActivityRegistroBinding
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.UUID
import com.bumptech.glide.Glide

class Registro : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private lateinit var queue: RequestQueue
    private lateinit var photo: File
    private lateinit var base64Image: String

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa la cola de solicitudes Volley
        queue = Volley.newRequestQueue(this)

        val members: List<String> = listOf("Báisca", "Platinum", "Gold")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, members)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.SpnMembresia.adapter = adapter

        // Solicitar permisos de cámara si no están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }

        binding.btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                photo = craarArchivo()
                val uri = FileProvider.getUriForFile(this, "com.example.integradora2.fileprovider", photo)
                cameraLuncher.launch(uri)
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el botón de Registro
        binding.btnRegister.setOnClickListener {
            // Obtén los datos del formulario
            val username = binding.edtUsername.text.toString()
            val nombrefull = binding.edtNombre.text.toString()
            val email = binding.edtEmail.text.toString()
            val telefono = binding.edtTelefono.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtConfirmPassword.text.toString()
            val membership = binding.SpnMembresia.selectedItem.toString()

            // Realiza las validaciones
            if (nombrefull.isEmpty() || email.isEmpty() || telefono.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendUserDataToApi(nombrefull, email, telefono, password, membership, username,base64Image)
        }
    }

    private fun sendUserDataToApi(nombre: String, email: String, telefono: String, password: String, membership: String, fullname: String,photo:String) {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val fechainicio = LocalDate.now().toString()
        val fechafin = when (membership) {
            "Báisca" -> LocalDate.now().plusMonths(3).toString()
            "Platinum" -> LocalDate.now().plusMonths(6).toString()
            "Gold" -> LocalDate.now().plusYears(1).toString()
            else -> ""
        }
        val url = "http://192.168.0.7:8080/auth/registrer"
        val jsonObject = JSONObject().apply {
            put("username", fullname)
            put("password", password)
            put("fullName", nombre)
            put("email", email)
            put("activeMembership", true)
            put("startDate", fechainicio)
            put("endDate", fechafin)
            put("membershipType",membership)
            put("telephone", telefono)
            put("photo",base64Image)
        }

        // Crea una solicitud POST con Volley usando JSONObject
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            { response ->
                val token = response.getString("token")
                val user = response.getJSONObject("user")
                // Obtener una instancia de SharedPreferences y crear un editor para hacer cambios
                with(sharedPreferences.edit()) {
                    putString("token", token)//no
                    putString("id", user.getString("id"))//no
                    putString("username", user.getString("username"))//si
                    putString("password", user.getString("password"))//no
                    putString("fullName", user.getString("fullName"))//si
                    putString("email", user.getString("email"))//si
                    putBoolean("enabled", user.getBoolean("enabled"))//no
                    putString("role", user.getString("role"))//no
                    putString("startDate", user.getString("startDate"))//si
                    putString("endDate", user.getString("endDate"))//si
                    putString("membershipType", user.getString("membershipType"))
                    putString("telephone", user.getString("telephone"))
                    putString("photo", user.getString("photo"))
                    putString("accountNonLocked", user.getString("accountNonLocked"))
                    apply()
                    commit()
                }

                if (user.getString("username").isNotEmpty()) {
                    val intent = Intent(this, Lobby::class.java)
                    startActivity(intent)
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                } else {
                    // Maneja la respuesta del servidor
                    Toast.makeText(this, "is empty", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("ESTA MAL, ESTA MAL EN ALGO", error.message.toString())
                Toast.makeText(this, "Error, Confirma los datos", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun craarArchivo(): File {
        val tempFolder = File(applicationContext.filesDir, "photos")
        tempFolder.deleteRecursively()
        tempFolder.mkdir()
        return File(tempFolder, "tempPhoto${UUID.randomUUID()}.jpg")
    }

    private val cameraLuncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { result ->
        if (result) {
            val resizedBitmap = decodeSampledBitmapFromFile(photo.path, 800, 800) // Ajusta los valores según necesites
            val resizedPhoto = saveBitmapToFile(resizedBitmap, photo.path)
            base64Image = convertImageToBase64(resizedPhoto).toString()
            Glide.with(this)
                .load(resizedPhoto.toUri())
                .into(binding.logo)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun convertImageToBase64(file: File): String? {
        return try {
            val bytes = FileInputStream(file).readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap {

    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
        BitmapFactory.decodeFile(path, options)
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path,options)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth)
        {
            inSampleSize *= 2
        }
        }
        return inSampleSize
    }

    fun saveBitmapToFile(bitmap: Bitmap, path: String): File {
        val file = File(path)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file
    }
}