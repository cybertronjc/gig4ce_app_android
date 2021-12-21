package com.gigforce.modules.feature_chat.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.FragmentMessageViewedInfoBinding
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel


class GroupMessageViewInfoFragment : Fragment() {

    private val viewModel: GroupChatViewModel by viewModels()
    private lateinit var viewBinding: FragmentMessageViewedInfoBinding

    private lateinit var groupId: String
    private lateinit var messageId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentMessageViewedInfoBinding.inflate(
            inflater,
            container,
            false
        )
        return viewBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        getDataFrom(arguments, savedInstanceState)
        initView()
        initViewModel()
        getMessageInfo()
    }

    private fun initView() {
        viewBinding.toolbar.apply {
            showTitle(context.getString(R.string.message_info_chat))
            hideActionMenu()
            setBackButtonListener {
                activity?.onBackPressed()
            }
        }

        viewBinding.messageViewedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
            messageId = it.getString(INTENT_EXTRA_MESSAGE_ID) ?: return@let
        }

        savedInstanceState?.let {
            groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
            messageId = it.getString(INTENT_EXTRA_MESSAGE_ID) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GROUP_ID, groupId)
        outState.putString(INTENT_EXTRA_MESSAGE_ID, messageId)
    }

    private fun initViewModel() {
        viewModel.messageReadingInfo
            .observe(viewLifecycleOwner, {

                viewBinding.toolbar.showSubtitle(getString(R.string.viewed_by_chat) + it.readingInfo.size + "/ " + it.totalMembers)
                viewBinding.messageViewedRecyclerView.collection = it.readingInfo
            })
    }

    private fun getMessageInfo() {


        viewModel.getMessageReadingInfo(
            groupId,
            messageId
        )
    }


    companion object {
        const val INTENT_EXTRA_GROUP_ID = "group_id"
        const val INTENT_EXTRA_MESSAGE_ID = "message_id"
    }


}