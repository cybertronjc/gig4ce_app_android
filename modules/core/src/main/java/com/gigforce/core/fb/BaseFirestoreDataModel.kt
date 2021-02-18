package com.gigforce.core.fb

abstract public class BaseFirestoreDataModel(tableName:String){
    public var tableName:String

    init {
        this.tableName = tableName
    }
}