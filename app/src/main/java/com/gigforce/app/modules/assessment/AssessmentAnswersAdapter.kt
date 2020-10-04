package com.gigforce.app.modules.assessment

import android.app.ActionBar
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.assessment.models.OptionsArr
import kotlinx.android.synthetic.main.layout_message_rv_answers_access_frag.view.*
import kotlinx.android.synthetic.main.layout_rv_answers_adapter.view.*

class AssessmentAnswersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<OptionsArr>? = null
    private var showAnswerStatus = false;
    private lateinit var adapterCallbacks: AssessAdapterCallbacks
    private var message: String? = null

    interface AssessAdapterCallbacks {
        fun submitAnswer()
        fun setAnswered(isCorrect: Boolean, position: Int)
    }


    inner class ViewHolderAnswer(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class ViewHolderMessage(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MESSAGE_ROW -> ViewHolderMessage(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_message_rv_answers_access_frag, parent, false)
            )
            else -> ViewHolderAnswer(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_answers_adapter, parent, false)
            )
        }

    }

    override fun getItemCount(): Int {
        return if (items == null) 0 else items?.size?.plus(1)!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            DEFAULT_ROW -> {
                val obj = items!![position - 1]

                holder.itemView.tv_number_rv_access_frag.text = (65 + (position - 1)).toChar() + "."
                holder.itemView.tv_option_rv_access_frag.text = obj.que
                holder.itemView.tv_helper_rv_access_frag.text =
                    items!![holder.adapterPosition - 1].reason
                if (obj.selectedAnswer != null && obj.selectedAnswer!!) {
                    val color =
                        if (obj.is_answer == true) ColorAndBg(
                            holder.itemView.context.getColor(R.color.app_green),
                            holder.itemView.context.getDrawable(R.drawable.border_option_green)!!
                        ) else
                            ColorAndBg(
                                holder.itemView.context.getColor(R.color.red),
                                holder.itemView.context.getDrawable(R.drawable.border_option_red)!!
                            )

                    holder.itemView.tv_number_rv_access_frag.setTextColor(color.color)
                    holder.itemView.tv_option_rv_access_frag.setTextColor(color.color)
                    holder.itemView.tv_option_rv_access_frag.background = color.drawable


                    holder.itemView.tv_helper_rv_access_frag.visibility =
                        if (obj.reason.isEmpty()) View.GONE else (if (obj.showReason == true) View.VISIBLE else View.GONE)

                } else {
                    val color =
                        if (obj.clickStatus == true) ColorAndBg(
                            holder.itemView.context.getColor(R.color.black_85),
                            holder.itemView.context.getDrawable(R.drawable.border_tv_rv_answers_assess_frag)!!
                        )
                        else (if (obj.is_answer == true) ColorAndBg(
                            holder.itemView.context.getColor(R.color.app_green),
                            holder.itemView.context.getDrawable(R.drawable.border_option_green)!!
                        ) else if (obj.showReason == false) ColorAndBg(
                            holder.itemView.context.getColor(R.color.black_85),
                            holder.itemView.context.getDrawable(R.drawable.border_tv_rv_answers_assess_frag)!!
                        ) else
                            ColorAndBg(
                                holder.itemView.context.getColor(R.color.red),
                                holder.itemView.context.getDrawable(R.drawable.border_option_red)!!
                            ))


                    holder.itemView.tv_number_rv_access_frag.setTextColor(color.color)
                    holder.itemView.tv_option_rv_access_frag.setTextColor(color.color)
                    holder.itemView.tv_option_rv_access_frag.background = color.drawable

                    holder.itemView.tv_helper_rv_access_frag.visibility =
                        if (obj.clickStatus == true || obj.reason.isEmpty()) View.GONE else (if (obj.showReason == true) View.VISIBLE else View.GONE)

//                    holder.itemView.tv_helper_rv_access_frag.text = ""

                }
                if (items!![holder.adapterPosition - 1].clickStatus!!) {
                    holder.itemView.setOnClickListener {

                        items!![holder.adapterPosition - 1].selectedAnswer = true
                        adapterCallbacks.setAnswered(
                            items!![holder.adapterPosition - 1].is_answer!!,
                            holder.adapterPosition - 1
                        )
//                        notifyItemChanged(holder.adapterPosition)


                    }
                } else {
                    holder.itemView.setOnClickListener(null)
                }

            }
            MESSAGE_ROW -> {
                var params: ViewGroup.LayoutParams? = null
                if (showAnswerStatus) {
                    params = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.WRAP_CONTENT
                    )
                } else {
                    params = holder.itemView.layoutParams
                    params.height = 0
                }

                holder.itemView.layoutParams = params
                holder.itemView.tv_message_rv_answers_access_frag.text = message ?: ""
            }

        }


    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) MESSAGE_ROW else DEFAULT_ROW
    }

    fun addData(items: ArrayList<OptionsArr>, showAnswerStatus: Boolean, message: String) {
        this.items = items;
        this.showAnswerStatus = showAnswerStatus
        this.message = message;
        notifyDataSetChanged()
    }

    companion object {
        const val DEFAULT_ROW = 1;
        const val MESSAGE_ROW = 2;
    }

    fun setCallbacks(assessmentCallbacks: AssessAdapterCallbacks) {
        this.adapterCallbacks = assessmentCallbacks;
    }

}

data class ColorAndBg(val color: Int, val drawable: Drawable)