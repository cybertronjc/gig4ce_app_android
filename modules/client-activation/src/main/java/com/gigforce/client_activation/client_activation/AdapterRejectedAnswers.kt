package com.gigforce.client_activation.client_activation

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.client_activation.R
import kotlinx.android.synthetic.main.layout_rv_rejected_answers.view.*

class AdapterRejectedAnswers : RecyclerView.Adapter<AdapterRejectedAnswers.ViewHolder>() {
    private lateinit var items: List<String>

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_rejected_answers, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tv_rejected_item.text = Html.fromHtml(items[position])
    }

    override fun getItemCount(): Int {
        return if (::items.isInitialized) items.size else 0
    }

    fun addData(items: List<String>) {
        this.items = items
        notifyDataSetChanged()
    }

}