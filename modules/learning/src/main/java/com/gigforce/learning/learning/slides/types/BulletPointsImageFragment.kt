package com.gigforce.learning.learning.slides.types

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.learning.R

//import com.gigforce.app.R

class BulletPointsImageFragment : Fragment() {

    companion object {
        const val TAG = "SingleImageFragment"
        private const val KEY_IMAGE_URI = "image_uri"

        fun getInstance(image: Int): BulletPointsImageFragment {
            return BulletPointsImageFragment().apply {
                arguments = bundleOf(KEY_IMAGE_URI to image)
            }
        }
    }

    private var mImageUri: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_learning_slide_bullet_points, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getInt(KEY_IMAGE_URI)
        } else {
            mImageUri = arguments?.getInt(KEY_IMAGE_URI)
                ?: throw IllegalStateException("No uri in args")
        }

        setImageOnView()
    }

    private fun setImageOnView() {

    }
}