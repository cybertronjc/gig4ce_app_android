package com.gigforce.learning.learning.slides.types

import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.learning.R
//import com.gigforce.app.R
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.core.utils.GlideApp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_learning_slide_dos_donts.*

class DoAndDontImageFragment : Fragment() {

    companion object {
        const val TAG = "DoAndDontImageFragment"
        private const val KEY_LESSON_ID = "lesson_id"
        private const val KEY_DO_IMAGE_URI = "do_image_uri"
        private const val KEY_DO_IMAGE_TEXT = "do_text"
        private const val KEY_DONT_IMAGE_URI = "dont_image_uri"
        private const val KEY_DONT_IMAGE_TEXT = "dont_text"


        fun getInstance(
            lessonId: String,
            doImageUri: Uri,
            doImageText: String,
            dontImageUri: Uri,
            dontImageText: String
        ): DoAndDontImageFragment {
            return DoAndDontImageFragment().apply {
                arguments = bundleOf(
                    KEY_LESSON_ID to lessonId,
                    KEY_DO_IMAGE_URI to doImageUri.toString(),
                    KEY_DO_IMAGE_TEXT to doImageText,
                    KEY_DONT_IMAGE_URI to dontImageUri.toString(),
                    KEY_DONT_IMAGE_TEXT to dontImageText
                )
            }
        }
    }

    private lateinit var mLessonId: String
    private lateinit var mDoText: String
    private lateinit var mDoImage: Uri
    private lateinit var mDontText: String
    private lateinit var mDontImage: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_learning_slide_dos_donts, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            mLessonId = it.getString(KEY_LESSON_ID) ?: return@let
            mDoImage = it.getString(KEY_DO_IMAGE_URI)?.toUri() ?: return@let
            mDoText = it.getString(KEY_DO_IMAGE_TEXT) ?: return@let
            mDontImage = it.getString(KEY_DONT_IMAGE_URI)?.toUri() ?: return@let
            mDontText = it.getString(KEY_DONT_IMAGE_TEXT) ?: return@let
        }

        savedInstanceState?.let {

            mLessonId = it.getString(KEY_LESSON_ID) ?: return@let
            mDoImage = it.getString(KEY_DO_IMAGE_URI)?.toUri() ?: return@let
            mDoText = it.getString(KEY_DO_IMAGE_TEXT) ?: return@let
            mDontImage = it.getString(KEY_DONT_IMAGE_URI)?.toUri() ?: return@let
            mDontText = it.getString(KEY_DONT_IMAGE_TEXT) ?: return@let
        }

        setInfoOnView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {

            putString(KEY_LESSON_ID, mLessonId)
            putString(KEY_DO_IMAGE_URI, mDoImage.toString())
            putString(KEY_DO_IMAGE_TEXT, mDoText)
            putString(KEY_DONT_IMAGE_URI, mDontImage.toString())
            putString(KEY_DONT_IMAGE_TEXT, mDontText)
        }
    }

    private fun setInfoOnView() {

        GlideApp.with(requireContext())
            .load(mDoImage)
            .placeholder(getCircularProgressDrawable())
            .into(do_image_iv)

        do_info_tv.setOnClickListener {
            showText(mDoText)
        }

        if (mDoText.length >= 160)
            do_info_tv.text = getDescriptionText(mDoText.substring(0, 160))
        else
            do_info_tv.text = mDoText

        GlideApp.with(requireContext())
            .load(mDontImage)
            .placeholder(getCircularProgressDrawable())
            .into(dont_image)

        dont_info_tv.setOnClickListener {
            showText(mDontText)
        }

        if (mDontText.length >= 160)
            dont_info_tv.text = getDescriptionText(mDontText.substring(0, 160))
        else
            dont_info_tv.text = mDontText
    }

    private fun getDescriptionText(text: String): SpannableString {
        if (text.isBlank())
            return SpannableString("")

        val string = SpannableString(text + SingleImageFragment.READ_MORE)

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.white, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), text.length + 3, string.length - 1, 0)
        string.setSpan(UnderlineSpan(), text.length + 2 , string.length, 0)

        return string
    }

    private fun showText(text: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_show_text, null)
        val textView = dialogView.findViewById<TextView>(R.id.textView)
        textView.text = text

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.okay_text) { _, _ -> }
            .show()
    }
}