package com.gigforce.giger_app.calendarscreen.maincalendarscreen.bottomsheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.StringConstants
import com.gigforce.core.AppConstants
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.launch

class BSCalendarScreenViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private var disposable: CompositeDisposable? = CompositeDisposable()
    private val firebaseAuthStateListener: FirebaseAuthStateListener by lazy {
        FirebaseAuthStateListener.getInstance()
    }

    val bsCalendarScreenRepository = BsCalendarScreenRepository()
    private val _observableChatInfo: MutableLiveData<Gig> = MutableLiveData()
    val observableChatInfo: MutableLiveData<Gig> = _observableChatInfo

    init {
        viewModelScope.launch { prepareMenus() }
    }


    fun checkForChatProfile(gig: Gig): BehaviorSubject<Gig> {
        val subject: BehaviorSubject<Gig> = BehaviorSubject.create()
        if (gig.gigContactDetails == null) {
            subject.onNext(gig)
            return subject
        }

        var mobileNumber = gig.gigContactDetails?.contactNumberString
        if (mobileNumber?.contains("+91") == false) {
            mobileNumber = "+91$mobileNumber"

        }
        bsCalendarScreenRepository.getCollectionReference()
            .whereEqualTo("loginMobile", mobileNumber).addSnapshotListener { success, err ->
                run {
                    if (!success?.documents.isNullOrEmpty()) {
                        val toObject = success?.documents?.get(0)?.toObject(ProfileData::class.java)
                        gig.chatInfo = mapOf(
                            AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE to (toObject?.profileAvatarName
                                ?: ""),
                            AppConstants.INTENT_EXTRA_OTHER_USER_NAME to (toObject?.name ?: ""),
                            AppConstants.INTENT_EXTRA_CHAT_HEADER_ID to "",
                            AppConstants.INTENT_EXTRA_OTHER_USER_ID to (success?.documents?.get(0)?.id
                                ?: ""),
                            StringConstants.MOBILE_NUMBER.value to (toObject?.loginMobile ?: ""),
                            StringConstants.FROM_CLIENT_ACTIVATON.value to true

                        )


                    }
                }
            }

        return subject


    }

    fun getTeamLeadInfo(upcomingGigs: List<Gig>) {
        val observable: Observable<List<Gig>>? = Observable.just(upcomingGigs)
        disposable?.add(observable?.flatMap {
            Observable.fromIterable(it)
        }?.flatMap { checkForChatProfile(it) }?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ success ->
                _observableChatInfo.value = success

            }, { err ->


            })
        !!
        )


    }


    private val _isUserTlCheck: MutableLiveData<Boolean> = MutableLiveData()
    val isUserTlCheck: LiveData<Boolean> = _isUserTlCheck

    private suspend fun prepareMenus() {

        try {
            val currentUser =
                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()
            val phoneNumber = currentUser.phoneNumber!!
            val phoneNumber2 = phoneNumber.substring(1)
            val phoneNumber3 = "0" + phoneNumber.substring(3)
            val phoneNumber4 = phoneNumber.substring(3)

            val getBussinessContactQuery = bsCalendarScreenRepository
                .db
                .collection("Business_Contacts")
                .whereIn(
                    "primary_no",
                    arrayListOf(phoneNumber, phoneNumber2, phoneNumber3, phoneNumber4)
                )
                .
                getOrThrow()

            _isUserTlCheck.postValue(getBussinessContactQuery.size() > 0)
        } catch (e: Exception) {
            _isUserTlCheck.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}