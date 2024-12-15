package com.example.integradora2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.integradora2.databinding.ActivityLobbyBinding
import org.json.JSONObject
import android.util.Log
import com.google.zxing.integration.android.IntentIntegrator
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Lobby : AppCompatActivity() {
    private lateinit var binding: ActivityLobbyBinding
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.bottomAppBar3)

        queue = Volley.newRequestQueue(this)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val id = sharedPreferences.getString("id", null)

        if (!id.isNullOrEmpty()) {
            val url = "http://192.168.0.7:8080/assistances/assistacesById/$id"
            val method = Request.Method.GET
            val listener = Response.Listener<JSONObject> { result ->

            }
            val errorListener = Response.ErrorListener { error ->
                Log.e("Error", error.message.toString())
            }
            val request = JsonObjectRequest(method, url, null, listener, errorListener)
            queue.add(request)
        } else {
            Toast.makeText(this, "No hay nada en el teléfono", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewUser -> {
                val intent = Intent(this@Lobby, DataUser::class.java)
                startActivity(intent)
            }
            R.id.Assistance -> {
                startQRScanner()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea el código QR")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("Debug", "onActivityResult: Triggered")

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val userJson = JSONObject().apply {
            put("id", sharedPreferences.getString("id", "#"))
            put("role", "ROLE_USER")
        }

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val qrContent = result?.contents
        Log.d("Debug", "QR Content: $qrContent")
        if (!qrContent.isNullOrBlank() && qrContent == LocalDate.now().toString()) {
            Log.d("Debug", "QR Content matches today's date")
            val isAssistanceMarked = sharedPreferences.getBoolean("assistance?", false)
            if (!isAssistanceMarked) {
Toast.makeText(this,"Marking entrance time",Toast.LENGTH_SHORT)
                sharedPreferences.edit().apply {
                    putBoolean("assistance?", true)
                    putString("entrance", LocalTime.now().toString())
                    apply()
                }
            } else {
                Log.d("Debug", "Calculating visit duration")
                val entranceTime = sharedPreferences.getString("entrance", null)
                if (!entranceTime.isNullOrEmpty()) {
                    val jsonObject = JSONObject().apply {
                        put("entryDate", LocalDate.now().toString())
                        put("entrance", entranceTime)
                        put("outside", LocalTime.now().toString())
                        put("visitDuration", calculateVisitDuration(entranceTime, LocalTime.now().toString()))
                        put("user", userJson)
                    }

                    val url = "http://192.168.0.7:8080/assistances/assistanceServ"
                    val request = JsonObjectRequest(Request.Method.POST, url, jsonObject, { response ->
                        Log.d("Debug", "Response from server: $response")
                        val code = response.optJSONArray("metadata")?.optJSONObject(0)?.optString("codigo", "99")
                        if (code == "00") {
                            Toast.makeText(this, "Asistencia registrada con éxito", Toast.LENGTH_SHORT).show()
                            sharedPreferences.edit().putBoolean("assistance?", false).apply()
                        } else {
                            Toast.makeText(this, "Error en el servidor", Toast.LENGTH_SHORT).show()
                        }
                    }, { error ->
                        Log.e("Error", "Error in response: ${error.message}")
                        Log.e("Error", error.message.toString())
                        Toast.makeText(this, "No se pudo registrar la asistencia", Toast.LENGTH_SHORT).show()
                    })
                    queue.add(request)
                }
            }
        } else {
            Toast.makeText(this, "Código QR inválido o escaneo cancelado", Toast.LENGTH_SHORT).show()
            Log.d("Debug", "QR content is invalid or scan was canceled")
        }
    }

    fun calculateVisitDuration(inside: String, outside: String): String {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val cleanInside = inside.split(".")[0]
        val cleanOutside = outside.split(".")[0]

        val hora1 = LocalTime.parse(cleanInside, timeFormatter)
        val hora2 = LocalTime.parse(cleanOutside, timeFormatter)
        val diferencia = Duration.between(hora1, hora2)
        val horas = diferencia.toHours()
        val minutos = diferencia.toMinutes() % 60
        return String.format("%02d:%02d:00", horas, minutos)
    }

}
