package com.gigforce.modules.feature_chat.screens

import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.documentFileHelper.DocumentTreeDelegate
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.analytics.CommunityEvents
import com.gigforce.modules.feature_chat.databinding.ChatSettingsFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatSettingsFragment : Fragment() {

    companion object {
        fun newInstance() = ChatSettingsFragment()
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private  val viewModel: ChatSettingsViewModel by viewModels()

    private lateinit var viewBinding: ChatSettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
        viewBinding = ChatSettingsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObserver()
        eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_SETTINGS_SCREEN,null))
    }

    private fun initObserver() {
        viewModel.autoDownload.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            viewBinding.autoDownloadSwitch.isChecked = it
        })
    }


    private fun initListeners() = viewBinding.apply{

        appBarComp.setBackButtonListener(View.OnClickListener {
            activity?.onBackPressed()
        })
        appBarComp.changeBackButtonDrawable()

        autoDownloadSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                viewModel.updateMediaAutoDownloadInDB(enable = true)
                eventTracker.setProfileProperty(ProfilePropArgs("Media Auto Download", true))
            } else{
                viewModel.updateMediaAutoDownloadInDB(enable = false)
                eventTracker.setProfileProperty(ProfilePropArgs("Media Auto Download", false))
            }
        }

    }

    private fun initViews() {
        viewModel.getMediaAutoDownload()
    }
}