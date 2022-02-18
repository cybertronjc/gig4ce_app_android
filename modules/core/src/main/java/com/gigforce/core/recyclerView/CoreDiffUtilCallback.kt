package com.gigforce.core.recyclerView

import androidx.recyclerview.widget.DiffUtil

abstract class CoreDiffUtilCallback<T> : DiffUtil.Callback(){

    private lateinit var _oldList : List<Any>
    private lateinit var _newList : List<Any>

    val oldList : List<T>
        get() {
            if (!::_oldList.isInitialized) {
                throw IllegalStateException("please make sure setOldAndNewList() is called before accessing oldList")
            } else{
               return _oldList as List<T>
            }
        }

    val newList : List<T>
        get() {
            if (!::_newList.isInitialized) {
                throw IllegalStateException("please make sure setOldAndNewList() is called before accessing newList")
            } else{
                return _newList as List<T>
            }
        }

    fun setOldAndNewList(
        oldList :List<Any>,
        newList : List<Any>
    ){
        this._oldList = oldList
        this._newList = newList
    }
}