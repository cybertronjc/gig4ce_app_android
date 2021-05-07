package com.gigforce.app.modules.landingscreen.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.landingscreen.LandingScreenFragment
import com.gigforce.app.modules.landingscreen.models.SeeMore
import com.gigforce.client_activation.client_activation.explore.ClientActiExploreList
import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.core.utils.GlideApp

class ExploreGigsAdapter(
    private val context: Context,  private val landingScreenFragment: LandingScreenFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
    }

    private var originalJobList: List<Any> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_ONE) {
            return ExploreGigViewHolder(
                LayoutInflater.from(context).inflate(R.layout.client_activation_landing_item_view, parent, false)
            )
        }
        else{
            return SeeMoreViewHolder(
                LayoutInflater.from(context).inflate(R.layout.see_more_view_item, parent, false)
            )}
    }


    override fun getItemCount(): Int {
        return originalJobList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == originalJobList.size - 1 ){
            return 2
        }
        else {
            return 1

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var itemViewType = getItemViewType(position)
        if (itemViewType === VIEW_TYPE_ONE) {
            (holder as ExploreGigViewHolder).bindValues(originalJobList.get(position) as JobProfile, position)
        } else {
            (holder as SeeMoreViewHolder).bindValues(originalJobList.get(position), position)
        }

    }

    fun setData(contacts: List<Any>) {
        this.originalJobList = contacts
        notifyDataSetChanged()
    }


    inner class ExploreGigViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var jobTitleTv: TextView = itemView.findViewById(com.gigforce.client_activation.R.id.tv_client_activation)
        private var jobSubTitleTv: TextView = itemView.findViewById(com.gigforce.client_activation.R.id.tv_sub_client_activation)
        private var jobImage: ImageView = itemView.findViewById(com.gigforce.client_activation.R.id.iv_client_activation)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(jobProfile: JobProfile, position: Int) {
            jobTitleTv.text = jobProfile.cardTitle
            jobSubTitleTv.text = jobProfile.title
            GlideApp.with(context).load(jobProfile.cardImage).into(jobImage)

        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition
            val jobProfile = originalJobList[newPosition]
            //onJobSelectedListener?.onJobSelected(jobProfile)
            landingScreenFragment.navigateToGig((jobProfile as JobProfile).id)
        }

    }

    inner class SeeMoreViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        private var seeMoreImage: ImageView = itemView.findViewById(R.id.see_more_button)

        fun bindValues(jobProfile: Any, position: Int) {

            seeMoreImage.setOnClickListener {
                landingScreenFragment.navigateToExploreGigs()
            }
        }


    }

}