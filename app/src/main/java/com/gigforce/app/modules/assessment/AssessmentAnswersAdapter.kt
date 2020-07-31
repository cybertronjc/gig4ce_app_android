package com.gigforce.app.modules.assessment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import kotlinx.android.synthetic.main.layout_message_rv_answers_access_frag.view.*
import kotlinx.android.synthetic.main.layout_rv_answers_adapter.view.*

class AssessmentAnswersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var items: ArrayList<String>
    private var showHelper = false;
    private lateinit var adapterCallbacks: AssessAdapterCallbacks

    interface AssessAdapterCallbacks {
        fun submitAnswer()
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
        return items.size.plus(1)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            DEFAULT_ROW -> {
                holder.itemView.tv_number_rv_access_frag.text = (65 + position).toChar() + "."
                holder.itemView.setOnClickListener {
                    if (!showHelper) {
                        if (holder.adapterPosition == 0) {
                            var green = holder.itemView.context.getColor(
                                R.color.app_green
                            )
                            holder.itemView.tv_number_rv_access_frag.setTextColor(green)
                            holder.itemView.tv_option_rv_access_frag.setTextColor(green)
                            holder.itemView.tv_helper_rv_access_frag.visibility = View.VISIBLE
                            items.subList(0, 3).clear()
                            notifyItemRangeRemoved(1, 3);
                            showHelper = true;
                            notifyItemChanged(items.size);
                            adapterCallbacks.submitAnswer()
                        }
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
        return if (position == items.size) MESSAGE_ROW else DEFAULT_ROW
    }

    fun addData(items: ArrayList<String>) {
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