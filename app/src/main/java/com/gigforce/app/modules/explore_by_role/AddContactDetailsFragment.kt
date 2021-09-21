package com.gigforce.app.modules.explore_by_role

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.app.modules.explore_by_role.models.ContactModel
import com.gigforce.common_ui.decors.ItemDecorationAddContact
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.utils.isValidMail
import com.gigforce.core.utils.isValidMobile
import kotlinx.android.synthetic.main.layout_fragment_add_contact_details.*

class AddContactDetailsFragment : BaseFragment(), AdapterAddContact.AdapterAddContactsCallbacks {
    private lateinit var win: Window
    private var adapter: AdapterAddContact? = null
    val viewModel: AddContactViewmodel by activityViewModels<AddContactViewmodel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_add_contact_details, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycler()
        initObservers()
        initClicks()

    }

    private fun initClicks() {
        iv_close_add_contact.setOnClickListener {
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
        viewModel.observableContact.observe(viewLifecycleOwner, Observer {
            if (it?.contactPhone == null || it?.contactPhone?.isEmpty() == true) {
                viewModel.updateContactAndEmailSeparately(it?.contact!!)
            } else {
                val list: MutableList<ContactModel> = ArrayList()
                for (i in 0 until it?.contactPhone?.size!!) {
                    list.add(ContactModel(contactPhone = it?.contactPhone?.get(i)))
                }
                for (i in 0 until it?.contactEmail?.size!!) {
                    if (i < list.size) {
                        list[i].contactEmail = it?.contactEmail!![i]!!
                    } else {
                        list.add(ContactModel(contactEmail = it?.contactEmail!![i]))
                    }
                }
                adapter?.addData(list)

            }

        })
        viewModel.observableSetContacts.observe(viewLifecycleOwner, Observer {
            pb_add_contact.gone()
            if (it == "true") {
                navFragmentsData?.setData(bundleOf(StringConstants.MOVE_TO_NEXT_STEP.value to true))

                popBackState()
            } else {
                showToast(it!!)
            }
        })
        viewModel.getPrimaryContact()


    }

    private fun setUpRecycler() {
        rv_add_contact.layoutManager = LinearLayoutManager(requireActivity())
        rv_add_contact.addItemDecoration(
            ItemDecorationAddContact(
                requireContext()
            )
        )
        adapter = AdapterAddContact()
        rv_add_contact.adapter = adapter

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

    override fun submitClicked(items: MutableList<ContactModel>) {
        var submitContact = true
        for (i in 0 until items.size) {
            items[i].validateFields = true

            if (!isValidMobile(
                    items[i].contactPhone?.phone ?: ""
                ) || items[i].contactEmail?.email?.isNotEmpty()!! && !isValidMail(
                    items[i].contactEmail?.email ?: ""
                )
            ) {
                submitContact = false

            }
        }
        adapter?.notifyItemRangeChanged(0, items.size)


        if (submitContact) {
            pb_add_contact.visible()
            viewModel.addContacts(items)
        }
    }

    override fun goBack() {
        navFragmentsData?.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true

            )
        )
        popBackState()
    }
}