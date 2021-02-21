package com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.chatmodule.ui.ChatFragment
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.common_ui.StringConstants
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class BSCalendarScreenViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private var disposable: CompositeDisposable? = CompositeDisposable()

    val bsCalendarScreenRepository = BsCalendarScreenRepository()
    private val _observableChatInfo: MutableLiveData<Gig> = MutableLiveData()
    val observableChatInfo: MutableLiveData<Gig> = _observableChatInfo


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
        bsCalendarScreenRepository.getCollectionReference().whereEqualTo("loginMobile", mobileNumber).addSnapshotListener { success, err ->
            run {
                if (!success?.documents.isNullOrEmpty()) {
                    val toObject = success?.documents?.get(0)?.toObject(ProfileData::class.java)
                    gig.chatInfo = mapOf(
                            ChatFragment.INTENT_EXTRA_OTHER_USER_IMAGE to (toObject?.profileAvatarName ?: ""),
                            ChatFragment.INTENT_EXTRA_OTHER_USER_NAME to (toObject?.name ?: ""),
                            ChatFragment.INTENT_EXTRA_CHAT_HEADER_ID to "",
                            ChatFragment.INTENT_EXTRA_OTHER_USER_ID to (success?.documents?.get(0)?.id ?: ""),
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
        }?.flatMap { checkForChatProfile(it) }?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ success ->
            _observableChatInfo.value = success

        }, { err ->


        })
        !!)


    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}