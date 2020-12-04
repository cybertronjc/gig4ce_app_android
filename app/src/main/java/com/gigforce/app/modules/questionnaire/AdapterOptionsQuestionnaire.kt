package com.gigforce.app.modules.questionnaire

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.questionnaire.models.Questions
import com.gigforce.app.utils.GenericSpinnerAdapter
import kotlinx.android.synthetic.main.layout_answers_rv_questionnaire.view.*
import kotlinx.android.synthetic.main.layout_date_rv_questionnaire.view.*
import kotlinx.android.synthetic.main.layout_drop_down_questionnaire.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AdapterOptionsQuestionnaire : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var callbacks: AdapterOptionsQuestionnaireCallbacks
    private lateinit var item: Questions


    class ViewHolderText(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderDropDown(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderDate(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT ->
                ViewHolderText(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_answers_rv_questionnaire, parent, false)
                )
            TYPE_DROPDOWN -> ViewHolderDropDown(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.layout_drop_down_questionnaire, parent, false)
            )

            DATE -> ViewHolderDate(

                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.layout_date_rv_questionnaire, parent, false)
            )

            else ->
                ViewHolderText(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_answers_rv_questionnaire, parent, false)
                )

        }


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val option = item.options[position]


        when (getItemViewType(position)) {
            TYPE_TEXT -> {
                holder.itemView.tv_answer_questionnaire.setBackgroundResource(if (item.selectedAnswer == position) R.drawable.border_lipstick_rad_4 else R.drawable.border_27979797_rad_4)

                holder.itemView.tv_answer_questionnaire.text = option.answer
                holder.itemView.tv_answer_questionnaire.setCompoundDrawablesWithIntrinsicBounds(
                        if (option.isAnswer) R.drawable.ic_thumbs_up else R.drawable.ic_thumbs_down,
                        0,
                        0,
                        0
                )
                holder.itemView.setOnClickListener {
                    if (holder.adapterPosition == -1) return@setOnClickListener
                    callbacks.onClick(holder.adapterPosition, null, null, option.type)
                }


            }
            TYPE_DROPDOWN -> {
                if (option.options[0] != "Select") {
                    option.options.add(0, "Select")

                }
                holder.itemView.tv_question.text = option.answer
                holder.itemView.ll_questionnaire.setBackgroundResource(if (item.selectedAnswer == position) R.drawable.border_lipstick_rad_4 else R.drawable.border_27979797_rad_4)

                holder.itemView.sp_options.adapter = GenericSpinnerAdapter(
                        holder.itemView.context,
                        R.layout.layout_custom_spinner_drop_down, option.options
                )
                holder.itemView.sp_options.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                            override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                            ) {
                                holder.itemView.setOnClickListener {
                                    if (holder.adapterPosition == -1) return@setOnClickListener
                                    callbacks.onClick(
                                            holder.adapterPosition,
                                            holder.itemView.sp_options.selectedItem.toString(), null, option.type
                                    )
                                }
                            }

                        }


            }
            DATE -> {
                holder.itemView.tv_date_label.text = option.answer
                holder.itemView.ll_date.setBackgroundResource(if (item.selectedAnswer == position) R.drawable.border_lipstick_rad_4 else R.drawable.border_27979797_rad_4)
                holder.itemView.setOnClickListener {

                    val c = Calendar.getInstance()
                    val year = c.get(Calendar.YEAR)
                    val month = c.get(Calendar.MONTH)
                    val day = c.get(Calendar.DAY_OF_MONTH)


                    val dpd = DatePickerDialog(holder.itemView.context, { view, year, monthOfYear, dayOfMonth ->
                        val calendar = Calendar.getInstance()
                        calendar[year, monthOfYear] = dayOfMonth
                        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")
                        val dateString: String = dateFormat.format(calendar.time)
                        holder.itemView.tv_date_value.text = dateString
                        callbacks.onClick(
                                holder.adapterPosition,
                                null, calendar.time, option.type
                        )
                        // Display Selected date in textbox

                    }, year, month, day)

                    dpd.show()

                }
            }
        }

    }

    override fun getItemCount(): Int {
        return if (::item.isInitialized) item.options.size else 0
    }

    fun addData(item: Questions) {
        this.item = item;
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (item.options[position].type == "mcq") TYPE_TEXT
        else if (item.options[position].type == "state_city_dropdown") TYPE_DROPDOWN
        else if (item.options[position].type == "date") DATE else TYPE_TEXT
    }

    companion object {
        val TYPE_TEXT = 0
        val TYPE_DROPDOWN = 1
        val DATE = 2


    }

    fun setCallbacks(callbacks: AdapterOptionsQuestionnaireCallbacks) {
        this.callbacks = callbacks;
    }

    public interface AdapterOptionsQuestionnaireCallbacks {
        fun onClick(position: Int, value: String?, date: Date?, typ: String)
    }


}