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
import java.util.*

class Escanear : AppCompatActivity() {
    private lateinit var binding: ActivityEscanearBinding
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEscanearBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa Volley
        queue = Volley.newRequestQueue(this)

        // Configura el botón para escanear el código QR
        binding.btnEscanear.setOnClickListener {
            startQRScanner()
        }
    }

    // Inicia el escaneo del código QR usando IntentIntegrator
    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea el código QR")
        integrator.setCameraId(0)  // 0 es la cámara trasera
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }

    // Método para manejar los resultados del escaneo del código QR
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // El código QR fue leído, ahora procesa los datos
                val qrData = result.contents
                Toast.makeText(this, "Código QR escaneado: $qrData", Toast.LENGTH_SHORT).show()

                // Supongamos que el código QR contiene los datos en formato "nombre;mensualidad"
                val qrDataParts = qrData.split(";")
                if (qrDataParts.size == 2) {
                    val name = qrDataParts[0]
                    val mensualidad = qrDataParts[1].toDouble()

                    // Verifica el estado de la persona según la mensualidad
                    val estado = if (mensualidad > 0) "Activo" else "Inactivo"

                    // Obtén la hora en el formato timestamp (milisegundos)
                    val timestamp = System.currentTimeMillis()

                    // Actualiza la interfaz de usuario con los datos obtenidos
                    binding.tvNombre.text = "Nombre: $name"
                    binding.tvEstado.text = "Estado: $estado"
                    binding.tvHora.text = "Hora del escaneo: ${Date(timestamp)}"  // Muestra la hora del escaneo

                    // Envía los datos a la API
                    sendQRDataToApi(name, estado, timestamp)
                }
            } else {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Envía los datos del código QR a la API
    private fun sendQRDataToApi(name: String, estado: String, timestamp: Long) {
        val url = "http://192.168.63.19:3000/persona"  // URL de tu API

        // Crea el JSONObject con los datos del código QR
        val jsonObject = JSONObject()
        jsonObject.put("nombre", name)
        jsonObject.put("estado", estado)
        jsonObject.put("hora_escaneo", timestamp)

        // Crea una solicitud POST con Volley usando JSONObject
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            { response ->
                // Maneja la respuesta del servidor
                Toast.makeText(this, "Datos enviados correctamente", Toast.LENGTH_SHORT).show()
            },
            { error ->
                // Maneja el error de la solicitud
                Toast.makeText(this, "Error al enviar los datos", Toast.LENGTH_SHORT).show()
            }
        )

        // Agrega la solicitud a la cola de Volley
        queue.add(jsonObjectRequest)
    }
}
