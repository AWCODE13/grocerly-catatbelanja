package com.example.grocerly.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.grocerly.R
import com.example.grocerly.TambahActivity
import com.example.grocerly.data.database.AppDatabase
import com.example.grocerly.data.entity.ListBelanjaEntity
import com.example.grocerly.fragment.HomeFragment
import com.example.grocerly.fragment.InsightsFragment
import com.example.grocerly.fragment.ProfileFragment
import com.example.grocerly.utils.AppSettings
import com.example.grocerly.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val session = SessionManager(this)

        AppSettings.applyLanguage(session.getLanguage())

        AppCompatDelegate.setDefaultNightMode(
            if (session.getTheme() == "dark")
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        loadFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_insights -> loadFragment(InsightsFragment())
                R.id.nav_profile -> {
                    if (session.isLogin()) {
                        loadFragment(ProfileFragment())
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                }
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun openCreateCartSheet() {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_tambah_list, null, false)

        val etJudul = view.findViewById<EditText>(R.id.etJudul)
        val etBudget = view.findViewById<EditText>(R.id.etBudget)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        dialog.setContentView(view)

        btnSimpan.setOnClickListener {
            val judul = etJudul.text.toString().trim()
            if (judul.isEmpty()) {
                etJudul.error = getString(R.string.make_list)
                return@setOnClickListener
            }

            val budget = etBudget.text.toString().toLongOrNull() ?: 0L
            val ownerEmail =
                if (SessionManager(this).isLogin()) SessionManager(this).getEmail()
                else "guest"

            lifecycleScope.launch {
                val idBaru = AppDatabase.getInstance(this@MainActivity)
                    .listBelanjaDao()
                    .insert(
                        ListBelanjaEntity(
                            judul = judul,
                            budgetLimit = budget,
                            ownerEmail = ownerEmail
                        )
                    ).toInt()

                startActivity(
                    Intent(this@MainActivity, TambahActivity::class.java)
                        .putExtra("judul_list", judul)
                        .putExtra("list_id", idBaru)
                )
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
