package com.abhijai.gigschatdemo.contacts_module.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abhijai.gigschatdemo.contacts_module.models.ChatModel
import com.abhijai.gigschatdemo.contacts_module.models.ContactModel
import com.gigforce.app.utils.AppConstants

class ContactViewModel : ViewModel() {

    private var contactsMutableLiveData : MutableLiveData<List<ContactModel>> = MutableLiveData()
    private var chatsMutableLiveData : MutableLiveData<List<ChatModel>> = MutableLiveData()
    private val contactList : ArrayList<ContactModel> = ArrayList()
    private val chatList : ArrayList<ChatModel> = ArrayList()
    private lateinit var contactModel : ContactModel
    fun prepareList()
    {
        for (i in 0..AppConstants.DEFAULT_SEARCH_CATEGORIES.size-1)
        {
            if (i==0){
                 contactModel = ContactModel(
                    AppConstants.DEFAULT_SEARCH_CATEGORY_IMAGES[i],
                    AppConstants.DEFAULT_SEARCH_CATEGORIES[i],
                    "How can I help you?",
                    ""
                )
            }
            else{
                 contactModel = ContactModel(
                    AppConstants.DEFAULT_SEARCH_CATEGORY_IMAGES[i],
                    AppConstants.DEFAULT_SEARCH_CATEGORIES[i],
                    "${i+1}/6/2020",
                    "3:31 PM"
                )
            }
            contactList.add(contactModel)
        }
        contactsMutableLiveData.value = contactList
    }

//    fun prepareChatList(){
//        val chatModel1 = ChatModel(AppConstants.FROM_CONTACT_MSG_1,"6:00 AM",true)
//        val chatModel2 = ChatModel(AppConstants.TO_CONTACT_MSG_1,"6:30 AM",false)
//        val chatModel3 = ChatModel(AppConstants.FROM_CONTACT_MSG_2,"7:15 AM",true)
//        val chatModel4 = ChatModel(AppConstants.TO_CONTACT_MSG_2,"8:10 AM",false)
//        val chatModel5 = ChatModel(AppConstants.FROM_CONTACT_MSG_3,"8:45 AM",true)
//        chatList.add(chatModel1)
//        chatList.add(chatModel2)
//        chatList.add(chatModel3)
//        chatList.add(chatModel4)
//        chatList.add(chatModel5)
//
//        chatList.add(chatModel1)
//        chatList.add(chatModel2)
//        chatList.add(chatModel3)
//        chatList.add(chatModel4)
//        chatList.add(chatModel5)
//        chatsMutableLiveData.value = chatList
//    }

    fun prepareChatList(username:String){
//        val chatModel1 = ChatModel(AppConstants.FROM_CONTACT_MSG_1,"6:00 AM",true)
        val chatModel2 = ChatModel(AppConstants.TO_CONTACT_MSG_1,"6:30 AM",false)
        val chatModel3 = ChatModel("Hi $username, how did that happen?","7:15 AM",true)
        val chatModel4 = ChatModel(AppConstants.TO_CONTACT_MSG_2,"8:10 AM",false)
        val chatModel5 = ChatModel(AppConstants.FROM_CONTACT_MSG_3,"8:45 AM",true)
        val chatModel6 = ChatModel(AppConstants.TO_CONTACT_MSG_3,"8:55 AM",false)
        val chatModel7 = ChatModel(AppConstants.FROM_CONTACT_MSG_4,"9:00 AM",true)
        val chatModel8 = ChatModel(AppConstants.TO_CONTACT_MSG_4,"9:10 AM",false)

//        chatList.add(chatModel1)
        chatList.add(chatModel2)
        chatList.add(chatModel3)
        chatList.add(chatModel4)
        chatList.add(chatModel5)
        chatList.add(chatModel6)
        chatList.add(chatModel7)
        chatList.add(chatModel8)

//        chatList.add(chatModel1)
//        chatList.add(chatModel2)
//        chatList.add(chatModel3)
//        chatList.add(chatModel4)
//        chatList.add(chatModel5)
        chatsMutableLiveData.value = chatList
    }

    fun addNewMessageToTheList(msg:String, msgTime:String){
        val chatModel = ChatModel(
            msg,
            msgTime,
            false
        )
        chatList.add(chatModel)
        chatsMutableLiveData.value = chatList
    }

    fun getContactsLiveData():MutableLiveData<List<ContactModel>>{
        return contactsMutableLiveData
    }

    fun getChatLiveData() : MutableLiveData<List<ChatModel>>{
        return chatsMutableLiveData
    }



}