package com.example.tareaapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.example.tareaapp.R

class Prueba: ComponentActivity() , SensorEventListener{
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor?=null
    private lateinit var textoSensor: TextView
    private var sensorType = Sensor.TYPE_ACCELEROMETER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(sensorType)
        setContentView(R.layout.prueba)

        textoSensor = findViewById(R.id.texto)
        startSensor()
    }

    private fun startSensor(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 1001)
            return
        }
        if(sensor != null){
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == sensorType) {
            val lectura = event.values[0]
            textoSensor.text = "Lectura: $lectura"
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensor?.also { pressure ->
            sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
}



