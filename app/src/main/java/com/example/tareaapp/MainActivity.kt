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

// Diapositiva 27: Herencias
class MainActivity : AppCompatActivity(),
    CoroutineScope by MainScope(),
    MessageClient.OnMessageReceivedListener {

    // Diapositiva 28: Variables
    private var activityContext: Context? = null
    private var deviceConnected: Boolean = false
    private val PAYLOAD_PATH = "/CHAT_APP" // Identificador de la comunicación
    private lateinit var nodeID: String

    private lateinit var miLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityContext = this

        // Buscar el nodo del reloj al iniciar
        getNodes(this)

        val cajaTexto = findViewById<EditText>(R.id.cajaTexto)
        val botonPasar = findViewById<Button>(R.id.botonPasar)
        miLabel = findViewById<TextView>(R.id.miLabel)

        botonPasar.setOnClickListener {
            val textoIngresado = cajaTexto.text.toString()

            if (textoIngresado.isNotEmpty()) {
                miLabel.text = "Tú: $textoIngresado"

                // Enviar al smartwatch
                sendMessage(textoIngresado)

                val builder = AlertDialog.Builder(this)
                builder.setTitle("¡Éxito!")
                builder.setMessage("Mensaje enviado al reloj correctamente.")
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()

                cajaTexto.text.clear()
            } else {
                cajaTexto.error = "Por favor ingresa un texto"
            }
        }
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
        try {
            Wearable.getMessageClient(this).addListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            Wearable.getMessageClient(this).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Diapositiva 31: Enviar mensaje
    private fun sendMessage(textoMensaje: String) {
        if (deviceConnected && ::nodeID.isInitialized) {
            Wearable.getMessageClient(this)
                .sendMessage(nodeID, PAYLOAD_PATH, textoMensaje.toByteArray())
                .addOnSuccessListener {
                    Log.d("sendMessage", "Mensaje enviado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.d("sendMessage", "Error al enviar mensaje: ${e.message}")
                }
        }
    }

    // Diapositiva 32: Recibir mensaje
    override fun onMessageReceived(ME: MessageEvent) {
        val message = String(bytes = ME.data, charset = StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Mensaje del reloj: $message")

        runOnUiThread {
            miLabel.text = "Reloj: $message"
        }
    }
}