package com.gigforce.app.modules.learning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.utils.Lce
import kotlinx.coroutines.launch

class LearningViewModel constructor(
    private val learningRepository: LearningRepository = LearningRepository()
) : ViewModel() {

    private var mCachedRoleBasedCourses : List<Course>? = null
    private var mUserBasedCourses : List<Course>? = null
    private var mAllCoursesBasedCourses : List<Course>? = null


    private val _roleBasedCourses = MutableLiveData<Lce<List<Course>>>()
    val roleBasedCourses : LiveData<Lce<List<Course>>> = _roleBasedCourses


    fun getRoleBasedCourses() = viewModelScope.launch {

        if(mCachedRoleBasedCourses != null){
            _roleBasedCourses.postValue(Lce.content(mCachedRoleBasedCourses!!))
            return@launch
        }

        _roleBasedCourses.postValue(Lce.loading())

        try {
            val courses = learningRepository.getRoleBasedCourses()
            mCachedRoleBasedCourses = courses

            _roleBasedCourses.postValue(Lce.content(courses))
        } catch (e: Exception) {
            _roleBasedCourses.postValue(Lce.error(e.toString()))
        }
    }


    private val _userCourses = MutableLiveData<Lce<List<Course>>>()
    val userCourses : LiveData<Lce<List<Course>>> = _userCourses

    fun getUserCourses() = viewModelScope.launch {
        _userCourses.postValue(Lce.loading())

        try {
            val courses = learningRepository.getUserCourses()
            _userCourses.postValue(Lce.content(courses))
        } catch (e: Exception) {
            _userCourses.postValue(Lce.error(e.toString()))
        }
    }

    private val _allCourses = MutableLiveData<Lce<List<Course>>>()
    val allCourses : LiveData<Lce<List<Course>>> = _allCourses

    fun getAllCourses() = viewModelScope.launch {

        if(mAllCoursesBasedCourses != null){
            _allCourses.postValue(Lce.content(mAllCoursesBasedCourses!!))
            return@launch
        }

        _allCourses.postValue(Lce.loading())

        try {
            val courses = learningRepository.getAllCourses()
            mAllCoursesBasedCourses = courses

            _allCourses.postValue(Lce.content(courses))
        } catch (e: Exception) {
            _allCourses.postValue(Lce.error(e.toString()))
        }
    }


}