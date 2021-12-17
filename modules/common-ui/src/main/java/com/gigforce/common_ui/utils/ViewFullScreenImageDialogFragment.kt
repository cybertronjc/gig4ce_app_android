package com.gigforce.common_ui.utils

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.storage.MediaStoreApiHelpers
import com.jsibbold.zoomage.ZoomageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream



class ViewFullScreenImageDialogFragment : DialogFragment(), PopupMenu.OnMenuItemClickListener {

    companion object {
        private const val TAG = "ViewFullScreenImageDialogFragment"

        const val INTENT_EXTRA_IMAGE_URI = "image_uri"
        const val INTENT_EXTRA_IMAGE_PATH = "image_path"

        @JvmStatic
        fun showImage(fragmentManager: FragmentManager, file: File) {
            val fragment =
                ViewFullScreenImageDialogFragment()

            val args = Bundle()
            args.putString(INTENT_EXTRA_IMAGE_PATH, file.absolutePath)
            fragment.arguments = args

            fragment.show(fragmentManager,
                TAG
            )
        }

        @JvmStatic
        fun showImage(fragmentManager: FragmentManager, uri: Uri) {
            val fragment =
                ViewFullScreenImageDialogFragment()

            val args = Bundle()
            args.putString(INTENT_EXTRA_IMAGE_URI, uri.toString())
            fragment.arguments = args

            fragment.show(fragmentManager,
                TAG
            )
        }
    }

    private lateinit var imageUri : Uri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_image_full_screen, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
    }

//    override fun getTheme(): Int = R.style.DialogTheme

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    private fun findViews(view: View) {
        val imageView: ZoomageView = view.findViewById(R.id.imageView)
        val optionsImageView : View = view.findViewById(R.id.options_iv)
        val backImageView : View = view.findViewById(R.id.back_iv)

        backImageView.setOnClickListener {
            dismiss()
        }

        optionsImageView.setOnClickListener {

            val popUpMenu = PopupMenu(context, it)
            popUpMenu.inflate(R.menu.menu_image_viewer)

            popUpMenu.setOnMenuItemClickListener(this)
            popUpMenu.show()
        }

        val uri = arguments?.getString(INTENT_EXTRA_IMAGE_URI)
        if (uri != null) {
            imageUri = Uri.parse(uri)
            Glide.with(requireContext()).load(imageUri).into(imageView)
        }

        val path = arguments?.getString(INTENT_EXTRA_IMAGE_PATH)
        if(path != null) {
            val imageFile = File(path)
            imageUri = imageFile.toUri()
            Glide.with(requireContext()).load(imageFile).into(imageView)
        }
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {

        GlobalScope.launch {

            try {
                MediaStoreApiHelpers.saveImageToGallery(requireContext(),imageUri)

                launch(Dispatchers.Main) {
                    showToast("Image saved in gallery")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    showToast("Unable to save image in gallery")
                }
            }
        }

        return true
    }
}