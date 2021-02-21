package com.gigforce.app.modules.learning.learningVideo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.common_ui.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_lesson_completed_.*

class LearningCompletionDialog : DialogFragment() {

    private lateinit var callbacks: LearningCompletedDialogCallbacks
    override fun onStart() {
        super.onStart()
        dialog?.window
                ?.setLayout(
                        (getScreenWidth(
                            requireActivity()
                        ).width - resources.getDimension(R.dimen.size_32)).toInt(),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        dialog?.window
                ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_lesson_completed_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_action_learning_complete_dialog.setOnClickListener {
            LearningCompletedDialogCallbacks@this.dialog?.dismiss()
            callbacks.actionClick()
        }
        tv_do_it_later_learning_complete_dialog.setOnClickListener {
            LearningCompletedDialogCallbacks@this.dialog?.dismiss()
            callbacks.dismissDialog()
        }
    }

    fun setCallbacks(callbacks: LearningCompletedDialogCallbacks) {
        this.callbacks = callbacks
    }

    public interface LearningCompletedDialogCallbacks {
        fun actionClick()
        fun dismissDialog()

    }
}