package com.gigforce.app.modules.preferences.location

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.ViewModel

class LocationViewModel() : ViewModel(), Parcelable {
    // TODO: Implement the ViewModel
    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationViewModel> {
        override fun createFromParcel(parcel: Parcel): LocationViewModel {
            return LocationViewModel(parcel)
        }

        override fun newArray(size: Int): Array<LocationViewModel?> {
            return arrayOfNulls(size)
        }
    }
}