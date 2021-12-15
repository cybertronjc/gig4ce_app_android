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
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.documentFileHelper.DocumentTreeDelegate
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.R
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
    lateinit var documentTreeDelegate : DocumentTreeDelegate

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

    }


    private val openDocumentTreeContract = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) {
        if (it == null) return@registerForActivityResult

        documentTreeDelegate.handleDocumentTreeSelectionResult(
            context = requireContext(),
            uri = it,
            onSuccess = {
               handleStorageTreeSelectedResult()
            },
            onFailure = {
                handleStorageTreeSelectionFailure(it)
            }
        )
    }

    private fun initListeners() = viewBinding.apply{

        appBarComp.setBackButtonListener(View.OnClickListener {
            activity?.onBackPressed()
        })
        appBarComp.changeBackButtonDrawable()

        autoDownloadSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                showToast("auto download turned on")
                checkForScopeAndStoragePermission()
            }
        }

    }

    private fun checkForScopeAndStoragePermission(){
        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            if (!documentTreeDelegate.storageTreeSelected()) {
                //launch folder selection
                openDocumentTreeContract.launch(null)
            } else {
                //update value in db
            }
        } else{

            if (!isStoragePermissionGranted()) {
                askForStoragePermission()
            } else {
                //update value in db
            }
        }
    }


    private fun initViews() {

    }

    private fun handleStorageTreeSelectionFailure(
        e : Exception
    ) {

        //make the switch off
        viewBinding.autoDownloadSwitch.isChecked = false

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select storage")
            .setMessage(e.message.toString())
            .setPositiveButton("Okay"){_,_ ->}
            .show()
    }

    private fun handleStorageTreeSelectedResult() {

        sharedPreAndCommonUtilInterface.saveDataBoolean("auto_download", true)
        if (!isStoragePermissionGranted()) {
            askForStoragePermission()
        }
    }

    private fun askForStoragePermission() {
        Log.v(ChatPageFragment.TAG, "Permission Required. Requesting Permission")

        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA
                ),
                23
            )
        } else {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA
                ),
                23
            )
        }
    }

    private fun isStoragePermissionGranted(): Boolean {

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

}