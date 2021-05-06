package com.gigforce.app.modules.questionnaire


import `in`.galaxyofandroid.spinerdialog.OnSpinerItemClick
import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.app.Activity
import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.client_activation.client_activation.models.Cities
import com.gigforce.client_activation.client_activation.models.States
import com.gigforce.app.modules.questionnaire.models.Questions
import com.gigforce.app.utils.GenericSpinnerAdapter
import kotlinx.android.synthetic.main.layout_answers_rv_questionnaire.view.*
import kotlinx.android.synthetic.main.layout_date_rv_questionnaire.view.*
import kotlinx.android.synthetic.main.layout_drop_down_questionnaire.view.*
import kotlinx.android.synthetic.main.layout_rv_dropdown_questionnaire.view.*
import kotlinx.android.synthetic.main.layout_sp_city_dropdown.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class AdapterOptionsQuestionnaire : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var callbacks: AdapterOptionsQuestionnaireCallbacks
    private lateinit var item: Questions
    private var stateCityMap: MutableMap<States, MutableList<Cities>?> = mutableMapOf()
    private var allCities: MutableList<Cities>? = null

    class ViewHolderText(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderStateCityDropdown(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderDate(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderDropdown(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderCities(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT ->
                ViewHolderText(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.layout_answers_rv_questionnaire, parent, false)
                )
            STATE_CITY_DROPDOWN -> ViewHolderStateCityDropdown(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.layout_drop_down_questionnaire, parent, false)
            )

            TYPE_DROPDOWN -> {
                ViewHolderDropdown(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_rv_dropdown_questionnaire, parent, false)
                )
            }
            CITIES -> {
                ViewHolderCities(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_sp_city_dropdown, parent, false)
                )
            }

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

                if (item.selectedAnswer == position) {
                    holder.itemView.tv_answer_questionnaire.setCompoundDrawablesWithIntrinsicBounds(
                        if (option.isAnswer) R.drawable.ic_thumbs_up else R.drawable.ic_thumbs_down,
                        0,
                        0,
                        0
                    )

                } else {
                    holder.itemView.tv_answer_questionnaire.setCompoundDrawablesWithIntrinsicBounds(
                        if (option.isAnswer) R.drawable.ic_thumbs_up_not_selected else R.drawable.ic_thumbs_down_not_selected,
                        0,
                        0,
                        0
                    )

                }
                holder.itemView.setOnClickListener {
                    if (holder.adapterPosition == -1) return@setOnClickListener
                    callbacks.onClick(
                        holder.adapterPosition,
                        item.options[holder.adapterPosition].answer,
                        null,
                        option.type
                    )
                }
            }
            STATE_CITY_DROPDOWN -> {
                if (stateCityMap.isEmpty()) {
                    callbacks.getStates(stateCityMap, position)
                    holder.itemView.sp_state.gone()
                    holder.itemView.pb_state_city.visible()
                } else {
                    holder.itemView.sp_state.visible()
                    val arrayAdapter: GenericSpinnerAdapter<States> = GenericSpinnerAdapter(
                        holder.itemView.context,
                        R.layout.tv_options_header_sp,
                        stateCityMap.keys.toList()
                    )
                    holder.itemView.sp_state.adapter = arrayAdapter
                    if (option.selectedItemPosition != -1) {
                        holder.itemView.sp_state.setSelection(option.selectedItemPosition)
                    }
                    holder.itemView.sp_state.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}

                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                if (holder.adapterPosition == -1) return
                                item.selectedAnswer = -1
                                item.options[holder.adapterPosition].selectedItemPosition = position
                                val states = holder.itemView.sp_state.selectedItem as States
                                val cityForState = getCityForState(states)
                                item.selectedState = states.id

                                if (cityForState.isNotEmpty()) {
                                    holder.itemView.pb_state_city.gone()
                                    val arrayAdapter: GenericSpinnerAdapter<Cities> =
                                        GenericSpinnerAdapter(
                                            holder.itemView.context,
                                            R.layout.tv_options_header_sp,
                                            cityForState
                                        )
                                    holder.itemView.sp_city.visible()
                                    holder.itemView.sp_city.adapter = arrayAdapter
                                    holder.itemView.sp_city.onItemSelectedListener =
                                        object : AdapterView.OnItemSelectedListener {
                                            override fun onItemSelected(
                                                parent: AdapterView<*>?,
                                                view: View?,
                                                position: Int,
                                                id: Long
                                            ) {
                                                if (holder.adapterPosition == -1 || position == 0) return
                                                item.selectedAnswer = 0
                                                val city =
                                                    holder.itemView.sp_city.selectedItem as Cities
                                                item.selectedCity = city.name

                                            }

                                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                            }
                                        }

                                } else {
                                    holder.itemView.sp_city.gone()
                                    holder.itemView.pb_state_city.visible()

                                }

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
                    val dpd = DatePickerDialog(
                        holder.itemView.context,
                        R.style.DatePickerDialogTheme,
                        { view, year, monthOfYear, dayOfMonth ->
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

                        },
                        year,
                        month,
                        day
                    )
                    if (item.openDates?.openAllDates == false) {
                        if (item.openDates?.openPastDates == true) {
                            dpd.datePicker.maxDate = yesterday()?.time!!;
                        } else if (item.openDates?.openFutureDates == true) {
                            dpd.datePicker.minDate = System.currentTimeMillis();
                        }
                    }
                    dpd.show()
                    dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                        .setTextColor(holder.itemView.resources.getColor(R.color.colorPrimary));
                    dpd.getButton(DatePickerDialog.BUTTON_POSITIVE)
                        .setTextColor(holder.itemView.resources.getColor(R.color.colorPrimary));

                }
            }
            TYPE_DROPDOWN -> {
                if (option.options[0] != option.dropDownHint) {
                    option.options.add(0, option.dropDownHint)
                }
                val arrayAdapter: GenericSpinnerAdapter<String> = GenericSpinnerAdapter(
                    holder.itemView.context,
                    R.layout.tv_options_header_sp,
                    option.options
                )
                holder.itemView.sp_dropdown.adapter = arrayAdapter
                if (option.selectedItemPosition != -1) {
                    holder.itemView.sp_dropdown.setSelection(option.selectedItemPosition)
                }
                holder.itemView.sp_dropdown.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (holder.adapterPosition == -1) return
                            item.selectedAnswer = position - 1
                            item.options[holder.adapterPosition].selectedItemPosition = position
                            item.answer =
                                holder.itemView.sp_dropdown.selectedItem.toString()

                        }


                    }

            }
            CITIES -> {
                if (option.cities.isNullOrEmpty()) {
                    holder.itemView.pb_city.visible()
                    holder.itemView.tv_cities.gone()
                    callbacks.getAllCities(position)
                } else {
                    holder.itemView.pb_city.gone()
                    holder.itemView.tv_cities.visible()
                    holder.itemView.tv_cities.text = option.dropDownHint
                    val spinnerDialog = SpinnerDialog(
                        holder.itemView.context as Activity,
                        option.cities?.map { it.city }?.toList() as ArrayList<String>,
                        option.dropDownHint,
                        "close"
                    ) // With No Animation
                    spinnerDialog.setCancellable(true) // for cancellable
                    spinnerDialog.setShowKeyboard(false) // for open keyboard by default
                    spinnerDialog.bindOnSpinerListener(OnSpinerItemClick { spinnerItem, position ->
                        holder.itemView.tv_cities.text = spinnerItem
                        item.selectedAnswer = 0
                        item.selectedCity = spinnerItem
                        item.answer = spinnerItem
                    })
                    holder.itemView.tv_cities.setOnClickListener {
                        spinnerDialog.showSpinerDialog();
                    }
                }
            }
        }

    }

    private fun yesterday(): Date? {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return cal.time
    }

    override fun getItemCount(): Int {
        return if (::item.isInitialized) item.options.size else 0
    }

    fun addData(item: Questions) {
        this.item = item;
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (item.options[position].type) {
            "mcq" -> TYPE_TEXT
            "state_city_dropdown" -> STATE_CITY_DROPDOWN
            "date" -> DATE
            "dropdown" -> TYPE_DROPDOWN
            "cities" -> CITIES
            else -> TYPE_TEXT
        }
    }

    companion object {
        val TYPE_TEXT = 0
        val TYPE_DROPDOWN = 1
        val DATE = 2
        val STATE_CITY_DROPDOWN = 3
        val CITIES = 4;


    }

    private fun getCityForState(states: States): List<Cities> {
        if (stateCityMap[states] != null) {
            return stateCityMap[states]!!
        }
        callbacks.getCities(stateCityMap, states)
        return listOf()
    }

    fun setCallbacks(callbacks: AdapterOptionsQuestionnaireCallbacks) {
        this.callbacks = callbacks;
        callbacks.refresh()
    }

    fun setStates(states: List<States>) {
        stateCityMap.clear()
        states.forEach {
            stateCityMap[it] = null
        }
        notifyDataSetChanged()

    }

    fun setCities(states: States, cities: MutableList<Cities>) {

        stateCityMap[states] = cities
        notifyDataSetChanged()
    }

    fun getStateCityMap(): MutableMap<States, MutableList<Cities>?> {
        return stateCityMap
    }

    fun setStateCityMap(map: MutableMap<States, MutableList<Cities>?>) {
        this.stateCityMap = map
    }

    public interface AdapterOptionsQuestionnaireCallbacks {
        fun onClick(position: Int, value: String?, date: Date?, typ: String)
        fun getStates(stateCityMap: MutableMap<States, MutableList<Cities>?>, position: Int)
        fun getCities(stateCityMap: MutableMap<States, MutableList<Cities>?>, states: States)
        fun getAllCities(childPosition: Int)
        fun refresh()
    }


}