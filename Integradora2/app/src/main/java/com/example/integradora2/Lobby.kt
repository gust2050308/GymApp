package com.example.integradora2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.integradora2.databinding.ActivityLobbyBinding
import org.json.JSONObject
import java.util.Queue
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

class Lobby : AppCompatActivity() {
    private lateinit var binding: ActivityLobbyBinding
    public lateinit var queue: RequestQueue
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.bottomAppBar)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val id = sharedPreferences.getString("id", null)

        queue = Volley.newRequestQueue(this)

        if (id.toString().isNotEmpty()) {
            Toast.makeText(this, "Bienbenido${id}", Toast.LENGTH_SHORT).show()
            val url = "http://192.168.0.7:8080/auth/login${id}"
            val metod = Request.Method.GET
            val listtener = Response.Listener<JSONObject> { result ->

            }
        } else {
            Toast.makeText(this, "no hay nada en el cel", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewUser -> {
                var intent = Intent(this@Lobby, DataUser::class.java)
                startActivity(intent)
            }
            R.id.Assistance->{
                var intent = Intent(this@Lobby, Escanear::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}