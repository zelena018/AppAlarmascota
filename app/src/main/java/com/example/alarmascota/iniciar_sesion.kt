package com.example.alarmascota

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class iniciar_sesion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion)

        //Para ir de la pantalla "Iniciar sesion" a la pantalla HOME
        val btnEntrarInicio: Button = findViewById(R.id.btn_entrarIS)
        btnEntrarInicio.setOnClickListener {

            val intent: Intent = Intent(this, home_alarmascota::class.java)
            startActivity(intent)
        }
    }
}