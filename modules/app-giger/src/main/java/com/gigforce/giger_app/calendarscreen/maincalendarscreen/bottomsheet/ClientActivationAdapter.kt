package com.gigforce.giger_app.calendarscreen.maincalendarscreen.bottomsheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.client_activation.JobProfile
import com.gigforce.core.utils.AdapterClickListener
import com.gigforce.core.utils.GlideApp
import com.gigforce.giger_app.R

class ClientActivationAdapter (val context: Context, val itemWidth : Int) :
    RecyclerView.Adapter<ClientActivationAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val view = itemView.findViewById<View>(R.id.top_to_cardview)

        val tv_client_activation = itemView.findViewById<TextView>(R.id.tv_client_activation)
        val tv_sub_client_activation = itemView.findViewById<TextView>(R.id.tv_sub_client_activation)

        val iv_client_activation = itemView.findViewById<ImageView>(R.id.iv_client_activation)

        fun bindView(featureItem: JobProfile) {

//            val lp = view.layoutParams
//            lp.height = lp.height
//            lp.width = itemWidth
//            view.layoutParams = lp

            GlideApp.with(context)
                .load(featureItem.cardImage)
                .placeholder(getCircularProgressDrawable(context))
                .into(iv_client_activation)
//            showGlideImage(
//                obj?.cardImage ?: "",
//                getImageView(viewHolder, R.id.iv_client_activation)
//            )
            tv_client_activation.text = featureItem?.cardTitle
            tv_sub_client_activation.text = featureItem?.title

        }
    }

    var data: List<JobProfile>? = null

    var clickListener: AdapterClickListener<JobProfile>? = null

    fun setOnclickListener(listener: AdapterClickListener<JobProfile>) {
        this.clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.client_activation_item, null)
        )
    }

    override fun getItemCount() = data?.size ?: 0


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        data?.let {
            holder.bindView(it.get(position))
        }
    }

    override fun onViewAttachedToWindow(holder: ClientActivationAdapter.CustomViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.updateLayoutParams {
            width = itemWidth
        }
    }
}