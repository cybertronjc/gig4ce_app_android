package com.gigforce.app.modules.client_activation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.app.modules.preferences.daytime.SlotsRecyclerAdapter
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.fragment_slots_driving_test.*


class TimeSlotsDialog : DialogFragment() {
    private lateinit var callbacks: TimeSlotDialogCallbacks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_slots_driving_test, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(getScreenWidth(requireActivity()).width - resources.getDimensionPixelSize(R.dimen.size_32), ViewGroup.LayoutParams.WRAP_CONTENT)
//            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
    }

    private fun initClicks() {
        tv_cancel_time_slots.setOnClickListener {
            this.dismiss()
        }
        tv_done_time_slots.setOnClickListener {
            if (groupradio.checkedRadioButtonId == -1) {
                Toast.makeText(requireContext(), getString(R.string.no_time_slot), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val radioButtonID: Int = groupradio.checkedRadioButtonId
            val radioButton: RadioButton = groupradio.findViewById(radioButtonID)
            callbacks.setSelectedTimeSlot(radioButton.hint.toString())
            this.dismiss()
        }
    }

    fun setCallbacks(callbacks: TimeSlotDialogCallbacks) {
        this.callbacks = callbacks
    }

    public interface TimeSlotDialogCallbacks {
        fun setSelectedTimeSlot(time: String)
    }

    companion object {

        fun newInstance(): TimeSlotsDialog {
            return TimeSlotsDialog()
        }
    }
}