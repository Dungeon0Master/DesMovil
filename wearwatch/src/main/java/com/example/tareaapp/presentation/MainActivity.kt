/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.tareaapp.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.tareaapp.R

import com.example.tareaapp.presentation.theme.TareaAppTheme

import android.media.MediaPlayer
import android.util.Log
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
class MainActivity : ComponentActivity(),
    CoroutineScope by MainScope(),
    MessageClient.OnMessageReceivedListener {

    private lateinit var mediaPlayer: MediaPlayer

    // Diapositiva 28: Variables
    private var activityContext: Context? = null
    private var deviceConnected: Boolean = false
    private val PAYLOAD_PATH = "/CHAT_APP" // Debe ser EXACTAMENTE el mismo que en el celular
    private lateinit var nodeID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityContext = this

        // Buscar el nodo del celular al iniciar
        getNodes(this)

        mediaPlayer = MediaPlayer.create(this, R.raw.coin)

        val boton: Button = findViewById(R.id.boton)

        boton.setOnClickListener {
            // Acción 1: Responder al celular
            sendMessage("¡Hola desde el reloj!")

            // Acción 2: Tu lógica original (abrir actividad y reproducir sonido)
            Toast.makeText(this, "Alerta enviada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, Prueba::class.java)
            startActivity(intent)

            if(!mediaPlayer.isPlaying){
                mediaPlayer.start()
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
                    Log.d("NODO", "Celular encontrado con ID: ${node.id}")
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
                    Log.d("sendMessage", "Mensaje enviado al celular correctamente")
                }
                .addOnFailureListener { e ->
                    Log.d("sendMessage", "Error al enviar mensaje: ${e.message}")
                }
        }
    }

    // Diapositiva 32: Recibir mensajes
    override fun onMessageReceived(ME: MessageEvent) {
        val message = String(ME.data, StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Mensaje del celular: $message")

        runOnUiThread {
            Toast.makeText(this, "Celular dice: $message", Toast.LENGTH_LONG).show()
        }
    }
}