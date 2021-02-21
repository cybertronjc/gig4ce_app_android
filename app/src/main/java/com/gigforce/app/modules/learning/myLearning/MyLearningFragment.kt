package com.gigforce.app.modules.learning.myLearning

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.learning.myLearning.assessment.MyAssessmentsInnerFragment
import com.gigforce.app.modules.learning.myLearning.journey.MyJourneyInnerFragment
import com.gigforce.app.modules.learning.myLearning.learning.MyLearningsInnerFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_my_learning.*


class MyLearningFragment : Fragment() {

    private val viewModelProfile: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_my_learning, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MyLearningViewPager(childFragmentManager)
        myLearningViewPager.adapter = adapter

        val indicator = ResourcesCompat.getDrawable(resources, R.drawable.tab_round_indicator, null)
        my_learning_tablayout.setSelectedTabIndicator(indicator)
        my_learning_tablayout.isTabIndicatorFullWidth = false
        my_learning_tablayout.setupWithViewPager(myLearningViewPager)



        for (i in 0 until my_learning_tablayout.tabCount) {
            val tab = my_learning_tablayout.getTabAt(i)
            val currentTab = tab ?: continue

            TextView(requireContext()).apply {
                currentTab.customView = this

                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

                text = currentTab.text.toString()
                letterSpacing = 0.0f
                setTextSize(TypedValue.COMPLEX_UNIT_SP,16.0f)


                if (currentTab.position == 0) {
                    setTextColor(ResourcesCompat.getColor(resources, R.color.black_85, null))
                    val typeFace = ResourcesCompat.getFont(requireContext(), R.font.lato_bold)
                    typeface = typeFace
                } else {
                    setTextColor(ResourcesCompat.getColor(resources, R.color.warm_grey, null))
                    val typeFace = ResourcesCompat.getFont(requireContext(), R.font.lato)
                    typeface = typeFace
                }
            }
        }

        my_learning_tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val tabUnSelected = tab ?: return

                val textView: TextView = tabUnSelected.customView as TextView
                textView.setTextColor(ResourcesCompat.getColor(resources, R.color.warm_grey, null))
                textView.letterSpacing = 0.0f
                val typeFace = ResourcesCompat.getFont(requireContext(), R.font.lato)
                textView.typeface = typeFace
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabUnSelected = tab ?: return
                val textView: TextView = tabUnSelected.customView as TextView

                textView.letterSpacing = 0.0f
                textView.setTextColor(ResourcesCompat.getColor(resources, R.color.black_85, null))
                val typeFace = ResourcesCompat.getFont(requireContext(), R.font.lato_bold)
                textView.typeface = typeFace
            }
        })


    }

    private inner class MyLearningViewPager constructor(
        fragmentManager: FragmentManager
    ) : FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        private val myJourneyFragment: Fragment by lazy {
            MyJourneyInnerFragment()
        }
        private val myLearningFragment: Fragment by lazy {
            MyLearningsInnerFragment()
        }
        private val myAssesmentFragment: Fragment by lazy {
            MyAssessmentsInnerFragment()
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> myJourneyFragment
                1 -> myLearningFragment
                2 -> myAssesmentFragment
                else -> {
                    throw IllegalArgumentException("error illegal page index $position")
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {

            return when (position) {
                0 -> "Journey"
                1 -> "Learning"
                2 -> "Assessments"
                else -> {
                    throw IllegalArgumentException("error illegal page index $position")
                }
            }
        }
    }
}