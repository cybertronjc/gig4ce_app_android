package com.gigforce.modules.feature_chat.screens

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gigforce.common_ui.chat.ChatHeadersViewModel
import com.gigforce.common_ui.chat.models.ChatHeader
import com.gigforce.common_ui.chat.models.ChatListItemDataObject
import com.gigforce.common_ui.chat.models.ChatListItemDataWrapper
import com.gigforce.common_ui.components.cells.AppBar
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.documentFileHelper.DocumentTreeDelegate
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.core.utils.GlideApp
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [ChatHeadersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ChatHeadersFragment : Fragment(), PopupMenu.OnMenuItemClickListener, GigforceToolbar.SearchTextChangeListener {

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private val viewModel: ChatHeadersViewModel by viewModels()

    private lateinit var contactsFab: FloatingActionButton
    private lateinit var noChatsLayout: View
//    private lateinit var contactsButton: Button
    private lateinit var startChatting: TextView
    private lateinit var noChatGif: ImageView
    private lateinit var toolbar: AppBar
    private lateinit var coreRecyclerView: CoreRecyclerView

    private lateinit var moreChatOptionsLayout: View
    private lateinit var muteNotifications: TextView
    private lateinit var markAsRead: TextView
    private lateinit var deleteButton: TextView


    private var sharedFileSubmitted = false
    private var isMultiSelectEnable = false
    private var unreadHeaderIds = arrayListOf<String>()
    private var readHeaderIds = arrayListOf<String>()

    var anyUnreadMessages = false
    var anyReadMessages = false
    var searchText = ""

    private val backPressHandler = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
            if (toolbar.isSearchCurrentlyShown) {
                hideSoftKeyboard()
            } else if (sharedFileSubmitted) {
                navigation.navigateTo("common/landingScreen")
            } else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }
    

    private fun setObserver(owner: LifecycleOwner) {
        Log.d("chat/header/fragment", "UserId " + FirebaseAuth.getInstance().currentUser!!.uid)
        viewModel.chatHeaders.observe(owner, Observer {
            Log.d("chat/header/fragment", "Observer Triggered")
            Log.d("chat/header/fragment", it.toString())
            this.setCollectionData(ArrayList(it))
        })

        viewModel.isMultiSelectEnable.observe(viewLifecycleOwner, Observer {
            // multiselect ui changes
            if (it){
                makeMultiSelectUiEnable(true)
            }else{
                makeMultiSelectUiEnable(false)
            }
        })

        viewModel.selectedChats.observe(viewLifecycleOwner, Observer {
            val selectedChatList = it ?: return@Observer
            makeSelectedChatListEnable(selectedChatList)
        })
    }

    private fun makeSelectedChatListEnable(selectedChatList: java.util.ArrayList<ChatListItemDataObject>) {
        if (isMultiSelectEnable){
            if (selectedChatList.size > 1){
                toolbar.setAppBarTitle("${selectedChatList.size}  ${getString(R.string.chat_selected_chat)}")
            } else{
                toolbar.setAppBarTitle("${selectedChatList.size}  ${getString(R.string.single_chat_selected_chat)}")
            }

            unreadHeaderIds.clear()
            readHeaderIds.clear()
            selectedChatList.forEach {
                if (it.unreadCount != 0){
                    unreadHeaderIds.add(it.id)
                } else{
                    readHeaderIds.add(it.id)
                }
            }
            if (unreadHeaderIds.size > 0){
                //atleast one unread chat is there
                markAsRead.setTextColor(resources.getColor(R.color.chat_switch_checked))
                markAsRead.isEnabled = true
            } else{
                markAsRead.setTextColor(resources.getColor(R.color.gray_text_color))
                markAsRead.isEnabled = false
            }
        } else{
            toolbar.setAppBarTitle(getString(R.string.community_chat))
        }

    }

    private fun makeMultiSelectUiEnable(b: Boolean) {
        if (b){
            moreChatOptionsLayout.visible()
            contactsFab.gone()
            isMultiSelectEnable = true
            coreRecyclerView.clipToPadding = true
            toolbar.backImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_close_24))
            //toolbar.setAppBarTitle("0  ${getString(R.string.chat_selected_chat)}")
            val countSelected = viewModel.getSelectedChats().size
            if (countSelected > 1){
                toolbar.setAppBarTitle("$countSelected  ${getString(R.string.chat_selected_chat)}")
            }else {
                toolbar.setAppBarTitle("$countSelected  ${getString(R.string.single_chat_selected_chat)}")
            }

        }else{
            isMultiSelectEnable = false
            moreChatOptionsLayout.gone()
            contactsFab.visible()
            coreRecyclerView.clipToPadding = false
            toolbar.backImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_chevron))
            toolbar.setAppBarTitle(getString(R.string.community_chat))
            //toolbar.setAppBarTitle("${getString(R.string.community_chat)}")
        }
    }

    private fun setCollectionData(list: ArrayList<ChatHeader>) {
        noChatsLayout.isVisible = list.isEmpty()

        coreRecyclerView.collection =
            ArrayList(list.map {

                var timeToDisplayText = ""
                it.lastMsgTimestamp?.let {
                    val chatDate = it.toDate()
                    timeToDisplayText =
                        if (DateUtils.isToday(chatDate.time)) SimpleDateFormat("hh:mm aa").format(
                            chatDate
                        ) else SimpleDateFormat("dd MMM").format(chatDate)
                }

                ChatListItemDataWrapper(
                    chatItem = ChatListItemDataObject(
                        id = it.id,
                        title = it.otherUser?.name ?: "",
                        subtitle = it.lastMsgText,
                        timeDisplay = timeToDisplayText,
                        type = it.chatType,
                        profilePath = it.otherUser?.profilePic ?: "",
                        unreadCount = it.unseenCount,
                        profileId = it.otherUserId,
                        isOtherUserOnline = it.isOtherUserOnline,
                        groupName = it.groupName,
                        groupAvatar = it.groupAvatar,
                        lastMessage = it.lastMsgText,
                        lastMessageType = it.lastMessageType,
                        lastMsgFlowType = it.lastMsgFlowType,
                        chatType = it.chatType,
                        status = it.status,
                        lastMessageDeleted = it.lastMessageDeleted,
                        senderName = it.senderName
                    ),
                    viewModel = viewModel,
                    searchText
                )
            })

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        cancelAnyNotificationIfShown()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )

        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }


    private fun cancelAnyNotificationIfShown() {
        val mNotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(67)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val directoryUri = data?.data ?: return

            requireContext().contentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFrom(arguments, savedInstanceState)
        findViews(view)
        initListeners()
        setObserver(this.viewLifecycleOwner)

        if (!isStoragePermissionGranted()) {
            askForStoragePermission()
        }
    }

    var title = ""
    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            title = it.getString("title") ?: ""

            if (title.isEmpty() && !sharedFileSubmitted) {
                val sharedFilesBundle =
                    it.getBundle(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE)
                viewModel.sharedFiles = sharedFilesBundle
                sharedFileSubmitted = true
            }


        }
    }

    private fun findViews(view: View) {
        contactsFab = view.findViewById(R.id.contactsFab)
        contactsFab.setOnClickListener {
            navigation.navigateTo("chats/contactsFragment",
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to viewModel.sharedFiles)
            )
            viewModel.sharedFiles = null
        }

        coreRecyclerView = view.findViewById(R.id.rv_chat_headers)
        noChatsLayout = view.findViewById(R.id.no_chat_layout)
//        contactsButton = view.findViewById(R.id.go_to_contacts_btn)
        startChatting = view.findViewById(R.id.start_chatting)
        noChatGif = view.findViewById(R.id.no_chat_gif)
        toolbar = view.findViewById(R.id.toolbar)
//        needStorageAccessLayout = view.findViewById(R.id.storage_access_required_layout)
//        grantStorageAccessButton = view.findViewById(R.id.storage_access_btn)
        moreChatOptionsLayout = view.findViewById(R.id.chat_options)
        muteNotifications = view.findViewById(R.id.muteNotifications)
        markAsRead = view.findViewById(R.id.markAsRead)
        deleteButton = view.findViewById(R.id.deleteChat)

//        grantStorageAccessButton.setOnClickListener {
//
//            if (!documentTreeDelegate.storageTreeSelected()) {
//                openDocumentTreeContract.launch(null)
//            }
//        }
        Glide.with(this)
            .asGif()
            .load(R.drawable.no_chat)
            .into(noChatGif)


        startChatting.isEnabled = true
        startChatting.setOnClickListener {
//            chatNavigation.navigateToContactsPage(
//                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to viewModel.sharedFiles)
//            )
            navigation.navigateTo("chats/contactsFragment",
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to viewModel.sharedFiles,
                    ContactsAndGroupFragment.INTENT_EXTRA_NEW_GROUP to false
                )
            )
            viewModel.sharedFiles = null
        }


        if (title.isNotBlank()) {
            toolbar.setAppBarTitle(title)
        } else {
            toolbar.setAppBarTitle(getString(R.string.community_chat))

        }

        toolbar.apply {
            changeBackButtonDrawable()
            makeBackgroundMoreRound()
            makeTitleBold()
        }

//        toolbar.hideActionMenu()
        toolbar.setBackButtonListener {

            if (toolbar.isSearchCurrentlyShown) {
                hideSoftKeyboard()
            } else if (sharedFileSubmitted) {
                navigation.navigateTo("common/landingScreen")
            } else if(isMultiSelectEnable){
                viewModel.setMultiSelectEnable(false)
                viewModel.clearSelectedChats()
                makeMultiSelectUiEnable(false)

            } else {
                backPressHandler.isEnabled = false
                activity?.onBackPressed()
            }
        }
        toolbar.setOnOpenActionMenuItemClickListener(View.OnClickListener {
            val ctw = ContextThemeWrapper(context, R.style.PopupMenuChat)
            val popUp = PopupMenu(ctw, toolbar.getOptionMenuViewForAnchor(), Gravity.END)
            popUp.setOnMenuItemClickListener(this)
            popUp.inflate(R.menu.menu_chat_settings)
//            popUp.menu.findItem(R.id.action_block).title =
//                if (chatFooter.isTypingEnabled())
//                    getString(R.string.block_chat)
//                else
//                    getString(R.string.unblock_chat)
            popUp.show()
        })
    }

    private fun initListeners() {
        lifecycleScope.launch {
            toolbar.search_item.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { searchString ->
                    Log.d("Search ", "Searhcingg...$searchString")
                    searchText = searchString
                    viewModel.filterChatList(searchString)
                }
        }
//        toolbar.showSearchOption(getString(R.string.search_chat_chat))
//
//        lifecycleScope.launch {
//            toolbar.getSearchTextChangeAsFlow()
//                .debounce(300)
//                .distinctUntilChanged()
//                .flowOn(Dispatchers.Default)
//                .collect { searchString ->
//                    Log.d("Search ", "Searhcingg...$searchString")
//                    viewModel.filterChatList(searchString)
//                }
//        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressHandler
        )

        markAsRead.setOnClickListener {
            // make mark as read
            Log.d("unreadIds", "$unreadHeaderIds")
            viewModel.setHeadersMarkAsRead(unreadHeaderIds)
            viewModel.setMultiSelectEnable(false)
            viewModel.clearSelectedChats()
            makeMultiSelectUiEnable(false)

        }
    }

    private fun askForStoragePermission() {
        Log.v(ChatPageFragment.TAG, "Permission Required. Requesting Permission")

        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA
                ),
                23
            )
        } else {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA
                ),
                23
            )
        }
    }

    private fun isStoragePermissionGranted(): Boolean {

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onSearchTextChanged(newText: String) {
        viewModel.filterChatList(newText)
    }

    private fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    companion object {
        private const val OPEN_DIRECTORY_REQUEST_CODE = 0xf11e
        const val INTENT_EXTRA_SHARED_FILE_DEPLOYED_TO_ITEMS_ONCE = "deployed_once"
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean = when (item?.itemId){
        R.id.action_chat_settings -> {
            //redirect to chat settings
            navigation.navigateTo("chats/chatSettings")
            true
        }
        R.id.action_select_chat -> {
            //multi select enable ui
            isMultiSelectEnable = true
            makeMultiSelectUiEnable(true)
            viewModel.setMultiSelectEnable(enable = true)
            true
        }
        R.id.action_new_group -> {
            //go to contacts screenext
            navigation.navigateTo("chats/contactsFragment",
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to viewModel.sharedFiles,
                    ContactsAndGroupFragment.INTENT_EXTRA_NEW_GROUP to true
                )
            )
            viewModel.sharedFiles = null
            true
        }
        else -> {
            false
        }
    }
}