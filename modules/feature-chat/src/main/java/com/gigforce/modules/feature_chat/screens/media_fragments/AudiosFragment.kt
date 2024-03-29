package com.gigforce.modules.feature_chat.screens.media_fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.FragmentAudiosBinding
import com.gigforce.modules.feature_chat.databinding.FragmentImageVideoBinding
import com.gigforce.modules.feature_chat.models.ChatAudioViewModels
import com.gigforce.modules.feature_chat.screens.MediaDocsAndAudioViewModel
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AudiosFragment : Fragment() {

    companion object {
        fun newInstance() = AudiosFragment()
        const val TAG = "AudiosFragment"
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
    private lateinit var viewBinding: FragmentAudiosBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
        viewBinding = FragmentAudiosBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        initViews()
        initListeners()
        initObserver()
    }

    private fun initObserver() {
        viewModel.audioInfo.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "audio data: $it")
            showAudios(it)
        })
    }

    private fun showAudios(it: List<ChatAudioViewModels>?) {
        if (it != null) {
            viewBinding.audioRv.collection = it
        }
    }

    private fun initListeners() = viewBinding.apply{

    }

    private fun initViews() = viewBinding.apply{
        groupId.let {
            viewModel.startWatchingGroupDetails(it)
        }
        viewBinding.audioRv.layoutManager = LinearLayoutManager(requireContext())
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


}