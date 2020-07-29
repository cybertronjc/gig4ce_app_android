package com.gigforce.app.modules.gigPage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.invisible
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.ViewFullScreenImageDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_rate_gig_dialog.*
import kotlinx.android.synthetic.main.fragment_rate_gig_dialog_main.*


class RateGigDialogFragment : DialogFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val TAG = "RateGigDialogFragment"
        const val REQUEST_CODE_CAPTURE_FEEDBACK_IMAGES = 23

        fun launch(gigId: String, fragmentManager: FragmentManager) {
            val frag = RateGigDialogFragment()
            frag.arguments = bundleOf(INTENT_EXTRA_GIG_ID to gigId)
            frag.show(fragmentManager, TAG)
        }
    }

    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String

    private var attachmentsList: MutableList<Uri> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rate_gig_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        arguments?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }
        initView()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.gigDetails.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lce.Loading -> {
                    ratingMainLayout.invisible()
                    progressBar.visible()
                }
                is Lce.Content -> {
                    progressBar.gone()
                    ratingMainLayout.visible()
                    showDataOnview(it)
                }
                is Lce.Error -> {

                }
            }
        })

        viewModel.submitGigRatingState.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lse.Loading -> {
                    ratingMainLayout.invisible()
                    progressBar.visible()
                }
                Lse.Success -> {
                    Toast.makeText(requireContext(), "Feedback Submitted", Toast.LENGTH_SHORT)
                        .show()
                    dismiss()
                }
                is Lse.Error -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Unable to Submit Gig Feedback, ${it.error}")
                        .setPositiveButton("Okay") { _, _ -> }
                        .show()
                }
            }
        })

        viewModel.getGig(gigId)
    }

    private fun showDataOnview(it: Lce.Content<Gig>) {
        val rating = it.content.gigRating
        val feedback = it.content.gigUserFeedback
        ratingBar.rating = rating
        reviewET.setText(feedback)

        attachmentsList = it.content.gigUserFeedbackAttachments.map {
            Uri.parse(it)
        }.toMutableList()

        attachmentsList.forEach {
            setClickedImage(it)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {

            setBackgroundDrawableResource(R.drawable.dialog_round_bg)

            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GIG_ID, gigId)
    }

    private fun initView() {

        addAttachmentTV.setOnClickListener {

            val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
            photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_PURPOSE, PhotoCrop.PURPOSE_VERIFICATION)
            photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/verification/")
            photoCropIntent.putExtra("folder", "verification")
            photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
            photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME, "image.jpg")
            startActivityForResult(photoCropIntent, REQUEST_CODE_CAPTURE_FEEDBACK_IMAGES)
        }

        submitBtn.setOnClickListener {

            val rating = ratingBar.rating
            val feedback = reviewET.text.toString()

            if (rating == 0.0f) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Please provide a rating")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()

                return@setOnClickListener
            }

            viewModel.submitGigFeedback(gigId, rating, feedback, attachmentsList)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CAPTURE_FEEDBACK_IMAGES) {

            if (resultCode == Activity.RESULT_OK) {
                val file: Uri =
                    data?.getParcelableExtra(PhotoCrop.INTENT_EXTRA_RESULTING_FILE_URI) ?: return

                attachmentsList.add(file)
                setClickedImage(file)

            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Unable to Capture Image")
                    .setPositiveButton("OK") { _, _ -> }
                    .show()
            }
        }
    }


    private fun setClickedImage(imageFile: Uri) {

        layoutInflater.inflate(R.layout.dialog_rating_image_layout, attachments, true)
        val inflatedImageView = attachments.getChildAt(attachments.childCount - 1)

        inflatedImageView.setOnClickListener(onClickImageListener)
        inflatedImageView.findViewById<View>(R.id.ic_delete_btn)
            .setOnClickListener(onDeleteImageClickImageListener)

        inflatedImageView.tag = imageFile.toString()

        val imageNameTV = inflatedImageView.findViewById<TextView>(R.id.imageNameTV)
        imageNameTV.text = if (imageFile.lastPathSegment!!.contains("/")) {
            imageFile.lastPathSegment!!.substringAfterLast("/")
        } else
            imageFile.lastPathSegment!!
    }

    private fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    private val onClickImageListener = View.OnClickListener { imageView ->
        //TAG INFO - Parent Container Layout have Fixed
        val uriString = imageView.tag.toString()
        val uri = Uri.parse(uriString)
        ViewFullScreenImageDialogFragment.showImage(childFragmentManager, uri)
    }


    private val onDeleteImageClickImageListener = View.OnClickListener { imageView ->
        //TAG INFO - Parent Container Layout have Fixed

        val imageToDeleteIndex =
            (imageView.parent.parent as LinearLayout).indexOfChild(imageView.parent as View)

        showDeleteImageConfirmationDialog(imageToDeleteIndex)
    }

    private fun showDeleteImageConfirmationDialog(
        index: Int
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage("Delete Image?")
            .setPositiveButton("Yes") { _, _ -> deleteImage(index) }
            .setNegativeButton("No") { _, _ -> }
            .show()
    }

    private fun deleteImage(imageToDeleteIndex: Int) {
        attachmentsList.removeAt(imageToDeleteIndex)
        attachments.removeViewAt(imageToDeleteIndex)
    }
}