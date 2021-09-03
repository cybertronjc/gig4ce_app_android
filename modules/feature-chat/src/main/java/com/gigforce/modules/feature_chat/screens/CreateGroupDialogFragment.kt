package com.gigforce.modules.feature_chat.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.gigforce.modules.feature_chat.screens.vm.factories.GroupChatViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


class CreateGroupDialogFragment : DialogFragment() {

    companion object {
        const val INTENT_EXTRA_CONTACTS = "contacts"
        const val TAG = "ReportUserDialogFragment"

        fun launch(
                contacts: ArrayList<ContactModel>,
                createGroupDialogFragmentListener: CreateGroupDialogFragmentListener,
                fragmentManager: FragmentManager
        ) {
            val frag = CreateGroupDialogFragment()
            frag.arguments = bundleOf(
                    INTENT_EXTRA_CONTACTS to contacts
            )
            frag.createGroupDialogFragmentListener = createGroupDialogFragmentListener
            frag.show(fragmentManager, TAG)
        }

    }


    private val chatGroupViewModel: GroupChatViewModel by lazy {
        ViewModelProvider(
                this,
                GroupChatViewModelFactory(requireContext())
        ).get(GroupChatViewModel::class.java)
    }


    private var contacts: ArrayList<ContactModel> = arrayListOf()
    private var createGroupDialogFragmentListener: CreateGroupDialogFragmentListener? = null

    //View
    private lateinit var createGroupMainLayout: View
    private lateinit var progressBar: View
    private lateinit var groupNameET: EditText
    private lateinit var submitBtn: Button

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            contacts = it.getParcelableArrayList(INTENT_EXTRA_CONTACTS)!!
        }

        arguments?.let {
            contacts = it.getParcelableArrayList(INTENT_EXTRA_CONTACTS)!!
        }
        initView(view)
        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(INTENT_EXTRA_CONTACTS, contacts)
    }


    private fun initViewModel() {
        chatGroupViewModel.createGroup
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lce.Loading -> {
                            createGroupMainLayout.invisible()
                            progressBar.visible()
                        }
                        is Lce.Content -> {
                            Toast.makeText(requireContext(), getString(R.string.group_created_chat), Toast.LENGTH_LONG).show()
                            createGroupDialogFragmentListener?.onGroupCreated(it.content)
                            dismiss()
                        }
                        is Lce.Error -> {
                            progressBar.gone()
                            createGroupMainLayout.visible()
                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(getString(R.string.alert_chat))
                                    .setMessage(getString(R.string.unable_to_create_group_chat) + it.error)
                                    .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                                    .show()
                        }
                    }
                })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {

            //setBackgroundDrawableResource(R.drawable.dialog_round_bg)

            setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }


    private fun initView(view: View) {

        createGroupMainLayout = view.findViewById(R.id.createGroupMain)
        progressBar = view.findViewById(R.id.progressBar)
        groupNameET = view.findViewById(R.id.group_name_et)

        submitBtn = view.findViewById(R.id.create_button)
        view.findViewById<View>(R.id.cancel_button).setOnClickListener {
            dismiss()
        }

        submitBtn.setOnClickListener {

            if (groupNameET.length() == 0) {
                Toast.makeText(requireContext(), getString(R.string.enter_a_group_name_chat), Toast.LENGTH_SHORT).show()
            } else {
                chatGroupViewModel.createGroup(
                        groupName = groupNameET.text.toString().capitalize(),
                        groupMembers = contacts
                )
            }
        }
    }


    interface CreateGroupDialogFragmentListener {

        fun onGroupCreated(groupId: String)
    }

}