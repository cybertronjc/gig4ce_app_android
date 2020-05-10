package com.gigforce.app.modules.auth.ui.main
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.setDarkStatusBarTheme

class LoginSuccessfulFragment: BaseFragment() {
    companion object {
        fun newInstance() = LoginSuccessfulFragment()
    }
    private val SPLASH_TIME_OUT:Long = 2500 // 1 sec
    var layout: View? = null;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        this.setDarkStatusBarTheme()
        layout = inflateView(R.layout.fragment_login_success, inflater, container);
        return layout
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
            popFragmentFromStack(R.id.loginSuccessfulFragment)
            navigateWithAllPopupStack(R.id.mainHomeScreen);
        }, SPLASH_TIME_OUT)
    }
}