package com.homio.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.homio.app.databinding.ItemMessageBubbleBinding
import com.homio.app.model.ChatMessage

class MessageBubbleAdapter(private var items: List<ChatMessage>) :
    RecyclerView.Adapter<MessageBubbleAdapter.BubbleViewHolder>() {

    inner class BubbleViewHolder(val binding: ItemMessageBubbleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BubbleViewHolder {
        val binding = ItemMessageBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BubbleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BubbleViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvBubble.text = item.message
        holder.binding.tvTime.text = item.sentAt ?: ""

        val params = holder.binding.root.layoutParams as? ViewGroup.MarginLayoutParams
        if (item.isMine) {
            holder.binding.root.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        } else {
            holder.binding.root.layoutDirection = android.view.View.LAYOUT_DIRECTION_LTR
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ChatMessage>) {
        items = newItems
        notifyDataSetChanged()
    }
}
