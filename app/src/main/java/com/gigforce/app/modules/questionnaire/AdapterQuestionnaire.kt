package com.gigforce.app.modules.questionnaire

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.questionnaire.models.Questions
import com.gigforce.app.utils.getCircularProgressDrawable
import kotlinx.android.synthetic.main.layout_rv_questionnaire_cards.view.*

class AdapterQuestionnaire : RecyclerView.Adapter<AdapterQuestionnaire.ViewHolder>() {
    private lateinit var items: List<Questions>

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_questionnaire_cards, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = items[position]
        holder.itemView.tv_question_no_questionnaire.text =
                "${holder.itemView.resources.getString(R.string.ques)} ${position + 1}/${items?.size} :"
        holder.itemView.tv_question_questionnaire.text = question.question
        if (!question.url.isNullOrEmpty()) {
            holder.itemView.iv_hint_questionnaire.visible()
            Glide.with(holder.itemView).load(question.url).placeholder(getCircularProgressDrawable(holder.itemView.context)).into(holder.itemView.iv_hint_questionnaire)
        } else {
            holder.itemView.iv_hint_questionnaire.gone()
        }
    }

    override fun getItemCount(): Int {
        return if (::items.isInitialized) items.size else 0;
    }

    fun addData(items: List<Questions>) {
        this.items = items
        notifyDataSetChanged()
    }
}