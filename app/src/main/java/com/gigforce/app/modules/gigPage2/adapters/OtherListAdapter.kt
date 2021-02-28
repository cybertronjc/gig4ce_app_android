package com.gigforce.app.modules.gigPage2.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage2.models.OtherOption
import com.gigforce.core.utils.GlideApp
import kotlinx.android.synthetic.main.recycler_item_other_option.view.*

interface OtherOptionClickListener {
    fun onOptionClicked(option: OtherOption)
}

class OtherOptionsAdapter(
    private val context: Context,
    private val otherOptions: List<OtherOption>
) :
    RecyclerView.Adapter<OtherOptionsAdapter.TimeLineViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater
    private var otherOptionClickListener: OtherOptionClickListener? = null

    fun setListener(otherOptionClickListener: OtherOptionClickListener) {
        this.otherOptionClickListener = otherOptionClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return TimeLineViewHolder(
            mLayoutInflater.inflate(
                R.layout.recycler_item_other_option,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val options = otherOptions[position]
        GlideApp.with(context).load(options.icon).into(holder.imageView)
        holder.title.text = options.name
    }

    override fun getItemCount() = otherOptions.size

    inner class TimeLineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        val title = itemView.textview
        val imageView = itemView.image_view

        override fun onClick(v: View?) {
            otherOptionClickListener?.onOptionClicked(otherOptions[adapterPosition])
        }
    }

}