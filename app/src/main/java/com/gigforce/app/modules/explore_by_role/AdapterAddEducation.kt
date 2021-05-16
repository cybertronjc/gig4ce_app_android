package com.gigforce.app.modules.explore_by_role

import android.app.DatePickerDialog
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.datamodels.profile.Education
import com.gigforce.app.utils.DropdownAdapter
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.utils.getCircularProgressDrawable
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
        fun uploadEducationDocument(position: Int)
        fun goBack()

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
                viewHolderAddEducation.itemView.et_school_name_add_ducation.addTextChangedListener(
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
                            items?.get(viewHolderAddEducation.adapterPosition)?.institution =
                                s.toString()
                        }
                    })
                viewHolderAddEducation.itemView.et_field_add_education.addTextChangedListener(object :
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
                        items?.get(viewHolderAddEducation.adapterPosition)?.field = s.toString()
                    }
                })
                viewHolderAddEducation.itemView.et_field_activities.addTextChangedListener(object :
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
                        items?.get(viewHolderAddEducation.adapterPosition)?.activities =
                            s.toString()
                    }
                })

                viewHolderAddEducation.itemView.et_school_name_add_ducation.setText(
                    if (education?.institution != null)
                        education?.institution
                    else ""
                )
                viewHolderAddEducation.itemView.et_field_add_education.setText(
                    if (education?.field != null)
                        education?.field
                    else ""
                )
                viewHolderAddEducation.itemView.et_field_activities.setText(
                    if (education?.activities != null)
                        education?.activities
                    else ""
                )
                holder.itemView.degree_name.onFocusChangeListener =
                    OnFocusChangeListener { v, hasFocus -> if (hasFocus) holder.itemView.degree_name.showDropDown() }

                holder.itemView.degree_name.setOnTouchListener(OnTouchListener { v, event ->
                    holder.itemView.degree_name.showDropDown()
                    false
                })

                val degreeList = getDegreeList()

                val degreeAdapter = DropdownAdapter(holder.itemView.context, degreeList)
                holder.itemView.degree_name.setAdapter(degreeAdapter)
                holder.itemView.degree_name.onItemClickListener =
                    OnItemClickListener { parent, arg1, pos, id ->
                        if (viewHolderAddEducation.adapterPosition == -1) return@OnItemClickListener
                        items?.get(viewHolderAddEducation.adapterPosition)?.degree =
                            degreeList.get(pos)
                    }
                if (!education?.degree.isNullOrEmpty()) {
//                    val index = degreeList.indexOf(education?.degree)
                    holder.itemView.degree_name.setText(education?.degree)

                } else {
                    holder.itemView.degree_name.setText("")
                }


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

                viewHolderAddEducation.itemView.bt_add_education_add_more.visibility =
                    if (position == items?.size?.minus(1) ?: false || items?.size == 1) View.VISIBLE else View.GONE
                viewHolderAddEducation.itemView.bt_add_education_add_more.setOnClickListener {
                    if (viewHolderAddEducation.adapterPosition == -1) return@setOnClickListener
                    items?.add(Education())
                    notifyItemInserted(viewHolderAddEducation.adapterPosition + 1)
                    notifyItemChanged(viewHolderAddEducation.adapterPosition)
                }
                viewHolderAddEducation.itemView.bt_remove_education_add_more.visibility =
                    if (position != 0) View.VISIBLE else View.GONE
                viewHolderAddEducation.itemView.bt_remove_education_add_more.setOnClickListener {
                    if (viewHolderAddEducation.adapterPosition == -1) return@setOnClickListener
                    items?.removeAt(viewHolderAddEducation.adapterPosition)
                    notifyItemRemoved(viewHolderAddEducation.adapterPosition)
                    items?.size?.minus(1)?.let { it1 -> notifyItemChanged(it1) }
                }
                viewHolderAddEducation.itemView.ll_upload_education_cert.setOnClickListener {
                    if (viewHolderAddEducation.adapterPosition == -1) return@setOnClickListener
                    adapterEducationCallbacks?.uploadEducationDocument(viewHolderAddEducation.adapterPosition)
                }
                if (education?.educationDocument != null) {
                    holder.itemView.uploadImageLayout_educcation.visible()
                    holder.itemView.upload_ed_rl.gone()
                } else {
                    holder.itemView.uploadImageLayout_educcation.gone()
                    holder.itemView.upload_ed_rl.visible()
                }
                Glide.with(holder.itemView.context)
                    .load(education?.educationDocument)
                    .placeholder(
                        getCircularProgressDrawable(
                            holder.itemView.context
                        )
                    )
                    .into(viewHolderAddEducation.itemView.clickedImageIV_education)

                if (education?.validateFields == true) {
                    validate(viewHolderAddEducation, education)
                }
            }
        }

    }

    private fun validate(viewholder: ViewHolderAddEducation, education: Education) {
//
        if (education.institution.isNullOrEmpty() || education.degree.isNullOrEmpty() || education.field.isNullOrEmpty() || education.startYear == null || education.endYear == null || education.activities == null) {
            viewholder.itemView.form_error_add_education.visible()
        } else {
            viewholder.itemView.form_error_add_education.gone()
        }
        viewholder.itemView.line_et_school.setBackgroundColor(
            if (viewholder.itemView.et_school_name_add_ducation.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )
        viewholder.itemView.line_et_field.setBackgroundColor(
            if (viewholder.itemView.et_field_add_education.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )
        viewholder.itemView.line_et_activities.setBackgroundColor(
            if (viewholder.itemView.et_field_activities.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )
        viewholder.itemView.line_et_degree.setBackgroundColor(
            if (viewholder.itemView.degree_name.text.isEmpty()) viewholder.itemView.resources.getColor(
                R.color.red
            ) else Color.parseColor(
                "#68979797"
            )
        )
        viewholder.itemView.start_date.error =
            if (viewholder.itemView.start_date.text.isEmpty()) viewholder.itemView.resources.getString(
                R.string.select_start_date_add_education
            ) else null
        viewholder.itemView.end_date.error =
            if (viewholder.itemView.start_date.text.isEmpty()) viewholder.itemView.resources.getString(
                R.string.select_end_date_add_education
            ) else null


    }

    fun getDegreeList(): List<String> {
        return listOf(
            "<10th",
            "10th",
            "12th",
            "Certificate",
            "Diploma",
            "Bachelor",
            "Masters",
            "PhD"
        )
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

    fun setImageAdapter(position: Int, url: String?) {
        items?.get(position)?.educationDocument = url;
        notifyItemChanged(position)
    }


}