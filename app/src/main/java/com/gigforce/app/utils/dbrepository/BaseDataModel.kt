package com.gigforce.app.utils.dbrepository

abstract public class BaseDataModel(tableName:String){
    public var tableName:String

    init {
        this.tableName = tableName
    }
}