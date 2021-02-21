package com.gigforce.app.modules.profile_

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.utils.InputFilterMinMax
import com.gigforce.app.utils.PermissionUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.layout_add_experience_profile_v2.*


class AddExperienceProfileV2 : BaseFragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_add_experience_profile_v2, inflater, container)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }


    private fun initViews() {
        et_months_add_experience.setPadding(resources.getDimensionPixelSize(R.dimen.size_8), resources.getDimensionPixelSize(R.dimen.size_10), resources.getDimensionPixelSize(R.dimen.size_8), resources.getDimensionPixelSize(R.dimen.size_10))
        et_years_add_experience.setPadding(resources.getDimensionPixelSize(R.dimen.size_8), resources.getDimensionPixelSize(R.dimen.size_10), resources.getDimensionPixelSize(R.dimen.size_8), resources.getDimensionPixelSize(R.dimen.size_10))
        intLangChipGroup()
        initClicks()
        initExperienceAutoComplete()
        initExperienceEtSuffix()

    }

    private fun initExperienceEtSuffix() {
        et_months_add_experience.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "12"))
        et_years_add_experience.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    val str = s.toString().replace(" ", "")
                    val value = Integer.parseInt(str.replace("[^0-9]", ""))
                    et_years_add_experience.suffix = if (value > 1) " years" else " year"
                }
            }

        })
        et_months_add_experience.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    val str = s.toString().replace(" ", "")
                    val value = Integer.parseInt(str.replace("[^0-9]", ""))
                    et_months_add_experience.suffix = if (value > 1) " months" else " month"
                }
            }

        })
    }


    private fun initClicks() {
        iv_back_application_add_experience_v2.setOnClickListener { activity?.onBackPressed() }

    }

    private fun initExperienceAutoComplete() {
        act_add_experience_profile_v2.setAdapter(
                resources.getStringArray(R.array.lang_array).toList()
        )
        act_add_experience_profile_v2.setHint(getString(R.string.search_role))
        act_add_experience_profile_v2.setContentPadding(
                paddingTop = resources.getDimensionPixelSize(
                        R.dimen.size_15
                ), paddingBottom = resources.getDimensionPixelSize(R.dimen.size_15),
                paddingLeft = resources.getDimensionPixelSize(R.dimen.size_5)
        )
    }

    private fun intLangChipGroup() {
        val listOf = listOf(
                "Other",
                "Warehouse Helper",
                "Delivery Executive",
                "Driver"
        )
        listOf.forEach { element ->
            val chip = Chip(requireContext())
            val drawable = ChipDrawable.createFromAttributes(
                    requireContext(),
                    null,
                    0,
                    R.style.AppSingleChoiceChip2
            )
            chip.setChipDrawable(drawable)
            chip.text = element
            chip.tag = element

            chip.chipStrokeWidth = 1f
            chip_group_add_experience.addView(chip, 0)

            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.tag == "Other") {
                    act_add_experience_profile_v2.isVisible = isChecked
                    tv_save_add_experience.isVisible = isChecked
                    return@setOnCheckedChangeListener
                }
                act_add_experience_profile_v2.gone()
                if (isChecked) {
                    listOf.forEach {
                        val nesChip = chip_group_add_experience.findViewWithTag<Chip>(it)
                        if (nesChip.tag != buttonView.tag) {
                            chip_group_add_experience.removeView(nesChip)
                        }
                    }
                    tv_save_add_experience.visible()
                    rl_experience_details_add_experience.visible()

                    showViewsAsPerSelection(buttonView.text.toString())


                } else {
                    chip_group_add_experience.removeAllViews()
                    intLangChipGroup()
                    tv_save_add_experience.gone()
                    rl_experience_details_add_experience.gone()

//                    rl_education_details_add_education.gone()
                }

            }
        }
    }

    private fun showViewsAsPerSelection(education: String) {
//        when (education) {
//            getString(R.string.below_10th) -> rl_education_details_add_education.gone()
//            getString(R.string.post_grad), getString(R.string.grad), getString(R.string.diploma) -> {
//                rl_education_details_add_education.visible()
//                showViews(
//                        true,
//                        et_institute_add_education,
//                        et_course_add_education,
//                        tv_completion_month_add_education,
//                        tv_completion_year_add_education,
//                        tv_label_course_add_education,
//                        tv_label_institute_add_education,
//                        tv_label_media_add_education,
//                        tv_add_certification_add_education,
//                        tv_label_completion_date_add_education
//                )
//            }
//            getString(R.string.pass_10th), getString(R.string.pass_12th) -> {
//                rl_education_details_add_education.visible()
//                showViews(false, et_course_add_education, tv_label_course_add_education)
//                showViews(
//                        true,
//                        et_institute_add_education,
//                        tv_completion_month_add_education,
//                        tv_completion_year_add_education,
//                        tv_label_institute_add_education,
//                        tv_label_media_add_education,
//                        tv_add_certification_add_education,
//                        tv_label_completion_date_add_education
//                )
//            }
//
//        }
    }

    private fun pickFile() {
        val fetchPdf = Intent(Intent.ACTION_GET_CONTENT)
        fetchPdf.type = "application/pdf"
        fetchPdf.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(fetchPdf, "Select PDF"), 1098)

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.reqCodePerm) {
            pickFile()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionUtils.reqCodePerm) {
            pickFile()
        }
        if (requestCode == 1098 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedPdfFromStorage = data.data
//            showPdfFromUri(selectedPdfFromStorage)
        }

    }

//    private fun showPdfFromUri(uri: Uri?) {
//        ll_pdf_view_add_education.visible()
//        pdfView_add_education.fromUri(uri)
//                .defaultPage(0)
//                .enableDoubletap(false)
//                .enableSwipe(false)
//                .load()
//        pdfView_add_education.zoomTo(1.75f)
//        tv_file_name_add_education.text = getFileName(uri!!)
//        tv_add_certification_add_education.text = getString(R.string.delete_media)
//
//    }


}