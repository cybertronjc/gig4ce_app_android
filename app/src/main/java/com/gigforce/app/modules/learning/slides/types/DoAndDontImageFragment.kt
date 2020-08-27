package com.gigforce.app.modules.learning.slides.types

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_learning_slide_dos_donts.*

class DoAndDontImageFragment : BaseFragment() {

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
    ): View? = inflateView(R.layout.fragment_learning_slide_dos_donts, inflater, container)

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
        do_info_tv.text = mDoText

        GlideApp.with(requireContext())
            .load(mDontImage)
            .placeholder(getCircularProgressDrawable())
            .into(dont_image)
        dont_info_tv.text = mDontText
    }
}