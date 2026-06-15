package com.example.grocerly.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.R
import com.example.grocerly.data.entity.ItemBelanjaEntity
import java.text.NumberFormat
import java.util.Locale

class ItemBelanjaAdapter2(
    private val onToggleChecked: (ItemBelanjaEntity, Boolean) -> Unit,
    private val onClick: (ItemBelanjaEntity) -> Unit
) : RecyclerView.Adapter<ItemBelanjaAdapter2.VH>() {

    private val items = ArrayList<ItemBelanjaEntity>()
    private val nf = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    fun submitList(data: List<ItemBelanjaEntity>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tambah_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.cbDone.setOnCheckedChangeListener(null)

        holder.cbDone.isChecked = item.isChecked

        val total = item.price * item.quantity
        holder.tvName.text = item.namaBarang
        holder.tvSub.text =
            "${nf.format(item.price)} x ${item.quantity}${if (item.unit.isNotBlank()) " ${item.unit}" else ""}"
        holder.tvTotal.text = nf.format(total)

        holder.tvName.paintFlags =
            if (item.isChecked)
                holder.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else
                holder.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        holder.cbDone.setOnCheckedChangeListener { _, checked ->
            onToggleChecked(item, checked)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cbDone: CheckBox = v.findViewById(R.id.cbDone)
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
        val tvTotal: TextView = v.findViewById(R.id.tvTotal)
    }
}
