package com.gigforce.app.tl_work_space.change_client_id

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.app.android_common_utils.extensions.vibrate
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.TLWorkSpaceSharedViewModel
import com.gigforce.app.tl_work_space.databinding.BottomsheetChangeClientIdBinding
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

@AndroidEntryPoint
class ChangeClientIdSheetFragment : BaseBottomSheetDialogFragment<BottomsheetChangeClientIdBinding>(
    fragmentName = TAG,
    layoutId = R.layout.bottomsheet_change_client_id
) {

    companion object {
        const val TAG = "ChangeClientIdSheetFragment"
    }

    private val sharedViewModel: TLWorkSpaceSharedViewModel by activityViewModels()
    private val viewModel: ChangeClientIdViewModel by viewModels()

    //View Related Data
    private var existingClientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)

        arguments?.let {
            getDataFromIntents(it)
        }
    }


    private fun getDataFromIntents(
        it: Bundle
    ) {
        existingClientId = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_EXISTING_CLIENT_ID) ?: ""

        val gigerId = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_ID) ?: return
        val gigerMobile = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_MOBILE_NO) ?: return
        val gigerName = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_NAME) ?: return

        val jobProfileId = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID) ?: return
        val jobProfileName =
            it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_NAME) ?: return
        val businessId = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_BUSINESS_ID) ?: return

        viewModel.setKeysReceivedFromPreviousScreen(
            existingClientId = existingClientId!!,
            gigerId = gigerId,
            gigerMobile = gigerMobile,
            gigerName = gigerName,
            jobProfileId = jobProfileId,
            jobProfileName = jobProfileName,
            businessId = businessId
        )
    }

    override fun viewCreated(
        viewBinding: BottomsheetChangeClientIdBinding,
        savedInstanceState: Bundle?
    ) {
        removeBackGroundTint()
        initView()
        initViewModel()

    }

    private fun initView() = viewBinding.apply {

        this.clientIdEt.requestFocus()

        bindProgressButton(changeButton)
        changeButton.attachTextChangeAnimator()

        changeButton.setOnClickListener {
            viewModel.handleEvent(
                ChangeClientIdFragmentViewEvents.ChangeClientIdClicked
            )
        }

        lifecycleScope.launchWhenCreated {

            viewBinding.clientIdEt.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect {
                    viewModel.handleEvent(
                        ChangeClientIdFragmentViewEvents.NewClientIdEntered(it)
                    )
                }

            delay(200)
            viewBinding.clientIdEt.setText(existingClientId)
        }
    }

    private fun initViewModel() {
        viewModel.setSharedViewModel(sharedViewModel)

        lifecycleScope.launchWhenCreated {

            viewModel
                .uiState
                .collect {
                    consumeUiState(it)
                }
        }

        lifecycleScope.launchWhenCreated {

            viewModel
                .effect
                .collect {
                    consumeUiEffect(it)
                }
        }
    }

    private fun consumeUiEffect(it: ChangeClientIdFragmentViewUiEffects) {
        when (it) {
            is ChangeClientIdFragmentViewUiEffects.ClientIdValidationError -> {
                viewBinding.clientIdTextInputLayout.error = it.error
            }
            ChangeClientIdFragmentViewUiEffects.DisableSubmitButton -> disableSubmitButton()
            ChangeClientIdFragmentViewUiEffects.EnableSubmitButton -> enableSubmitButton()
        }
    }

    private fun consumeUiState(it: ChangeClientIdFragmentUiState) {
        when (it) {
            ChangeClientIdFragmentUiState.ChangingClientId -> showDroppingProgressView()
            ChangeClientIdFragmentUiState.ClientIdChanged -> {

                ToastHandler.showToast(requireContext(), "Client Id updated", Toast.LENGTH_SHORT)
                requireContext().vibrate()
                dismiss()
            }
            is ChangeClientIdFragmentUiState.ErrorWhileChangingClientId -> {

                hideDroppingProgressView()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Unable to change client id, ${it.error}")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()
            }
            ChangeClientIdFragmentUiState.ScreenInitializedOrRestored -> {}
        }
    }

    private fun hideDroppingProgressView() {
        viewBinding.changeButton.hideProgress("Change")
        enableSubmitButton()
    }

    private fun showDroppingProgressView() {
        viewBinding.changeButton.showProgress {
            buttonText = "Changing.."
            progressColor = Color.WHITE
        }
        disableSubmitButton()
    }

    private fun disableSubmitButton() {
        viewBinding.changeButton.isEnabled = false
    }

    private fun enableSubmitButton() {
        viewBinding.changeButton.isEnabled = true
    }
}