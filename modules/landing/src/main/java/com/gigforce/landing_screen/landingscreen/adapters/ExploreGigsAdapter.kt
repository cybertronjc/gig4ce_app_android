package com.gigforce.landing_screen.landingscreen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
//import com.gigforce.app.R
//import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.common_ui.shimmer.ShimmerHelper
//import com.gigforce.core.datamodels.gigpage.JobProfile
import com.gigforce.core.utils.GlideApp
import com.gigforce.landing_screen.R

class ExploreGigsAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
    }

    private var originalJobList: List<Any> = emptyList()
    private var onCardSelectedListener : OnCardSelectedListener? = null
    private var onSeeMoreSelectedListener: OnSeeMoreSelectedListener? = null
//
    fun setOnCardSelectedListener(onCardSelectedListener: OnCardSelectedListener){
        this.onCardSelectedListener = onCardSelectedListener
    }

    fun setOnSeeMoreSelectedListener(onSeeMoreSelectedListener: OnSeeMoreSelectedListener){
        this.onSeeMoreSelectedListener = onSeeMoreSelectedListener
    }

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

        private var jobTitleTv: TextView = itemView.findViewById(R.id.tv_client_activation)
        private var jobSubTitleTv: TextView = itemView.findViewById(R.id.tv_sub_client_activation)
        private var jobImage: ImageView = itemView.findViewById(R.id.iv_client_activation)
        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(jobProfile: JobProfile, position: Int) {
            jobTitleTv.text = jobProfile.cardTitle
            jobSubTitleTv.text = jobProfile.title
            GlideApp.with(context).load(jobProfile.cardImage).placeholder(ShimmerHelper.getShimmerDrawable()).into(jobImage)
        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition
            val jobProfile = originalJobList[newPosition]

            //onJobSelectedListener?.onJobSelected(jobProfile)
            onCardSelectedListener?.onCardSelected(jobProfile)
            //landingScreenFragment.navigateToGig((jobProfile as JobProfile).id)
        }

    }

    inner class SeeMoreViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),  View.OnClickListener {

        private var seeMoreImage: ImageView = itemView.findViewById(R.id.see_more_button)

        fun bindValues(jobProfile: Any, position: Int) {

            seeMoreImage.setOnClickListener {
                onSeeMoreSelectedListener?.onSeeMoreSelected(jobProfile)
            }
        }

        override fun onClick(p0: View?) {
            //onSeeMoreSelectedListener?.onSeeMoreSelected(job)
        }


    }

    interface OnCardSelectedListener {

        fun onCardSelected(
                any: Any
        )
    }

    interface OnSeeMoreSelectedListener {

        fun onSeeMoreSelected(
                any: Any
        )
    }

}

private val shimmer = Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
    .setDuration(1800) // how long the shimmering animation takes to do one full sweep
    .setBaseAlpha(0.9f) //the alpha of the underlying children
    .setHighlightAlpha(0.8f) // the shimmer alpha amount
    .setAutoStart(false)
    .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
    .build()

// This is the placeholder for the imageView
val shimmerDrawable = ShimmerDrawable().apply {
    setShimmer(shimmer)
    startShimmer()

}
