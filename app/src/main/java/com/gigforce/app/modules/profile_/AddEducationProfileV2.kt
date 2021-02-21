package com.gigforce.app.modules.profile_

import `in`.galaxyofandroid.spinerdialog.OnSpinerItemClick
import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.utils.PermissionUtils
import com.gigforce.app.utils.openPopupMenu
import com.gigforce.app.utils.showViews
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.fragment_add_education.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddEducationProfileV2 : BaseFragment(), PopupMenu.OnMenuItemClickListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_add_education, inflater, container)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }


    private fun initViews() {
        intLangChipGroup()
        initClicks()
        initCompletionDateViews()
    }

    private fun initCompletionDateViews() {
        val years = ArrayList<String>()
        val thisYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (i in (thisYear - 100)..thisYear) {
            years.add(i.toString())
        }
        val spinnerDialog = SpinnerDialog(
            requireActivity(),
            years,
            getString(R.string.select_year),
            "close"
        ) // With No Animation
        spinnerDialog.setCancellable(true) // for cancellable
        spinnerDialog.setShowKeyboard(false) // for open keyboard by default
        spinnerDialog.bindOnSpinerListener(OnSpinerItemClick { spinnerItem, _ ->
            tv_completion_year_add_education.text = spinnerItem
        })
        tv_completion_year_add_education.setOnClickListener {
            spinnerDialog.showSpinerDialog();
        }
        val months = ArrayList<String>()

        for (i in 0..11) {
            val cal = Calendar.getInstance()
            val monDate = SimpleDateFormat("MMMM")
            cal[Calendar.MONTH] = i
            val monthName: String = monDate.format(cal.time)
            months.add(monthName)
        }
        val spinnerDialog2 = SpinnerDialog(
            requireActivity(),
            months,
            getString(R.string.select_year),
            "close"
        ) // With No Animation
        spinnerDialog2.setCancellable(true) // for cancellable
        spinnerDialog2.setShowKeyboard(false) // for open keyboard by default
        spinnerDialog2.bindOnSpinerListener(OnSpinerItemClick { spinnerItem, _ ->
            tv_completion_month_add_education.text = spinnerItem
        })
        tv_completion_month_add_education.setOnClickListener {
            spinnerDialog2.showSpinerDialog();
        }


    }

    private fun initClicks() {
        iv_back_application_add_education_v2.setOnClickListener { activity?.onBackPressed() }
        tv_add_certification_add_education.setOnClickListener {
            if (ll_pdf_view_add_education.visibility == View.VISIBLE) {
                ll_pdf_view_add_education.gone()
                tv_add_certification_add_education.text = getString(R.string.add_media)
            } else {
                pickFile()
            }

        }
        tv_privacy_add_education.setOnClickListener {
            openPopupMenu(it, R.menu.menu_privacy, this, activity)
        }

    }


    private fun intLangChipGroup() {
        val listOf = listOf(
            getString(R.string.below_10th),
            getString(R.string.post_grad),
            getString(R.string.grad),
            getString(R.string.diploma),
            getString(R.string.pass_12th),
            getString(R.string.pass_10th)
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
            chip_group_add_education.addView(chip, 0)
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    listOf.forEach {
                        val nesChip = chip_group_add_education.findViewWithTag<Chip>(it)
                        if (nesChip.tag != buttonView.tag) {
                            chip_group_add_education.removeView(nesChip)
                        }
                    }
                    tv_save_add_education.visible()
                    showViewsAsPerSelection(buttonView.text.toString())


                } else {
                    chip_group_add_education.removeAllViews()
                    intLangChipGroup()
                    tv_save_add_education.gone()
                    rl_education_details_add_education.gone()
                }

            }
        }
    }

    private fun showViewsAsPerSelection(education: String) {
        when (education) {
            getString(R.string.below_10th) -> rl_education_details_add_education.gone()
            getString(R.string.post_grad), getString(R.string.grad), getString(R.string.diploma) -> {
                rl_education_details_add_education.visible()
                showViews(
                    true,
                    et_institute_add_education,
                    et_course_add_education,
                    tv_completion_month_add_education,
                    tv_completion_year_add_education,
                    tv_label_course_add_education,
                    tv_label_institute_add_education,
                    tv_label_media_add_education,
                    tv_add_certification_add_education,
                    tv_label_completion_date_add_education
                )
            }
            getString(R.string.pass_10th), getString(R.string.pass_12th) -> {
                rl_education_details_add_education.visible()
                showViews(false, et_course_add_education, tv_label_course_add_education)
                showViews(
                    true,
                    et_institute_add_education,
                    tv_completion_month_add_education,
                    tv_completion_year_add_education,
                    tv_label_institute_add_education,
                    tv_label_media_add_education,
                    tv_add_certification_add_education,
                    tv_label_completion_date_add_education
                )
            }

        }
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
            showPdfFromUri(selectedPdfFromStorage)
        }

    }

    private fun showPdfFromUri(uri: Uri?) {
        ll_pdf_view_add_education.visible()
        pdfView_add_education.fromUri(uri)
            .defaultPage(0)
            .enableDoubletap(false)
            .enableSwipe(false)
            .load()
        pdfView_add_education.zoomTo(1.75f)
        tv_file_name_add_education.text = getFileName(uri!!)
        tv_add_certification_add_education.text = getString(R.string.delete_media)

    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = activity?.contentResolver?.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (cut != null) {
                    result = result?.substring(cut + 1)
                }
            }
        }
        return result
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_private -> tv_privacy_add_education.text = "Private"
            R.id.action_public -> tv_privacy_add_education.text = "Public"
        }
        return true
    }


}