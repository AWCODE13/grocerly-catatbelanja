package com.example.grocerly.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.R
import com.example.grocerly.adapter.DraftItemAdapter
import com.example.grocerly.data.database.AppDatabase
import com.example.grocerly.data.entity.ItemBelanjaEntity
import com.example.grocerly.utils.SessionManager
import kotlinx.coroutines.launch

class AddItemActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: DraftItemAdapter

    private var listId: Int = 0
    private lateinit var ownerEmail: String

    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        db = AppDatabase.getInstance(this)

        val session = SessionManager(this)
        ownerEmail = if (session.isLogin()) {
            session.getEmail()
        } else {
            getString(R.string.guest_user)
        }

        listId = intent.getIntExtra("list_id", 0)
        if (listId == 0) {
            Toast.makeText(
                this,
                getString(R.string.invalid_list_id),
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        val searchView = findViewById<SearchView>(R.id.searchBarang)
        val tvDone = findViewById<TextView>(R.id.tvDone)
        val rv = findViewById<RecyclerView>(R.id.rvDraftItems)

        adapter = DraftItemAdapter(
            onPlus = { item ->
                lifecycleScope.launch {
                    db.itemBelanjaDao().updateQtyById(
                        item.id,
                        item.quantity + 1
                    )
                }
            },
            onMinus = { item ->
                lifecycleScope.launch {
                    val newQty = (item.quantity - 1).coerceAtLeast(0)
                    db.itemBelanjaDao().updateQtyById(item.id, newQty)
                }
            }
        )

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        lifecycleScope.launch {
            db.itemBelanjaDao()
                .getItemByListFlow(listId, ownerEmail)
                .collect { items ->
                    adapter.submitList(items)
                }
        }

        tvDone.setOnClickListener { finish() }

        searchView.isIconified = false
        searchView.queryHint = getString(R.string.search_item_hint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                val name = query?.trim().orEmpty()
                if (name.isEmpty()) return true
                if (isSubmitting) return true
                isSubmitting = true

                lifecycleScope.launch {
                    try {
                        val existing =
                            db.itemBelanjaDao().getByName(listId, name)

                        if (existing == null) {
                            db.itemBelanjaDao().insert(
                                ItemBelanjaEntity(
                                    listId = listId,
                                    namaBarang = name,
                                    quantity = 0,
                                    ownerEmail = ownerEmail
                                )
                            )
                        }
                    } finally {
                        isSubmitting = false
                    }
                }

                searchView.setQuery("", false)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
}
