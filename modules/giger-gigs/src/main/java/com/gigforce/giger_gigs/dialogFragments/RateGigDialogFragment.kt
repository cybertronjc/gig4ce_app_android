package com.gigforce.giger_gigs.dialogFragments

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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.utils.ViewFullScreenImageDialogFragment
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_gigs.R
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_rate_gig_dialog.*
import kotlinx.android.synthetic.main.fragment_rate_gig_dialog_main.*
import javax.inject.Inject

@AndroidEntryPoint
class RateGigDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val TAG = "RateGigDialogFragment"
        const val REQUEST_CODE_CAPTURE_FEEDBACK_IMAGES = 23

        fun launch(gigId: String, fragmentManager: FragmentManager) {
            val frag = RateGigDialogFragment()
            frag.arguments = bundleOf(INTENT_EXTRA_GIG_ID to gigId)
            frag.isCancelable = true
            frag.show(fragmentManager, TAG)
        }
    }
    @Inject lateinit var navigation : INavigation
    private val viewModel: GigViewModel by viewModels()
    private val sharedViewModel : SharedGigViewModel by activityViewModels()
    private lateinit var gigId: String

    private var attachmentsList: MutableList<Uri> = mutableListOf()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
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
        getGigDetails()
    }

    private fun initViewModel() {
        viewModel
                .gigDetails
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lce.Loading -> {
                            ratingMainLayout.invisible()
                            progressBar.visible()
                        }
                        is Lce.Content -> {
                            progressBar.gone()
                            ratingMainLayout.visible()
                            showDataOnView(it)
                        }
                        is Lce.Error -> {
                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(getString(R.string.alert_giger_gigs))
                                    .setMessage(getString(R.string.unable_to_fetch_details_giger_gigs) + it.error)
                                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                                    .show()
                        }
                    }
                })

        viewModel
                .submitGigRatingState
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lse.Loading -> {
                            ratingMainLayout.invisible()
                            progressBar.visible()
                        }
                        Lse.Success -> {
                            Toast.makeText(requireContext(), getString(R.string.feedback_submitted_giger_gigs), Toast.LENGTH_SHORT)
                                    .show()
                            dismiss()
                        }
                        is Lse.Error -> {
                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(getString(R.string.alert_giger_gigs))
                                    .setMessage(getString(R.string.unable_to_submit_gig_giger_gigs) + it.error)
                                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                                    .show()
                        }
                    }
                })
    }

    private fun getGigDetails() {
        viewModel.getGig(gigId)
    }

    override fun isCancelable(): Boolean {
        return false
    }

    private fun showDataOnView(it: Lce.Content<Gig>) {
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

        submitBtn.isEnabled = rating == 0.0f
    }

//    override fun onStart() {
//        super.onStart()
//        dialog?.window?.apply {
//
//            setBackgroundDrawableResource(R.drawable.dialog_round_bg)
//            setLayout(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//        }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GIG_ID, gigId)
    }

    private fun initView() {

        addAttachmentTV.setOnClickListener {
//            val photoCropIntent = Intent(requireContext(), PhotoCrop::class.java)
            val photoCropIntent = Intent()
            photoCropIntent.putExtra("purpose", "verification")
            photoCropIntent.putExtra("fbDir", "/verification/")
            photoCropIntent.putExtra("folder", "verification")
            photoCropIntent.putExtra("detectFace", 0)
            photoCropIntent.putExtra("file", "image.jpg")
            navigation.navigateToPhotoCrop(photoCropIntent,REQUEST_CODE_CAPTURE_FEEDBACK_IMAGES,requireContext(),this)
//            startActivityForResult(photoCropIntent, REQUEST_CODE_CAPTURE_FEEDBACK_IMAGES)
        }

        submitBtn.setOnClickListener {

            val rating = ratingBar.rating
            val feedback = reviewET.text.toString()

            if (rating == 0.0f) {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_giger_gigs))
                        .setMessage(getString(R.string.provide_rating_giger_gigs))
                        .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                        .show()

                return@setOnClickListener
            }

            viewModel.submitGigFeedback(sharedViewModel,gigId, rating, feedback, attachmentsList)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CAPTURE_FEEDBACK_IMAGES) {

            if (resultCode == Activity.RESULT_OK) {
                val file: Uri =
                        data?.getParcelableExtra("uri")
                                ?: return

                attachmentsList.add(file)
                setClickedImage(file)

            } else {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_giger_gigs))
                        .setMessage(getString(R.string.unable_to_capture_giger_gigs))
                        .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
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
                .setTitle(getString(R.string.alert_giger_gigs))
                .setMessage(getString(R.string.delete_image_giger_gigs))
                .setPositiveButton(getString(R.string.yes_giger_gigs)) { _, _ -> deleteImage(index) }
                .setNegativeButton(getString(R.string.no_giger_gigs)) { _, _ -> }
                .show()
    }

    private fun deleteImage(imageToDeleteIndex: Int) {
        attachmentsList.removeAt(imageToDeleteIndex)
        attachments.removeViewAt(imageToDeleteIndex)
    }
}