package com.gigforce.app.modules.profile

import android.app.Activity
import android.content.Context
import android.content.Intent

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.GlideApp
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import kotlinx.android.synthetic.main.profile_main_card_background.view.*
import java.text.SimpleDateFormat
import java.util.*


class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var storage: FirebaseStorage
    private lateinit var layout: View
    private lateinit var profileAvatarName: String
    private lateinit var dWidth: Display
    private lateinit var win:Window
    private var PHOTO_CROP: Int = 45
    private var isShow: Boolean = true
    private var scrollRange: Int = -1
    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeStatusBarTransparent()
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.homeScreenNew)
        }
    }

    private fun makeStatusBarTransparent(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    private fun restoreStatusBar(){
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

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()

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
        layout = inflater.inflate(R.layout.fragment_profile_main_expanded, container, false)
        layout.appbar.post(Runnable {
            val heightPx: Int = dWidth.width * 1 / 3
            setAppBarOffset(heightPx)
        })
        layout.profile_avatar.layoutParams.height = dWidth.width
        return layout
    }

    private fun setAppBarOffset(offsetPx: Int) {
        val params = layout.appbar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior?

        behavior!!.onNestedPreScroll(
            layout.coordinator,
            layout.appbar,
            this.requireView(),
            0,
            offsetPx,
            intArrayOf(0, 0),
            0
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: ProfileViewModel by activityViewModels<ProfileViewModel>()

        // load user data
        viewModel.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            layout.gigger_rating.text = if (profile.rating != null) profile.rating!!.getTotal().toString()
                                        else "-"
            layout.task_done.text = profile.tasksDone.toString()
            layout.connection_count.text = profile.connections.toString()
            layout.main_expanded_user_name.text = profile.name

            Log.d("ProfileFragment", profile.isVerified.toString())
            if (profile.isVerified) {
                //layout.main_expanded_is_verified.setBackgroundColor(Color.parseColor("#00FF00"))
            }

            if (profile.bio.trim().isEmpty()) {
                layout.add_bio_default.visibility = View.VISIBLE
                layout.add_bio_default.setOnClickListener {
                    findNavController().navigate(R.id.editCoverBottomSheet)
                }
                layout.bio_card.visibility = View.GONE
            } else {
                layout.add_bio_default.visibility = View.GONE
                layout.bio_card.visibility = View.VISIBLE
                layout.edit_cover_bio.visibility = View.VISIBLE
                layout.bio.text = profile.bio
                layout.bio_card.setOnClickListener {
                    findNavController().navigate(R.id.editCoverBottomSheet)
                }
            }

            layout.main_tags.removeAllViews()
            profile.tags?.let {
                if (it.size == 0) {
                    layout.tag_card.visibility = View.GONE
                    layout.add_tags_default.visibility = View.VISIBLE
                    layout.add_tags_default.setOnClickListener {
                        findNavController().navigate(R.id.editCoverBottomSheet)
                    }
                } else {
                    layout.add_tags_default.visibility = View.GONE
                    layout.tag_card.visibility = View.VISIBLE

                    layout.tag_card.setOnClickListener {
                        findNavController().navigate(R.id.editCoverBottomSheet)
                    }

                    layout.edit_cover_bio.visibility = View.INVISIBLE
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
                                            "Language known: " + language.name + " (" +
                                                    getLanguageLevel(language.speakingSkill.toInt()) + ")\n"
                                        else
                                            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + language.name + " (" +
                                                    getLanguageLevel(language.speakingSkill.toInt()) + ")\n"
                }
            }

            layout.main_about_card.card_title.text = "About me"
            layout.main_about_card.card_content.text = mainAboutString
            layout.main_about_card.card_icon.setImageResource(R.drawable.ic_about_me)
            if (mainAboutString.trim().isEmpty())
                layout.main_about_card.card_view_more.text = "Add bio"
            layout.main_about_card.card_view_more.setOnClickListener {
                findNavController().navigate(R.id.aboutExpandedFragment)
            }
            layout.main_about_card.setOnClickListener {
                findNavController().navigate(R.id.aboutExpandedFragment)
            }

            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            var mainEducationString = ""
            profile.educations?.let {
                val educations = it.sortedByDescending {
                        education -> education.startYear
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
                mainEducationString += "\n"
            }

            profile.achievements?.let {
                val achievements = it.sortedByDescending { achievement -> achievement.year }
                for ((index, value) in achievements.withIndex()) {
                    mainEducationString += if (index == 0) "Achievements: " + value.title + "\n"
                    else "\t\t\t\t\t\t\t\t\t\t\t\t" + value.title + "\n"
                }
            }

            Log.d("ProfileFragment", mainEducationString)
            layout.main_education_card.card_title.text = "Education"
            layout.main_education_card.card_content.text = mainEducationString
            layout.main_education_card.card_icon.setImageResource(R.drawable.ic_education)
            if (mainEducationString.trim().isEmpty())
                layout.main_education_card.card_view_more.text = "Add Education"
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
                    mainExperienceString += experiences[0].title + "\n"
                    mainExperienceString += experiences[0].employmentType + "\n"
                    mainExperienceString += experiences[0].location + "\n"
                    mainExperienceString += format.format(experiences[0].startDate!!) + "-"
                    mainExperienceString += if(experiences[0].endDate != null) format.format(experiences[0].endDate!!) + "\n"
                                            else "current" + "\n"
                }
            }

            layout.main_experience_card.card_title.text = "Experience"
            layout.main_experience_card.card_content.text = mainExperienceString
            layout.main_experience_card.card_icon.setImageResource(R.drawable.ic_experience)
            if (mainExperienceString.trim().isEmpty())
                layout.main_experience_card.card_view_more.text = "Add Experience"
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

        layout.edit_cover.setOnClickListener{
            this.findNavController().navigate(R.id.editCoverBottomSheet)
        }

        /**
         * back page navigation
         */
        layout.profile_main_expanded_back_button.setOnClickListener {
            activity?.onBackPressed()
        }
    }


    private fun loadImage(Path: String) {
        var profilePicRef: StorageReference =
            storage.reference.child(PROFILE_PICTURE_FOLDER).child(Path)
        GlideApp.with(this.requireContext())
            .load(profilePicRef)
            .into(layout.profile_avatar)
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
            var imageName: String? = data?.getStringExtra("filename")
            Log.v("PROFILE_FRAG_OAR", "filename is:" + imageName)
            if (null != imageName) {
                loadImage(imageName)
            }
        }
    }

}
