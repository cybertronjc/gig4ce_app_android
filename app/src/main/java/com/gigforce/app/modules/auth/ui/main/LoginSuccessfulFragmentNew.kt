package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment


/**
 * A simple [Fragment] subclass.
 * Use the [LoginSuccessfulFragmentNew.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginSuccessfulFragmentNew : BaseFragment() {
    // TODO: Rename and change types of parameters

    private val SPLASH_TIME_OUT:Long = 3000 // 1 sec

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflateView(R.layout.fragment_login_successful_new, inflater,container)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        layout?.setOnClickListener() {
//        }
//        successful_screen.setOnClickListener(){
//            popFragmentFromStack(R.id.homeScreenIcons)
//            navigateWithAllPopupStack(R.id.homeScreenIcons1);
//        }
        Handler().postDelayed({
            popFragmentFromStack(R.id.homeScreenIcons)
            navigateWithAllPopupStack(R.id.homeScreenIcons1);
        }, SPLASH_TIME_OUT)
    }
}