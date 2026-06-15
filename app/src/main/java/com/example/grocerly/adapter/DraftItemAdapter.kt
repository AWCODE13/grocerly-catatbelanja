package com.example.grocerly.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.R
import com.example.grocerly.data.entity.ItemBelanjaEntity

class DraftItemAdapter(
    private val onPlus: (ItemBelanjaEntity) -> Unit,
    private val onMinus: (ItemBelanjaEntity) -> Unit
) : ListAdapter<ItemBelanjaEntity, DraftItemAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_add_item_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.tvName.text = item.namaBarang

        if (item.quantity == 0) {
            holder.btnMinus.visibility = View.GONE
            holder.tvQty.visibility = View.GONE
        } else {
            holder.btnMinus.visibility = View.VISIBLE
            holder.tvQty.visibility = View.VISIBLE
            holder.tvQty.text = item.quantity.toString()
        }

        holder.btnPlus.setOnClickListener { onPlus(item) }
        holder.btnMinus.setOnClickListener { onMinus(item) }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvQty: TextView = v.findViewById(R.id.tvQty)
        val btnPlus: TextView = v.findViewById(R.id.btnPlus)
        val btnMinus: TextView = v.findViewById(R.id.btnMinus)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ItemBelanjaEntity>() {
            override fun areItemsTheSame(oldItem: ItemBelanjaEntity, newItem: ItemBelanjaEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ItemBelanjaEntity, newItem: ItemBelanjaEntity): Boolean =
                oldItem == newItem
        }
    }
}