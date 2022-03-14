package com.gigforce.giger_gigs.attendance_tl.resolve_attendance_conflict

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.AttendanceTLSharedViewModel
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceConstants
import com.gigforce.giger_gigs.databinding.FragmentGigerAttendanceDetailsBinding
import com.gigforce.giger_gigs.databinding.FragmentMarkActiveConfirmationBinding
import com.gigforce.giger_gigs.databinding.FragmentResolveAttendanceConflictConfirmationBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResolveAttendanceConflictConfirmationBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentResolveAttendanceConflictConfirmationBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_resolve_attendance_conflict_confirmation
) {
    companion object {
        const val TAG = "MarkActiveConfirmationBottomSheetFragment"
    }

    private val viewModel: AttendanceTLSharedViewModel by activityViewModels()

    private lateinit var resolveId: String
    private var hasGigerMarkedHimselfActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            resolveId = it.getString(GigAttendanceConstants.INTENT_EXTRA_RESOLVE_ID) ?: return@let
        }

        savedInstanceState?.let {
            resolveId = it.getString(GigAttendanceConstants.INTENT_EXTRA_RESOLVE_ID) ?: return@let
        }

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(GigAttendanceConstants.INTENT_EXTRA_RESOLVE_ID, resolveId)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentResolveAttendanceConflictConfirmationBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            initView()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun initView() = viewBinding.apply {

        if (hasGigerMarkedHimselfActive) {
            this.confirmationTextLabel.text = buildSpannedString {
                append("Giger has marked ")
                color(ResourcesCompat.getColor(resources, R.color.lipstick_2, null)) {
                    append("Active")
                }
                append(" in his app. but you have marked Inactive.Do you want to change your response?")
            }
        } else {
            this.confirmationTextLabel.text = buildSpannedString {
                append("Giger has marked ")
                color(ResourcesCompat.getColor(resources, R.color.lipstick_2, null)) {
                    append("Inactive")
                }
                append(" in his app. but you have marked Active.Do you want to change your response?")
            }
        }

        this.yesButton.setOnClickListener {
            viewModel.tlSelectedYesInResolveDialog(resolveId)
            dismiss()
        }

        this.noButton.setOnClickListener {
            viewModel.tlSelectedNoInResolveDialog(resolveId)
            dismiss()
        }
    }
}