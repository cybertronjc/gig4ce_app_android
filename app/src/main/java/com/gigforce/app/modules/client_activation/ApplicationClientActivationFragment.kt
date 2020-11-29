package com.gigforce.app.modules.client_activation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.JpDraft
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.StringConstants
import io.reactivex.Observable
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
    private lateinit var mNextDep: String


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

        tv_action_application_client_activation.setOnClickListener {
            if (viewModel.observableJpApplication.value != null) {
                val value = viewModel.observableJpApplication.value
                value?.stepDone = 2
                viewModel.apply(value!!)

            } else {
                val jpApplication = JpApplication(JPId = mWordOrderID, gigerId = viewModel.getUID(), stepsTotal = adapter.items.size)
                adapter.items.forEach { dependency ->
                    val list = mutableListOf<JpDraft>()
                    list.add(JpDraft(dependency.isDone, dependency.title
                            ?: "", dependency.feature
                            ?: ""))
                    jpApplication.draft = list
                }
                jpApplication.stepDone = 2
                viewModel.apply(jpApplication)
            }

            pb_application_client_activation.visible()


        }

    }

    private fun initObservers() {
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            pb_application_client_activation.gone()
            showToast(it ?: "")
        })
        viewModel.observableApplicationStatus.observe(viewLifecycleOwner, Observer {
            pb_application_client_activation.gone()
            navigate(R.id.fragment_gig_activation, bundleOf(
                    StringConstants.NEXT_DEP.value to mNextDep,
                    StringConstants.WORK_ORDER_ID.value to mWordOrderID

            ))
        })

        viewModel.observableWorkOrderDependency.observe(viewLifecycleOwner, Observer {
            h_pb_application_frag.max = it?.dependency?.size!!
            tv_thanks_application.text = it?.title
            tv_completion_application.text = it?.subTitle
            mNextDep = it.nextDependency;

            adapter.addData(it?.dependency!!);
            profileViewModel.getProfileData().observe(viewLifecycleOwner, Observer { profileData ->
                h_pb_application_frag.progress = 0;
                tv_steps_pending_application_value.text =
                        "0/" + it?.dependency?.size!!
                profileAvatarName = profileData.profileAvatarName;
                profileViewModel.profileID = profileData?.id ?: ""
                adapter.setCallbacks(this)
                if (profileData.profileAvatarName.isNotEmpty() && profileData.profileAvatarName != "avatar.jpg") {
                    adapter.setImageDrawable(
                            "profile_pic", resources.getDrawable(R.drawable.ic_applied)
                    )
                } else {
                    adapter.setImageDrawable(
                            "profile_pic", resources.getDrawable(R.drawable.ic_status_pending)
                    )
                }
                if (!profileData.aboutMe.isNullOrEmpty()) {
                    adapter.setImageDrawable(
                            "about_me", resources.getDrawable(
                            R.drawable.ic_applied
                    ))
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


                            checkAndUpdateUI(it.dependency?.size ?: 0)
                            viewModel.getApplication(mWordOrderID)

                        })


                adapter.setImageDrawable(
                        "questionnary", resources.getDrawable(
                        R.drawable.ic_status_pending
                ))

                viewModel.getVerification()


            })


        })
        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer { data ->
            val index = data.draft.indexOf(JpDraft(type = "questionnary"))
            if (index != -1) {
                if (data.draft[index].isDone) {
                    adapter.setImageDrawable(
                            "questionnary", resources.getDrawable(
                            R.drawable.ic_applied
                    ))
                }
                checkAndUpdateUI(data.draft.size)


            }

        })
        viewModel.getWorkOrderDependency(workOrderId = mWordOrderID)


    }

    fun checkAndUpdateUI(size: Int) {
        Observable.fromIterable(adapter.items).all { item -> item.isDone }.subscribe({ success ->
            tv_action_application_client_activation.isEnabled = success
        }, { err -> })
        Observable.fromIterable(adapter.items).filter { item -> item.isDone }.toList().subscribe({ success ->
            run {
                h_pb_application_frag.progress = success.size
                tv_steps_pending_application_value.text =
                        "" + (h_pb_application_frag.progress) + "/" + size
            }
        }, { _ -> })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)
        outState.putString(StringConstants.NEXT_DEP.value, mNextDep)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
            mNextDep = it.getString(StringConstants.NEXT_DEP.value) ?: return@let

        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
            mNextDep = it.getString(StringConstants.NEXT_DEP.value) ?: return@let

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
            "questionnary" -> navigate(R.id.application_questionnaire, bundleOf(
                    StringConstants.WORK_ORDER_ID.value to mWordOrderID,
                    StringConstants.WORK_DEP_DATA.value to viewModel.observableWorkOrderDependency.value?.dependency
            ))
            "driving_licence" -> navigate(R.id.fragment_upload_dl_cl_act)
        }
    }

    private var PHOTO_CROP: Int = 45
    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"


}