package com.gigforce.app.modules.explore_by_role

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.utils.ItemDecorationAddContact
import kotlinx.android.synthetic.main.layout_add_education_fragment.*
import kotlinx.android.synthetic.main.layout_fragment_add_contact_details.*

class AddEducationFragment : BaseFragment(), AdapterAddEducation.AdapterAddEducationCallbacks {
    private lateinit var win: Window
    private var adapter: AdapterAddEducation? = null

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
//        initObservers()
        initClicks()
    }

    private fun initClicks() {
        iv_close_add_education.setOnClickListener {
            popBackState()
        }
    }

//    private fun initObservers() {
//        viewModel.observableContact.observe(viewLifecycleOwner, Observer {
//            val list: MutableList<ContactModel> = ArrayList()
//            for (i in 0 until it?.contactPhone?.size!!) {
//                list.add(ContactModel(contactPhone = it?.contactPhone?.get(i)))
//            }
//            for (i in 0 until it?.contactEmail?.size!!) {
//                if (i < list.size) {
//                    list[i].contactEmail = it?.contactEmail!![i]!!
//                } else {
//                    list.add(ContactModel(contactEmail = it?.contactEmail!![i]))
//                }
//            }
//            adapter?.addData(list)
//
//        })
//        viewModel.observableSetContacts.observe(viewLifecycleOwner, Observer {
//            pb_add_contact.gone()
//            if (it == "true") {
//                popBackState()
//            } else {
//                showToast(it!!)
//            }
//        })
//        viewModel.getPrimaryContact()
//
//
//    }

    private fun setUpRecycler() {
        rv_add_education.layoutManager = LinearLayoutManager(requireActivity())
        rv_add_education.addItemDecoration(ItemDecorationAddContact(requireContext()))
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
//        var submitContact = true
//        for (i in 0 until items.size) {
//            if (!isValidMobile(
//                    items[i].contactPhone?.phone ?: ""
//                ) || items[i].contactEmail?.email?.isNotEmpty()!! && !isValidMail(
//                    items[i].contactEmail?.email ?: ""
//                )
//            ) {
//                items[i].validateFields = true
//                submitContact = false
//
//            }
//        }
//        adapter?.notifyItemRangeChanged(0, items.size)
//
//
//        if (submitContact) {
//            pb_add_contact.visible()
//            viewModel.addContacts(items)
//        }
    }


}