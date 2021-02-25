package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForLearningModule(
    baseImplementation: BaseNavigationImpl){

    init {
        val moduleName:String = "learning"

        baseImplementation.registerRoute("${moduleName}/main", R.id.mainLearningFragment)
        baseImplementation.registerRoute("${moduleName}/coursedetails", R.id.learningCourseDetails)
//        baseImplementation.registerRoute("${moduleName}/lesson", R.id.mainLearningFragment)
//        baseImplementation.registerRoute("${moduleName}/assessment", R.id.mainLearningFragment)
        baseImplementation.registerRoute("${moduleName}/mylearning", R.id.myLearningFragment)
        baseImplementation.registerRoute("${moduleName}/assessment", R.id.assessment_fragment)
        baseImplementation.registerRoute("${moduleName}/assessmentslides", R.id.slidesFragment)
        baseImplementation.registerRoute("${moduleName}/assessmentListFragment", R.id.assessmentListFragment)
        baseImplementation.registerRoute("${moduleName}/courseContentListFragment", R.id.courseContentListFragment)

        baseImplementation.registerRoute("${moduleName}/assessmentResultFragment", R.id.assessment_result_fragment)






    }
}