package com.gigforce.verification

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.verification_image_card_component.view.*
import kotlinx.android.synthetic.main.verification_image_card_component.view.documentUploadLabelTV
import kotlinx.android.synthetic.main.verification_image_card_component.view.documentUploadSubLabelTV
import kotlinx.android.synthetic.main.verification_image_card_component.view.imageLabelTV
import kotlinx.android.synthetic.main.verification_image_card_component.view.uploadDocumentCardView
import kotlinx.android.synthetic.main.verification_image_card_component.view.uploadImageLayout
import javax.inject.Inject

@AndroidEntryPoint
open class VerficationImageCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {

    @Inject
    lateinit var navigation : INavigation

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.verification_image_card_component, this, true)

    }


    fun setDocumentUploadLabel(label: String){
        documentUploadLabelTV.text = label
    }
    fun setDocumentUploadSubLabel(subLabel: String){
        documentUploadSubLabelTV.text = subLabel
    }

    fun uploadImageLabel(label: String){
        imageLabelTV.text = label
    }

    fun makeEditLayoutVisible(){
        uploadImageLayout.visibility = View.VISIBLE
        uploadDocumentCardView.visibility = View.GONE
    }

    fun makeUploadLayoutVisible(){
        uploadImageLayout.visibility = View.GONE
        uploadDocumentCardView.visibility = View.VISIBLE
    }

    fun setImage(uri: Uri){
        Glide.with(context)
            .load(uri)
            .placeholder(getCircularProgressDrawable(context))
            .into(clickedImage)
    }

    override fun bind(data: Any?) {

    }
}