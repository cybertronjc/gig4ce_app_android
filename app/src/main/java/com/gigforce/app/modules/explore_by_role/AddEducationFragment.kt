package com.gigforce.app.modules.explore_by_role

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.common_ui.decors.ItemDecorationAddContact
import com.gigforce.common_ui.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_add_education_fragment.*

class AddEducationFragment : BaseFragment(), AdapterAddEducation.AdapterAddEducationCallbacks {
    private var position: Int = 0
    private lateinit var win: Window
    private var adapter: AdapterAddEducation? = null
    val addEducationViewModel: AddEducationViewModel by activityViewModels<AddEducationViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_add_education_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycler()
        initObservers()
        initClicks()
    }

    private fun initClicks() {
        iv_close_add_education.setOnClickListener {
            navFragmentsData?.setData(
                bundleOf(
                    StringConstants.BACK_PRESSED.value to true

                )
            )
            popBackState()
        }
    }

    override fun onBackPressed(): Boolean {
        navFragmentsData?.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true

            )
        )
        return super.onBackPressed()
    }

    private fun initObservers() {
        addEducationViewModel.observableSuccess.observe(viewLifecycleOwner, Observer {
            pb_add_education.gone()
            if (it == "true") {
                navFragmentsData?.setData(bundleOf(StringConstants.MOVE_TO_NEXT_STEP.value to true))
                popBackState()
            } else {
                showToast(it!!)
            }

        })


    }

    private fun setUpRecycler() {
        rv_add_education.layoutManager = LinearLayoutManager(requireActivity())
        rv_add_education.addItemDecoration(
            ItemDecorationAddContact(
                requireContext()
            )
        )
        adapter = AdapterAddEducation()
        rv_add_education.adapter = adapter
        adapter?.addData(mutableListOf(Education()))

        adapter?.setCallbacks(this)


    }


    private fun makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(requireActivity().getColor(R.color.white))
        }
    }


    private fun restoreStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onStart() {
        super.onStart()
        makeStatusBarTransparent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreStatusBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        restoreStatusBar()
    }

    override fun submitClicked(items: MutableList<Education>) {
        var submitEducation = true
        for (i in 0 until items.size) {
            val education = items.get(i)
            items[i].validateFields = true

            if (education.institution.isNullOrEmpty() || education.field.isNullOrEmpty() || education.degree.isNullOrEmpty() || education.startYear == null || education.endYear == null || education.activities.isNullOrEmpty()) {
                submitEducation = false

            }
        }
        adapter?.notifyItemRangeChanged(0, items.size)


        if (submitEducation) {
            pb_add_education.visible()
            addEducationViewModel.addEducation(items)
        }
    }

    override fun uploadEducationDocument(position: Int) {
        this.position = position;
        val photoCropIntent = Intent(requireActivity(), PhotoCrop::class.java)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_PURPOSE,
            PhotoCrop.UPLOAD_DOCUMENT
        )
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_FIREBASE_FOLDER_NAME, "/education/")
        photoCropIntent.putExtra("folder", "education")
        photoCropIntent.putExtra(PhotoCrop.INTENT_EXTRA_DETECT_FACE, 0)
        photoCropIntent.putExtra(
            PhotoCrop.INTENT_EXTRA_FIREBASE_FILE_NAME,
            addEducationViewModel.getUid() + "_" + System.currentTimeMillis()
        )
        startActivityForResult(
            photoCropIntent,
            1097
        )
    }

    override fun goBack() {
        navFragmentsData?.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true

            )
        )
        popBackState()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1097) {

            if (resultCode == Activity.RESULT_OK) {
                val url = data?.getStringExtra("image_url")
                adapter?.setImageAdapter(position, url)

            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.unable_to_capture_image))
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
            }
        }
    }

}