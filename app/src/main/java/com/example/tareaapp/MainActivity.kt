package com.example.tareaapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

// Diapositiva 34 y 35: imports para HTTP
import okhttp3.Callback
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

// Diapositiva 27: Herencias
class MainActivity : AppCompatActivity(),
    CoroutineScope by MainScope(),
    MessageClient.OnMessageReceivedListener {

    // Diapositiva 28: Variables
    private var activityContext: Context? = null
    private var deviceConnected: Boolean = false
    private val PAYLOAD_PATH = "/CHAT_APP"
    private lateinit var nodeID: String
    private lateinit var miLabel: TextView

    // URL de la API en Render
    private val API_URL = "https://api-celuar.onrender.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityContext = this
        getNodes(this)

        val cajaTexto  = findViewById<EditText>(R.id.cajaTexto)
        val botonPasar = findViewById<Button>(R.id.botonPasar)
        miLabel        = findViewById<TextView>(R.id.miLabel)

        // ── Botón original: pasa texto al label y al reloj ───────────
        botonPasar.setOnClickListener {
            val textoIngresado = cajaTexto.text.toString()
            if (textoIngresado.isNotEmpty()) {
                miLabel.text = "Tú: $textoIngresado"
                sendMessage(textoIngresado)

                val builder = AlertDialog.Builder(this)
                builder.setTitle("¡Éxito!")
                builder.setMessage("Mensaje enviado al reloj correctamente.")
                builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                builder.show()

                cajaTexto.text.clear()
            } else {
                cajaTexto.error = "Por favor ingresa un texto"
            }
        }

        // ── NUEVO: Botón GET (Diapositiva 34) ─────────────────────────
        val botonGet = findViewById<Button>(R.id.botonGet)
        botonGet.setOnClickListener {
            fetchMensajes()
        }

        // ── NUEVO: Botón POST (Diapositiva 35) ────────────────────────
        val botonPost = findViewById<Button>(R.id.botonPost)
        botonPost.setOnClickListener {
            val texto = cajaTexto.text.toString()
            if (texto.isNotEmpty()) {
                postMensaje(texto)
                cajaTexto.text.clear()
            } else {
                cajaTexto.error = "Escribe algo para enviar"
            }
        }
    }

    // ── Diapositiva 34: petición GET ──────────────────────────────────
    private fun fetchMensajes() {
        // Crear un cliente de OkHttp
        val client = OkHttpClient()

        // Construir la petición
        val request = Request.Builder()
            .url("$API_URL/mensajes")
            .build()

        // Ejecutar la petición en un hilo aparte
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                // Manejo de error
                Log.d("FETCH", "Error: ${e.message}")
                runOnUiThread {
                    miLabel.text = "GET Error: ${e.message}"
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.d("FETCH", "Error en la respuesta: ${response.code}")
                        runOnUiThread {
                            miLabel.text = "GET Error código: ${response.code}"
                        }
                    } else {
                        // Aquí se maneja la respuesta, por ejemplo, convertirla en String
                        val responseData = response.body?.string()
                        Log.d("FETCH", "Respuesta: $responseData")
                        runOnUiThread {
                            miLabel.text = "GET OK: $responseData"
                        }
                    }
                }
            }
        })
    }

    // ── Diapositiva 35: petición POST ─────────────────────────────────
    private fun postMensaje(texto: String) {
        val client   = OkHttpClient()
        val JSON     = "application/json; charset=utf-8".toMediaTypeOrNull()
        val jsonBody = """{"texto":"$texto"}"""
        val body     = jsonBody.toRequestBody(JSON)

        val request = Request.Builder()
            .url("$API_URL/mensaje")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.d("POST", "Error: ${e.message}")
                runOnUiThread {
                    miLabel.text = "POST Error: ${e.message}"
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.d("POST", "Error: ${response.code}")
                        runOnUiThread {
                            miLabel.text = "POST Error código: ${response.code}"
                        }
                    } else {
                        val responseData = response.body?.string()
                        Log.d("POST", "Guardado: $responseData")
                        runOnUiThread {
                            miLabel.text = "POST OK: $responseData"
                        }
                    }
                }
            }
        })
    }

    // Diapositiva 29: Obtener ID del nodo
    private fun getNodes(context: Context) {
        launch(Dispatchers.Default) {
            try {
                val nodes = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
                for (node in nodes) {
                    nodeID = node.id
                    deviceConnected = true
                    Log.d("NODO", "Reloj encontrado con ID: ${node.id}")
                }
            } catch (exception: Exception) {
                Log.d("Error en el nodo", exception.toString())
            }
        }
    }

    // Diapositiva 30: Listeners
    override fun onResume() {
        super.onResume()
        try { Wearable.getMessageClient(this).addListener(this) }
        catch (e: Exception) { e.printStackTrace() }
    }

    override fun onPause() {
        super.onPause()
        try { Wearable.getMessageClient(this).removeListener(this) }
        catch (e: Exception) { e.printStackTrace() }
    }

    // Diapositiva 31: Enviar mensaje al reloj
    private fun sendMessage(textoMensaje: String) {
        if (deviceConnected && ::nodeID.isInitialized) {
            Wearable.getMessageClient(this)
                .sendMessage(nodeID, PAYLOAD_PATH, textoMensaje.toByteArray())
                .addOnSuccessListener { Log.d("sendMessage", "Mensaje enviado correctamente") }
                .addOnFailureListener { e -> Log.d("sendMessage", "Error: ${e.message}") }
        }
    }

    // Diapositiva 32: Recibir mensaje del reloj
    override fun onMessageReceived(ME: MessageEvent) {
        val message = String(bytes = ME.data, charset = StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Mensaje del reloj: $message")
        runOnUiThread { miLabel.text = "Reloj: $message" }
    }
}