package com.gigforce.app.modules.assessment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.assessment.models.OptionsArr
import kotlinx.android.synthetic.main.layout_message_rv_answers_access_frag.view.*
import kotlinx.android.synthetic.main.layout_rv_answers_adapter.view.*

class AssessmentAnswersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<OptionsArr>? = null
    private var showHelper = false;
    private lateinit var adapterCallbacks: AssessAdapterCallbacks

    interface AssessAdapterCallbacks {
        fun submitAnswer()
        fun setAnswered(boolean: Boolean)
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
                val obj = items!![position]

                holder.itemView.tv_number_rv_access_frag.text = (65 + position).toChar() + "."
                holder.itemView.tv_option_rv_access_frag.text = obj.que
                holder.itemView.tv_helper_rv_access_frag.text =
                    items!![holder.adapterPosition].reason
                if (obj.selectedAnswer != null && obj.selectedAnswer!!) {
                    val color = holder.itemView.context.getColor(
                        if (obj.is_answer == true) R.color.app_green else
                            R.color.red
                    )
                    holder.itemView.tv_number_rv_access_frag.setTextColor(color)
                    holder.itemView.tv_option_rv_access_frag.setTextColor(color)
                    holder.itemView.tv_helper_rv_access_frag.visibility = View.VISIBLE

                } else {
                    val color = holder.itemView.context.getColor(
                        if (obj.clickStatus == true) R.color.black_85 else (if (obj.is_answer == true) R.color.app_green else
                            R.color.red)
                    )

                    holder.itemView.tv_helper_rv_access_frag.visibility =
                        if (obj.clickStatus == true) View.GONE else View.VISIBLE
                    holder.itemView.tv_number_rv_access_frag.setTextColor(color)
                    holder.itemView.tv_option_rv_access_frag.setTextColor(color)
//                    holder.itemView.tv_helper_rv_access_frag.text = ""

                }
                holder.itemView.setOnClickListener {
                    if (items!![holder.adapterPosition].clickStatus!!) {
                        items!![holder.adapterPosition].selectedAnswer = true
                        adapterCallbacks.setAnswered(true)
//                        notifyItemChanged(holder.adapterPosition)

                    }

                }

            }
            MESSAGE_ROW -> {
                holder.itemView.tv_message_rv_answers_access_frag.visibility =
                    if (showHelper) View.VISIBLE else View.GONE
                holder.itemView.tv_message_rv_answers_access_frag.text = "Wow ! You Are Correct"
            }

        }


    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items?.size) MESSAGE_ROW else DEFAULT_ROW
    }

    fun addData(items: ArrayList<OptionsArr>) {
        this.items = items;
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