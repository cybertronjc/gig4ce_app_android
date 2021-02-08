package com.gigforce.modules.feature_chat.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.IChatNavigation
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.models.ContactModel
import com.gigforce.modules.feature_chat.screens.adapters.ContactsRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.adapters.OnContactClickListener
import com.gigforce.modules.feature_chat.screens.vm.ContactsViewModel
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.gigforce.modules.feature_chat.service.SyncContactsService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.vinners.cmi.ui.activity.NewContactsViewModelFactory
import com.vinners.cmi.ui.activity.GroupChatViewModelFactory
import javax.inject.Inject


class ContactsFragment : DialogFragment(),
        PopupMenu.OnMenuItemClickListener,
        OnContactClickListener {

    @Inject
    lateinit var navigation: IChatNavigation

    private val viewModel: ContactsViewModel by lazy {
        ViewModelProvider(
                this,
                NewContactsViewModelFactory(requireContext())
        ).get(ContactsViewModel::class.java)
    }

    private val chatGroupViewModel: GroupChatViewModel by lazy {
        ViewModelProvider(
                this,
                GroupChatViewModelFactory(requireContext())
        ).get(GroupChatViewModel::class.java)
    }

    // Views
    private lateinit var rootContactsLayout: View

    private lateinit var contactsPermissionLayout: View

    private lateinit var createGroupLayout: View
    private lateinit var createGroupLabel: TextView
    private lateinit var backArrow: View
    private lateinit var searchET: EditText

    private lateinit var noContactsLayout: View
    private lateinit var askPermissionView: View
    private lateinit var toolbarOverflowMenu: View
    private lateinit var searchGigersLayout: View
    private lateinit var searchGigersImageView: View

    private lateinit var contactsToolbarLabel: TextView
    private lateinit var refreshingUserHorizontalProgressBar: View
    private lateinit var refreshingUserCenterProgressBar: View

    private lateinit var userSelectedLayout: View
    private lateinit var selectedUserCountTV: TextView

    private var onContactSelectedListener: OnContactsSelectedListener? = null

    private val contactsAdapter: ContactsRecyclerAdapter by lazy {
        ContactsRecyclerAdapter(requireContext(), Glide.with(requireContext()), this)
    }

    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser!!.uid
    }

    private val onBackPressCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {


            if (searchGigersLayout.isVisible) {
                searchGigersLayout.gone()

                contactsToolbarLabel.visible()
                backArrow.visible()
            } else {
                //todo fix it
                isEnabled = false
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
        createGroupLabel = view.findViewById(R.id.create_group_label)
        contactsToolbarLabel = view.findViewById(R.id.textView101)
        noContactsLayout = view.findViewById(R.id.noContactsLayout)

        askPermissionView = view.findViewById(R.id.askContactsPermission)
        contactsPermissionLayout = view.findViewById(R.id.contactsPermissionLayout)

        refreshingUserHorizontalProgressBar = view.findViewById(R.id.processing_contacts_horizontal_progressbar)
        refreshingUserCenterProgressBar = view.findViewById(R.id.processing_contacts_progressbar)

        toolbarOverflowMenu = view.findViewById(R.id.imageView41)
        searchGigersLayout = view.findViewById(R.id.search_gigers_layout)
        searchET = view.findViewById(R.id.search_textview)

        userSelectedLayout = view.findViewById(R.id.user_selected_layout)
        selectedUserCountTV = view.findViewById(R.id.selected_user_count_tv)

        searchGigersImageView = view.findViewById(R.id.imageView40)
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
        showToast("Refreshing...")
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

            if (shouldReturnToPreviousScreen)
                dismiss()
            else
                activity?.onBackPressed()
        }

        if (shouldReturnToPreviousScreen) {
            createGroupLabel.text = "Add To Group"
        } else {
            createGroupLabel.text = "Create Group"
        }

        askPermissionView.setOnClickListener {
            startAppSettingsPage()
        }

        createGroupLayout.setOnClickListener {

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
//                            chatGroupViewModel.createGroup(
//                                groupName = groupNameEt.text.toString().capitalize(),
//                                groupMembers = contactsAdapter.getSelectedContact()
//                            )
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ ->

                        }.show()
            }
        }

        toolbarOverflowMenu.setOnClickListener {

            val popUp = PopupMenu(requireContext(), it)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_contacts)
            popUp.show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressCallback)
    }

    private fun setClickListeners() {
        backArrow.setOnClickListener {

            if (shouldReturnToPreviousScreen)
                dismiss()
            else
                activity?.onBackPressed()
        }

        searchET.onTextChanged {
            contactsAdapter.filter.filter(it)
        }

        searchGigersImageView.setOnClickListener {

            if (searchGigersLayout.isVisible) {
                searchGigersLayout.gone()

                contactsToolbarLabel.visible()
                backArrow.visible()
            } else {
                searchGigersLayout.visible()

                contactsToolbarLabel.gone()
                backArrow.gone()

                searchET.requestFocus()
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
        viewModel.contacts
                .observe(viewLifecycleOwner, Observer {
                    showContactsOnView(it)
                })

        chatGroupViewModel
                .createGroup
                .observe(viewLifecycleOwner, Observer {
                    it ?: return@Observer

                    //todo shift group creation to a diff model

//                    when (it) {
//                        Lce.Loading -> UtilMethods.showLoading(requireContext())
//                        is Lce.Content -> {
//                            UtilMethods.hideLoading()
//                            showToast("Group created")
//
//                            findNavController()
//                                    .navigate(
//                                            R.id.groupChatFragment, bundleOf(
//                                            GroupChatFragment.INTENT_EXTRA_GROUP_ID to it.content
//                                    )
//                                    )
//                        }
//                        is Lce.Error -> {
//                            UtilMethods.hideLoading()
//
//                            MaterialAlertDialogBuilder(requireContext())
//                                    .setTitle("Error while creating group")
//                                    .setMessage(it.error)
//                                    .setPositiveButton("okay") { _, _ -> }
//                                    .show()
//                        }
//                    }
                })
    }

    private fun showContactsAsSyncing() {
        refreshingUserHorizontalProgressBar.visible()
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
        refreshingUserHorizontalProgressBar.gone()
        refreshingUserCenterProgressBar.gone()

        showToast("Contacts Synced")
    }

    private fun errorInSyncingContacts(error: String) {
        refreshingUserHorizontalProgressBar.gone()
        refreshingUserCenterProgressBar.gone()

        MaterialAlertDialogBuilder(requireContext())
                .setMessage(error)
                .setTitle("Unable to sync contacts")
                .setPositiveButton("Okay") { _, _ -> }
                .show()
    }

    private fun showContactsOnView(it: List<ContactModel>) {
        val contacts = it ?: return
        contactsAdapter.setData(contacts)

        if (it.isEmpty()) {
            noContactsLayout.visible()
        } else {
            noContactsLayout.gone()
        }

        refreshingUserHorizontalProgressBar.gone()
        refreshingUserCenterProgressBar.gone()
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
                        rootContactsLayout,
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


//            val bundle = Bundle()
//            bundle.putString(
//                    AppConstants.IMAGE_URL,
//                    contact.imageUrl
//            )
//            bundle.putString(AppConstants.CONTACT_NAME, contact.name)
//            bundle.putString("chatHeaderId", contact.headerId)
//            bundle.putString("forUserId", currentUserId)
//            bundle.putString("otherUserId", contact.uid)
//            findNavController().navigate(R.id.chatScreenFragment, bundle)

            navigation.navigateToChatPage(
                    otherUserId = contact.uid!!,
                    headerId = contact.headerId ?: "",
                    otherUserName = contact.name ?: "",
                    otherUserProfilePicture = contact.imagePathInStorage ?: ""
            )
        }
    }

    override fun onContactSelected(selectedContactsCount: Int) {
        userSelectedLayout.isVisible = selectedContactsCount != 0
        selectedUserCountTV.text = "$selectedContactsCount Contacts ( selected )"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (this.requireContext().applicationContext as ChatModuleProvider)
                .provideChatModule()
                .inject(this)
        navigation.context = requireContext()

        setStyle(
                STYLE_NORMAL,
                android.R.style.Theme_Light_NoTitleBar_Fullscreen
        )
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_referesh -> {
                checkForPermissionElseSyncContacts()
                true
            }
            R.id.action_invite_friends -> {
                // findNavController().navigate(R.id.referrals_fragment)
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
}