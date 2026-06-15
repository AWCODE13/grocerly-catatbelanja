package com.example.grocerly.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.HapusActivity
import com.example.grocerly.R
import com.example.grocerly.TambahActivity
import com.example.grocerly.adapter.ListBelanjaAdapter
import com.example.grocerly.data.database.AppDatabase
import com.example.grocerly.data.model.ListSummary
import com.example.grocerly.ui.MainActivity
import com.example.grocerly.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ListBelanjaAdapter
    private lateinit var ownerEmail: String

    private lateinit var rv: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var emptyState: View
    private lateinit var btnAddEmpty: MaterialButton
    private lateinit var fabAddCart: FloatingActionButton

    private var allLists: List<ListSummary> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        db = AppDatabase.getInstance(requireContext())

        val session = SessionManager(requireContext())
        ownerEmail = if (session.isLogin()) session.getEmail() else "guest"

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarHome)
        toolbar.inflateMenu(R.menu.menu_home)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_hapus) {
                startActivity(Intent(requireContext(), HapusActivity::class.java))
                true
            } else false
        }

        rv = view.findViewById(R.id.rvListBelanja)
        rv.layoutManager = LinearLayoutManager(requireContext())

        searchView = view.findViewById(R.id.searchList)
        emptyState = view.findViewById(R.id.emptyStateHome)
        btnAddEmpty = view.findViewById(R.id.btnAddEmpty)
        fabAddCart = view.findViewById(R.id.fabAddCart)

        adapter = ListBelanjaAdapter(
            onClick = { list ->
                startActivity(
                    Intent(requireContext(), TambahActivity::class.java)
                        .putExtra("judul_list", list.judul)
                        .putExtra("list_id", list.id)
                )
            },
            onMoreClick = { anchor, list ->
                showItemMenu(anchor, list)
            }
        )
        rv.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText.orEmpty())
                return true
            }
        })

        btnAddEmpty.setOnClickListener {
            (requireActivity() as MainActivity).openCreateCartSheet()
        }

        fabAddCart.setOnClickListener {
            (requireActivity() as MainActivity).openCreateCartSheet()
        }

        collectHomeSummaries()
        return view
    }

    private fun collectHomeSummaries() {
        viewLifecycleOwner.lifecycleScope.launch {
            db.itemBelanjaDao()
                .getHomeSummariesFlow(ownerEmail)
                .collectLatest {
                    allLists = it
                    renderHome(it)
                }
        }
    }

    private fun renderHome(data: List<ListSummary>) {
        val isEmpty = data.isEmpty()

        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rv.visibility = if (isEmpty) View.GONE else View.VISIBLE
        searchView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        fabAddCart.visibility = if (isEmpty) View.GONE else View.VISIBLE

        adapter.submitList(data)
    }

    private fun filterList(keyword: String) {
        if (keyword.isBlank()) {
            adapter.submitList(allLists)
        } else {
            adapter.submitList(
                allLists.filter {
                    it.judul.contains(keyword, ignoreCase = true)
                }
            )
        }
    }

    private fun showItemMenu(anchor: View, list: ListSummary) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_list_item, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_pin -> {
                    lifecycleScope.launch {
                        db.listBelanjaDao().togglePin(listOf(list.id))
                    }
                    true
                }
                R.id.action_delete -> {
                    lifecycleScope.launch {
                        db.listBelanjaDao().softDelete(listOf(list.id))
                        startActivity(Intent(requireContext(), HapusActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
