package com.gigforce.app.modules.profile

import android.view.View
import android.widget.EditText

class ProfileValidation() {

    private fun isNotEmptyEditText(view: EditText?): Boolean {
        view?.let {
            if (view.text.toString().isNotEmpty())
                return true
        }
        return false
    }

    private fun isNotEmptyString(value: String?): Boolean {
        value?.let {
            if (value.isNotEmpty())
                return true
        }
        return false
    }

    private fun isEmptyString(value: String?): Boolean {
        return value.isNullOrEmpty()
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
}