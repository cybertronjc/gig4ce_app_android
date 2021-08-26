package com.gigforce.app.modules.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.extensions.gone
import com.gigforce.common_ui.viewmodels.GigVerificationViewModel
import com.gigforce.giger_gigs.photocrop.PhotoCrop
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.utils.GlideApp
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.gigforce.core.navigation.INavigation
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import kotlinx.android.synthetic.main.profile_main_card_background.view.*
import kotlinx.android.synthetic.main.verified_button.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATION)


    }
    var allNavigationList = ArrayList<String>()
    var intentBundle : Bundle? = null
    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATION =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            ACTION_TO_PERFORM = it.getInt(StringConstants.ACTION.value, -1)

            it.getStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
        }

        arguments?.let {
            FROM_CLIENT_ACTIVATION =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            ACTION_TO_PERFORM = it.getInt(StringConstants.ACTION.value, -1)
            it.getStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
        }
    }


    companion object {
        fun newInstance() = ProfileFragment()
        val UPLOAD_PROFILE_PIC = 1
    }


    private var ACTION_TO_PERFORM: Int = -1;
    private lateinit var storage: FirebaseStorage
    private lateinit var layout: View
    private lateinit var profileAvatarName: String
    private lateinit var dWidth: Display
    private lateinit var win: Window
    private var PHOTO_CROP: Int = 45
    private var isShow: Boolean = true
    private var scrollRange: Int = -1
    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"
    private var FROM_CLIENT_ACTIVATION = false
    private var profilePicUploadInProgress = false

    private val gigerVerificationViewModel: GigVerificationViewModel by activityViewModels()
    val viewModel: ProfileViewModel by activityViewModels<ProfileViewModel>()

    @Inject
    lateinit var navigation : INavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getDataFromIntents(savedInstanceState)
        makeStatusBarTransparent()
    }

    private fun makeStatusBarTransparent() {
        win = requireActivity().window
        win.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        win.setStatusBarColor(requireActivity().getColor(R.color.white))
    }


    private fun restoreStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onResume() {
        super.onResume()
        makeStatusBarTransparent()
    }

    override fun onStart() {
        super.onStart()
        makeStatusBarTransparent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreStatusBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        restoreStatusBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        makeStatusBarTransparent()
        storage = FirebaseStorage.getInstance()
        Log.d("DEBUG", "ENTERED PROFILE VIEW")
        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        dWidth = wm.defaultDisplay
        layout = inflateView(R.layout.fragment_profile_main_expanded, inflater, container)!!
        return layout
    }



    private fun setAppBarOffset(offsetPx: Int) {
        val params = layout.appbar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior?


        try {
            behavior!!.onNestedPreScroll(
                layout.coordinator,
                layout.appbar,
                this.requireView(),
                0,
                offsetPx,
                intArrayOf(0, 0),
                0
            )
        } catch (e : Exception){
            e.printStackTrace()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout.appbar.post(Runnable {
            val heightPx: Int = dWidth.width * 1 / 3
            setAppBarOffset(heightPx)

            if (!viewModel.profileAppBarExpanded) {
                appbar.setExpanded(true)
                viewModel.profileAppBarExpanded = true
            }
        })

        layout.profile_avatar.layoutParams.height = dWidth.width
        layout.main_expanded_is_verified.setOnClickListener {
            navigate(R.id.gigerVerificationFragment)
        }



        gigerVerificationViewModel.gigerVerificationStatus.observe(viewLifecycleOwner, Observer {

            if (it.requiredDocsVerified) {
                layout.main_expanded_is_verified.verification_status_tv.text =
                    getString(R.string.verified_text)
                layout.main_expanded_is_verified.verification_status_tv.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.green,
                        null
                    )
                )
                layout.main_expanded_is_verified.status_iv.setImageResource(R.drawable.ic_check)
                layout.main_expanded_is_verified.verification_status_cardview.strokeColor =
                    ResourcesCompat.getColor(resources, R.color.green, null)
            } else if (it.requiredDocsUploaded) {
                layout.main_expanded_is_verified.verification_status_tv.text =
                    getString(R.string.under_verification)
                layout.main_expanded_is_verified.verification_status_tv.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.app_orange,
                        null
                    )
                )
                layout.main_expanded_is_verified.status_iv.setImageResource(R.drawable.ic_clock_orange)
                layout.main_expanded_is_verified.verification_status_cardview.strokeColor =
                    ResourcesCompat.getColor(resources, R.color.app_orange, null)
            } else {
                layout.main_expanded_is_verified.verification_status_tv.text = getString(R.string.not_verified)
                layout.main_expanded_is_verified.verification_status_tv.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.red,
                        null
                    )
                )
                layout.main_expanded_is_verified.status_iv.setImageResource(R.drawable.ic_cross_red)
                layout.main_expanded_is_verified.verification_status_cardview.strokeColor =
                    ResourcesCompat.getColor(resources, R.color.red, null)
            }
        })

        gigerVerificationViewModel.startListeningForGigerVerificationStatusChanges()


        location_card.setOnClickListener {
            navigation.navigateTo("preferences/locationFragment")
//            showToast(getString(R.string.work_in_progress_app))
        }
        // load user data
        viewModel.ambassadorProfilePicUpdate.observe(viewLifecycleOwner, Observer {
            loader_progress.gone()
            loadImage(it)
        })
        viewModel.errorObs.observe(viewLifecycleOwner, Observer {
            showToast(it)
        })
        viewModel.getProfileData().observe(viewLifecycleOwner, Observer { profileObs ->
            val profile: ProfileData = profileObs!!
            viewModel.profileID = profile?.id ?: ""
            layout.gigger_rating.text =
                if (profile.rating != null) profile.rating!!.getTotal().toString()
                else "-"
            rating_bar.rating = profile.rating!!.getTotal()
            layout.task_done.text = profile.tasksDone.toString()
            layout.connection_count.text = profile.connections.toString()
            layout.main_expanded_user_name.text = profile.name


            Log.d("ProfileFragment", profile.isVerified.toString())


            if (profile.bio.trim().isEmpty()) {
                layout.add_bio_default.visibility = View.VISIBLE
                layout.add_bio_default.setOnClickListener {
                    findNavController().navigate(R.id.editHeadlineBottomSheet)
                }
                layout.bio_card.visibility = View.GONE
            } else {
                layout.add_bio_default.visibility = View.GONE
                layout.bio_card.visibility = View.VISIBLE
                layout.edit_cover_bio.visibility = View.VISIBLE
                layout.bio.text = profile.bio
                layout.bio_card.setOnClickListener {
                    findNavController().navigate(R.id.editHeadlineBottomSheet)
                }
                layout.edit_cover_bio.setOnClickListener {
                    navigate(R.id.editHeadlineBottomSheet)
                }
            }

            layout.main_tags.removeAllViews()
            profile.tags?.let {
                if (it.size == 0) {
                    layout.tag_card.visibility = View.GONE
                    layout.add_tags_default.visibility = View.VISIBLE
                    layout.add_tags_default.setOnClickListener {
                        findNavController().navigate(R.id.editTagBottomSheet)
                    }
                } else {
                    layout.add_tags_default.visibility = View.GONE
                    layout.tag_card.visibility = View.VISIBLE

                    layout.tag_card.setOnClickListener {
                        findNavController().navigate(R.id.editTagBottomSheet)
                    }

                    layout.edit_cover.setOnClickListener {
                        navigate(R.id.editTagBottomSheet)
                    }

                    //layout.edit_cover_bio.visibility = View.INVISIBLE
                }
                for (tag in it) {
                    layout.main_tags.addView(addChip(this.requireContext(), tag))
                }
            }

            var mainAboutString = ""
            if (profile.aboutMe.isNotEmpty()) {
                mainAboutString += profile.aboutMe + "\n\n"
            }
            profile.languages?.let {
                val languages = it.sortedByDescending { language ->
                    language.speakingSkill
                }
                // TODO: Add a generic way for string formatting.
                for ((index, language) in languages.withIndex()) {
                    mainAboutString += if (index == 0)
                        getString(R.string.know_lang_app) + language.name + " (" +
                                getLanguageLevel(language.speakingSkill.toInt()) + ")\n"
                    else
                        "\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + language.name + " (" +
                                getLanguageLevel(language.speakingSkill.toInt()) + ")\n"
                }
            }

            layout.main_about_card.card_title.text = getString(R.string.about_me)
            layout.main_about_card.card_content.text = mainAboutString
            layout.main_about_card.card_icon.setImageResource(R.drawable.ic_user_profile_about_me)
            if (mainAboutString.trim().isEmpty())
                layout.main_about_card.card_view_more.text = getString(R.string.add_bio_profile)
            layout.main_about_card.card_view_more.setOnClickListener {
                findNavController().navigate(
                    R.id.aboutExpandedFragment, bundleOf(
                        Pair(StringConstants.PROFILE_ID.value, viewModel.profileID)
                    )
                )
            }
            layout.main_about_card.setOnClickListener {
                findNavController().navigate(
                    R.id.aboutExpandedFragment, bundleOf(
                        Pair(StringConstants.PROFILE_ID.value, viewModel.profileID)
                    )
                )
            }

            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            var mainEducationString = ""
            profile.educations?.let {
                val educations = it.sortedByDescending { education ->
                    education.startYear
                }
                if (educations.isNotEmpty()) {
                    mainEducationString += educations[0].institution + "\n"
                    mainEducationString += educations[0].degree + " - " + educations[0].course + "\n"
                    mainEducationString += format.format(educations[0].startYear!!) + " - " + format.format(
                        educations[0].endYear!!
                    ) + "\n\n"
                }
            }

            // TODO: Add a generic way for string formatting
            profile.skills?.let {
                val skills = it
                for ((index, value) in skills.withIndex()) {
                    if (index < 5) {
                        mainEducationString += if (index == 0)
                            "Skills: " + value.id + "\n"
                        else
                            "\t\t\t\t\t" + value.id + "\n"
                    }
                }
            }

            profile.achievements?.let {
                val achievements = it.sortedByDescending { achievement -> achievement.year }
                for ((index, value) in achievements.withIndex()) {
                    mainEducationString += if (index == 0) getString(R.string.achievements_colon) + " " + value.title + "\n"
                    else "\t\t\t\t\t\t\t\t\t\t\t\t" + value.title + "\n"
                }
            }

            Log.d("ProfileFragment", mainEducationString)
            layout.main_education_card.card_title.text = getString(R.string.education)
            layout.main_education_card.card_content.text = mainEducationString
            layout.main_education_card.card_icon.setImageResource(R.drawable.ic_education_new)
            if (mainEducationString.trim().isEmpty())
                layout.main_education_card.card_view_more.text = getString(R.string.add_education)
            layout.main_education_card.card_view_more.setOnClickListener {
                findNavController().navigate(R.id.educationExpandedFragment)
            }
            layout.main_education_card.setOnClickListener {
                findNavController().navigate(R.id.educationExpandedFragment)
            }


            var mainExperienceString = ""
            profile.experiences?.let {
                val experiences = it.sortedByDescending { experience -> experience.startDate }
                if (experiences.isNotEmpty()) {
                    mainExperienceString += (experiences[0]?.title ?: "") + "\n"
                    mainExperienceString += experiences[0]?.employmentType ?: "" + "\n"
                    mainExperienceString += experiences[0]?.location ?: "" + "\n"
                    experiences[0]?.startDate?.let {
                        mainExperienceString += format.format(it) + "-"
                    }
                    experiences[0]?.endDate?.let {
                        mainExperienceString += format.format(it) + "\n"
                    }?:let {
                        mainExperienceString += "current" + "\n"
                    }
                }
            }

            layout.main_experience_card.card_title.text = getString(R.string.experience)
            layout.main_experience_card.card_content.text = mainExperienceString
            layout.main_experience_card.card_icon.setImageResource(R.drawable.ic_experience)
            if (mainExperienceString.trim().isEmpty())
                layout.main_experience_card.card_view_more.text = getString(R.string.add_experience)
            layout.main_experience_card.card_view_more.setOnClickListener {
                findNavController().navigate(R.id.experienceExpandedFragment)
            }
            layout.main_experience_card.setOnClickListener {
                findNavController().navigate(R.id.experienceExpandedFragment)
            }

            layout.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
                if (scrollRange == -1) {
                    scrollRange = barLayout?.totalScrollRange!!
                }
                if (scrollRange + verticalOffset == 0) {
                    layout.collapse_toolbar.title = profile.name
                    layout.collapse_toolbar.isTitleEnabled = true
                    isShow = true
                } else if (isShow) {
//                    layout.collapse_toolbar.title = " " //careful there should a space between double quote otherwise it wont work
                    layout.collapse_toolbar.isTitleEnabled = false
                    isShow = false
                }
            })

            profileAvatarName = profile.profileAvatarName
            Log.e("PROFILE AVATAR", profileAvatarName)
            if (profileAvatarName != "")
                loadImage(profileAvatarName)
            layout.loader_progress.visibility = View.GONE
            if (ACTION_TO_PERFORM != -1) {
                when (ACTION_TO_PERFORM) {
                    UPLOAD_PROFILE_PIC -> {
                        profilePicUpload()
                    }
                }

            }


        })

        /*
        Clicking on profile picture opens Photo Crop Activity
         */
        layout.profile_avatar.setOnClickListener {

            val photoCropIntent = Intent(context, PhotoCrop::class.java)
            photoCropIntent.putExtra("purpose", "profilePictureCrop")
            photoCropIntent.putExtra("uid", viewModel.uid)
            photoCropIntent.putExtra("fbDir", "/profile_pics/")
            photoCropIntent.putExtra("detectFace", 1)
            photoCropIntent.putExtra("folder", PROFILE_PICTURE_FOLDER)
            photoCropIntent.putExtra("file", profileAvatarName)

            startActivityForResult(photoCropIntent, PHOTO_CROP)
        }

        layout.edit_cover.setOnClickListener {
            this.findNavController().navigate(R.id.editTagBottomSheet)
        }

        /**
         * back page navigation
         */
        layout.profile_main_expanded_back_button.setOnClickListener {
            activity?.onBackPressed()
        }
        listener()
    }

    fun profilePicUpload() {
        if (profilePicUploadInProgress) return
        val photoCropIntent = Intent(context, PhotoCrop::class.java)
        photoCropIntent.putExtra("purpose", "profilePictureCrop")
        photoCropIntent.putExtra("uid", viewModel.uid)
        photoCropIntent.putExtra("fbDir", "/profile_pics/")
        photoCropIntent.putExtra("detectFace", 1)
        photoCropIntent.putExtra("folder", PROFILE_PICTURE_FOLDER)
        photoCropIntent.putExtra("file", profileAvatarName)
        startActivityForResult(photoCropIntent, PHOTO_CROP)
        profilePicUploadInProgress = true
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATION) {
            navFragmentsData?.setData(bundleOf(StringConstants.BACK_PRESSED.value to true))
            popBackState()
            return true
        }

        return super.onBackPressed()
    }

    private fun listener() {
        collapseListener()
    }

    private fun collapseListener() {
//        appbar.addOnOffsetChangedListener(object:AppBarStateChangeListener() {
//            override fun onStateChanged(appBarLayout:AppBarLayout, state:State) {
//                if(state == State.EXPANDED){
//                    main_expanded_user_name.visibility = View.VISIBLE
//                }
//                else if(state == State.COLLAPSED){
//                    main_expanded_user_name.visibility = View.INVISIBLE
//
//                }
//            }
//        })
        appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {

                try {
                    if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                        main_expanded_user_name.animate().alpha(0.0f).setDuration(100)
                        main_expanded_user_name.visibility = View.INVISIBLE
                    } else {
                        main_expanded_user_name.animate().alpha(1.0f).setDuration(0)
                        main_expanded_user_name.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }


    private fun loadImage(Path: String) {


        if (Path != "avatar.jpg" && Path != "") {
            var profilePicRef: StorageReference =
                storage.reference.child(PROFILE_PICTURE_FOLDER).child(Path)
            if (layout.profile_avatar != null)
                GlideApp.with(this.requireContext())
                    .load(profilePicRef)
                    .into(layout.profile_avatar)
        } else {
            GlideApp.with(requireContext())
                .load(R.drawable.avatar)
                .into(layout.profile_avatar)
        }
    }

    private fun addChip(context: Context, name: String): Chip {
        var chip = Chip(context)
        chip.text = " #$name "
        chip.isClickable = false
        chip.setTextAppearanceResource(R.style.chipTextDefaultColor)
        chip.setChipStrokeColorResource(R.color.colorPrimary)
        chip.setChipStrokeWidthResource(R.dimen.border_width)
        chip.setChipBackgroundColorResource(R.color.fui_transparent)
        return chip
    }

    private fun getLanguageLevel(level: Int): String {
        return when (level) {
            in 0..25 -> "beginner"
            in 26..75 -> "moderate"
            else -> "advanced"
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Unit {

        super.onActivityResult(requestCode, resultCode, data)

        /*
        For photo crop. The activity returns the the filename with which the cropped photo
        is saved on firestore. The name is updated in profile information and the new
        photo is loaded in the view
        */
        if (requestCode == PHOTO_CROP && resultCode == Activity.RESULT_OK) {
            val imageName: String? = data?.getStringExtra("filename")
            val thumbNailName = data?.getStringExtra("thumbnail_name")
            Log.v("PROFILE_FRAG_OAR", "filename is:" + imageName)
            if (null != imageName && null != thumbNailName) {
                viewModel.updateInAmbassadorEnrollment(imageName, thumbNailName)
                loader_progress.visibility = View.VISIBLE
            }
            if (ACTION_TO_PERFORM != -1) {
                when (ACTION_TO_PERFORM) {
                    UPLOAD_PROFILE_PIC -> {
                        checkForNextDoc()
//                        popBackState()
                    }


                }
            }
        }
        if (requestCode == PHOTO_CROP && resultCode == Activity.RESULT_CANCELED) {
            if (ACTION_TO_PERFORM != -1) {
                when (ACTION_TO_PERFORM) {
                    UPLOAD_PROFILE_PIC -> onBackPressed()
                }
            }
        }
    }

    private fun checkForNextDoc() {
        if (allNavigationList.size == 0) {
            activity?.onBackPressed()
        } else {
            var navigationsForBundle = emptyList<String>()
            if (allNavigationList.size > 1) {
                navigationsForBundle =
                    allNavigationList.slice(IntRange(1, allNavigationList.size - 1))
                        .filter { it.length > 0 }
            }
            navigation.popBackStack()
            intentBundle?.putStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value,  ArrayList(navigationsForBundle))
            navigation.navigateTo(
                allNavigationList.get(0),intentBundle)
//            navigation.navigateTo(
//                allNavigationList.get(0),
//                bundleOf(VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle,if(FROM_CLIENT_ACTIVATION) StringConstants.FROM_CLIENT_ACTIVATON.value to true else StringConstants.FROM_CLIENT_ACTIVATON.value to false)
//            )

        }
    }

}

internal abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {
    private var mCurrentState = State.IDLE

    enum class State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        if (i == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED)
            }
            mCurrentState = State.EXPANDED
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED)
            }
            mCurrentState = State.COLLAPSED
        } else {
            if (mCurrentState != State.IDLE) {
                onStateChanged(appBarLayout, State.IDLE)
            }
            mCurrentState = State.IDLE
        }
    }


    abstract fun onStateChanged(appBarLayout: AppBarLayout, state: State)
}
//And then you can use it:
//appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
//    #Override
//    public void onStateChanged(AppBarLayout appBarLayout, State state) {
//        Log.d("STATE", state.name());
//    }
//});
