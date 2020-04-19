package com.gigforce.app.core.base.basefirestore

abstract public class BaseFirestoreDataModel(tableName:String){
    public var tableName:String

    init {
        this.tableName = tableName
    }
}