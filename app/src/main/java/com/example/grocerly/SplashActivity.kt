package com.example.grocerly.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.grocerly.ui.MainActivity
import com.example.grocerly.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 800
        }

        binding.imgLogo.startAnimation(fadeIn)
        binding.tvAppName.startAnimation(fadeIn)

        binding.imgLogo.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }
}
