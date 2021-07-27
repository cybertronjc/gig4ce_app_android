package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.giger_profile_card_component_layout.view.*
import javax.inject.Inject

@AndroidEntryPoint
class GigerProfileCardComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs){
    val view : View
    @Inject
    lateinit var profileFirebaseRepository: ProfileFirebaseRepository
    private val profileImg: ImageView
    private val logoImg: ImageView
    private val gigerName: TextView
    private val gigerNumber: TextView
    private val jobProfileName: TextView
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view = LayoutInflater.from(context).inflate(R.layout.giger_profile_card_component_layout, this, true)
            profileImg = this.findViewById(R.id.gigerImg)
            logoImg = this.findViewById(R.id.jobProfileLogo)
            gigerName = this.findViewById(R.id.gigerName)
            gigerNumber = this.findViewById(R.id.gigerNumber)
            jobProfileName = this.findViewById(R.id.jobProfileTitle)

    }

    fun setProfilePicture(image: String?){
        image?.let {
            if (image.isEmpty()) {
                profileImg.setImageDrawable(resources.getDrawable(R.drawable.profile_avatar))
            }
            else {
                val profilePicRef: StorageReference =
                    FirebaseStorage.getInstance().reference.child("profile_pics").child(image)
                Log.d("profilePicRef", profilePicRef.toString())
                Glide.with(context)
                    .load(profilePicRef)
                    .placeholder(ShimmerHelper.getShimmerDrawable())
                    .into(profileImg)
            }
        }

    }

    fun setJobProfileLogo(image: String?){
        image?.let {
            if (image.isEmpty()) logoImg.invisible()
            else {
                logoImg.visible()
                Glide.with(context)
                    .load(image)
                    .placeholder(ShimmerHelper.getShimmerDrawable())
                    .into(logoImg)
            }
        }

    }

    fun setJobProfileTitle(jobProfile: String?){
        jobProfile?.let {
            if (it.isEmpty()) jobProfileTitle.invisible() else {
                jobProfileName.visible()
                jobProfileTitle.text = it
            }
        }

    }
    fun setGigerNumber(mobile: String?){
        mobile?.let {
            if (it.isEmpty()) gigerNumber.gone() else gigerNumber.text = it
        }

    }

    fun setGigerName(name: String?){
        name?.let {
            if (it.isEmpty()) gigerName.gone() else gigerName.text = it
        }
    }

    suspend fun setGigerProfileData(userUid: String){
        val profiledata = profileFirebaseRepository.getProfileData(userUid)
        setGigerName(profiledata.name)
        setProfilePicture(profiledata.profileAvatarThumbnail)
        setGigerNumber(profiledata.loginMobile)
    }

    fun setJobProfileData(title: String, companyLogo: String){
        setJobProfileTitle(title)
        setJobProfileLogo(companyLogo)
    }

    fun setProfileCard(gigerProfileCardDVM: GigerProfileCardDVM){
        gigerProfileCardDVM?.let {
            setGigerName(it.name)
            setGigerNumber(it.number)
            setProfilePicture(it.gigerImg)
            setJobProfileData(it.jobProfileName, it.jobProfileLogo)

        }
    }
}