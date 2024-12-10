package com.example.integradora2

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.integradora2.databinding.ActivityDataUserBinding

class DataUser : AppCompatActivity() {
    private lateinit var binding: ActivityDataUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.bottomAppBar2)

        val shared = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        binding.etdUsername.setText(shared.getString("username", "#"))
        binding.edtTelefono.setText(shared.getString("telephone", "#"))
        binding.edtFulName.setText(shared.getString("telephone", "#"))
        binding.edtFulName.setText(shared.getString("fullName", "#"))
        binding.edtemail.setText(shared.getString("email", "#"))
        binding.txtFecha.text = shared.getString("startDate", "#")
        binding.txtFechaEnd.text = shared.getString("endDate", "#")
        binding.txtmemer.text = shared.getString("membershipType", "#")

        val imageView = binding.imgUser
        val base64String = shared.getString("photo", "#")

        if (base64String != null) {
            decodeBase64ToImage(base64String, imageView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_data_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.returnButton -> {
                var intent = Intent(this@DataUser, Home::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun decodeBase64ToImage(base64String: String, imageView: ImageView) {
        // Decodificar la cadena Base64 a un arreglo de bytes
        val imageBytes = Base64.decode(base64String, Base64.DEFAULT) // Convertir los bytes a un Bitmap
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) // Mostrar el Bitmap en el ImageView
        imageView.setImageBitmap(decodedImage)
    }
}

