package com.gigforce.app.modules.profile

import android.view.View
import android.widget.EditText
import com.google.protobuf.BoolValueOrBuilder

class ProfileValidation() {

    private fun isNotEmptyEditText(view: EditText?): Boolean {
        view?.let {
            if (view.text.toString().trim().isNotEmpty())
                return true
        }
        return false
    }

    private fun isFixedLengthEditText(view: EditText?, length: Int): Boolean {
        view?.let {
            if (view.text.toString().length == length) {
                return true
            }
        }
        return false
    }

    private fun isNotEmptyString(value: String?): Boolean {
        value?.let {
            if (value.trim().isNotEmpty())
                return true
        }
        return false
    }

    private fun isEmptyString(value: String?): Boolean {
        return value.isNullOrEmpty() && value!!.trim().isEmpty()
    }

    private fun doesNotContainHash(value: String?): Boolean {
        if (value!!.contains('#'))
            return false
        return true
    }

    fun isValidEducation(
        institutionName: EditText?,
        courseName: EditText?,
        degreeName: String?,
        startDate: String?,
        endDate: String?
    ): Boolean {
        if (isNotEmptyEditText(institutionName) && isNotEmptyEditText(courseName) &&
                    isNotEmptyString(degreeName) && isNotEmptyString(startDate) &&
                    isNotEmptyString(endDate))
        {
            return true
        }
        return false
    }

    fun isValidExperience(
        role: EditText?,
        company: EditText?,
        employmentType: String?,
        location: EditText?,
        startDate: String?,
        endDate: String?,
        currentlyWorkHere: Boolean?
    ): Boolean {
        if (isNotEmptyEditText(role) && isNotEmptyEditText(company) &&
                isNotEmptyString(employmentType) && isNotEmptyEditText(location) &&
                isNotEmptyString(startDate) &&
            ((currentlyWorkHere!! && isEmptyString(endDate))|| isNotEmptyString(endDate)))
        {
            return true
        }
        return false
    }

    fun isValidAchievement(
        title: EditText?,
        authority: EditText?,
        year: EditText?
    ): Boolean {
        if (isNotEmptyEditText(title) && isNotEmptyEditText(authority) && isNotEmptyEditText(year) &&
                    isFixedLengthEditText(year, 4)) {
            return true
        }
        return false
    }

    fun isValidSkill(skill: String?): Boolean {
        return isNotEmptyString(skill)
    }

    fun isValidLanguage(language: EditText?): Boolean {
        return isNotEmptyEditText(language)
    }

    fun isValidTag(tag: String?): Boolean {
        if (isNotEmptyString(tag) && doesNotContainHash(tag.toString())) {
            return true
        }
        return false
    }
}