package com.gigforce.app.modules.profile_

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile_.models.ContentCardData
import com.gigforce.app.utils.GlideApp
import com.gigforce.user_profile.components.AddContentCard
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.content_scrolling.*
import kotlinx.android.synthetic.main.fragment_profile_v2.*

class ProfileFragmentv2 : BaseFragment() {
    val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_profile_v2, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAppBar()
        initViews()
        initObservers()
        initClicks()
    }

    private fun initClicks() {
        iv_profile_v2.setOnClickListener {
            startActivity(Intent(activity, ProfilePicUploadActivity::class.java))
        }
        tv_upload_profile_pic_profile_v2.setOnClickListener {
            startActivity(Intent(activity, ProfilePicUploadActivity::class.java))
        }
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }


    }

    private fun initObservers() {
        viewModel.getProfileData().observe(viewLifecycleOwner, Observer { profileData ->
            tv_name_profile_v2.text = profileData?.name
            tv_about_profile_v2.text = profileData?.aboutMe
            loadImage(profileData.profileAvatarName)
        })
    }

    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"
    private fun loadImage(path: String) {
        val isPicturePresent = path != "avatar.jpg" && path != ""
        ll_no_pic_profile_v2.isVisible = !isPicturePresent
        iv_profile_v2.isVisible = isPicturePresent
        val profilePicRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(PROFILE_PICTURE_FOLDER).child(path)
        GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .into(iv_profile_v2)
    }

    private fun initAppBar() {
        val act = activity as AppCompatActivity
        act.setSupportActionBar(toolbar)
        act.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        act.supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon?.setColorFilter(
                resources.getColor(R.color.black),
                PorterDuff.Mode.SRC_ATOP
        );
        toolbar.setTitleTextColor(resources.getColor(R.color.black))
        toolbar_layout.setExpandedTitleColor(resources.getColor(R.color.black))
        toolbar_layout.setCollapsedTitleTextColor(resources.getColor(R.color.black))
        var isShow = true
        var scrollRange = -1
        app_bar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                toolbar_layout.title = "Profile"
                iv_share_profile_v2.gone()
                isShow = true
            } else if (isShow) {
                iv_share_profile_v2.visible()
                toolbar_layout.title =
                        " " //careful there should a space between double quote otherwise it wont work
                isShow = false
            }


        })


    }


    private fun initViews() {
        updateContentCardUI(
                add_address_profile_v2, ContentCardData(
                topLabel = R.string.current_address,
                topIcon = R.drawable.ic_location_black,
                contentIllustration = R.drawable.ic_address_illustration,
                contentTitle = R.string.add_contact_address,
                actionText = R.string.add_now
        )
        )
        updateContentCardUI(
                add_education_profile_v2, ContentCardData(topLabel = R.string.education,
                topIcon = R.drawable.ic_education_top_profile_v2,
                contentIllustration = R.drawable.ic_education_profile_v2,
                contentTitle = R.string.education,
                actionText = R.string.update_now,
                contentText = R.string.let_people_education,
                clickHandler = {
                    this@ProfileFragmentv2.navigate(R.id.fragment_add_education_v2)
                })
        )
        updateContentCardUI(
                add_experience_profile_fragment_v2, ContentCardData(
                topLabel = R.string.experience,
                topIcon = R.drawable.ic_experience_top_label_profile_v2,
                contentIllustration = R.drawable.ic_experience_illustration_profile_v2,
                contentTitle = R.string.exp_matters,
                actionText = R.string.update_now,
                contentText = R.string.exp_matters,
                clickHandler = {
                    this@ProfileFragmentv2.navigate(R.id.fragment_add_experience_profile_v2)
                })

        )
        updateContentCardUI(
                add_skills_profile_v2, ContentCardData(
                topLabel = R.string.skills_known,
                topIcon = R.drawable.ic_skills_top_icon_profile_v2,
                contentIllustration = R.drawable.ic_skills_profile_v2,
                contentTitle = R.string.skills,
                actionText = R.string.add_now,
                contentText = R.string.let_people_skills
        )
        )
        updateContentCardUI(
                add_documents_profile_v2, ContentCardData(
                topLabel = R.string.documents,
                topIcon = R.drawable.ic_documents_top_icon_profile_v2,
                contentIllustration = R.drawable.ic_documents_profile_v2,
                contentTitle = R.string.upload_your_documents,
                actionText = R.string.upload_now
        )
        )
        updateContentCardUI(
                add_language_profile_v2, ContentCardData(topLabel = R.string.lang_known,
                topIcon = R.drawable.ic_language_title,
                contentIllustration = R.drawable.ic_add_language_illustration,
                contentText = R.string.add_lang_known,
                contentTitle = R.string.language,
                actionText = R.string.add_now,
                clickHandler = {
                    this@ProfileFragmentv2.navigate(R.id.fragment_add_lang_profile_v2)
                })
        )

    }

    private fun updateContentCardUI(contentCard: AddContentCard, contentData: ContentCardData) {
        contentCard.topLabel = contentData.topLabel ?: R.string.empty_dash_string
        contentCard.topIcon = contentData.topIcon ?: R.drawable.ic_circle_empty
        contentCard.contentIllustration = contentData.contentIllustration
                ?: R.drawable.ic_circle_empty
        contentCard.contentHeading = contentData.contentTitle ?: R.string.empty_dash_string
        contentCard.rightActionText = contentData.actionText ?: R.string.empty_dash_string
        contentCard.contentText = contentData.contentText ?: R.string.empty_dash_string
        contentCard.setRightClickAction(contentData.clickHandler)

    }


}