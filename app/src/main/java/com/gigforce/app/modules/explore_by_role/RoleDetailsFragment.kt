package com.gigforce.app.modules.explore_by_role

import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigerVerfication.VerificationBaseModel
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.profile.models.RoleInterests
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.ViewModelProviderFactory
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_role_details_fragment.*


class RoleDetailsFragment : BaseFragment() {
    private var fragmentLoaded = false
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(RoleDetailsVIewModel(RoleDetailsRepository()))
    }

    private val viewModel: RoleDetailsVIewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(RoleDetailsVIewModel::class.java)
    }
    private val viewModelProfile: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }
    private val exploreByRoleViewModel = ExploreByRoleViewModel(ExploreByRoleRepository())


    private val adapterPreferredLocation: AdapterPreferredLocation by lazy {
        AdapterPreferredLocation()
    }
    private lateinit var mRoleID: String;
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_role_details_fragment, inflater, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromSavedState(savedInstanceState)
        setupPreferredLocationRv()
        initObservers()
        checkForMarkedAsInterest()
        checkForRedirection()
    }

    private fun checkForRedirection() {
        if (navFragmentsData?.getData() != null) {
            if (navFragmentsData?.getData()
                    ?.getBoolean(StringConstants.BACK_PRESSED.value, false) == true
            ) {


            } else if (navFragmentsData?.getData()
                    ?.getBoolean(StringConstants.MOVE_TO_NEXT_STEP.value) == true
            ) {
                viewModel.checkForProfileCompletionAndVerification()
                navFragmentsData?.getData()
                    ?.putBoolean(StringConstants.MOVE_TO_NEXT_STEP.value, false)
            }

        }

    }

    private fun checkForMarkedAsInterest() {
        pb_role_details.visible()
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer {
            if (!it.role_interests.isNullOrEmpty()) {

                exploreByRoleViewModel.observerVerified.observe(
                    viewLifecycleOwner,
                    Observer { verified ->
                        pb_role_details.gone()
                        run {
                            val rolePresent = it.role_interests!!.contains(RoleInterests(mRoleID))
                            tv_mark_as_interest_role_details.setOnClickListener {
                                pb_role_details.visible()
                                checkForProfileAndVerificationData()

                            }
                            if (rolePresent && verified!!) {
                                tv_mark_as_interest_role_details.visible()
                                tv_mark_as_interest_role_details.setOnClickListener(null)
                                tv_mark_as_interest_role_details.text =
                                    Html.fromHtml("&#x2713 " + getString(R.string.activated))
                                tv_mark_as_interest_role_details.compoundDrawablePadding =
                                    resources.getDimensionPixelSize(R.dimen.size_10)


                            } else if (rolePresent) {
                                tv_mark_as_interest_role_details.text =
                                    Html.fromHtml("&#x2713 " + getString(R.string.applied))



                            }
                        }

                    })
                exploreByRoleViewModel.checkVerifiedDocs()
            } else {
                tv_mark_as_interest_role_details.text = getString(R.string.apply_now)
                tv_mark_as_interest_role_details.setOnClickListener {
                    pb_role_details.visible()
                    checkForProfileAndVerificationData()

                }

            }

        })


    }

    private fun checkForProfileAndVerificationData() {
        pb_role_details.visible()
        viewModel.checkForProfileCompletionAndVerification()

    }


    private fun initObservers() {

        viewModel.observerRole.observe(viewLifecycleOwner, Observer { role ->
            run {
                tv_role_role_details.text = role?.role_title
                tv_what_content_role_details.text = role?.about
                tv_what_read_more_details.text =
                    "${getString(R.string.what_does_a)} ${role?.role_title} ${
                        getString(
                            R.string.do_question_mark
                        )
                    }"
                adapterPreferredLocation.addData(role?.top_locations ?: mutableListOf())
                tv_earnings_role_details.setOnClickListener {
                    setOnExpandListener(
                        role?.payments_and_benefits,
                        tl_earnings_role_details,
                        tv_earnings_role_details
                    )

                }
                tv_requirements_role_details.setOnClickListener {
                    setOnExpandListener(
                        role?.requirements,
                        tl_requirements_role_details,
                        tv_requirements_role_details
                    )

                }
                tv_responsibilities_role_details.setOnClickListener {
                    setOnExpandListener(
                        role?.job_description,
                        tl_responsibilities_role_details,
                        tv_responsibilities_role_details
                    )

                }

            }
            pb_role_details.gone()

        })
        viewModel.observerError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })
        viewModel.observerMarkedAsInterest.observe(viewLifecycleOwner, Observer {
            navigate(R.id.fragment_marked_as_interest)
        })
        viewModel.observerDataToCheck.observe(viewLifecycleOwner, Observer {
            run {

                pb_role_details.gone()
                for (i in 0 until it?.size!!) {
                    val element = it[i]
                    if (navFragmentsData?.getData()
                            ?.getBoolean(StringConstants.NAVIGATE_TO_MARK_AS_INTERESTED.value) == true
                    ) {
                        navFragmentsData?.getData()
                            ?.putBoolean(
                                StringConstants.NAVIGATE_TO_MARK_AS_INTERESTED.value,
                                false
                            )
                        navigate(
                            R.id.fragment_marked_as_interest,
                            bundleOf(StringConstants.ROLE_ID.value to mRoleID)
                        )
                        break
                    } else if (navFragmentsData?.getData()
                            ?.getBoolean(StringConstants.NAV_TO_QUESTIONNARE.value) == true
                    ) {
                        navFragmentsData?.getData()
                            ?.putBoolean(StringConstants.NAV_TO_QUESTIONNARE.value, false)
                        navigate(R.id.fragment_questionnaire)
                        break
                    } else if (element is ProfileData) {
                        if (element.aboutMe == null || element.aboutMe.isEmpty()) {
                            navigate(R.id.fragment_add_bio)
                            break
                        } else if (element.languages == null || element.languages!!.isEmpty()) {
                            navigate(R.id.fragment_add_language)
                            break
                        } else if (element.contactEmail == null || element.contactEmail!!.isEmpty() || element.contactEmail!![0].email.isNullOrEmpty()
                        ) {
                            navigate(R.id.fragment_add_contact)
                            break
                        } else if (element.educations == null || element.educations!!.isEmpty()) {
                            navigate(R.id.fragment_new_education)
                            break
                        } else if (element.experiences == null || element.experiences!!.isEmpty()) {
                            navigate(R.id.fragment_add_experience)
                            break

                        }

                    } else if (element is VerificationBaseModel) {
                        if (element?.bank_details == null || element?.bank_details?.verified == false || element?.selfie_video == null || element?.selfie_video?.verified == false
                            || element?.pan_card == null || element?.pan_card?.verified == false || element?.aadhar_card == null || element?.aadhar_card?.verified == false
                            || element?.driving_license == null || element?.driving_license?.verified == false

                        ) {
                            navigate(
                                R.id.gigerVerificationFragment, bundleOf(
                                    StringConstants.SHOW_ACTION_BUTTONS.value to true
                                )
                            )
                            break
                        } else {
                            navigate(R.id.fragment_questionnaire)
                        }


                    }
                }

            }

        })

        viewModel.getRoleDetails(mRoleID)
        pb_role_details.visible()
    }

    fun setOnExpandListener(role: List<String>?, layout: TableLayout, textView: TextView) {
        if (layout.childCount > 0) {
            layout.removeAllViews()
            textView.setCompoundDrawablesWithIntrinsicBounds(
                textView.compoundDrawables[0],
                null,
                resources.getDrawable(R.drawable.ic_keyboard_arrow_down_c7c7cc),
                null
            )

        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                textView.compoundDrawables[0],
                null,
                resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_c7c7c7),
                null
            )
            addBulletsTill(
                0,
                if (role?.size!! > 2) 1 else role.size!! - 1,
                layout,
                role,
                true
            )
            if (role?.size!! > 2) {
                val moreTextView = AppCompatTextView(requireContext())
                moreTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    14F
                )
                moreTextView.setTextColor(resources.getColor(R.color.lipstick))
                moreTextView.text = getString(R.string.plus_more)
                val face =
                    Typeface.createFromAsset(requireActivity().assets, "fonts/Lato-Regular.ttf")
                moreTextView.typeface = face
                moreTextView.setPadding(resources.getDimensionPixelSize(R.dimen.size_16), 0, 0, 0)

                layout.addView(moreTextView)
                moreTextView.setOnClickListener {
                    layout.removeViewInLayout(moreTextView)
                    addBulletsTill(
                        2,
                        role.size!! - 1,
                        layout,
                        role,
                        false
                    )
                }
            }
        }

    }

    fun addBulletsTill(
        from: Int,
        to: Int,
        layout: TableLayout,
        arr: List<String>?,
        removeAllViews: Boolean
    ) {
        if (removeAllViews)
            ll_earn_role_details.removeAllViews()
        for (i in from..to) {

            val iv = ImageView(requireContext())
            val layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(
                0,
                resources.getDimensionPixelSize(R.dimen.font_9),
                resources.getDimensionPixelSize(R.dimen.size_8),
                0
            )
            iv.layoutParams = layoutParams
            iv.setImageResource(R.drawable.shape_circle_lipstick)
            val textView = TextView(requireContext())
            val face =
                Typeface.createFromAsset(requireActivity().assets, "fonts/Lato-Regular.ttf")
            textView.typeface = face
            textView.layoutParams = TableRow.LayoutParams(
                getScreenWidth(requireActivity()).width - (resources.getDimensionPixelSize(R.dimen.size_66)),
                TableRow.LayoutParams.WRAP_CONTENT
            )

            textView.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                18F
            )
            textView.text = arr?.get(i)
            textView.setTextColor(resources.getColor(R.color.black))
            val tr = TableRow(requireContext())


            tr.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            tr.addView(iv)
            tr.addView(textView)
            layout.addView(
                tr,
                TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            )

        }
    }

    private fun setupPreferredLocationRv() {
        rv_preferred_locations_role_details.adapter = adapterPreferredLocation
        rv_preferred_locations_role_details.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv_preferred_locations_role_details.addItemDecoration(
            HorizontaltemDecoration(
                requireContext(),
                R.dimen.size_11
            )
        )

    }

    private fun getDataFromSavedState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mRoleID = it.getString(StringConstants.ROLE_ID.value) ?: return@let
        }

        arguments?.let {
            mRoleID = it.getString(StringConstants.ROLE_ID.value) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.ROLE_ID.value, mRoleID)
    }
}