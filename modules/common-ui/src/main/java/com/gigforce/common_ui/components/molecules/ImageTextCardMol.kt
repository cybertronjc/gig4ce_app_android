package com.gigforce.common_ui.components.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.common_ui.R
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ImageTextCardMol(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.icon_text_card_mol, this, true)
    }

    @Inject lateinit var navigation:INavigation

//    fun setImageFromUrl(url:String){
//        GlideApp.with(context)
//            .load(url)
//            .into(feature_icon)
//    }
//
//    fun setImageFromUrl(storageReference: StorageReference){
//        GlideApp.with(context)
//            .load(storageReference)
//            .into(feature_icon)
//    }
//
//    fun setImage(data:FeatureItemCard2DVM){
//        data.imageUrl?.let {
//            setImageFromUrl(it)
//            return
//        }
//
//        data.imageRes ?. let {
//            feature_icon.setImageResource(data.imageRes)
//        }
//
//        data.image_type ?. let{
//            val firebaseStoragePath = "gs://gigforce-dev.appspot.com/pub/app_icons/ic_${data.image_type}.png"
//            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseStoragePath)
//            setImageFromUrl(gsReference)
//        }
//    }

    override fun bind(data: Any?) {

//        this.setOnClickListener(null)
//
//        if(data is FeatureItemCard2DVM){
//
//            feature_title.text = data.title
//
//            data.getNavArgs() ?. let {
//                this.setOnClickListener{ view ->
//                    navigation.navigateTo(it.navPath, it.args)
//                }
//            }
//            setImage(data)
//        }

    }
}