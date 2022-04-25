package com.gigforce.core.retrofit.custom_serialization

import com.gigforce.core.date.DateUtil
import com.google.firebase.Timestamp
import com.google.gson.*
import java.lang.reflect.Type

object FirebaseTimestampFromMongoTimeStringSerializer  : JsonDeserializer<Timestamp?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Timestamp? {
        val jsonString = json ?: return null
        return DateUtil.getFirebaseTimestampFromUTCDateTimeString(jsonString.asString)
    }
}