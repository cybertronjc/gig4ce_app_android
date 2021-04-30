package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface ILoginInfoRepo {
    fun loadData()
    fun getData(): LiveData<LoginInfoRepo.UserLoginInfo>
}

class LoginInfoRepo @Inject constructor() : ILoginInfoRepo {
    private var data: MutableLiveData<UserLoginInfo> = MutableLiveData()
    val collectionName = "Profiles"

    class UserLoginInfo{
        var isLoggedIn: Boolean = false
        var uid: String? = null
        var profilePicPath: String? = null
        var profileName: String? = null
        var isAmbassador: Boolean? = null

        constructor(uid : String,profilePicPath:String,profileName : String,isAmbassador:Boolean){
            this.isLoggedIn = true
            this.uid = uid
            this.profilePicPath = profilePicPath
            this.profileName = profileName
            this.isAmbassador = isAmbassador
        }

        constructor() {
            isLoggedIn = false

            uid = null
            profilePicPath = null
            profileName = ""
            isAmbassador = false
        }
    }

    init {
        loadData()
    }

    companion object{
        lateinit var userLoginInfo : UserLoginInfo
    }

    override fun loadData() {
        FirebaseAuth.getInstance().addAuthStateListener {it1->
            it1.currentUser?.let {it2->
                FirebaseFirestore.getInstance().collection(collectionName).document(it2.uid)
                    .addSnapshotListener(
                        EventListener<DocumentSnapshot> { value, e ->
                            if (e != null) {
                                return@EventListener
                            }
                            value?.let {
                                var dataProfile = it.data as Map<String, Any>
                                val name = dataProfile.get("name") as? String ?: ""
                                val profilePic = dataProfile.get("profileAvatarName") as? String ?: ""
                                userLoginInfo = UserLoginInfo(uid = it2.uid,profileName = name,profilePicPath = profilePic,isAmbassador = dataProfile.get("isUserAmbassador") as? Boolean ?: false)
//                                data.value = UserLoginInfo(uid = it2.uid,profileName = name,profilePicPath = profilePic,isAmbassador = dataProfile.get("isUserAmbassador") as? Boolean ?: false)
                            }
                        })
            } ?: let {
                data.value = UserLoginInfo()
            }
        }
    }

    override fun getData(): LiveData<UserLoginInfo> {
        return data
    }

}


