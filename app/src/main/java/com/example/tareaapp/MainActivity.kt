package com.example.tareaapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazamos las variables con el diseño XML
        val cajaTexto = findViewById<EditText>(R.id.cajaTexto)
        val botonPasar = findViewById<Button>(R.id.botonPasar)
        val miLabel = findViewById<TextView>(R.id.miLabel)

        // Qué pasa al presionar el botón
        botonPasar.setOnClickListener {
            val textoIngresado = cajaTexto.text.toString()

            if (textoIngresado.isNotEmpty()) {
                // 1. Pasar el texto al label
                miLabel.text = textoIngresado

                // 2. Crear y mostrar la alerta tipo pop-up
                val builder = AlertDialog.Builder(this)
                builder.setTitle("¡Éxito!")
                builder.setMessage("El texto se ha actualizado correctamente.")
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss() // Cierra la alerta
                }
                builder.show()

                // Opcional: Limpiar la caja de texto después de enviar
                cajaTexto.text.clear()
            } else {
                cajaTexto.error = "Por favor ingresa un texto"
            }
        }
    }
}