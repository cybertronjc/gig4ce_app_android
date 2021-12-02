package com.gigforce.core.retrofit

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

class GsonExclusionStrategy : ExclusionStrategy {

    override fun shouldSkipClass(clazz: Class<*>?): Boolean {
        return clazz?.getAnnotation(DoNotSerialize::class.java) != null
    }

    override fun shouldSkipField(f: FieldAttributes?): Boolean {
        return f?.getAnnotation(DoNotSerialize::class.java) != null
    }
}