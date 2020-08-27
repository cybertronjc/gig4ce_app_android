package com.gigforce.app.modules.learning.slides.types

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_learning_slide_single_image.*

class SingleImageFragment : BaseFragment() {

    companion object {
        const val TAG = "SingleImageFragment"

        private const val KEY_LESSON_ID = "lesson_id"
        private const val KEY_IMAGE_URI = "image_uri"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"

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
    ): View? = inflateView(R.layout.fragment_learning_slide_single_image, inflater, container)

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

    private fun setInfoOnView() {
        GlideApp.with(requireContext())
            .load(mImageUri)
            .placeholder(getCircularProgressDrawable())
            .into(imageView)

        slideTitleTV.text = mTitle
        slideDescriptionTV.text = mDescription
    }
}