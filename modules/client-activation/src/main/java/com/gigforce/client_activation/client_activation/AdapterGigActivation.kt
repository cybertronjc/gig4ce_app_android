package com.gigforce.client_activation.client_activation

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.client_activation.R
import com.gigforce.core.datamodels.client_activation.Dependency
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import kotlinx.android.synthetic.main.layout_rv_status_pending.view.*

class AdapterGigActivation : RecyclerView.Adapter<AdapterGigActivation.ViewHolder>() {
    private var callbacks: AdapterApplicationClientActivationCallbacks? = null
    var items: List<Dependency> = arrayListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_status_pending, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dependency = items[position]
        holder.itemView.iv_status_application.setImageDrawable(
            if (dependency.drawable == null) getCircularProgressDrawable(
                holder.itemView.context
            ) else {
                dependency.drawable
            }
        )
//        holder.itemView.tv_status.visibility =
//            if (!dependency.status.isNullOrEmpty()) View.VISIBLE else View.GONE
//        holder.itemView.tv_status.text = dependency.status
        holder.itemView.tv_status_application.text = dependency.title
//        holder.itemView.divider_bottom.visibility =
//            if (position == items.size - 1) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener {
            if (holder.adapterPosition == -1) return@setOnClickListener
            callbacks?.onItemClick(
                items.get(holder.adapterPosition)
            )
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addData(items: List<Dependency>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setImageDrawable(feature: String, drawable: Drawable, isDone: Boolean) {
        val i = items.indexOf(Dependency(type = feature))
        items[i].drawable = drawable
        items[i].isDone = isDone
        notifyItemChanged(i)
    }

    fun setCallbacks(callbacks: AdapterApplicationClientActivationCallbacks) {
        this.callbacks = callbacks
    }

    interface AdapterApplicationClientActivationCallbacks {
        fun onItemClick(dependency: Dependency)

    }

}