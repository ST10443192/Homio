package com.homio.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.homio.app.databinding.ItemCardRowBinding

data class CardRow(
    val title: String,
    val subtitle: String,
    val status: String? = null,
    val tag: Any? = null
)

class SimpleCardAdapter(
    private var items: List<CardRow>,
    private val onClick: ((CardRow) -> Unit)? = null
) : RecyclerView.Adapter<SimpleCardAdapter.CardViewHolder>() {

    inner class CardViewHolder(val binding: ItemCardRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvSubtitle.text = item.subtitle
        if (item.status.isNullOrBlank()) {
            holder.binding.tvStatus.visibility = android.view.View.GONE
        } else {
            holder.binding.tvStatus.visibility = android.view.View.VISIBLE
            holder.binding.tvStatus.text = item.status
        }
        holder.itemView.setOnClickListener { onClick?.invoke(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<CardRow>) {
        items = newItems
        notifyDataSetChanged()
    }
}
