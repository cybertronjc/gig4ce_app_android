package com.gigforce.app.modules.client_activation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.client_activation.models.DependencyGigActivation
import kotlinx.android.synthetic.main.layout_fragment_activation_gig.view.*
import kotlinx.android.synthetic.main.layout_rv_gig_activation.view.*
import kotlinx.android.synthetic.main.layout_rv_status_pending.view.divider_bottom

class AdapterGigActivation : RecyclerView.Adapter<AdapterGigActivation.ViewHolder>() {

    private lateinit var callbacks: AdapterGigActivationCallbacks
    var items: List<DependencyGigActivation> = arrayListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_rv_gig_activation, parent, false)
        );
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dependency = items[position]
        holder.itemView.iv_status_gig_activation.setImageResource(
                if (dependency.drawable == -1)
                    R.drawable.ic_status_pending
                else {
                    dependency.drawable
                }
        )
        holder.itemView.tv_rv_gig_activation.text = dependency.title
        holder.itemView.divider_bottom.visibility =
                if (position == items.size - 1) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener {
            if (holder.adapterPosition == -1) return@setOnClickListener
            callbacks?.onItemClick(items[holder.adapterPosition].docType)
        }

    }

    override fun getItemCount(): Int {
        return items.size;
    }

    fun addData(items: List<DependencyGigActivation>) {
        this.items = items;
        notifyDataSetChanged()
    }

    fun setImageDrawable(feature: String, drawable: Int) {
        val i = items.indexOf(DependencyGigActivation(docType = feature))
        items[i].drawable = drawable
        items[i].isDone = true
        notifyItemChanged(i);
    }

    fun setCallbacks(callbacks: AdapterGigActivationCallbacks) {
        this.callbacks = callbacks;
    }

    interface AdapterGigActivationCallbacks {
        fun onItemClick(feature: String);

    }


}