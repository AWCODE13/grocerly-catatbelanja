package com.example.grocerly.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.grocerly.R
import com.example.grocerly.utils.AppSettings
import com.example.grocerly.utils.DataMigrationHelper
import com.example.grocerly.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val session = SessionManager(this)
        AppSettings.applyLanguage(session.getLanguage())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(
                    this,
                    "Email dan password wajib diisi",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!session.login(email, pass)) {
                Toast.makeText(
                    this,
                    "Email atau password salah",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            DataMigrationHelper.migrateGuestToUser(
                context = this,
                userEmail = email
            )

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
