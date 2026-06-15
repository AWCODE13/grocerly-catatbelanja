package com.example.grocerly.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.R
import com.example.grocerly.data.model.FinishedCartSummary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

class RecentCartsAdapter(
    private val onClick: ((FinishedCartSummary) -> Unit)? = null
) : RecyclerView.Adapter<RecentCartsAdapter.VH>() {

    private val items = ArrayList<FinishedCartSummary>()

    private val nf: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    }

    fun submitList(data: List<FinishedCartSummary>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_belanja, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvJudul.text = item.judul
        holder.tvPinned.visibility = View.GONE

        val budget = item.budgetLimit.coerceAtLeast(0L)
        val spent = item.spent.coerceAtLeast(0.0)

        if (budget <= 0L) {
            holder.progress.max = 100
            holder.progress.progress = 0
            holder.tvSpent.text = "${nf.format(spent)} of ${nf.format(0)} spent"
        } else {
            holder.progress.max = budget.toInt()
            val p = spent.roundToInt().coerceIn(0, budget.toInt())
            holder.progress.progress = p
            holder.tvSpent.text = "${nf.format(spent)} of ${nf.format(budget)} spent"
        }

        holder.itemView.setOnClickListener {
            onClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvJudul: TextView = v.findViewById(R.id.tvJudul)
        val tvPinned: TextView = v.findViewById(R.id.tvPinned)
        val progress: ProgressBar = v.findViewById(R.id.progressBudget)
        val tvSpent: TextView = v.findViewById(R.id.tvSpent)
    }
}
