package com.gigforce.modules.feature_chat.screens

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.chat.ChatHeadersViewModel
import com.gigforce.common_ui.chat.models.ChatHeader
import com.gigforce.common_ui.chat.models.ChatListItemDataObject
import com.gigforce.common_ui.chat.models.ChatListItemDataWrapper
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
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
class ChatHeadersFragment : Fragment(), GigforceToolbar.SearchTextChangeListener {

    @Inject
    lateinit var navigation: INavigation

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    private val viewModel: ChatHeadersViewModel by viewModels()


    private lateinit var contactsFab: FloatingActionButton
    private lateinit var noChatsLayout: View
    private lateinit var contactsButton: Button
    private lateinit var toolbar: GigforceToolbar
    private lateinit var coreRecyclerView: CoreRecyclerView

    private var sharedFileSubmitted = false

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

    private fun handleBackPress() {

    }

    private fun setObserver(owner: LifecycleOwner) {
        Log.d("chat/header/fragment", "UserId " + FirebaseAuth.getInstance().currentUser!!.uid)
        viewModel.chatHeaders.observe(owner, Observer {
            Log.d("chat/header/fragment", "Observer Triggered")
            Log.d("chat/header/fragment", it.toString())
            this.setCollectionData(ArrayList(it))
        })

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
                    viewModel = viewModel
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getDataFrom(arguments, savedInstanceState)
        findViews(view)
        initListeners()
        setObserver(this.viewLifecycleOwner)

        if (!isStoragePermissionGranted())
            askForStoragePermission()

        super.onViewCreated(view, savedInstanceState)
    }

    var title = ""
    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {

            if (!sharedFileSubmitted) {
                val sharedFilesBundle =
                    it.getBundle(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE)
                viewModel.sharedFiles = sharedFilesBundle
                sharedFileSubmitted = true
            }
            title = it.getString("title") ?: ""

        }
    }

    private fun findViews(view: View) {
        contactsFab = view.findViewById(R.id.contactsFab)
        contactsFab.setOnClickListener {

            chatNavigation.navigateToContactsPage(
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to viewModel.sharedFiles)
            )
            viewModel.sharedFiles = null
        }

        coreRecyclerView = view.findViewById(R.id.rv_chat_headers)
        noChatsLayout = view.findViewById(R.id.no_chat_layout)
        contactsButton = view.findViewById(R.id.go_to_contacts_btn)
        toolbar = view.findViewById(R.id.toolbar)

        contactsButton.isEnabled = true
        contactsButton.setOnClickListener {

            chatNavigation.navigateToContactsPage(
                bundleOf(ChatPageFragment.INTENT_EXTRA_SHARED_FILES_BUNDLE to viewModel.sharedFiles)
            )
            viewModel.sharedFiles = null
        }

        if (title.isNotBlank()) {
            toolbar.showTitle(title)
        } else {
            toolbar.showTitle(getString(R.string.chats_chat))

        }

        toolbar.hideActionMenu()
        toolbar.setBackButtonListener {

            if (toolbar.isSearchCurrentlyShown) {
                hideSoftKeyboard()
            } else if (sharedFileSubmitted) {
                navigation.navigateTo("common/landingScreen")
            } else {
                backPressHandler.isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

    private fun initListeners() {
        toolbar.showSearchOption(getString(R.string.search_chat_chat))

        lifecycleScope.launch {
            toolbar.getSearchTextChangeAsFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { searchString ->
                    Log.d("Search ", "Searhcingg...$searchString")
                    viewModel.filterChatList(searchString)
                }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressHandler
        )
    }

    private fun askForStoragePermission() {
        Log.v(ChatPageFragment.TAG, "Permission Required. Requesting Permission")
        requestPermissions(
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ),
            23
        )
    }

    private fun isStoragePermissionGranted(): Boolean {

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

    override fun onSearchTextChanged(newText: String) {
        viewModel.filterChatList(newText)

//        if (newText.isBlank()) {
//            coreRecyclerView.resetFilter()
//        } else {
//            coreRecyclerView.filter {
//
//                val itemWrapper = it as ChatListItemDataWrapper
//                val item = itemWrapper.chatItem
//                item.groupName.contains(
//                    newText, true
//                ) || item.title.contains(
//                    newText, true
//                ) || item.subtitle.contains(
//                    newText, true
//                )
//
//            }
//        }
    }

    private fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    companion object {
        const val INTENT_EXTRA_SHARED_FILE_DEPLOYED_TO_ITEMS_ONCE = "deployed_once"
    }
}