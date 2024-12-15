package com.example.integradora2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.integradora2.databinding.ActivityEscanearBinding
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class Escanear : AppCompatActivity() {
    private lateinit var binding: ActivityEscanearBinding
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEscanearBinding.inflate(layoutInflater)
        setContentView(binding.root)

        queue = Volley.newRequestQueue(this)

        binding.btnEscanear.setOnClickListener {
        }
    }





    private fun sendQRDataToApi(name: String, timestamp: Long) {
        val url = "http://192.168.63.19:3000/persona"

        val jsonObject = JSONObject()
        jsonObject.put("nombre", name)
        jsonObject.put("hora_escaneo", timestamp)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            { response ->
                Toast.makeText(this, "Datos enviados correctamente", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Error al enviar los datos", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonObjectRequest)
    }
}
