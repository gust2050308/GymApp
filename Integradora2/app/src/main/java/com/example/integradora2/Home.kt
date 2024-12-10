package com.example.integradora2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
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

        if(token!="#"){
            val intent = Intent(this@Home, Lobby::class.java)
            startActivity(intent)
        }

            binding.btnSignIn.setOnClickListener {
                val formUsername = binding.edtEmail.text.toString()
                val formPassword = binding.edtPassword.text.toString()
                sendUserDataToApi(formUsername, formPassword)
            }

        binding.btnRegistro.setOnClickListener {
            val intent = Intent(this@Home,Registro::class.java)
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
                } else {
                    // Maneja la respuesta del servidor
                    Toast.makeText(this, "is empty", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("ESTA MAL, ESTA MAL EN ALGO", error.message.toString())
            }
        )
        queue.add(jsonObjectRequest)
    }
}