package com.gigforce.giger_app.calendarscreen.maincalendarscreen.bottomsheet

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.utils.AdapterClickListener
import com.gigforce.giger_app.R

class FeatureItemAdapter(val context: Context,val itemWidth : Int) :
    RecyclerView.Adapter<FeatureItemAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val cardView = itemView.findViewById<View>(R.id.card_view)
        val feature_icon = itemView.findViewById<ImageView>(R.id.feature_icon)
        val feature_title = itemView.findViewById<TextView>(R.id.feature_title)


        fun bindView(featureItem: FeatureModel) {
//            val displayMetrics = DisplayMetrics()
//            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//            val width = displayMetrics.widthPixels
            val lp = cardView.layoutParams
            lp.height = lp.height
            lp.width = itemWidth
            cardView.layoutParams = lp
            feature_icon.setImageResource(featureItem.icon)
            feature_title.text = featureItem.title
            cardView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            view?.let {
                data?.get(adapterPosition)?.let { it1 ->
                    clickListener?.onItemClick(it, it1, adapterPosition)
                }
            }
        }
    }

    var data: List<FeatureModel>? = null

    var clickListener: AdapterClickListener<FeatureModel>? = null

    fun setOnclickListener(listener: AdapterClickListener<FeatureModel>) {
        this.clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.feature_item, null)
        )
    }

    override fun getItemCount() = data?.size ?: 0


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        data?.let {
            holder.bindView(it.get(position))
        }
    }
}