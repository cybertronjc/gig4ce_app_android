package com.gigforce.app.core.base.models

abstract class BaseResponse<T> {
    abstract val status: Boolean
    abstract val message: String
    abstract val data: T
}