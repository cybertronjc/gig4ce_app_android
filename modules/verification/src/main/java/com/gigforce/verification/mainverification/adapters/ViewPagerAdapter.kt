package com.gigforce.verification.mainverification.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.utils.GlideApp
import com.gigforce.verification.R


class ViewPagerAdapter(private val context: Context) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    private var list: List<KYCImageModel> = listOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        return ViewPagerViewHolder(parent)
    }


    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setItem(list: List<KYCImageModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    class ViewPagerViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        constructor(parent: ViewGroup) : this(
            LayoutInflater.from(parent.context).inflate(
                R.layout.kyc_image_card_view_item,
                parent, false
            )
        )
        private var title: TextView = itemView.findViewById(R.id.title)
        private var backgroundImage: ImageView = itemView.findViewById(R.id.imageBack)

        fun bind(kYCImageModel: KYCImageModel) {
            title.text = kYCImageModel.text
            GlideApp.with(itemView.context)
                .load(kYCImageModel.imageIcon)
                .into(backgroundImage)
        }
    }
}
