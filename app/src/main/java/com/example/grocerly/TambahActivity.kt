package com.example.grocerly

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.adapter.ItemBelanjaAdapter2
import com.example.grocerly.data.database.AppDatabase
import com.example.grocerly.data.entity.ItemBelanjaEntity
import com.example.grocerly.data.entity.PurchaseEntity
import com.example.grocerly.ui.AddItemActivity
import com.example.grocerly.ui.MainActivity
import com.example.grocerly.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt
import android.text.Editable
import android.text.TextWatcher


class TambahActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var toolbar: MaterialToolbar
    private lateinit var ownerEmail: String

    private var listId: Int = 0
    private var budgetLimit: Long = 0L

    private lateinit var tvBudget: TextView
    private lateinit var tvSpent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var progressBudget: ProgressBar

    private lateinit var rv: RecyclerView
    private lateinit var emptyState: View
    private lateinit var adapter: ItemBelanjaAdapter2

    private val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private var successSheetShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah)

        db = AppDatabase.getInstance(this)

        val session = SessionManager(this)
        ownerEmail = if (session.isLogin()) session.getEmail() else "guest"

        listId = intent.getIntExtra("list_id", 0)
        if (listId == 0) {
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbarTambah)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvBudget = findViewById(R.id.tvBudget)
        tvSpent = findViewById(R.id.tvSpent)
        tvRemaining = findViewById(R.id.tvRemaining)
        progressBudget = findViewById(R.id.progressBudget)

        rv = findViewById(R.id.rvItemBelanja)
        emptyState = findViewById(R.id.emptyState)

        rv.layoutManager = LinearLayoutManager(this)

        adapter = ItemBelanjaAdapter2(
            onToggleChecked = { item, checked ->
                lifecycleScope.launch {
                    db.itemBelanjaDao().setChecked(item.id, checked)
                }
            },
            onClick = { item ->
                openEditItemSheet(item)
            }
        )

        rv.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabProduk).setOnClickListener {
            startActivity(
                Intent(this, AddItemActivity::class.java)
                    .putExtra("list_id", listId)
            )
        }

        lifecycleScope.launch {
            val list = db.listBelanjaDao().getById(listId)
            if (list != null) {
                toolbar.title = list.judul
                budgetLimit = list.budgetLimit
                tvBudget.text = nf.format(budgetLimit)
            }
        }

        lifecycleScope.launch {
            db.itemBelanjaDao()
                .getItemQtyGtZeroFlow(listId, ownerEmail)
                .collect { items ->
                    if (items.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        rv.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        rv.visibility = View.VISIBLE
                    }

                    adapter.submitList(items)
                    val spent = updateBudgetHeader(items)
                    maybeShowBudgetSuccess(spent)
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tambah, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_rename -> {
                showRenameDialog()
                true
            }
            R.id.action_edit_budget -> {
                showEditBudgetDialog()
                true
            }
            R.id.action_delete_checked -> {
                showConfirmDeleteChecked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateBudgetHeader(items: List<ItemBelanjaEntity>): Long {
        val spent = items.filter { it.isChecked }
            .sumOf { it.price * it.quantity }

        val remaining = budgetLimit - spent

        tvBudget.text = nf.format(budgetLimit)
        tvSpent.text = nf.format(spent)
        tvRemaining.text = nf.format(remaining.coerceAtLeast(0L))

        progressBudget.max = 100
        progressBudget.progress =
            if (budgetLimit <= 0L) 0
            else ((spent.toDouble() / budgetLimit) * 100)
                .roundToInt()
                .coerceIn(0, 100)

        return spent
    }

    private fun maybeShowBudgetSuccess(spent: Long) {
        if (budgetLimit <= 0L || spent != budgetLimit || successSheetShowing) return

        successSheetShowing = true
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(
            R.layout.bottomsheet_budget_success,
            findViewById(android.R.id.content),
            false
        )
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvSpentValue)?.text = nf.format(spent)
        view.findViewById<TextView>(R.id.tvBudgetValue)?.text = nf.format(budgetLimit)

        view.findViewById<View>(R.id.btnFinish)?.setOnClickListener {
            lifecycleScope.launch {
                dialog.dismiss()
                finishListToInsights()
            }
        }

        view.findViewById<View>(R.id.btnKeepEditing)?.setOnClickListener {
            dialog.dismiss()
            successSheetShowing = false
        }

        dialog.setOnCancelListener { successSheetShowing = false }
        dialog.show()
    }

    private suspend fun finishListToInsights() {
        val checkedItems = db.itemBelanjaDao().getCheckedItemsOnce(listId)
        if (checkedItems.isEmpty()) return

        for (it in checkedItems) {
            db.purchaseDao().insert(
                PurchaseEntity(
                    listId = listId,
                    itemName = it.namaBarang,
                    qty = it.quantity,
                    price = it.price.toDouble(),
                    budgetLimit = budgetLimit,
                    ownerEmail = ownerEmail
                )
            )
        }

        db.itemBelanjaDao().deleteByListId(listId)
        db.listBelanjaDao().deletePermanent(listOf(listId))

        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra("start_tab", "insights")
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    private fun openEditItemSheet(item: ItemBelanjaEntity) {

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(
            R.layout.bottomsheet_edit_item,
            null,
            false
        )
        dialog.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val etQty = view.findViewById<EditText>(R.id.etQty)
        val etUnit = view.findViewById<EditText>(R.id.etUnit)

        val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
        val tvDone = view.findViewById<TextView>(R.id.tvDone)
        val btnPlus = view.findViewById<View>(R.id.btnPlus)
        val btnMinus = view.findViewById<View>(R.id.btnMinus)

        etName.setText(item.namaBarang)
        etPrice.setText(item.price.toString())
        etQty.setText(item.quantity.toString())
        etUnit.setText(item.unit.orEmpty())

        fun recalc() {
            val price = etPrice.text.toString().toLongOrNull() ?: 0L
            val qty = etQty.text.toString().toIntOrNull() ?: 0
            tvTotal.text = nf.format(price * qty)
        }

        recalc()

        etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recalc()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etQty.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recalc()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnPlus.setOnClickListener {
            val qty = etQty.text.toString().toIntOrNull() ?: 0
            etQty.setText((qty + 1).toString())
        }

        btnMinus.setOnClickListener {
            val qty = etQty.text.toString().toIntOrNull() ?: 0
            etQty.setText((qty - 1).coerceAtLeast(0).toString())
        }
        tvDone.setOnClickListener {

            val name = etName.text.toString().trim()
            if (name.isEmpty()) return@setOnClickListener

            val price = etPrice.text.toString().toLongOrNull() ?: 0L
            val qty = etQty.text.toString().toIntOrNull() ?: 0
            val unit = etUnit.text.toString().trim()

            lifecycleScope.launch {
                db.itemBelanjaDao().updateItem(
                    item.id,
                    name,
                    price,
                    qty,
                    unit
                )
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showRenameDialog() {
        val input = EditText(this)
        input.setText(toolbar.title)
        input.setSelection(input.text.length)

        AlertDialog.Builder(this)
            .setTitle("Rename Judul")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isEmpty()) return@setPositiveButton

                lifecycleScope.launch {
                    db.listBelanjaDao().updateTitle(listId, newTitle)
                    toolbar.title = newTitle
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditBudgetDialog() {
        val input = EditText(this)
        input.setText(budgetLimit.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Budget")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newBudget = input.text.toString().toLongOrNull() ?: 0L
                lifecycleScope.launch {
                    db.listBelanjaDao().updateBudget(listId, newBudget)
                    budgetLimit = newBudget
                    tvBudget.text = nf.format(newBudget)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showConfirmDeleteChecked() {
        AlertDialog.Builder(this)
            .setTitle("Hapus item dicentang?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    db.itemBelanjaDao().deleteCheckedItems(listId)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
