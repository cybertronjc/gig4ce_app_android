package com.gigforce.app.modules.explore_by_role

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.profile.models.Experience
import com.gigforce.app.utils.DropdownAdapter
import com.gigforce.app.utils.PushDownAnim
import kotlinx.android.synthetic.main.layout_next_add_profile_segments.view.*
import kotlinx.android.synthetic.main.layout_rv_add_experience.view.*
import java.text.SimpleDateFormat
import java.util.*

class AdapterAddExperience : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var adapterEducationCallbacks: AdapterAddEducationCallbacks? = null
    private var items: MutableList<Experience>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ACTION -> ViewHolderAction(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_next_add_profile_segments, parent, false)
            )
            else ->
                ViewHolderAddExperience(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_rv_add_experience, parent, false)
                )
        }


    }

    fun setCallbacks(adapterEducationCallbacks: AdapterAddEducationCallbacks) {
        this.adapterEducationCallbacks = adapterEducationCallbacks
    }

    interface AdapterAddEducationCallbacks {
        fun submitClicked(items: MutableList<Experience>)
        fun goBack()
//        fun uploadEducationDocument(position: Int)
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy")


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ACTION -> {
                val viewholder: ViewHolderAction = holder as ViewHolderAction
                PushDownAnim.setPushDownAnimTo(viewholder.itemView.tv_action).setOnClickListener(
                    View.OnClickListener {
                        adapterEducationCallbacks?.submitClicked(items!!)
                    })
                PushDownAnim.setPushDownAnimTo(viewholder.itemView.tv_cancel).setOnClickListener(
                    View.OnClickListener {
                        adapterEducationCallbacks?.goBack()
                    })
            }
            else -> {
                val experience = items?.get(position)
                val viewHolderAddEducation: ViewHolderAddExperience =
                    holder as ViewHolderAddExperience

                var calendar = Calendar.getInstance(TimeZone.getDefault())

                viewHolderAddEducation.itemView.currently_work_here_add_experience.setOnClickListener {
                    if (viewHolderAddEducation.adapterPosition == -1) return@setOnClickListener

                    items?.get(viewHolderAddEducation.adapterPosition)?.endDate = Date()
                    viewHolderAddEducation.itemView.end_date_add_experience.setText(
                        dateFormatter.format(Date())
                    )
                    items?.get(viewHolderAddEducation.adapterPosition)?.currentExperience =
                        viewHolderAddEducation.itemView.currently_work_here_add_experience.isChecked
                }

                viewHolderAddEducation.itemView.fresher_cb.setOnClickListener {
                    if (viewHolderAddEducation.adapterPosition == -1) return@setOnClickListener
                    items?.get(viewHolderAddEducation.adapterPosition)?.isFresher =
                        viewHolderAddEducation.itemView.fresher_cb.isChecked
                    notifyItemChanged(viewHolderAddEducation.adapterPosition)
                }
                viewHolderAddEducation.itemView.fresher_cb.isChecked =
                    experience?.isFresher ?: false
                viewHolderAddEducation.itemView.currently_work_here_add_experience.isChecked =
                    experience?.currentExperience ?: false
                viewHolderAddEducation.itemView.start_date_add_experience.setText(
                    if (experience?.startDate != null) dateFormatter.format(
                        experience?.startDate
                    ) else ""
                )
                holder.itemView.employment_type_add_experience.onFocusChangeListener =
                    View.OnFocusChangeListener { v, hasFocus -> if (hasFocus) holder.itemView.employment_type_add_experience.showDropDown() }

                holder.itemView.employment_type_add_experience.setOnTouchListener(View.OnTouchListener { v, event ->
                    holder.itemView.employment_type_add_experience.showDropDown()
                    false
                })
                viewHolderAddEducation.itemView.end_date_add_experience.setText(
                    if (experience?.endDate != null) dateFormatter.format(
                        experience?.endDate
                    ) else ""
                )
                viewHolderAddEducation.itemView.title_add_experience.addTextChangedListener(
                    object :
                        TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {

                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                            if (viewHolderAddEducation.adapterPosition == -1) return
                            items?.get(viewHolderAddEducation.adapterPosition)?.title =
                                s.toString()
                        }
                    })
                viewHolderAddEducation.itemView.company_add_experience.addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (viewHolderAddEducation.adapterPosition == -1) return
                        items?.get(viewHolderAddEducation.adapterPosition)?.company = s.toString()
                    }
                })
                val list = getEmployeeTypeList(viewHolderAddEducation.itemView.context)

                val employeeTypeAdapter = DropdownAdapter(holder.itemView.context, list)
                holder.itemView.employment_type_add_experience.setAdapter(employeeTypeAdapter)
                holder.itemView.employment_type_add_experience.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, arg1, pos, id ->
                        if (viewHolderAddEducation.adapterPosition == -1) return@OnItemClickListener
                        items?.get(viewHolderAddEducation.adapterPosition)?.employmentType =
                            list[pos]
                    }
                if (!experience?.employmentType.isNullOrEmpty()) {
                    holder.itemView.employment_type_add_experience.setText(experience?.employmentType)

                } else {
                    holder.itemView.employment_type_add_experience.setText("")

                }
                viewHolderAddEducation.itemView.location_add_experience.addTextChangedListener(
                    object :
                        TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {

                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                            if (viewHolderAddEducation.adapterPosition == -1) return
                            items?.get(viewHolderAddEducation.adapterPosition)?.location =
                                s.toString()
                        }
                    })

                viewHolderAddEducation.itemView.title_add_experience.setText(
                    if (experience?.title != null)
                        experience?.title
                    else ""
                )
                viewHolderAddEducation.itemView.company_add_experience.setText(
                    if (experience?.company != null)
                        experience?.company
                    else ""
                )
                viewHolderAddEducation.itemView.location_add_experience.setText(
                    if (experience?.location != null)
                        experience?.location
                    else ""
                )



                viewHolderAddEducation.itemView.start_date_add_experience.setOnClickListener {
                    val datePicker = DatePickerDialog(
                        holder.itemView.context,
                        DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                            Log.d("TEMP", "tmp date")
                            if (viewHolderAddEducation.adapterPosition == -1) return@OnDateSetListener
                            val date = "$i2/${i1 + 1}/$i"
                            items?.get(viewHolderAddEducation.adapterPosition)?.startDate =
                                dateFormatter.parse(date)
                            viewHolderAddEducation.itemView.start_date_add_experience.setText(date)

                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePicker.datePicker.maxDate = System.currentTimeMillis();
                    datePicker.show()
                }

                viewHolderAddEducation.itemView.end_date_add_experience.setOnClickListener {
                    val datePicker = DatePickerDialog(
                        holder.itemView.context,
                        DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                            Log.d("TEMP", "tmp date")
                            if (viewHolderAddEducation.adapterPosition == -1) return@OnDateSetListener
                            val date = "$i2/${i1 + 1}/$i"
                            items?.get(viewHolderAddEducation.adapterPosition)?.endDate =
                                dateFormatter.parse(date)
                            viewHolderAddEducation.itemView.end_date_add_experience.setText(date)

                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePicker.datePicker.maxDate = System.currentTimeMillis();
                    datePicker.show()
                }

                viewHolderAddEducation.itemView.bt_add_experience_add_more.visibility =
                    if (position == items?.size?.minus(1) ?: false || items?.size == 1) View.VISIBLE else View.GONE
                viewHolderAddEducation.itemView.bt_add_experience_add_more.setOnClickListener {
                    if (viewHolderAddEducation.adapterPosition == -1) return@setOnClickListener
                    items?.add(Experience())
                    notifyItemInserted(viewHolderAddEducation.adapterPosition + 1)
                    notifyItemChanged(viewHolderAddEducation.adapterPosition)
                }
                viewHolderAddEducation.itemView.bt_remove_experience_add_more.visibility =
                    if (position != 0) View.VISIBLE else View.GONE
                viewHolderAddEducation.itemView.bt_remove_experience_add_more.setOnClickListener {
                    if (viewHolderAddEducation.adapterPosition == -1) return@setOnClickListener
                    items?.removeAt(viewHolderAddEducation.adapterPosition)
                    notifyItemRemoved(viewHolderAddEducation.adapterPosition)
                    items?.size?.minus(1)?.let { it1 -> notifyItemChanged(it1) }
                }

                holder.itemView.title_add_experience.isEnabled = experience?.isFresher == false
                holder.itemView.company_add_experience.isEnabled = experience?.isFresher == false
                holder.itemView.employment_type_add_experience.isEnabled =
                    experience?.isFresher == false
                holder.itemView.location_add_experience.isEnabled = experience?.isFresher == false
                holder.itemView.start_date_add_experience.isEnabled = experience?.isFresher == false
                holder.itemView.end_date_add_experience.isEnabled = experience?.isFresher == false
                holder.itemView.currently_work_here_add_experience.isEnabled =
                    experience?.isFresher == false




                if (experience?.validateFields == true) {
                    validate(viewHolderAddEducation, experience)
                }
            }
        }

    }

    private fun getEmployeeTypeList(ctx: Context): List<String> {
        return listOf(
            ctx.resources.getString(R.string.full_time),
            ctx.resources.getString(R.string.internship),
            ctx.resources.getString(
                R.string.part_time
            )
        )

    }

    private fun validate(viewholder: ViewHolderAddExperience, education: Experience) {

        if (education.title.isNullOrEmpty() || education.company.isNullOrEmpty() || education.employmentType.isNullOrEmpty() || education.location.isNullOrEmpty() || education.startDate == null || education.endDate == null) {
            viewholder.itemView.form_error_add_experience.visible()
        } else {
            viewholder.itemView.form_error_add_experience.gone()
        }
        viewholder.itemView.line_et_title_add_experience.setBackgroundColor(
            if (viewholder.itemView.title_add_experience.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )
        viewholder.itemView.line_et_company_add_experience.setBackgroundColor(
            if (viewholder.itemView.company_add_experience.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )
        viewholder.itemView.line_employment_type_add_experience.setBackgroundColor(
            if (viewholder.itemView.employment_type_add_experience.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )
        viewholder.itemView.line_et_location_add_experience.setBackgroundColor(
            if (viewholder.itemView.location_add_experience.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )
        viewholder.itemView.start_date_add_experience.error =
            if (viewholder.itemView.start_date_add_experience.text.isEmpty()) viewholder.itemView.resources.getString(
                R.string.select_start_date_add_education
            ) else null
        viewholder.itemView.end_date_add_experience.error =
            if (viewholder.itemView.end_date_add_experience.text.isEmpty()) viewholder.itemView.resources.getString(
                R.string.select_end_date_add_education
            ) else null


    }


    override fun getItemCount(): Int {
        return if (items == null) 0 else items!!.size + 1
    }

    class ViewHolderAddExperience(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderAction(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val ADD_LANGUAGE = 0;
        const val ACTION = 1;


    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items?.size) ACTION else ADD_LANGUAGE
    }

    fun addData(items: MutableList<Experience>) {
        this.items = items;
        notifyDataSetChanged()
    }


}