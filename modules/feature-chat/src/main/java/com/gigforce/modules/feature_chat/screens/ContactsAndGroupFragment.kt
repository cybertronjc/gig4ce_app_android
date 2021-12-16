package com.gigforce.modules.feature_chat.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.loader.content.CursorLoader
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_image_picker.ClickOrSelectImageBottomSheet
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity.Companion.EXTENSION
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity.Companion.PREFIX
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.ContactModel
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.AppConstants
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.datamodels.profile.Contact
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.ContactsAndGroupFragmentBinding
import com.gigforce.modules.feature_chat.screens.adapters.ContactsRecyclerAdapter
import com.gigforce.modules.feature_chat.screens.adapters.OnContactClickListener
import com.gigforce.modules.feature_chat.screens.vm.GroupChatViewModel
import com.gigforce.modules.feature_chat.screens.vm.NewContactsViewModel
import com.gigforce.modules.feature_chat.screens.vm.factories.GroupChatViewModelFactory
import com.gigforce.modules.feature_chat.screens.vm.factories.NewContactsViewModelFactory
import com.gigforce.modules.feature_chat.service.SyncContactsService
import com.google.android.gms.common.util.SharedPreferencesUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ContactsAndGroupFragment : BaseFragment2<ContactsAndGroupFragmentBinding>(
    fragmentName = "ContactsAndGroupFragment",
    layoutId = R.layout.contacts_and_group_fragment,
    statusBarColor = R.color.lipstick_2
), OnContactClickListener, ClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    companion object {
        fun newInstance() = ContactsAndGroupFragment()
        const val TAG = "ContactsAndGroupFragment"

        const val INTENT_EXTRA_RETURN_SELECTED_RESULTS = "return_selected_results"
        const val INTENT_EXTRA_NEW_GROUP = "new_group"
        private const val REQUEST_CAPTURE_IMAGE = 1011
        private const val REQUEST_PICK_IMAGE = 1012
        const val REQUEST_CONTACTS_PERMISSION = 101
        const val REQUEST_CONTACTS_PERMISSION_WITH_API_CALL = 102
        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    //private lateinit var viewModel: ContactsAndGroupViewModel

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private var clickedImagePath: String? = null
    var fileName: String = ""

    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val chatGroupViewModel: GroupChatViewModel by lazy {
        ViewModelProvider(
            this,
            GroupChatViewModelFactory(requireContext())
        ).get(GroupChatViewModel::class.java)
    }

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private val viewModelNew: NewContactsViewModel by lazy {
        ViewModelProvider(
            this,
            NewContactsViewModelFactory(requireContext())
        ).get(NewContactsViewModel::class.java)
    }


    private var onContactSelectedListener: OnContactsSelectedListener? = null

    private val contactsAdapter: ContactsRecyclerAdapter by lazy {
        ContactsRecyclerAdapter(requireContext(), Glide.with(requireContext()), this)
    }

    private var sharedFilesBundle: Bundle? = null
    private var shouldReturnToPreviousScreen = false

    private var permissionSnackBar: Snackbar? = null

    private var namingGroup: Boolean = false
    private var creatingGroup = false


    private val onBackPressCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {

            hideSoftKeyboard()
            if (shouldReturnToPreviousScreen) {
                //dismiss()
            } else {

                if (viewBinding.appBarComp.isSearchCurrentlyShown) {
//                    searchGigersLayout.gone()
//
//                    contactsToolbarLabel.visible()
//                    backArrow.visible()
//                    contactsToolbarSubTitle.visible()
//
//                    searchET.text = null
                    Log.d(TAG, "search is being shown")

                } else if (contactsAdapter.isStateCreateGroup()) {

                    contactsAdapter.stateCreateGroup(false)
                    contactsAdapter.clearSelectedContacts()
                    viewBinding.groupAndBroadcastLayout.visible()
                    viewBinding.appBarComp.makeRefreshVisible(true)
                    viewBinding.appBarComp.showSubtitle("${contactsAdapter.itemCount} ${getString(R.string.contacts_with_space)}")
                    viewBinding.createGroupFab.gone()

                } else if (namingGroup) {
                    viewBinding.nameGroupLayout.root.gone()
                    viewBinding.rvContactsList.visible()
                    viewBinding.createGroupFab.visible()
                    namingGroup = false
                    viewBinding.appBarComp.makeRefreshVisible(false)
                    contactsAdapter.stateCreateGroup(true)
                    viewBinding.appBarComp.makeRefreshVisible(true)
                    viewBinding.appBarComp.makeSearchVisible(true)
                    viewBinding.appBarComp.showSubtitle("${contactsAdapter.itemCount} ${getString(R.string.contacts_with_space)}")
                    viewBinding.createGroupFab.visible()
                } else {
                    isEnabled = false
                    activity?.onBackPressed()
                }
        }
        }
    }

    override fun viewCreated(
        viewBinding: ContactsAndGroupFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFromIntents(arguments, savedInstanceState)
        initToolbar()
        setListeners()
        //setClickListeners()
        initViewModel()

        shouldShowSyncContactsBottomSheet()
    }

    private fun initViewModel() {
        viewModelNew.contacts
            .observe(viewLifecycleOwner, Observer {
                sharedPreAndCommonUtilInterface.saveDataBoolean(AppConstants.CONTACTS_SYNCED, true)
                showContactsOnView(it)
            })

        chatGroupViewModel.createGroup
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                        viewBinding.nameGroupLayout.progressBar.visible()
                    }
                    is Lce.Content -> {
                        viewBinding.nameGroupLayout.progressBar.gone()
                        showToast(getString(R.string.group_created_chat))
                        chatNavigation.navigateToGroupChat(
                            headerId = it.content.toString()
                        )
                    }
                    is Lce.Error -> {
                        viewBinding.nameGroupLayout.progressBar.gone()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_chat))
                            .setMessage(getString(R.string.unable_to_create_group_chat) + it.error)
                            .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                            .show()
                    }
                }
            })
    }

    private fun showContactsOnView(it: List<ContactModel>?) {
        val contacts = it ?: return

        if (it.isEmpty()) {
            Log.d("synced", "contacts empty")

            val cursor = CursorLoader(
                requireContext(),
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
                viewBinding.noContactsLayout.gone()
                viewBinding.contactsSyncingLayout.visible()
            } else {
                viewBinding.appBarComp.showSubtitle("${contacts.size} ${getString(R.string.contacts_with_space)}")
                contactsAdapter.setData(contacts)
                viewBinding.noContactsLayout.visible()
                viewBinding.contactsSyncingLayout.gone()
            }
        } else {
            viewBinding.contactsSyncingLayout.gone()
            viewBinding.noContactsLayout.gone()
            //viewBinding.appBarComp.showSubtitle("$selectedContactsCount of $totalContactsCount Contact(s) Selected")
            contactsAdapter.setData(contacts)
        }
        viewBinding.processingContactsHorizontalProgressbar.gone()
        viewBinding.processingContactsProgressbar.gone()
    }


    private fun initToolbar() = viewBinding.apply {
        appBarComp.apply {
            changeBackButtonDrawable()
            makeBackgroundMoreRound()
            makeTitleBold()
            Log.d(TAG, "initToolbar: initToolbar")
        }
    }

    private fun setListeners()  {
        Glide.with(this)
            .asGif()
            .load(R.drawable.sync)
            .into(viewBinding.syncGif)

        Log.d(TAG, "initToolbar: setListeners")

        viewBinding.apply {
            this.rvContactsList.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
            )
            this.rvContactsList.adapter = contactsAdapter

            appBarComp.refreshImageButton.setOnClickListener {
                checkForPermissionElseSyncContacts(true)
            }

            newGroupLayout.setOnClickListener {
                stateCreateNewGroup()
            }

            nameGroupLayout.groupIcon.setOnClickListener {
                //check permissions for taking/selecting picture
                checkForPermissionElseShowCameraGalleryBottomSheet()
            }

            appBarComp.setBackButtonListener(View.OnClickListener {
//                if (searchGigersLayout.isVisible) {
//                    searchGigersLayout.gone()
//
//                    contactsToolbarLabel.visible()
//                    backArrow.visible()
//                } else {

                    if (shouldReturnToPreviousScreen)
                        //dismiss()
                    else
                        activity?.onBackPressed()
                //}
            })

            lifecycleScope.launch {
                appBarComp.search_item.getTextChangeAsStateFlow()
                    .debounce(300)
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect { searchString ->
                        Log.d("Search ", "Searhcingg...$searchString")
                        contactsAdapter.filter.filter(searchString)
                    }
            }

            if (creatingGroup){
                stateCreateNewGroup()
            }

        createGroupFab.setOnClickListener {
            if (contactsAdapter.getSelectedContact().isEmpty()) {
                showToast(getString(R.string.select_at_least_one_contact_chat))
                return@setOnClickListener
            }
            nameGroupLayout.root.visible()
            rvContactsList.gone()
            createGroupFab.gone()
            appBarComp.makeRefreshVisible(false)
            contactsAdapter.stateCreateGroup(false)
            namingGroup = true
            appBarComp.showSubtitle(getString(R.string.add_subject_chat))
            appBarComp.makeSearchVisible(false)
            showParticipantsInLinearLayout(contactsAdapter.getSelectedContact())

        }
            nameGroupLayout.createAndSend.setOnClickListener {
                if(nameGroupLayout.groupNameEdit.text.isEmpty()){
                    showToast(getString(R.string.enter_a_group_name_chat))
                }else{
                    chatGroupViewModel.createGroup(
                        groupName = nameGroupLayout.groupNameEdit.text.toString().capitalize(),
                        groupAvatar = clickedImagePath,
                        groupMembers = contactsAdapter.getSelectedContact()
                    )
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressCallback
        )
        }


    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            sharedFilesBundle = it.getBundle(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE)
            shouldReturnToPreviousScreen = it.getBoolean(ContactsFragment.INTENT_EXTRA_RETURN_SELECTED_RESULTS)
            creatingGroup = it.getBoolean(INTENT_EXTRA_NEW_GROUP)
        }

        savedInstanceState?.let {
            sharedFilesBundle = it.getBundle(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE)
            shouldReturnToPreviousScreen = it.getBoolean(ContactsFragment.INTENT_EXTRA_RETURN_SELECTED_RESULTS)
            creatingGroup = it.getBoolean(INTENT_EXTRA_NEW_GROUP)
        }
    }


    override fun contactClick(contact: ContactModel) {
        if (shouldReturnToPreviousScreen) {
            onContactSelectedListener?.onContactsSelected(listOf(contact))
            //dismiss()
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

    override fun onContactSelected(selectedContactsCount: Int, totalContactsCount: Int) {
        onBackPressCallback.isEnabled = true
        contactsAdapter.stateCreateGroup(true)

        //userSelectedLayout.isVisible = selectedContactsCount != 0
        //contactsToolbarSubTitle.text = "$selectedContactsCount of $totalContactsCount Contact(s) Selected"
        viewBinding.appBarComp.showSubtitle("$selectedContactsCount of $totalContactsCount Contact(s) Selected")
    }

    fun showParticipantsInLinearLayout(selectedContacts: List<ContactModel>){
        viewBinding.nameGroupLayout.participantsCount.text = "Participants: ${selectedContacts.size}"
        viewBinding.nameGroupLayout.contactsLayout.removeAllViews()
        selectedContacts.forEach {
            Log.d("selected", "$it")
            val view = LayoutInflater.from(context).inflate(R.layout.layout_photo_name_contact, null)

            val profileImage: GigforceImageView = view.findViewById(R.id.user_profile_image)
            val profileUserName: TextView = view.findViewById(R.id.user_profile_name)
            //context?.let { it1 -> GlideApp.with(it1).load(it.imageUrl).into(profileImage) }

            if (!it.imageThumbnailPathInStorage.isNullOrBlank()) {

                val profilePathRef = if (it.imageThumbnailPathInStorage!!.startsWith("profile_pics/"))
                    firebaseStorage.reference.child(it.imageThumbnailPathInStorage!!)
                else
                    firebaseStorage.reference.child("profile_pics/${it.imageThumbnailPathInStorage!!}")

                Glide.with(requireContext())
                    .load(profilePathRef)
                    .placeholder(R.drawable.ic_user_2)
                    .circleCrop()
                    .into(profileImage)
            } else if (!it.imagePathInStorage.isNullOrBlank()) {

                val profilePathRef = if (it.imagePathInStorage!!.startsWith("profile_pics/"))
                    firebaseStorage.reference.child(it.imagePathInStorage!!)
                else
                    firebaseStorage.reference.child("profile_pics/${it.imagePathInStorage!!}")

                Glide.with(requireContext())
                    .load(profilePathRef)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_2)
                    .into(profileImage)
            } else if (!it.imageUrl.isNullOrBlank()) {
                profileImage.loadImageIfUrlElseTryFirebaseStorage(it.imageUrl!!)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_user_2)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_2)
                    .into(profileImage)
            }
            profileUserName.text = it.name

            viewBinding.nameGroupLayout.contactsLayout.addView(view)

        }


    }

    private fun shouldShowSyncContactsBottomSheet(){
            val contactsSynced = sharedPreAndCommonUtilInterface.getDataBoolean(AppConstants.CONTACTS_SYNCED)
            if (contactsSynced == true){
                checkForPermissionElseSyncContacts(false)
            } else{
                SyncContactsBottomSheetFragment.launch(
                    childFragmentManager
                )
                childFragmentManager.setFragmentResultListener("sync", viewLifecycleOwner) { key, bundle ->
                    val result = bundle.getInt("sync")
                    Log.d("sync", "$result")
                    if (result == 0){
                        sharedPreAndCommonUtilInterface.saveDataBoolean(AppConstants.CONTACTS_SYNCED, false)
                    }else{
                        checkForPermissionElseSyncContacts(false)
                    }
                }
            }
        }

    private fun checkForPermissionElseSyncContacts(
        shouldCallSyncApiOnDataUpload : Boolean
    ) {
        // check for permissions
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_CONTACTS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(ContactsFragment.TAG, "Permission Required. Requesting Permission")
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    if(shouldCallSyncApiOnDataUpload) ContactsAndGroupFragment.REQUEST_CONTACTS_PERMISSION_WITH_API_CALL else ContactsAndGroupFragment.REQUEST_CONTACTS_PERMISSION
                )
                showPermissionLayout()
            } else {
                startLoaderForGettingContacts(shouldCallSyncApiOnDataUpload)
                viewModelNew.startListeningForContactChanges()
            }

        }

    private fun startLoaderForGettingContacts(
        shouldCallSyncAPI : Boolean
    ) {
        viewBinding.contactsPermissionLayout.gone()
        showToast(getString(R.string.refreshing_chat))

        Log.d("refreshing", "refreshing")
        requireContext().startService(
            Intent(requireContext(),
                SyncContactsService::class.java
            ).apply {
                putExtra(SyncContactsService.SHOULD_CALL_SYNC_API,shouldCallSyncAPI)
            }
        )
    }

    fun stateCreateNewGroup() {
        viewBinding.apply {
            appBarComp.showSubtitle(getString(R.string.select_members_chat))
            appBarComp.setAppBarTitle(getString(R.string.create_new_group_chat))
            groupAndBroadcastLayout.gone()
            appBarComp.makeRefreshVisible(false)
            contactsAdapter.getSelectedItems().clear()
            contactsAdapter.notifyDataSetChanged()
            createGroupFab.show()
            contactsAdapter.stateCreateGroup(true)

            onBackPressCallback.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ContactsFragment.REQUEST_CONTACTS_PERMISSION ||
            requestCode == ContactsFragment.REQUEST_CONTACTS_PERMISSION_WITH_API_CALL
        ) {
            var allPermsGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermsGranted = false
                    break
                }
            }

            if (allPermsGranted) {

                if (contactsAdapter.itemCount == 0) {
                    viewBinding.contactsSyncingLayout.visible()
                }

                viewModelNew.startListeningForContactChanges()
                startLoaderForGettingContacts(requestCode == ContactsFragment.REQUEST_CONTACTS_PERMISSION_WITH_API_CALL)
            }
        }else if (requestCode == REQUEST_STORAGE_PERMISSION){
            var allPermsGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermsGranted = false
                    break
                }
            }
            if (allPermsGranted){
                ClickOrSelectImageBottomSheet.launch(
                    parentFragmentManager,
                    false,
                    this
                )
            }else {
                showToast("Please allow storage permissions")
            }
        }
    }

    private fun showPermissionLayout() {
        if (contactsAdapter.itemCount != 0) {

            if (permissionSnackBar == null) {
                permissionSnackBar = Snackbar.make(
                    viewBinding.root.rootView,
                    getString(R.string.grant_contacts_permission_chat),
                    Snackbar.LENGTH_INDEFINITE
                )
                permissionSnackBar?.setAction(getString(R.string.okay_chat)) {
                    startAppSettingsPage()
                }
            }

            permissionSnackBar?.show()
        } else {
            viewBinding.contactsPermissionLayout.visible()
        }
    }

    private fun startAppSettingsPage() {
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            data = uri
            startActivityForResult(this, ContactsFragment.REQUEST_CONTACTS_PERMISSION)
        }
    }

    fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()?.getWindowToken(), 0)
    }

    private fun openSoftKeyboard(view: View) {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
            view.applicationWindowToken,
            InputMethod.SHOW_FORCED,
            0
        )
    }

    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            ClickOrSelectImageBottomSheet.launch(
                parentFragmentManager,
                false,
                this
            )
        else
            requestStoragePermission()
    }

    private fun hasStoragePermissions(): Boolean {

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )

        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(requireContext(), resultCode, data)
            if (outputFileUri != null) {
                Log.d("image", "$outputFileUri")
                startCropImage(outputFileUri)
            }


        } else if (requestCode == ImageCropActivity.CROP_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? =
                Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
                //startCropImage(imageUriResultCrop!!)
            imageUriResultCrop?.let {
                viewBinding.nameGroupLayout.progressBarGroupIcon.visible()
                viewBinding.nameGroupLayout.groupIcon.invisible()
                val timeStamp = SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(Date())
                val imageFileName = PREFIX + "_" + timeStamp + "_"
                fileName = imageFileName + EXTENSION
                firebaseStorage.reference
                    .child("chat_attachments/group")
                    .child(fileName)
                    .putFile(it).addOnSuccessListener {

                        if (imageUriResultCrop != null) {
                            clickedImagePath = it.metadata?.path
                            Log.d("groupAvatar", "$clickedImagePath")
                            showGroupIcon(imageUriResultCrop!!)
                        }
                    }.addOnFailureListener {
                        viewBinding.nameGroupLayout.progressBarGroupIcon.gone()
                        viewBinding.nameGroupLayout.groupIcon.visible()
                    }.addOnCanceledListener { viewBinding.nameGroupLayout.progressBar.gone() }
            }
            }


        }

    override fun onClickPictureThroughCameraClicked() {
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        val intents = ImagePicker.getPickImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }

    override fun removeProfilePic() {

    }

    private fun startCropImage(imageUri: Uri): Unit {
        Log.d("image", "crop started")
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = PREFIX + "_" + timeStamp + "_"
        fileName = imageFileName + EXTENSION
        val photoCropIntent = Intent(context, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)
    }


    private fun showGroupIcon(imagePath: Uri) {
        viewBinding.nameGroupLayout.progressBarGroupIcon.gone()
        viewBinding.nameGroupLayout.groupIcon.visible()
        Log.d("groupImageIcon", "$imagePath")
        GlideApp.with(this).load(imagePath).circleCrop().into(viewBinding.nameGroupLayout.groupIcon)
    }
}