package com.example.grocerly

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.adapter.TrashAdapter
import com.example.grocerly.data.database.AppDatabase
import com.example.grocerly.data.entity.ListBelanjaEntity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class HapusActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: TrashAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hapus)

        db = AppDatabase.getInstance(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHapus)
        toolbar.setNavigationOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvTrash)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = TrashAdapter(
            onRestore = { item ->
                lifecycleScope.launch {
                    db.listBelanjaDao().restore(listOf(item.id))
                    loadTrash()
                }
            },
            onDeletePermanent = { item ->
                confirmDeletePermanent(item)
            }
        )

        rv.adapter = adapter
        loadTrash()
    }

    override fun onResume() {
        super.onResume()
        loadTrash()
    }

    private fun loadTrash() {
        lifecycleScope.launch {
            val data = db.listBelanjaDao().getAllDeleted()

            val emptyView = findViewById<View>(R.id.emptyTrash)
            val rv = findViewById<RecyclerView>(R.id.rvTrash)

            if (data.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                rv.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                rv.visibility = View.VISIBLE
            }

            adapter.submitList(data)
        }
    }


    private fun confirmDeletePermanent(item: ListBelanjaEntity) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.hapus))
            .setMessage(item.judul)
            .setPositiveButton(getString(R.string.hapus)) { _, _ ->
                lifecycleScope.launch {
                    db.listBelanjaDao().deletePermanent(listOf(item.id))
                    loadTrash()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded)
        dialog.show()
    }
}
