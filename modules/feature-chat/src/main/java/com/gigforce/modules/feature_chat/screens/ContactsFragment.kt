package com.gigforce.modules.feature_chat.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.modules.feature_chat.screens.adapters.ContactsRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.adapters.OnContactClickListener
import com.gigforce.modules.feature_chat.screens.vm.NewContactsViewModel
import com.gigforce.modules.feature_chat.screens.vm.factories.NewContactsViewModelFactory
import com.gigforce.modules.feature_chat.service.SyncContactsService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_contacts.*
import okhttp3.MultipartBody
import javax.inject.Inject

@AndroidEntryPoint
class ContactsFragment : DialogFragment(),
        PopupMenu.OnMenuItemClickListener,
        OnContactClickListener, CreateGroupDialogFragment.CreateGroupDialogFragmentListener {

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private val viewModelNew: NewContactsViewModel by lazy {
        ViewModelProvider(
                this,
                NewContactsViewModelFactory(requireContext())
        ).get(NewContactsViewModel::class.java)
    }

    // Views
    private lateinit var rootContactsLayout: View

    private lateinit var contactsPermissionLayout: View

    private lateinit var createGroupLayout: View
    private lateinit var createGroupLabel: TextView
    private lateinit var backArrow: View
    private lateinit var searchET: EditText

    private lateinit var noContactsLayout: View
    private lateinit var contactsSyncingLayout: View
    private lateinit var askPermissionView: View
    private lateinit var toolbarOverflowMenu: View
    private lateinit var searchGigersLayout: View
    private lateinit var searchGigersImageView: View

    private lateinit var contactsToolbarLabel: TextView
    private lateinit var contactsToolbarSubTitle: TextView
    private lateinit var refreshingUserHorizontalProgressBar: View
    private lateinit var refreshingUserCenterProgressBar: View

    private lateinit var userSelectedLayout: View
    private lateinit var selectedUserCountTV: TextView
    private lateinit var createGroupFab: FloatingActionButton

    private var onContactSelectedListener: OnContactsSelectedListener? = null

    private val contactsAdapter: ContactsRecyclerAdapter by lazy {
        ContactsRecyclerAdapter(requireContext(), Glide.with(requireContext()), this)
    }

    private var sharedFilesBundle : Bundle? = null

    private val onBackPressCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {

            hideSoftKeyboard()
            if (shouldReturnToPreviousScreen)
                dismiss()
            else {

                if (searchGigersLayout.isVisible) {
                    searchGigersLayout.gone()

                    contactsToolbarLabel.visible()
                    backArrow.visible()
                    contactsToolbarSubTitle.visible()

                    searchET.text = null

                } else if (contactsAdapter.isStateCreateGroup()) {

                    contactsAdapter.stateCreateGroup(false)
                    contactsAdapter.clearSelectedContacts()
                    contactsToolbarSubTitle.text = "${contactsAdapter.itemCount} Contact(s)"

                    userSelectedLayout.isVisible = false
                    selectedUserCountTV.text = getString(R.string.zero_contacts_selected)

                    createGroupFab.gone()
                } else {

                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        }
    }

    private var shouldReturnToPreviousScreen = false

    //Views
    private lateinit var contactRecyclerView: RecyclerView
    private var permissionSnackBar: Snackbar? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_contacts, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        findViews(view)
        setListeners()
        setClickListeners()
        initViewModel()

        checkForPermissionElseSyncContacts()
    }

    private fun findViews(view: View) {
        contactRecyclerView = view.findViewById(R.id.rv_contactsList)
        backArrow = view.findViewById(R.id.back_arrow)
        createGroupLayout = view.findViewById(R.id.create_group_layout)
        contactsSyncingLayout = view.findViewById(R.id.contactsSyncingLayout)
        createGroupLabel = view.findViewById(R.id.create_group_label)
        contactsToolbarLabel = view.findViewById(R.id.textView101)
        contactsToolbarSubTitle = view.findViewById(R.id.tv_sub_heading_contacts_list)
        noContactsLayout = view.findViewById(R.id.noContactsLayout)

        askPermissionView = view.findViewById(R.id.askContactsPermission)
        contactsPermissionLayout = view.findViewById(R.id.contactsPermissionLayout)

        refreshingUserHorizontalProgressBar =
                view.findViewById(R.id.processing_contacts_horizontal_progressbar)
        refreshingUserCenterProgressBar = view.findViewById(R.id.processing_contacts_progressbar)

        toolbarOverflowMenu = view.findViewById(R.id.imageView41)
        searchGigersLayout = view.findViewById(R.id.search_gigers_layout)
        searchET = view.findViewById(R.id.search_textview)

        userSelectedLayout = view.findViewById(R.id.user_selected_layout)
        selectedUserCountTV = view.findViewById(R.id.selected_user_count_tv)

        searchGigersImageView = view.findViewById(R.id.imageView40)
        createGroupFab = view.findViewById(R.id.create_group_fab)
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {

        arguments?.let {
            sharedFilesBundle = it.getBundle(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE)
            shouldReturnToPreviousScreen = it.getBoolean(INTENT_EXTRA_RETURN_SELECTED_RESULTS)
        }

        savedInstanceState?.let {
            sharedFilesBundle = it.getBundle(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE)
            shouldReturnToPreviousScreen = it.getBoolean(INTENT_EXTRA_RETURN_SELECTED_RESULTS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(INTENT_EXTRA_RETURN_SELECTED_RESULTS, shouldReturnToPreviousScreen)
        outState.putBundle(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE, sharedFilesBundle)
    }

    private fun startLoaderForGettingContacts() {
        contactsPermissionLayout.gone()
        showToast(getString(R.string.refreshing))
        requireActivity().startService(Intent(requireContext(), SyncContactsService::class.java))
    }

    private fun setListeners() {

        contactRecyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
        )
        contactRecyclerView.adapter = contactsAdapter

        backArrow.setOnClickListener {

            if (searchGigersLayout.isVisible) {
                searchGigersLayout.gone()

                contactsToolbarLabel.visible()
                backArrow.visible()
            } else {

                if (shouldReturnToPreviousScreen)
                    dismiss()
                else
                    activity?.onBackPressed()
            }
        }

        if (shouldReturnToPreviousScreen) {
            createGroupLabel.text = getString(R.string.add_to_group)
        } else {
            createGroupLabel.text = getString(R.string.create_group)
        }

        askPermissionView.setOnClickListener {
            startAppSettingsPage()
        }

        createGroupLayout.setOnClickListener {

            if (shouldReturnToPreviousScreen) {
                onContactSelectedListener?.onContactsSelected(contactsAdapter.getSelectedContact())
                dismiss()
            } else {

                CreateGroupDialogFragment.launch(
                        contacts = ArrayList(contactsAdapter.getSelectedContact()),
                        createGroupDialogFragmentListener = this,
                        fragmentManager = childFragmentManager
                )
            }
        }

        createGroupFab.setOnClickListener {
            if (contactsAdapter.getSelectedContact().isEmpty()) {
                showToast(getString(R.string.select_at_least_one_contact))
                return@setOnClickListener
            }

            CreateGroupDialogFragment.launch(
                    contacts = ArrayList(contactsAdapter.getSelectedContact()),
                    createGroupDialogFragmentListener = this,
                    fragmentManager = childFragmentManager
            )
        }

        toolbarOverflowMenu.setOnClickListener {

            val popUp = PopupMenu(requireContext(), it)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_contacts)
            popUp.show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                onBackPressCallback
        )
    }

    private fun setClickListeners() {
        backArrow.setOnClickListener {
            activity?.onBackPressed()
        }

        searchET.onTextChanged {
            contactsAdapter.filter.filter(it)
        }

        searchGigersImageView.setOnClickListener {

            if (searchGigersLayout.isVisible) {
                searchGigersLayout.gone()

                contactsToolbarLabel.visible()
                contactsToolbarSubTitle.visible()
            } else {
                searchGigersLayout.visible()

                contactsToolbarLabel.gone()
                contactsToolbarSubTitle.gone()
                searchET.requestFocus()
                openSoftKeyboard(searchET)

                onBackPressCallback.isEnabled = true
            }
        }

        askPermissionView.setOnClickListener {
            startAppSettingsPage()
        }

        createGroupLayout.setOnClickListener {

            if (shouldReturnToPreviousScreen) {
                onContactSelectedListener?.onContactsSelected(contactsAdapter.getSelectedContact())
                dismiss()
            } else {

                CreateGroupDialogFragment.launch(
                        contacts = ArrayList(contactsAdapter.getSelectedContact()),
                        createGroupDialogFragmentListener = this,
                        fragmentManager = childFragmentManager
                )
            }
        }

        toolbarOverflowMenu.setOnClickListener {

            val popUp = PopupMenu(requireContext(), it)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_contacts)
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
        viewModelNew.contacts
                .observe(viewLifecycleOwner, Observer {
                    showContactsOnView(it)
                })
    }

    private fun showContactsAsSyncing() {
        refreshingUserHorizontalProgressBar.visible()

    }

    private fun showContactsAsSynced() {
        refreshingUserHorizontalProgressBar.gone()
        refreshingUserCenterProgressBar.gone()

        showToast(getString(R.string.contacts_synced))
    }

    private fun errorInSyncingContacts(error: String) {
        refreshingUserHorizontalProgressBar.gone()
        refreshingUserCenterProgressBar.gone()

        MaterialAlertDialogBuilder(requireContext())
                .setMessage(error)
                .setTitle(getString(R.string.unable_to_sync_contacts))
                .setPositiveButton("Okay") { _, _ -> }
                .show()
    }

    private fun showContactsOnView(it: List<ContactModel>) {
        val contacts = it ?: return

        if (it.isEmpty()) {

            val cursor = CursorLoader(requireContext(),
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            ).loadInBackground()
            val totalContactList = cursor?.count ?: 0

            if (totalContactList != 0) {
                //Show Syncing Layout
                contactsAdapter.setData(emptyList())
                noContactsLayout.gone()
                contactsSyncingLayout.visible()
            } else {
                contactsToolbarSubTitle.text = "${contacts.size} Contact(s)"

                contactsAdapter.setData(contacts)
                noContactsLayout.visible()
                contactsSyncingLayout.gone()
            }
        } else {
            contactsSyncingLayout.gone()
            noContactsLayout.gone()

            contactsToolbarSubTitle.text = "${contacts.size} Contact(s)"
            contactsAdapter.setData(contacts)
        }

        refreshingUserHorizontalProgressBar.gone()
        refreshingUserCenterProgressBar.gone()
    }

    fun stateCreateNewGroup() {
        contactsToolbarSubTitle.visible()
        contactsToolbarLabel.text = getString(R.string.select_members)
        contactsToolbarSubTitle.text = getString(R.string.tap_to_select)
        user_selected_layout.visible()
        create_group_layout.gone()
        contactsAdapter.getSelectedItems().clear()
        contactsAdapter.notifyDataSetChanged()
        createGroupFab.show()
        contactsAdapter.stateCreateGroup(true)

        selectedUserCountTV.text = "0 Contact(s) Selected"

        onBackPressCallback.isEnabled = true
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
            viewModelNew.startListeningForContactChanges()
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

            if (allPermsGranted) {

                if (contactsAdapter.itemCount == 0) {
                    contactsSyncingLayout.visible()
                }

                viewModelNew.startListeningForContactChanges()
                startLoaderForGettingContacts()
            }
        }
    }

    private fun showPermissionLayout() {
        if (contactsAdapter.itemCount != 0) {

            if (permissionSnackBar == null) {
                permissionSnackBar = Snackbar.make(
                        rootContactsLayout,
                        getString(R.string.grant_contacts_permission),
                        Snackbar.LENGTH_INDEFINITE
                )
                permissionSnackBar?.setAction(getString(R.string.okay)) {
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

            chatNavigation.navigateToChatPage(
                    otherUserId = contact.uid!!,
                    headerId = contact.headerId ?: "",
                    otherUserName = contact.name ?: "",
                    otherUserProfilePicture = contact.getUserProfileImageUrlOrPath() ?: "",
                    chatType = ChatConstants.CHAT_TYPE_USER,
                    sharedFileBundle = sharedFilesBundle
            )
        }
    }

    override fun onContactSelected(selectedContactsCount: Int) {
        onBackPressCallback.isEnabled = true
        contactsAdapter.stateCreateGroup(true)

        userSelectedLayout.isVisible = selectedContactsCount != 0
        selectedUserCountTV.text = "$selectedContactsCount Contact(s) Selected"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(
                STYLE_NORMAL,
                android.R.style.Theme_Light_NoTitleBar_Fullscreen
        )
    }

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

                chatNavigation.openInviteAFriendFragment()
                true
            }
            else -> {
                showToast(getString(R.string.coming_soon))
                false
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val INTENT_EXTRA_RETURN_SELECTED_RESULTS = "return_selected_results"
        const val REQUEST_CONTACTS_PERMISSION = 101
        const val TAG = "ContactsFragment"
        private const val CONTACTS_LOADER_ID = 1


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

    override fun onGroupCreated(groupId: String) {

        chatNavigation.navigateToGroupChat(
                headerId = groupId
        )
    }

    fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()?.getWindowToken(), 0)
    }

    private fun openSoftKeyboard(view: View) {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethod.SHOW_FORCED,
                0
        )
    }
}