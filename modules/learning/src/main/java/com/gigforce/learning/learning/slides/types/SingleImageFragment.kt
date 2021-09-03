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
import kotlinx.android.synthetic.main.fragment_learning_slide_single_image.*

class SingleImageFragment : Fragment() {

    companion object {
        const val TAG = "SingleImageFragment"

        private const val KEY_LESSON_ID = "lesson_id"
        private const val KEY_IMAGE_URI = "image_uri"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"

        const val READ_MORE = "  Read more"

        fun getInstance(
            lessonId: String,
            imageUri: Uri,
            title: String,
            description: String
        ): SingleImageFragment {
            return SingleImageFragment().apply {
                arguments = bundleOf(
                    KEY_LESSON_ID to lessonId,
                    KEY_IMAGE_URI to imageUri.toString(),
                    KEY_TITLE to title,
                    KEY_DESCRIPTION to description
                )
            }
        }
    }

    private lateinit var mLessonId: String
    private lateinit var mImageUri: Uri
    private lateinit var mTitle: String
    private lateinit var mDescription: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_learning_slide_single_image, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            mLessonId = it.getString(KEY_LESSON_ID) ?: return@let
            mImageUri = it.getString(KEY_IMAGE_URI)?.toUri() ?: return@let
            mTitle = it.getString(KEY_TITLE) ?: return@let
            mDescription = it.getString(KEY_DESCRIPTION) ?: return@let
        }

        savedInstanceState?.let {

            mLessonId = it.getString(KEY_LESSON_ID) ?: return@let
            mImageUri = it.getString(KEY_IMAGE_URI)?.toUri() ?: return@let
            mTitle = it.getString(KEY_TITLE) ?: return@let
            mDescription = it.getString(KEY_DESCRIPTION) ?: return@let
        }

        setInfoOnView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {

            putString(KEY_LESSON_ID, mLessonId)
            // putString(KEY_SLIDE_ID, mSlideId)
            putString(KEY_IMAGE_URI, mImageUri.toString())
            putString(KEY_TITLE, mTitle)
            putString(KEY_DESCRIPTION, mDescription)
        }
    }

    private fun setInfoOnView() {
        GlideApp.with(requireContext())
            .load(mImageUri)
            .placeholder(getCircularProgressDrawable())
            .into(imageView)

        slideTitleTV.text = mTitle
        slideDescriptionTV.setOnClickListener {
            showText(mDescription)
        }

        if (mDescription.length >= 180)
            slideDescriptionTV.text = getDescriptionText(mDescription.substring(0,180))
        else
            slideDescriptionTV.text = mDescription
    }

    private fun getDescriptionText(text: String): SpannableString {
        if (text.isBlank())
            return SpannableString("")

        val string = SpannableString(text + READ_MORE)

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.white, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), text.length + 3, string.length - 1, 0)
        string.setSpan(UnderlineSpan(), text.length + 2, string.length , 0)

        return string
    }

    private fun showText(text: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_show_text, null)
        val textView = dialogView.findViewById<TextView>(R.id.textView)
        textView.text = text

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.okay_text_learning) { _, _ -> }
            .show()
    }
}