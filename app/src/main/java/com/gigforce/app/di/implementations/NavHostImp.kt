package com.gigforce.app.di.implementations

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.gigforce.app.R
import com.gigforce.core.di.interfaces.INavHost
import com.gigforce.user_preferences.location.PreferredLocationFragment
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class NavHostImp @Inject constructor(@ActivityContext val activity : Activity) : INavHost{
    override fun getFragment() : Fragment?{
//        val navHostFragment: NavHostFragment? =
//            (activity as FragmentActivity)?.supportFragmentManager?.findFragmentById(R.id.nav_fragment) as NavHostFragment?
//        var fragmentholder: Fragment? =
//            navHostFragment!!.childFragmentManager.fragments[navHostFragment!!.childFragmentManager.fragments.size - 1]
//        return fragmentholder

        return PreferredLocationFragment.newInstance()
    }
}