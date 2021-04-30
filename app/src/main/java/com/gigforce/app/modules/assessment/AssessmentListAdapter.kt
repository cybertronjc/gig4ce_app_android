package com.gigforce.app.modules.assessment

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.assessment.models.Assessment
import com.gigforce.app.modules.learning.models.CourseContent
import kotlinx.android.synthetic.main.assessment_bs_item.view.*

interface AssessmentClickListener{

    fun onAssessmentClicked(assessment : CourseContent)
}

class AssessmentListAdapter(
    private val resources: Resources,
    private val assessmentList: List<CourseContent>
) :
    RecyclerView.Adapter<AssessmentListAdapter.TimeLineViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater
    private var assessmentClickListener : AssessmentClickListener? = null

    fun setListener(assessmentClickListener : AssessmentClickListener){
        this.assessmentClickListener = assessmentClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return TimeLineViewHolder(
            mLayoutInflater.inflate(
                R.layout.assessment_bs_item,
                parent,
                false
            ), viewType
        )
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val videoModel = assessmentList[position]

        holder.assessmentTitle.text = videoModel.title
        holder.approxTime.text = videoModel.videoLengthString

        if (!videoModel.completed) {

            holder.statusTV.text = "PENDING"
            holder.statusTV.setBackgroundResource(R.drawable.rect_assessment_status_pending)
            holder.itemView.side_bar_status.setImageResource(R.drawable.assessment_line_pending)


        } else {

            holder.statusTV.text = "COMPLETED"
            holder.itemView.side_bar_status.setImageResource(R.drawable.assessment_line_done)
            holder.statusTV.setBackgroundResource(R.drawable.rect_assessment_status_completed)


        }
    }

    override fun getItemCount() = assessmentList.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        val assessmentTitle = itemView.title
        val statusTV = itemView.status
        val statusSlideRibbon = itemView.side_bar_status
        val approxTime = itemView.time

        override fun onClick(v: View?) {
            assessmentClickListener?.onAssessmentClicked(assessmentList[adapterPosition])
        }
    }

}