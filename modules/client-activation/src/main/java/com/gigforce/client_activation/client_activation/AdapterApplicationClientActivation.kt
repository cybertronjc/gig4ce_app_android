package com.gigforce.client_activation.client_activation

import android.graphics.drawable.Drawable
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.adapters.ActiveLocationsAdapter
import com.gigforce.client_activation.client_activation.models.City
import com.gigforce.core.datamodels.client_activation.Dependency
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.synthetic.main.layout_rv_status_pending.view.*

class AdapterApplicationClientActivation :
        RecyclerView.Adapter<AdapterApplicationClientActivation.ApplicationViewHolder>() {
    private var callbacks: AdapterApplicationClientActivationCallbacks? = null
    var items: List<Dependency> = arrayListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private lateinit var root: View
    override fun onCreateViewHolder(  parent: ViewGroup,
                                      viewType: Int
    ): ApplicationViewHolder {
        root = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.layout_rv_status_pending, parent, false)
        return ApplicationViewHolder(root)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bindValues(items[position], position)
    }

    inner class ApplicationViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        private var iv_status_application: ImageView = itemView.findViewById(R.id.iv_status_application)
        private var tv_status_application: TextView = itemView.findViewById(R.id.tv_status_application)
//        private var tv_optional_application: TextView = itemView.findViewById(R.id.tv_optional_application)


        fun bindValues(dependency: Dependency , position: Int) {

            iv_status_application.setImageDrawable(
                if (dependency.drawable == null) getCircularProgressDrawable(
                   itemView.context
                ) else {
                    dependency.drawable
                }
            )

            if (dependency.type.equals("aadhar_card") || dependency.type.equals("pan_card")){
                dependency.isOptional = true
                tv_status_application.text = dependency.title
            }
            else{
                dependency.isOptional = false
                var txt = dependency.title + "<font color=\"red\"> *</font>"
                tv_status_application.setText(Html.fromHtml(txt), TextView.BufferType.SPANNABLE)
            }
//        holder.itemView.divider_bottom.visibility =
//                if (position == items.size - 1) View.GONE else View.VISIBLE
           itemView.setOnClickListener {
                if (adapterPosition == -1) return@setOnClickListener
                callbacks?.onItemClick(dependency)

            }


        }

    }

    override fun getItemCount(): Int {
        return items.size;
    }

    fun addData(items: List<Dependency>) {
        this.items = items;
        notifyDataSetChanged()
    }

    fun setImageDrawable(feature: String, drawable: Drawable, isDone: Boolean) {
        val i = items.indexOf(Dependency(type = feature))
        items[i].drawable = drawable
        items[i].isDone = isDone
        notifyItemChanged(i);
    }

    fun setCallbacks(callbacks: AdapterApplicationClientActivationCallbacks) {
        this.callbacks = callbacks;
    }

    interface AdapterApplicationClientActivationCallbacks {
        fun onItemClick(dependency: Dependency);

    }

}