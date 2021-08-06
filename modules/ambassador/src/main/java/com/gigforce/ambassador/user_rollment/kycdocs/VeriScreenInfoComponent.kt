package com.gigforce.ambassador.user_rollment.kycdocs

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.ambassador.R
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.AppConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.veri_screen_info_component_ambassador.view.*
import javax.inject.Inject

@AndroidEntryPoint
class VeriScreenInfoComponent(context: Context, attrs: AttributeSet?) :
        FrameLayout(context, attrs) {

    @Inject
    lateinit var navigation: INavigation

    var pageChangeListener: OnCustomPageSelectListener? = null
    var pageClickListener: OnClickListener? = null
    lateinit var adapter: ViewPagerAdapter

    fun setOnCustomPageSelectListener(listener: OnCustomPageSelectListener?) {
        this.pageChangeListener = listener
    }

    init {
        this.layoutParams =
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.veri_screen_info_component_ambassador, this, true)
        attrs?.let {
            val styledAttributeSet =
                    context.obtainStyledAttributes(
                            it,
                            R.styleable.VeriScreenInfoComponent,
                            0,
                            0
                    )
            val uppercaptionstr =
                    styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_uppercaption)
                            ?: ""
            val titlestr =
                    styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_title) ?: ""
            val docinfostr =
                    styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_docinfotext)
                            ?: ""
            val querytextstr =
                    styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_querytext)
                            ?: "Why we need this"
            val missingdoctext =
                    styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_missingdoctext)
                            ?: ""
            setUpperCaption(uppercaptionstr)
            setTitle(titlestr)
            setDocInfo(docinfostr)
            setQueryStr(querytextstr)
            setMissingDocText(missingdoctext)
        }

    }

    fun hideOnVerifiedDocuments() {
        iconwhyweneed.gone()
        whyweneedit.gone()
        docsubtitledetail.gone()
    }

    fun isDocDontOptChecked(): Boolean {
        return checkboxidonthave.isChecked
    }

    private fun setMissingDocText(missingdoctext: String) {
        checkboxidonthave.text = missingdoctext
    }

    private fun setQueryStr(querytextstr: String) {
        whyweneedit.text = querytextstr
    }


    private fun setDocInfo(docinfostr: String) {
        docsubtitledetail.text = docinfostr
    }


    private fun setTitle(titlestr: String) {
        title.text = titlestr
    }

    private fun setUpperCaption(uppercaptionstr: String) {
        uppercaption.text = uppercaptionstr
    }

    fun showUploadHere() {
        uploadHereText.visible()
    }

    fun disableImageClick() {
        adapter?.let {
            it.setImageClickable(false)
            it.notifyDataSetChanged()

        }
    }

    fun enableImageClick(){
        adapter.let {
            it.setImageClickable(true)
            it.notifyDataSetChanged()

        }
    }

    fun setImageViewPager(list: List<KYCImageModel>) {

        if (list.isEmpty()) {
            viewPager2.gone()
            tabLayout.gone()
        } else {
            viewPager2.visible()
            tabLayout.visible()
            adapter = ViewPagerAdapter {
                pageClickListener?.onClick(it)
            }
            adapter.setItem(list)
            viewPager2.adapter = adapter
            if (list.size == 1) {
                tabLayout.gone()
            }
            Log.d("adapter", "" + adapter.itemCount + " list: " + list.toString())
            TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            }.attach()
        }

    }

    fun setDocumentImage(position: Int, uri: Uri) {
        adapter.updateData(position, uri)
    }


    fun uploadStatusLayout(status: Int, title: String, subTitle: String) {
        statusDialogLayout.visible()
        when (status) {
            AppConstants.UPLOAD_SUCCESS -> {
                statusDialogLayout.background = resources.getDrawable(R.drawable.upload_successfull_layout_bg)
                uploadTitle.setTextColor(context.resources.getColor(R.color.upload_success))
                uploadIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_verified_24))
                uploadTitle.text = title
                uploadSubTitle.text = subTitle
            }
            AppConstants.UNABLE_TO_FETCH_DETAILS -> {
                statusDialogLayout.background = resources.getDrawable(R.drawable.fetch_details_error_layout_bg)
                uploadTitle.setTextColor(context.resources.getColor(R.color.upload_mismatch))
                uploadIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_privacy_tip_24))
                uploadTitle.text = title
                uploadSubTitle.text = subTitle
            }
            AppConstants.DETAILS_MISMATCH -> {
                statusDialogLayout.background = resources.getDrawable(R.drawable.details_mismatch_layout_bg)
                uploadTitle.setTextColor(context.resources.getColor(R.color.upload_error))
                uploadIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_dangerous_white_48dp))
                uploadTitle.text = title
                uploadSubTitle.text = subTitle
            }
            AppConstants.VERIFICATION_COMPLETED -> {
                statusDialogLayout.background = resources.getDrawable(R.drawable.upload_successfull_layout_bg)
                uploadTitle.setTextColor(context.resources.getColor(R.color.upload_success))
                uploadIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_verified_user_24))
                uploadTitle.text = title
                uploadSubTitle.text = subTitle
            }
        }
    }

    fun setVerificationSuccessfulView(titleStr: String, upperCaptionStr: String? = null) {
        checkboxidonthave.gone()
        titleStr.let {
            title.text = it
        }
        upperCaptionStr?.let { uppercaption.text = it } ?: run { uppercaption.text = "Congratulation" }
        uploadHereText.gone()
    }

    interface OnCustomPageSelectListener {
        fun onPageSelectListener(model: KYCImageModel)
    }

    fun setPrimaryClick(pageClickListener: OnClickListener) {
        this.pageClickListener = pageClickListener
    }



    fun resetAllViews(){
        docsubtitledetail.visible()
        iconwhyweneed.visible()
        whyweneedit.visible()
        enableImageClick()
        statusDialogLayout.gone()
    }
    fun viewChangeOnVerified(){
        docsubtitledetail.gone()
        iconwhyweneed.gone()
        whyweneedit.gone()
        disableImageClick()
    }
    fun viewChangeOnStarted(){
        viewChangeOnVerified()
        statusDialogLayout.gone()
    }

}


