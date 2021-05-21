package com.gigforce.landing_screen.landingscreen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.landing_screen.landingscreen.LandingScreenFragment
import com.gigforce.core.utils.AdapterClickListener
import com.gigforce.core.utils.GlideApp

class ExploreByIndustryAdapter(
    private val context: Context
) : RecyclerView.Adapter<ExploreByIndustryAdapter.ExploreByIndustryViewHolder>(){

    private var originalList: List<LandingScreenFragment.TitleSubtitleModel> = emptyList()
    var clickListener : AdapterClickListener<LandingScreenFragment.TitleSubtitleModel>? = null
    fun setOnclickListener(listener : AdapterClickListener<LandingScreenFragment.TitleSubtitleModel>){
        this.clickListener = listener
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExploreByIndustryViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.explore_by_industry_item, parent, false)
        return ExploreByIndustryViewHolder(view)
    }


    override fun getItemCount(): Int {
        return originalList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == originalList.size - 1 ){
            return 2
        }
        else {
            return 1

        }
    }

    override fun onBindViewHolder(holder: ExploreByIndustryViewHolder, position: Int) {
        holder.bindValues(originalList.get(position), position)
    }

    fun setData(contacts: List<LandingScreenFragment.TitleSubtitleModel>) {
        this.originalList = contacts
        notifyDataSetChanged()
    }


    inner class ExploreByIndustryViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var title: TextView = itemView.findViewById(R.id.title)
        private var image: ImageView = itemView.findViewById(R.id.img_view)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(model: LandingScreenFragment.TitleSubtitleModel, position: Int) {
            title.text = model.title
            GlideApp.with(context).load(model.imgStr).into(image)
        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition
            clickListener?.let { setOnclickListener(it) }
        }

    }

}