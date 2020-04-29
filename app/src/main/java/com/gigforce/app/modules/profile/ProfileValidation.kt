package com.gigforce.app.modules.profile

import android.view.View
import android.widget.EditText

class ProfileValidation() {

    fun isNotEmptyEditText(view: EditText?): Boolean {
        view?.let {
            if (view.text.toString().isNotEmpty())
                return true
        }
        return false
    }

    fun isNotEmptyString(value: String?): Boolean {
        value?.let {
            if (value.isNotEmpty())
                return true
        }
        return false
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
}