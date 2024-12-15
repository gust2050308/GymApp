package com.example.integradora2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.integradora2.databinding.ActivityHomeBinding
import org.json.JSONObject

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var queue: RequestQueue

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        queue = Volley.newRequestQueue(this)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        val token = sharedPreferences.getString("token", "#")

        if (token != "#") {
            val intent = Intent(this@Home, Lobby::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.btnSignIn.setOnClickListener {
            val formUsername = binding.edtEmail.text.toString()
            val formPassword = binding.edtPassword.text.toString()
            sendUserDataToApi(formUsername, formPassword)
        }

        binding.btnRegistro.setOnClickListener {
            val intent = Intent(this@Home, Registro::class.java)
            startActivity(intent)
        }
    }

    private fun sendUserDataToApi(username: String, password: String) {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val url = "http://192.168.0.7:8080/auth/login"
        val jsonObject = JSONObject().apply {
            put("username", username)
            put("password", password)
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
                    putString("token", token)
                    putString("id", user.getString("id"))
                    putString("username", user.getString("username"))
                    putString("password", user.getString("password"))
                    putString("fullName", user.getString("fullName"))
                    putString("email", user.getString("email"))
                    putBoolean("enabled", user.getBoolean("enabled"))
                    putString("role", user.getString("role"))
                    putString("startDate", user.getString("startDate"))
                    putString("endDate", user.getString("endDate"))
                    putString("membershipType", user.getString("membershipType"))
                    putString("telephone", user.getString("telephone"))
                    putString("photo", user.getString("photo"))
                    putString("accountNonLocked", user.getString("accountNonLocked"))
                    apply() // Esto guarda los cambios de forma asíncrona
                }

                if (user.getString("username").isNotEmpty()) {
                    val intent = Intent(this, Lobby::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    // Maneja la respuesta del servidor
                    Toast.makeText(this, "El nombre de usuario está vacío", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("ESTA MAL, ESTA MAL EN ALGO", error.message.toString())
            }
        )
        queue.add(jsonObjectRequest)
    }
}
