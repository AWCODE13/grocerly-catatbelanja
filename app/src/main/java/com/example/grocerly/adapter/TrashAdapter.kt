package com.example.grocerly.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.R
import com.example.grocerly.data.entity.ListBelanjaEntity

class TrashAdapter(
    private val onRestore: (ListBelanjaEntity) -> Unit,
    private val onDeletePermanent: (ListBelanjaEntity) -> Unit
) : RecyclerView.Adapter<TrashAdapter.VH>() {

    private val items = ArrayList<ListBelanjaEntity>()

    fun submitList(data: List<ListBelanjaEntity>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_trash_list, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvJudul.text = item.judul

        holder.btnRestore.setOnClickListener { onRestore(item) }
        holder.btnDeletePermanent.setOnClickListener { onDeletePermanent(item) }
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvJudul: TextView = v.findViewById(R.id.tvJudul)
        val btnRestore: ImageButton = v.findViewById(R.id.btnRestore)
        val btnDeletePermanent: ImageButton = v.findViewById(R.id.btnDeletePermanent)
    }
}