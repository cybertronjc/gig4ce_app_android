package com.gigforce.app.modules.earn

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.app.utils.PushDownAnim
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_dialog_no_gigs.*

class NoGigsDialog : DialogFragment() {
    private var callbacks: NoGigsDialogCallbacks? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_dialog_no_gigs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PushDownAnim.setPushDownAnimTo(tv_explore_no_gigs_gig_hist)
            .setOnClickListener(View.OnClickListener {
                dismiss()
                callbacks?.navigateToExploreByRole()
            })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(
                (getScreenWidth(requireActivity()).width - resources.getDimension(R.dimen.size_32)).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        dialog?.window
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }

    fun setCallbacks(noGigsDialogCallbacks: NoGigsDialogCallbacks) {
        this.callbacks = noGigsDialogCallbacks
    }

    interface NoGigsDialogCallbacks {
        fun navigateToExploreByRole()
    }

    companion object {
        fun newInstance(): NoGigsDialog {
            return NoGigsDialog()
        }

    }
}