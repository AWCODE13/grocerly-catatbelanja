package com.example.grocerly.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.R
import com.example.grocerly.data.model.ListSummary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

class ListBelanjaAdapter(
    private val onClick: (ListSummary) -> Unit,
    private val onMoreClick: (anchor: View, item: ListSummary) -> Unit
) : ListAdapter<ListSummary, ListBelanjaAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list_belanja, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.tvJudul.text = item.judul
        holder.tvPinned.visibility = if (item.pinned) View.VISIBLE else View.GONE

        val spentLong = item.spent.roundToInt().toLong()
        val budget = item.budgetLimit

        holder.tvSpent.text = "${formatRupiah(spentLong)} of ${formatRupiah(budget)} spent"

        holder.progress.max = 100
        holder.progress.progress = calcPercent(item.spent, budget)

        holder.itemView.setOnClickListener { onClick(item) }
        holder.btnMore.setOnClickListener { onMoreClick(holder.btnMore, item) }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvJudul: TextView = v.findViewById(R.id.tvJudul)
        val tvPinned: TextView = v.findViewById(R.id.tvPinned)
        val tvSpent: TextView = v.findViewById(R.id.tvSpent)
        val progress: ProgressBar = v.findViewById(R.id.progressBudget)
        val btnMore: View = v.findViewById(R.id.btnMore)
    }

    private fun calcPercent(spent: Double, budgetLimit: Long): Int {
        if (budgetLimit <= 0L) return 0
        val p = (spent / budgetLimit.toDouble()) * 100.0
        return p.roundToInt().coerceIn(0, 100)
    }

    private fun formatRupiah(value: Long): String {
        val locale = Locale("id", "ID")
        val nf = NumberFormat.getCurrencyInstance(locale)
        nf.maximumFractionDigits = 0
        nf.minimumFractionDigits = 0
        return nf.format(value)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ListSummary>() {
            override fun areItemsTheSame(oldItem: ListSummary, newItem: ListSummary): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ListSummary, newItem: ListSummary): Boolean =
                oldItem == newItem
        }
    }
}
