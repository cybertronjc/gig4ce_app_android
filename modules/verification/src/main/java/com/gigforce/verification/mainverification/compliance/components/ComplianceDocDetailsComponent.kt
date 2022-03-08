package com.gigforce.verification.mainverification.compliance.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.gigforce.common_ui.chat.models.ChatMessage
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.databinding.ComplianceDocDetailsComponentBinding
import com.gigforce.common_ui.remote.verification.ComplianceDocDetailsDM
import com.toastfix.toastcompatwrapper.ToastHandler.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ComplianceDocDetailsComponent (context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder, View.OnClickListener {

    @Inject
    lateinit var navigation: INavigation

    private var viewBinding: ComplianceDocDetailsComponentBinding
    //Data
    private lateinit var currentData: ComplianceDocDetailsDM

    init {
        viewBinding = ComplianceDocDetailsComponentBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        viewBinding.copyIcon.setOnClickListener(this)
    }


    override fun bind(data: Any?) {
        if (data is ComplianceDocDetailsDM) {
            currentData = data
            data.name.let {
                if (it?.isNotBlank() == true){
                    viewBinding.docTitle.setText(data.name)
                }
            }
            data.value.let {
                if (it?.isNotBlank() == true){
                    viewBinding.docValue.setText(data.value)
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        copyMessageToClipBoard(currentData.value)
    }

    private fun copyMessageToClipBoard(text: String) {
        val clip: ClipData = ClipData.newPlainText("Copy", text)
        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(
            clip
        )
        showToast(context, "${currentData.name} Copied", Toast.LENGTH_SHORT)
    }

}