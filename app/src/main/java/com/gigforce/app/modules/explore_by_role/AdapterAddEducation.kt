package com.gigforce.app.modules.explore_by_role

import android.app.DatePickerDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.utils.PushDownAnim
import kotlinx.android.synthetic.main.layout_next_add_profile_segments.view.*
import kotlinx.android.synthetic.main.layout_rv_add_education_fragment.view.*
import java.text.SimpleDateFormat
import java.util.*

class AdapterAddEducation : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var adapterEducationCallbacks: AdapterAddEducationCallbacks? = null
    private var items: MutableList<Education>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ACTION -> ViewHolderAction(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_next_add_profile_segments, parent, false)
            )
            else ->
                ViewHolderAddEducation(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_rv_add_education_fragment, parent, false)
                )
        }


    }

    fun setCallbacks(adapterEducationCallbacks: AdapterAddEducationCallbacks) {
        this.adapterEducationCallbacks = adapterEducationCallbacks
    }

    interface AdapterAddEducationCallbacks {
        fun submitClicked(items: MutableList<Education>)
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
            }
            else -> {
                val education = items?.get(position)
                val viewHolderAddEducation: ViewHolderAddEducation =
                    holder as ViewHolderAddEducation

                var calendar = Calendar.getInstance(TimeZone.getDefault())
                viewHolderAddEducation.itemView.start_date.setText(
                    if (education?.startYear != null) dateFormatter.format(
                        education?.startYear
                    ) else ""
                )
                viewHolderAddEducation.itemView.end_date.setText(
                    if (education?.endYear != null) dateFormatter.format(
                        education?.endYear
                    ) else ""
                )
                viewHolderAddEducation.itemView.et_school_name_add_ducation.setText(
                    if (education?.institution != null) dateFormatter.format(
                        education?.institution
                    ) else ""
                )
                viewHolderAddEducation.itemView.course_name.setText(
                    if (education?.institution != null) dateFormatter.format(
                        education?.institution
                    ) else ""
                )
                viewHolderAddEducation.itemView.course_name.setText(
                    if (education?.institution != null) dateFormatter.format(
                        education?.institution
                    ) else ""
                )


                viewHolderAddEducation.itemView.start_date.setOnClickListener {
                    val datePicker = DatePickerDialog(
                        holder.itemView.context,
                        DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                            Log.d("TEMP", "tmp date")
                            if (viewHolderAddEducation.adapterPosition == -1) return@OnDateSetListener
                            val date = "$i2/${i1 + 1}/$i"
                            items?.get(viewHolderAddEducation.adapterPosition)?.startYear =
                                dateFormatter.parse(date)
                            viewHolderAddEducation.itemView.start_date.setText(date)

                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePicker.datePicker.maxDate = System.currentTimeMillis();
                    datePicker.show()
                }

                viewHolderAddEducation.itemView.end_date.setOnClickListener {
                    val datePicker = DatePickerDialog(
                        holder.itemView.context,
                        DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                            Log.d("TEMP", "tmp date")
                            if (viewHolderAddEducation.adapterPosition == -1) return@OnDateSetListener
                            val date = "$i2/${i1 + 1}/$i"
                            items?.get(viewHolderAddEducation.adapterPosition)?.endYear =
                                dateFormatter.parse(date)
                            viewHolderAddEducation.itemView.end_date.setText(date)

                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePicker.datePicker.maxDate = System.currentTimeMillis();
                    datePicker.show()
                }

//                if (language?.validateFields == true) {
//                    validate(viewHolderAddEducation)
//                }
            }
        }

    }

    private fun validate(viewholder: ViewHolderAddEducation) {
//        viewholder.itemView.form_error.visibility =
//            if (viewholder.itemView.add_language_name.text.isEmpty()) View.VISIBLE else View.GONE
//        viewholder.itemView.name_line.setBackgroundColor(
//            if (viewholder.itemView.add_language_name.text.isEmpty()) viewholder.itemView.resources.getColor(
//                R.color.red
//            ) else Color.parseColor(
//                "#68979797"
//            )
//        )

    }


    override fun getItemCount(): Int {
        return if (items == null) 0 else items!!.size + 1
    }

    class ViewHolderAddEducation(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderAction(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val ADD_LANGUAGE = 0;
        const val ACTION = 1;


    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items?.size) ACTION else ADD_LANGUAGE
    }

    fun addData(items: MutableList<Education>) {
        this.items = items;
        notifyDataSetChanged()
    }

}