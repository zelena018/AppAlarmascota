package com.example.alarmascota

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        //Para ir de la pantalla de registro a la pantalla HOME
        val btnEntrarregistro: Button = findViewById(R.id.btn_entrarR)
        btnEntrarregistro.setOnClickListener {

            val intent: Intent = Intent(this, home_alarmascota::class.java)
            startActivity(intent)
        }
    }
}