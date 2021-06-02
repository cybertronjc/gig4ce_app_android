package com.gigforce.app.modules.questionnaire

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.client_activation.client_activation.models.Cities
import com.gigforce.client_activation.client_activation.models.States
import com.gigforce.app.modules.questionnaire.models.GfUsers
import com.gigforce.app.modules.questionnaire.models.Questions
import com.gigforce.common_ui.decors.ItemOffsetDecoration
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import kotlinx.android.synthetic.main.layout_rv_questionnaire_cards.view.*
import java.util.*

class AdapterQuestionnaire : RecyclerView.Adapter<AdapterQuestionnaire.ViewHolder>() {
    private lateinit var callbacks: AdapterQuestionnaireCallbacks
    private var horizontalItemDecoration: ItemOffsetDecoration? = null
    var items: List<Questions> = listOf()
    private var states: MutableList<States>? = null
    private var cities: MutableList<Cities>? = null
    private var state: States? = null
    private var stateCityMap: MutableMap<States, MutableList<Cities>?>? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_questionnaire_cards, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = items[position]
        holder.itemView.tv_question_no_questionnaire.text = Html.fromHtml(
            "${holder.itemView.resources.getString(R.string.ques)} ${position + 1}/${items?.size} :"
        )
        holder.itemView.tv_question_questionnaire.text = Html.fromHtml(question.question)
        if (question.url.isNotEmpty()) {
            holder.itemView.iv_hint_questionnaire.visible()
            Glide.with(holder.itemView).load(question.url)
                .placeholder(
                    getCircularProgressDrawable(
                        holder.itemView.context
                    )
                )
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
        if (!stateCityMap.isNullOrEmpty()) {
            adapterAnswers.setStateCityMap(stateCityMap!!)

        }
        if (!states.isNullOrEmpty() && adapterAnswers.getStateCityMap().isNullOrEmpty()) {
            adapterAnswers.setStates(states!!)
        }
        if (!cities.isNullOrEmpty()) {
            adapterAnswers.setCities(state!!, cities!!)
        }
        holder.itemView.rv_answers_questionnaire.adapter = adapterAnswers
        holder.itemView.rv_answers_questionnaire.layoutManager =
            LinearLayoutManager(holder.itemView.context)
        adapterAnswers.addData(question)
        adapterAnswers.setCallbacks(object :
            AdapterOptionsQuestionnaire.AdapterOptionsQuestionnaireCallbacks {
            override fun onClick(position: Int, value: String?, date: Date?, type: String) {
                if (holder.adapterPosition == -1) return
                items[holder.adapterPosition].selectedAnswer = position
                items[holder.adapterPosition].answer = value ?: ""

                when (type) {
                    "date" -> {
                        items[holder.adapterPosition].selectedDate = date
                        items[holder.adapterPosition].dropDownItem = value ?: ""
                    }
                    "state_city_dropdown" -> {
                        items[holder.adapterPosition].dropDownItem = value ?: ""
                        items[holder.adapterPosition].selectedDate = date
                    }


                }

                adapterAnswers.notifyDataSetChanged()
            }

            override fun getStates(
                stateCityMap: MutableMap<States, MutableList<Cities>?>,
                position: Int
            ) {
                if (holder.adapterPosition == -1) return
                this@AdapterQuestionnaire.stateCityMap = stateCityMap
                callbacks.getStates(position, holder.adapterPosition)
            }

            override fun getCities(
                stateCityMap: MutableMap<States, MutableList<Cities>?>,
                states: States
            ) {
                if (holder.adapterPosition == -1) return
                this@AdapterQuestionnaire.stateCityMap = stateCityMap
                this@AdapterQuestionnaire.state = states
                callbacks.getCities(states, holder.adapterPosition)
            }

            override fun getAllCities(childPosition: Int) {
                if (holder.adapterPosition == -1) return
                callbacks.getAllCities(holder.adapterPosition, childPosition)
            }

            override fun refresh() {
                callbacks.refresh()
            }

        })

    }

    override fun getItemCount(): Int {
        return if (items != null) items.size else 0
    }

    fun addData(items: List<Questions>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setCallbacks(callbacks: AdapterQuestionnaireCallbacks) {
        this.callbacks = callbacks
    }

    fun setStates(it: MutableList<States>?, parentPosition: Int) {
        this.states = it
        notifyItemChanged(parentPosition)
    }

    fun setCities(it: MutableList<Cities>?, parentPosition: Int) {
        this.cities = it;
        notifyItemChanged(parentPosition)
    }

    fun setAllCities(cities: MutableList<GfUsers>, parentPosition: Int, childPosition: Int) {
        if (!items.isNullOrEmpty()) {
            items[parentPosition].options[childPosition].cities = cities
            notifyItemChanged(parentPosition)
        }
    }

    public interface AdapterQuestionnaireCallbacks {
        fun getStates(childPosition: Int, parentPosition: Int)
        fun getCities(state: States, parentPosition: Int)
        fun getAllCities(adapterPosition: Int, childPosition: Int)
        fun refresh()

    }
}