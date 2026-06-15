package com.example.grocerly.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.grocerly.R
import com.example.grocerly.data.database.AppDatabase
import com.example.grocerly.ui.MainActivity
import com.example.grocerly.ui.SplashActivity
import com.example.grocerly.utils.AppSettings
import com.example.grocerly.utils.SessionManager
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var session: SessionManager
    private lateinit var imgAvatar: ShapeableImageView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            session.setPhotoUri(session.getEmail(), uri.toString())
            imgAvatar.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        session = SessionManager(requireContext())

        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        tvEmail.text = session.getEmail().ifEmpty {
            getString(R.string.default_email)
        }

        imgAvatar = view.findViewById(R.id.imgAvatar)

        val savedUri = session.getPhotoUri(session.getEmail())
        if (savedUri.isNotEmpty()) {
            imgAvatar.setImageURI(Uri.parse(savedUri))
        } else {
            imgAvatar.setImageResource(R.drawable.ic_profile)
        }

        imgAvatar.setOnClickListener { pickImageLauncher.launch("image/*") }
        view.findViewById<View>(R.id.btnEditFoto)?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        view.findViewById<View>(R.id.menuEmail)
            .setOnClickListener { showDialogUbahEmail(tvEmail) }

        view.findViewById<View>(R.id.menuPassword)
            .setOnClickListener { showDialogUbahPassword() }

        view.findViewById<View>(R.id.menuBahasa)
            .setOnClickListener { showDialogBahasa() }

        view.findViewById<View>(R.id.menuTema)
            .setOnClickListener { showDialogTema() }

        view.findViewById<View>(R.id.menuLogout)
            .setOnClickListener { showLogoutConfirmation() }

        return view
    }

    private fun showDialogUbahEmail(tvEmail: TextView) {
        val et = EditText(requireContext())
        et.setText(session.getEmail())

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_email))
            .setView(et)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newEmail = et.text.toString().trim()
                val oldEmail = session.getEmail()

                if (newEmail.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.email_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val db = AppDatabase.getInstance(requireContext())
                    db.listBelanjaDao().updateOwnerEmail(oldEmail, newEmail)
                    db.itemBelanjaDao().updateOwnerEmail(oldEmail, newEmail)
                    db.purchaseDao().updateOwnerEmail(oldEmail, newEmail)

                    session.setEmail(newEmail)
                    tvEmail.text = newEmail
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDialogUbahPassword() {
        val box = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etPass1 = EditText(requireContext()).apply {
            hint = getString(R.string.new_password)
        }
        val etPass2 = EditText(requireContext()).apply {
            hint = getString(R.string.repeat_password)
        }

        box.addView(etPass1)
        box.addView(etPass2)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_password))
            .setView(box)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val p1 = etPass1.text.toString()
                val p2 = etPass2.text.toString()

                if (p1.isEmpty() || p2.isEmpty() || p1 != p2) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.password_not_match),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                session.setPassword(p1)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.password_updated),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDialogBahasa() {
        val items = arrayOf(
            getString(R.string.indonesian),
            getString(R.string.english)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_language))
            .setItems(items) { _, which ->
                val lang = if (which == 0) "id" else "en"
                session.setLanguage(lang)
                AppSettings.applyLanguage(lang)

                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                requireActivity().finish()
            }
            .show()
    }

    private fun showDialogTema() {
        val items = arrayOf(
            getString(R.string.light),
            getString(R.string.dark)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_theme))
            .setItems(items) { _, which ->
                session.setTheme(if (which == 0) "light" else "dark")
                requireActivity().recreate()
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                session.logout()
                startActivity(
                    Intent(requireContext(), SplashActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                requireActivity().finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
