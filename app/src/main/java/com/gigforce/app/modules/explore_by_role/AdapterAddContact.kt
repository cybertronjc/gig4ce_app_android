package com.gigforce.app.modules.explore_by_role

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.explore_by_role.models.ContactModel
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.core.utils.isValidMail
import com.gigforce.core.utils.isValidMobile
import kotlinx.android.synthetic.main.layout_next_add_profile_segments.view.*
import kotlinx.android.synthetic.main.layout_rv_contact_details.view.*

class AdapterAddContact : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var adapterAddContactsCallbacks: AdapterAddContactsCallbacks? = null
    private var items: MutableList<ContactModel>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ACTION -> ViewHolderAction(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_next_add_profile_segments, parent, false)
            )
            else ->
                ViewHolderContact(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_rv_contact_details, parent, false)
                )
        }


    }

    fun setCallbacks(adapterAddContactsCallbacks: AdapterAddContactsCallbacks) {
        this.adapterAddContactsCallbacks = adapterAddContactsCallbacks
    }

    interface AdapterAddContactsCallbacks {
        fun submitClicked(items: MutableList<ContactModel>)
        fun goBack()

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ACTION -> {
                val viewholder: ViewHolderAction = holder as ViewHolderAction
                PushDownAnim.setPushDownAnimTo(viewholder.itemView.tv_action).setOnClickListener(
                    View.OnClickListener {

                        adapterAddContactsCallbacks?.submitClicked(items!!)
                    })
                PushDownAnim.setPushDownAnimTo(viewholder.itemView.tv_cancel).setOnClickListener(
                    View.OnClickListener {
                        adapterAddContactsCallbacks?.goBack()
                    })
            }
            else -> {
                val contact = items?.get(position)
                val viewHolderContact: ViewHolderContact = holder as ViewHolderContact
                viewHolderContact.itemView.et_add_contact_phone.setText(contact?.contactPhone?.phone)
                viewHolderContact.itemView.et_add_contact_phone.isEnabled = position != 0
                viewHolderContact.itemView.et_add_email.setText(contact?.contactEmail?.email)
                viewHolderContact.itemView.et_add_contact_phone.addTextChangedListener(object :
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
                        if (viewHolderContact.adapterPosition == -1) return
                        items?.get(viewHolderContact.adapterPosition)?.contactPhone?.phone = s.toString()
                    }
                })
                viewHolderContact.itemView.et_add_email.addTextChangedListener(object :
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
                        if (viewHolderContact.adapterPosition == -1) return
                        items?.get(viewHolderContact.adapterPosition)?.contactEmail?.email = s.toString()
                    }
                })
                viewHolderContact.itemView.bt_add_contact_add_more.visibility =
                    if (position == items?.size?.minus(1) ?: false || items?.size == 1) View.VISIBLE else View.GONE
                viewHolderContact.itemView.bt_add_contact_add_more.setOnClickListener {
                    if (viewHolderContact.adapterPosition == -1) return@setOnClickListener
                    items?.add(ContactModel())
                    notifyItemInserted(viewHolderContact.adapterPosition + 1)
                    notifyItemChanged(viewHolderContact.adapterPosition)
                }
                viewHolderContact.itemView.bt_remove_contact_add_more.visibility =
                    if (position != 0) View.VISIBLE else View.GONE
                viewHolderContact.itemView.bt_remove_contact_add_more.setOnClickListener {
                    if (viewHolderContact.adapterPosition == -1) return@setOnClickListener
                    items?.removeAt(viewHolderContact.adapterPosition)
                    notifyItemRemoved(viewHolderContact.adapterPosition)
                    items?.size?.minus(1)?.let { it1 -> notifyItemChanged(it1) }
                }
                viewHolderContact.itemView.et_line_phone_number.setBackgroundColor(
                    Color.parseColor(
                        "#68979797"
                    )
                )
                viewHolderContact.itemView.et_line_email.setBackgroundColor(
                    Color.parseColor(
                        "#68979797"
                    )
                )
                if (contact?.validateFields == true) {
                    validate(viewHolderContact)
                }

            }
        }

    }

    private fun validate(viewholder: ViewHolderContact) {
        var isValidMail = true
        if (viewholder.itemView.et_add_email.text.isNotEmpty()) {
            isValidMail =
                isValidMail(viewholder.itemView.et_add_email.text.toString())


            viewholder.itemView.et_line_email.setBackgroundColor(
                if (!isValidMail) viewholder.itemView.resources.getColor(R.color.red) else Color.parseColor(
                    "#68979797"
                )

            )


        } else {
            viewholder.itemView.et_line_email.setBackgroundColor(
                Color.parseColor("#68979797")
            )
        }
        val isCorrect =
            isValidMobile(viewholder.itemView.et_add_contact_phone.text.toString())

        viewholder.itemView.form_error_contact_details.visibility =
            if (!isCorrect || !isValidMail) View.VISIBLE else View.GONE
        viewholder.itemView.et_line_phone_number.setBackgroundColor(
            if (!isCorrect) viewholder.itemView.resources.getColor(R.color.red) else Color.parseColor(
                "#68979797"
            )

        )


    }


    override fun getItemCount(): Int {
        return if (items == null) 0 else items!!.size + 1
    }

    class ViewHolderContact(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ViewHolderAction(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val ADD_CONTACT = 0;
        const val ACTION = 1;


    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items?.size) ACTION else ADD_CONTACT
    }

    fun addData(items: MutableList<ContactModel>) {
        this.items = items;
        notifyDataSetChanged()
    }

}