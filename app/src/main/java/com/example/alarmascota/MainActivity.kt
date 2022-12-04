package com.example.alarmascota

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Para que vaya de la pantalla principal a la pantalla "Iniciar sesion"
        val btnS: Button = findViewById(R.id.btn_iniciarS)
        btnS.setOnClickListener {

            val intent: Intent = Intent(this, iniciar_sesion::class.java)
            startActivity(intent)
        }

        //Para que vaya de la pantalla principal a la pantalla "Registro"
        val btnR: Button = findViewById(R.id.btn_registro)
        btnR.setOnClickListener {

            val intent: Intent = Intent(this, registro::class.java)
            startActivity(intent)
        }
    }
}