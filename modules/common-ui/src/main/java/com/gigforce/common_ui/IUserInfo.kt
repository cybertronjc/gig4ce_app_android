package com.gigforce.common_ui

import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

interface IUserInfo {
    fun getData(): UserInfoImp.UserLoginInfo
}

class UserInfoImp @Inject constructor(val sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface) :
    IUserInfo {
    val userLoginInfo: UserLoginInfo

    init {
        val isLoggedIn: Boolean =
            !sharedPreAndCommonUtilInterface.getLoggedInMobileNumber().equals("")
        val profileName: String = sharedPreAndCommonUtilInterface.getLoggedInUserName()
        val profilePicPath: String = sharedPreAndCommonUtilInterface.getUserProfilePic()
        val uid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        userLoginInfo = UserLoginInfo(
            isLoggedIn = isLoggedIn,
            profileName = profileName,
            profilePicPath = profilePicPath,
            uid = uid
        )
    }

    data class UserLoginInfo(
        val isLoggedIn: Boolean,
        val profileName: String,
        val profilePicPath: String,
        val uid: String
    )

    override fun getData(): UserLoginInfo {
        return userLoginInfo
    }

}


