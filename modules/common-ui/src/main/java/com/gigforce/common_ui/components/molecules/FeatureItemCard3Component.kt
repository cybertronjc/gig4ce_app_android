package com.gigforce.common_ui.components.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard3DVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.StringConstants
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.feature_item_card2.view.*
import javax.inject.Inject

@AndroidEntryPoint
class FeatureItemCard3Component(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.feature_item_card3, this, true)
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var buildConfig: IBuildConfig

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    fun setImageFromUrl(url: String) {
        GlideApp.with(context)
            .load(url)
            .into(feature_icon)
    }

    fun setImageFromUrl(storageReference: StorageReference) {
        GlideApp.with(context)
            .load(storageReference)
            .into(feature_icon)
    }

    fun setImage(data: FeatureItemCard3DVM) {
        data.imageUrl?.let {
            setImageFromUrl(it)
            return
        }

        data.imageRes?.let {
            feature_icon.setImageResource(data.imageRes)
        }

        data.icon?.let {
            val firebaseStoragePath = "${buildConfig.getFeaturesIconLocationUrl()}${data.icon}.png"
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseStoragePath)
            setImageFromUrl(gsReference)
        }
    }

    override fun bind(data: Any?) {

        this.setOnClickListener(null)

        if (data is FeatureItemCard3DVM) {
            if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                feature_title.text = data.hi?.title ?: data.title
            } else
                feature_title.text = data.title

            setImage(data)

            this.setOnClickListener {
                data.subicons?.let {
                    if (it.isNotEmpty()) {
                        var subiconFolderBS = 
                        navigation.navigateTo(
                            "subiconfolderBottomSheet",
                            bundleOf(
                                StringConstants.ICON.value to Gson().toJson(data)
                            )
                        )
                    } else {
                        navigate(data)
                    }

                } ?: run {
                    navigate(data)
                }
            }
        }
    }

    private fun navigate(data: FeatureItemCard3DVM) {
        data.getNavArgs()?.let {
            it.args?.let { it.putString("title", feature_title.text.toString()) }
            navigation.navigateTo(it.navPath, it.args)
        }
    }

}