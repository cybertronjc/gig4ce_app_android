package com.gigforce.app.modules.client_activation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.layout_application_client_activation_fragment.*

class ApplicationClientActivationFragment : BaseFragment(),
    AdapterApplicationClientActivation.AdapterApplicationClientActivationCallbacks {
    private var profileAvatarName: String? = null
    private val viewModel: ApplicationClientActivationViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()


    private val adapter: AdapterApplicationClientActivation by lazy {
        AdapterApplicationClientActivation()
    }
    private lateinit var mWordOrderID: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(
            R.layout.layout_application_client_activation_fragment,
            inflater,
            container
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        setupRecycler()
        initObservers()
        initClicks()

    }

    private fun initClicks() {
        iv_back_application_client_activation.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initObservers() {
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })

        viewModel.observableWorkOrderDependency.observe(viewLifecycleOwner, Observer {
            h_pb_application_frag.max = it?.dependency?.size!!
            tv_thanks_application.text = it?.title
            tv_completion_application.text = it?.sub_title

            adapter.addData(it?.dependency!!);
            profileViewModel.getProfileData().observe(viewLifecycleOwner, Observer { profileData ->
                h_pb_application_frag.progress = 0;
                tv_steps_pending_application_value.text =
                    "0/" + it?.dependency?.size!!
                profileAvatarName = profileData.profileAvatarName;
                profileViewModel.profileID = profileData?.id ?: ""
                adapter.setCallbacks(this)
                if (profileData.profileAvatarName.isNotEmpty() && profileData.profileAvatarName != "avatar.jpg") {
                    h_pb_application_frag.progress = h_pb_application_frag.progress + 1
                    tv_steps_pending_application_value.text =
                        "" + (h_pb_application_frag.progress) + "/" + it?.dependency?.size!!
                    adapter.setImageDrawable(
                        "profile_pic", resources.getDrawable(R.drawable.ic_applied)
                    )

                } else {
                    adapter.setImageDrawable(
                        "profile_pic", resources.getDrawable(R.drawable.ic_status_pending)
                    )
                }
                if (!profileData.aboutMe.isNullOrEmpty()) {
                    h_pb_application_frag.progress = h_pb_application_frag.progress + 1
                    tv_steps_pending_application_value.text =
                        "" + (h_pb_application_frag.progress) + "/" + it?.dependency?.size!!
                    adapter.setImageDrawable(
                        "about_me", resources.getDrawable(
                            R.drawable.ic_applied
                        )
                    )
                } else {
                    adapter.setImageDrawable(
                        "about_me", resources.getDrawable(R.drawable.ic_status_pending)
                    )
                }

                viewModel.observableVerification.observe(
                    viewLifecycleOwner,
                    Observer { verificationData ->
                        run {
                            if (verificationData?.driving_license?.verified == true) {
                                h_pb_application_frag.progress = h_pb_application_frag.progress + 1
                                tv_steps_pending_application_value.text =
                                    "" + (h_pb_application_frag.progress) + "/" + it?.dependency?.size!!
                                adapter.setImageDrawable(
                                    "driving_licence", resources.getDrawable(
                                        R.drawable.ic_applied
                                    )
                                )
                            } else {
                                adapter.setImageDrawable(
                                    "driving_licence", resources.getDrawable(
                                        R.drawable.ic_status_pending
                                    )
                                )
                            }

                        }

                    })
                viewModel.getVerification()


            })


        })
        viewModel.getWorkOrderDependency(workOrderId = mWordOrderID)


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }
    }

    private fun setupRecycler() {
        rv_status_pending.adapter = adapter
        rv_status_pending.layoutManager =
            LinearLayoutManager(requireContext())
//        rv_status_pending.addItemDecoration(
//            HorizontaltemDecoration(
//                requireContext(),
//                R.dimen.size_11
//            )
//        )

    }

    override fun onItemClick(feature: String) {
        when (feature) {
            "profile_pic" -> {
                val photoCropIntent = Intent(context, PhotoCrop::class.java)
                photoCropIntent.putExtra("purpose", "profilePictureCrop")
                photoCropIntent.putExtra("uid", viewModel.repository.getUID())
                photoCropIntent.putExtra("fbDir", "/profile_pics/")
                photoCropIntent.putExtra("detectFace", 1)
                photoCropIntent.putExtra("folder", PROFILE_PICTURE_FOLDER)
                photoCropIntent.putExtra("file", profileAvatarName)

                startActivityForResult(photoCropIntent, PHOTO_CROP)
            }
            "about_me" -> {
                navigate(R.id.profileFragment)
            }
        }
    }

    private var PHOTO_CROP: Int = 45
    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"


}