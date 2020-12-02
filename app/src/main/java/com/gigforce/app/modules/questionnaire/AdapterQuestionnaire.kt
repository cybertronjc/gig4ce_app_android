package com.gigforce.app.modules.questionnaire

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.questionnaire.models.Questions
import com.gigforce.app.utils.ItemOffsetDecoration
import com.gigforce.app.utils.getCircularProgressDrawable
import kotlinx.android.synthetic.main.layout_rv_questionnaire_cards.view.*
import java.util.*

class AdapterQuestionnaire : RecyclerView.Adapter<AdapterQuestionnaire.ViewHolder>() {
    private var horizontalItemDecoration: ItemOffsetDecoration? = null
    private lateinit var items: List<Questions>

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_rv_questionnaire_cards, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = items[position]
        holder.itemView.tv_question_no_questionnaire.text =
                "${holder.itemView.resources.getString(R.string.ques)} ${position + 1}/${items?.size} :"
        holder.itemView.tv_question_questionnaire.text = question.question
        if (question.url.isNotEmpty()) {
            holder.itemView.iv_hint_questionnaire.visible()
            Glide.with(holder.itemView).load(question.url)
                    .placeholder(getCircularProgressDrawable(holder.itemView.context))
                    .into(holder.itemView.iv_hint_questionnaire)
        } else {
            holder.itemView.iv_hint_questionnaire.gone()
        }
        if (horizontalItemDecoration == null) {
            horizontalItemDecoration =
                    ItemOffsetDecoration(
                            holder.itemView.resources.getDimensionPixelSize(
                                    R.dimen.size_16
                            )
                    )
        } else {
            holder.itemView.rv_answers_questionnaire.removeItemDecoration(
                    horizontalItemDecoration!!
            )
        }
        holder.itemView.rv_answers_questionnaire.addItemDecoration(
                horizontalItemDecoration!!
        )
        val adapterAnswers = AdapterOptionsQuestionnaire()
        holder.itemView.rv_answers_questionnaire.adapter = adapterAnswers
        holder.itemView.rv_answers_questionnaire.layoutManager =
                LinearLayoutManager(holder.itemView.context)
        adapterAnswers.addData(question)
        adapterAnswers.setCallbacks(object :
                AdapterOptionsQuestionnaire.AdapterOptionsQuestionnaireCallbacks {
            override fun onClick(position: Int, value: String?, date: Date?, type: String) {
                if (holder.adapterPosition == -1) return
                items[holder.adapterPosition].selectedAnswer = position
                when (type) {
                    "date" -> {
                        items[holder.adapterPosition].selectedDate = date
                        items[holder.adapterPosition].dropDownItem = value ?: ""
                    }
                    "dropdown" -> {
                        items[holder.adapterPosition].dropDownItem = value ?: ""
                        items[holder.adapterPosition].selectedDate = date
                    }


                }

                adapterAnswers.notifyDataSetChanged()
            }

        })

    }

    override fun getItemCount(): Int {
        return if (::items.isInitialized) items.size else 0
    }

    fun addData(items: List<Questions>) {
        this.items = items
        notifyDataSetChanged()
    }
}