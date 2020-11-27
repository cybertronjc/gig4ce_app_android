package com.gigforce.app.modules.explore_by_role

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.client_activation.models.Role
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.getCircularProgressDrawable
import kotlinx.android.synthetic.main.layout_rv_explore_by_role.view.*

class AdapterExploreByRole : RecyclerView.Adapter<AdapterExploreByRole.ViewHolder>() {

    interface AdapterExploreByRoleCallbacks {
        fun onItemClicked(id: String?)
    }

    fun setCallbacks(callbacks: AdapterExploreByRoleCallbacks) {
        this.callbacks = callbacks
    }

    private var horizontalCarousel: Boolean = false
    private var callbacks: AdapterExploreByRoleCallbacks? = null
    private var items: List<Role>? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_explore_by_role, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (items != null) items?.size!! else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj = items?.get(position)
        holder.itemView.tv_title_rv_explore_by_role.text = obj?.role_title ?: ""
        holder.itemView.cb_interested_rv_explore_role.isChecked = obj?.isMarkedAsInterest!!
        GlideApp.with(holder.itemView.context)
            .load(obj?.role_image)
            .placeholder(getCircularProgressDrawable(holder.itemView.context))
            .into(holder.itemView.iv_rv_explore_by_role)
        holder.itemView.setOnClickListener {
            callbacks?.onItemClicked(items?.get(holder.adapterPosition)?.id)
        }
        if (horizontalCarousel) {
            holder.itemView.layoutParams = ViewGroup.LayoutParams(
                holder.itemView.context.resources.getDimensionPixelSize(R.dimen.size_237),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun addData(items: List<Role>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun isHorizontalCarousel() {
        this.horizontalCarousel = true
    }
}