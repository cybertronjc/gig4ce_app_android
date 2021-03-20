package com.gigforce.common_ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.jsibbold.zoomage.ZoomageView
import java.io.File

class ViewFullScreenImageDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "ViewFullScreenImageDialogFragment"

        private const val INTENT_EXTRA_IMAGE_URI = "image_uri"
        private const val INTENT_EXTRA_IMAGE_PATH = "image_path"

        @JvmStatic
        fun showImage(fragmentManager: FragmentManager, file: File) {
            val fragment = ViewFullScreenImageDialogFragment()

            val args = Bundle()
            args.putString(INTENT_EXTRA_IMAGE_PATH, file.absolutePath)
            fragment.arguments = args

            fragment.show(fragmentManager, TAG)
        }

        @JvmStatic
        fun showImage(fragmentManager: FragmentManager, uri: Uri) {
            val fragment = ViewFullScreenImageDialogFragment()

            val args = Bundle()
            args.putString(INTENT_EXTRA_IMAGE_URI, uri.toString())
            fragment.arguments = args

            fragment.show(fragmentManager, TAG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_image_fullscreen, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    private fun findViews(view: View) {
        val imageView: ZoomageView = view.findViewById(R.id.imageView)

        val uri = arguments?.getString(INTENT_EXTRA_IMAGE_URI)
        if (uri != null) {
            Glide.with(requireContext()).load(Uri.parse(uri)).into(imageView)
        }

        val path = arguments?.getString(INTENT_EXTRA_IMAGE_PATH)
        if(path != null) {
            val imageFile = File(path)
            Glide.with(requireContext()).load(imageFile).into(imageView)
        }
    }
}