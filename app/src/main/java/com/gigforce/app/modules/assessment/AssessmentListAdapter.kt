package com.gigforce.app.modules.assessment

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.assessment.models.Assessment
import kotlinx.android.synthetic.main.assessment_bs_item.view.*

class AssessmentListAdapter(
    private val resources: Resources,
    private val assessmentList: List<Assessment>
) :
    RecyclerView.Adapter<AssessmentListAdapter.TimeLineViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater

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
        holder.approxTime.text = videoModel.assessmentLength

        if (videoModel.status == Assessment.STATUS_PENDING) {

            holder.statusTV.text = "PENDING"
            holder.statusTV.status.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.yellow,
                    null
                )
            )
            holder.statusSlideRibbon.setCardBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.yellow,
                    null
                )
            )

        } else {

            holder.statusTV.text = "COMPLETED"
            holder.statusTV.status.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.green,
                    null
                )
            )
            holder.statusSlideRibbon.setCardBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.green,
                    null
                )
            )
        }

    }

    override fun getItemCount() = assessmentList.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView) {

        val assessmentTitle = itemView.title
        val statusTV = itemView.status
        val statusSlideRibbon = itemView.side_bar_status
        val approxTime = itemView.time
    }

}