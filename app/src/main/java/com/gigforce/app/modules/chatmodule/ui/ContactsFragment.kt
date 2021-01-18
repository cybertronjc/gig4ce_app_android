package com.gigforce.app.modules.chatmodule.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.chatmodule.OnContactsSelectedListener
import com.gigforce.app.modules.chatmodule.SyncPref
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.chatmodule.ui.adapters.ContactsRecyclerAdapter
import com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners.OnContactClickListener
import com.gigforce.app.modules.chatmodule.viewModels.ContactsViewModel
import com.gigforce.app.modules.chatmodule.viewModels.GroupChatViewModel
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.vinners.cmi.ui.activity.ContactsViewModelFactory
import com.vinners.cmi.ui.activity.GroupChatViewModelFactory
import kotlinx.android.synthetic.main.day_view_top_bar.*
import kotlinx.android.synthetic.main.fragment_chat_new_contact.*

/*
    /////////////////////////////////////////////////////////////////////////////////
 */
class ContactsFragment : DialogFragment(),
        OnContactClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        PopupMenu.OnMenuItemClickListener {

    private val viewModel: ContactsViewModel by lazy {
        ViewModelProvider(
                this,
                ContactsViewModelFactory(requireContext())
        ).get(ContactsViewModel::class.java)
    }


    private val chatGroupViewModel: GroupChatViewModel by lazy {
        ViewModelProvider(
                this,
                GroupChatViewModelFactory(requireContext())
        ).get(GroupChatViewModel::class.java)
    }
    private var onContactSelectedListener: OnContactsSelectedListener? = null

    // Defines a variable for the search string
    private val searchString: String = ""

    // Defines the array to hold values that replace the ?
    private val selectionArgs = arrayOf("%$searchString%")

    private val contactsAdapter: ContactsRecyclerAdapter by lazy {
        ContactsRecyclerAdapter(requireContext(), Glide.with(requireContext()), this)
    }

    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }

    private val syncPref: SyncPref by lazy {
        SyncPref.getInstance(requireContext())
    }

    private var shouldReturnToPreviousScreen = false

    //Views
    private lateinit var contactRecyclerView: RecyclerView
    private var permissionSnackBar: Snackbar? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chat_new_contact, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        findViews(view)
        setClickListeners()
        initViewModel()

        if (syncPref.shouldSyncContacts())
            checkForPermissionElseSyncContacts()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {

        arguments?.let {
            shouldReturnToPreviousScreen = it.getBoolean(INTENT_EXTRA_RETURN_SELECTED_RESULTS)
        }

        savedInstanceState?.let {
            shouldReturnToPreviousScreen = it.getBoolean(INTENT_EXTRA_RETURN_SELECTED_RESULTS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(INTENT_EXTRA_RETURN_SELECTED_RESULTS, shouldReturnToPreviousScreen)
    }

    private fun startLoaderForGettingContacts() {
        contactsPermissionLayout.gone()

        LoaderManager
                .getInstance(this@ContactsFragment)
                .initLoader(CONTACTS_LOADER_ID, null, this@ContactsFragment)
    }

    private fun findViews(view: View) {
        contactRecyclerView = view.findViewById(R.id.rv_contactsList)
        contactRecyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
        )
        contactRecyclerView.adapter = contactsAdapter

        back_arrow.setOnClickListener {

            if (shouldReturnToPreviousScreen)
                dismiss()
            else
                activity?.onBackPressed()
        }

        if (shouldReturnToPreviousScreen) {
            create_group_label.text = "Add To Group"
        } else {
            create_group_label.text = "Create Group"
        }

        askContactsPermission.setOnClickListener {
            startAppSettingsPage()
        }

        create_group_layout.setOnClickListener {
            createNewGroup()
        }
        fab_create_group_contacts.setOnClickListener {
            if (contactsAdapter.getSelectedContact().isEmpty()) {
                showToast(getString(R.string.select_at_least_one_contact))
                return@setOnClickListener
            }
            createNewGroup()
        }

        imageView41.setOnClickListener {

            if (shouldReturnToPreviousScreen) {
                onContactSelectedListener?.onContactsSelected(contactsAdapter.getSelectedContact())
                dismiss()
            } else {

                val groupNameEt = EditText(requireContext())

                val layout = FrameLayout(requireContext())
                layout.setPaddingRelative(45, 15, 45, 0)
                layout.addView(groupNameEt)

                MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Enter a group name")
                        .setTitle("Group name")
                        .setView(layout)
                        .setPositiveButton("Okay") { _, _ ->

                            if (groupNameEt.length() == 0) {
                                showToast("Please enter a group name")
                            } else {
                                chatGroupViewModel.createGroup(
                                        groupName = groupNameEt.text.toString().capitalize(),
                                        groupMembers = contactsAdapter.getSelectedContact()
                                )
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ ->

                    }.show()
            }
        }

        imageView41.setOnClickListener {
            val themeWrapper = ContextThemeWrapper(requireContext(), R.style.PopUpMenuWithOffset)
            val popUp = PopupMenu(themeWrapper, it)
            popUp.gravity = Gravity.END
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_contact)

            popUp.show()
        }
    }

    private fun setClickListeners() {
        back_arrow.setOnClickListener {

            if (shouldReturnToPreviousScreen)
                dismiss()
            else
                activity?.onBackPressed()
        }

        search_textview.textChanged {
            contactsAdapter.filter.filter(it)
        }

        imageView40.setOnClickListener {

            if (search_gigers_layout.isVisible) {
                search_gigers_layout.gone()

                textView101.visible()
                back_arrow.visible()
                if (contactsAdapter.isStateCreateGroup()) {
                    tv_sub_heading_contacts_list.visible()
                }
            } else {
                search_gigers_layout.visible()

                textView101.gone()
                back_arrow.gone()
                if (contactsAdapter.isStateCreateGroup()) {
                    tv_sub_heading_contacts_list.gone()
                }
                search_textview.requestFocus()
            }
        }

        askContactsPermission.setOnClickListener {
            startAppSettingsPage()
        }

        create_group_layout.setOnClickListener {

            if (shouldReturnToPreviousScreen) {
                onContactSelectedListener?.onContactsSelected(contactsAdapter.getSelectedContact())
                dismiss()
            } else {

                val groupNameEt = EditText(requireContext())

                val layout = FrameLayout(requireContext())
                layout.setPaddingRelative(45, 15, 45, 0)
                layout.addView(groupNameEt)

                MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Enter a group name")
                        .setTitle("Group name")
                        .setView(layout)
                        .setPositiveButton("Okay") { _, _ ->

                            if (groupNameEt.length() == 0) {
                                showToast("Please enter a group name")
                            } else {
                                chatGroupViewModel.createGroup(
                                        groupName = groupNameEt.text.toString().capitalize(),
                                        groupMembers = contactsAdapter.getSelectedContact()
                                )
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ ->

                        }.show()
            }
        }

        imageView41.setOnClickListener {
            val themeWrapper = ContextThemeWrapper(requireContext(), R.style.PopUpMenuWithOffset)
            val popUp = PopupMenu(themeWrapper, it)
            popUp.gravity = Gravity.END
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_contact)

            popUp.show()
        }
    }

    private fun startAppSettingsPage() {
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            data = uri
            startActivityForResult(this, REQUEST_CONTACTS_PERMISSION)
        }
    }

    private fun initViewModel() {
        viewModel.contacts
                .observe(viewLifecycleOwner, Observer {
                    showContactsOnView(it)
                })

        viewModel.syncContacts
                .observe(viewLifecycleOwner, Observer {
                    when (it) {
                        Lse.Loading -> showContactsAsSyncing()
                        Lse.Success -> showContactsAsSynced()
                        is Lse.Error -> errorInSyncingContacts(it.error)
                    }
                })

        chatGroupViewModel
                .createGroup
                .observe(viewLifecycleOwner, Observer {
                    it ?: return@Observer

                    when (it) {
                        Lce.Loading -> UtilMethods.showLoading(requireContext())
                        is Lce.Content -> {
                            UtilMethods.hideLoading()
                            showToast("Group created")

                            findNavController()
                                    .navigate(
                                            R.id.groupChatFragment, bundleOf(
                                            GroupChatFragment.INTENT_EXTRA_GROUP_ID to it.content
                                    )
                                    )
                        }
                        is Lce.Error -> {
                            UtilMethods.hideLoading()

                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Error while creating group")
                                    .setMessage(it.error)
                                    .setPositiveButton("okay") { _, _ -> }
                                    .show()
                        }
                    }
                })
    }

    private fun showContactsAsSyncing() {
        processing_contacts_horizontal_progressbar.visible()
//        if (contactsAdapter.itemCount != 0) {
//            //show top
//            processing_contacts_horizontal_progressbar.visible()
//            processing_contacts_progressbar.gone()
//        } else {
//            processing_contacts_horizontal_progressbar.gone()
//            processing_contacts_progressbar.visible()
//        }
    }

    private fun showContactsAsSynced() {
        processing_contacts_horizontal_progressbar.gone()
        processing_contacts_progressbar.gone()

        showToast("Contacts Synced")
    }

    private fun errorInSyncingContacts(error: String) {
        processing_contacts_horizontal_progressbar.gone()
        processing_contacts_progressbar.gone()

        MaterialAlertDialogBuilder(requireContext())
                .setMessage(error)
                .setTitle("Unable to sync contacts")
                .setPositiveButton("Okay") { _, _ -> }
                .show()
    }

    private fun showContactsOnView(it: List<ContactModel>?) {
        val contacts = it ?: return
        contactsAdapter.setData(contacts)

        processing_contacts_horizontal_progressbar.gone()
        processing_contacts_progressbar.gone()
    }

    private fun checkForPermissionElseSyncContacts() {
        // check for permissions

        if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_CONTACTS
                )
                != PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(TAG, "Permission Required. Requesting Permission")
            requestPermissions(
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    REQUEST_CONTACTS_PERMISSION
            )
            showPermissionLayout()
        } else {
            startLoaderForGettingContacts()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            var allPermsGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermsGranted = false
                    break
                }
            }

            if (allPermsGranted)
                startLoaderForGettingContacts()
        }
    }

    private fun showPermissionLayout() {
        if (contactsAdapter.itemCount != 0) {

            if (permissionSnackBar == null) {
                permissionSnackBar = Snackbar.make(
                        contacts_root_layout,
                        "Grant contacts permission to sync contacts",
                        Snackbar.LENGTH_INDEFINITE
                )
                permissionSnackBar?.setAction("Okay") {
                    startAppSettingsPage()
                }
            }

            permissionSnackBar?.show()
        } else {
            contactsPermissionLayout.visible()
        }
    }

    override fun contactClick(contact: ContactModel) {

        if (shouldReturnToPreviousScreen) {
            onContactSelectedListener?.onContactsSelected(listOf(contact))
            dismiss()
        } else {

            val bundle = Bundle()
            bundle.putString(
                    AppConstants.IMAGE_URL,
                    contact.imageUrl
            )
            bundle.putString(AppConstants.CONTACT_NAME, contact.name)
            bundle.putString("chatHeaderId", contact.headerId)
            bundle.putString("forUserId", currentUserId)
            bundle.putString("otherUserId", contact.uid)
            findNavController().navigate(R.id.chatScreenFragment, bundle)
        }
    }

    override fun onContactSelected(
        selectedContactsCount: Int
    ) {
        user_selected_layout.isVisible = selectedContactsCount != 0
        create_group_layout.isVisible =
            selectedContactsCount != 0 && !contactsAdapter.isStateCreateGroup()
        selected_user_count_tv.text =
            "$selectedContactsCount ${getString(R.string.contacts_selected)}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
                STYLE_NORMAL,
                android.R.style.Theme_Light_NoTitleBar_Fullscreen
        )
    }

    fun stateCreateNewGroup() {
        tv_sub_heading_contacts_list.visible()
        textView101.text = getString(R.string.select_members)
        user_selected_layout.visible()
        create_group_layout.gone()
        contactsAdapter.getSelectedItems().clear()
        contactsAdapter.notifyDataSetChanged()
        fab_create_group_contacts.show()
        contactsAdapter.stateCreateGroup(true)
        selected_user_count_tv.text =
            "0 ${getString(R.string.contacts_selected)}"
    }

//    fun stateContactsList() {
//        tv_sub_heading_contacts_list.gone()
//        textView101.text = getString(R.string.contacts)
//        user_selected_layout.gone()
//        create_group_layout.visible()
//        contactsAdapter.getSelectedItems().clear()
//        contactsAdapter.notifyDataSetChanged()
//        contactsAdapter.stateCreateGroup(false)
//        selected_user_count_tv.text = ""
//    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_new_group -> {
                stateCreateNewGroup()
                true
            }
            R.id.action_referesh -> {
                checkForPermissionElseSyncContacts()
                true
            }
            R.id.action_invite_friends -> {
                findNavController().navigate(R.id.referrals_fragment)
                true
            }
            else -> {
                showToast("Coming soon")
                false
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun createContactsLoader(): Loader<Cursor> {
        return CursorLoader(
                requireContext(),
                // Contacts.CONTENT_URI,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                SELECTION,
                selectionArgs,
                null
        )
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return createContactsLoader()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        data?.let {
            val contacts = mapToContactModel(it)
            Log.d(TAG, "${contacts.size} unique Contacts Fetched")

            viewModel.syncContacts(contacts)
        }
    }

    private fun mapToContactModel(cursor: Cursor): List<ContactModel> {
        Log.v(TAG, "${cursor.count} Items Loaded!")
        val contacts: MutableList<ContactModel> = mutableListOf()

        cursor.moveToFirst()
        while (!cursor.isLast) {
            val name = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            )
            val phone = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            )
            val contactId = cursor.getString(
                    cursor.getColumnIndex((ContactsContract.Contacts._ID))
            )

            contacts.add(
                    ContactModel(
                            id = null,
                            mobile = cleanPhoneNo(phone),
                            name = name,
                            contactId = contactId
                    )
            )

            // Move Cursor to Next Position
            cursor.moveToNext()
        }
        return contacts.distinctBy { it.mobile }
    }

    private fun cleanPhoneNo(phone: String): String {
        var updatedPhoneNo = phone.replace("\\s|\t|[(]|[)]|[-]".toRegex(), "")
        if (updatedPhoneNo.startsWith('+')) {
            updatedPhoneNo = updatedPhoneNo.replace("[+]".toRegex(), "")
        } else {
            updatedPhoneNo = updatedPhoneNo.replace("^0".toRegex(), "")
            updatedPhoneNo = "91${updatedPhoneNo}"  // todo: CountryCode need to be generalised
        }
        return updatedPhoneNo
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}

    companion object {
        const val INTENT_EXTRA_RETURN_SELECTED_RESULTS = "return_selected_results"
        const val REQUEST_CONTACTS_PERMISSION = 101
        const val TAG = "ContactsFragment"
        private const val CONTACTS_LOADER_ID = 1

        private val PROJECTION: Array<out String> = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        private val SELECTION: String =
                "${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0 and ${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"


        fun launchForSelectingContact(
                fm: FragmentManager,
                onContactsSelectedListener: OnContactsSelectedListener
        ) {
            ContactsFragment().apply {
                arguments = bundleOf(
                        INTENT_EXTRA_RETURN_SELECTED_RESULTS to true
                )
                this.onContactSelectedListener = onContactsSelectedListener
                show(fm, TAG)
            }
        }
    }
    private fun createNewGroup() {
        if (shouldReturnToPreviousScreen) {
            onContactSelectedListener?.onContactsSelected(contactsAdapter.getSelectedContact())
            dismiss()
        } else {

            val groupNameEt = EditText(requireContext())

            val layout = FrameLayout(requireContext())
            layout.setPaddingRelative(45, 15, 45, 0)
            layout.addView(groupNameEt)

            MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Enter a group name")
                    .setTitle("Group name")
                    .setView(layout)
                    .setPositiveButton("Okay") { _, _ ->

                        if (groupNameEt.length() == 0) {
                            showToast("Please enter a group name")
                        } else {
                            chatGroupViewModel.createGroup(
                                    groupName = groupNameEt.text.toString().capitalize(),
                                    groupMembers = contactsAdapter.getSelectedContact()
                            )
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                    }.show()
        }
    }


}