package com.gigforce.modules.feature_chat.screens

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.MediaDocsAndAudioFragmentBinding
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import java.lang.NullPointerException
import javax.inject.Inject

@AndroidEntryPoint
class MediaDocsAndAudioFragment : Fragment() {

    companion object {
        fun newInstance() = MediaDocsAndAudioFragment()
        const val TAG = "MediaDocsAndAudioFragment"
        const val INTENT_EXTRA_GROUP_ID = "group_id"
    }

    private val viewModel: MediaDocsAndAudioViewModel by viewModels()

    //private val viewModel: GroupChatViewModel by viewModels()
    private lateinit var groupId: String

    @Inject
    lateinit var chatFileManager : ChatFileManager

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }


    var selectedTab = 0
    private lateinit var viewBinding: MediaDocsAndAudioFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
        viewBinding = MediaDocsAndAudioFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        initViews()
        initListeners()
        initObserver()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GROUP_ID, groupId)
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
        }

        savedInstanceState?.let {
            groupId = it.getString(INTENT_EXTRA_GROUP_ID) ?: return@let
        }
    }

    private fun initObserver() {

        viewModel.groupInfo.observe(viewLifecycleOwner, Observer {

        })
    }

    private fun initListeners() {
        viewBinding.mediaTabLayout.onTabSelected {
            showToast(it?.text.toString())
            selectedTab = it?.position!!
        }
    }

    private fun initViews() = viewBinding.apply{

        mediaTabLayout.addTab(mediaTabLayout.newTab().setText("Media"))
        mediaTabLayout.addTab(mediaTabLayout.newTab().setText("Document"))
        mediaTabLayout.addTab(mediaTabLayout.newTab().setText("Audio"))

        val betweenSpace = 25

        val slidingTabStrip: ViewGroup = mediaTabLayout.getChildAt(0) as ViewGroup

        for (i in 0 until slidingTabStrip.childCount - 1) {
            val v: View = slidingTabStrip.getChildAt(i)
            val params: ViewGroup.MarginLayoutParams =
                v.layoutParams as ViewGroup.MarginLayoutParams
            params.rightMargin = betweenSpace
        }

        try {
            //showToast("position: ${selectedTab}")
            mediaTabLayout.getTabAt(selectedTab)?.select()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        viewModel.startWatchingGroupDetails(groupId)

    }

}