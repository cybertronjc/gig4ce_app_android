package com.gigforce.app.modules.auth.ui.main
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.setDarkStatusBarTheme
import kotlinx.android.synthetic.main.mobile_number_input.*
import kotlinx.android.synthetic.main.mobile_number_input.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class OnOTPSuccess: BaseFragment() {
    companion object {
        fun newInstance() = OnOTPSuccess()
    }

    var layout: View? = null;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.setDarkStatusBarTheme()
        layout = inflateView(R.layout.layout_otp_sucess, inflater, container);
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout?.setOnClickListener() {
            navigate(R.id.homeScreenIcons);
        }
    }
}