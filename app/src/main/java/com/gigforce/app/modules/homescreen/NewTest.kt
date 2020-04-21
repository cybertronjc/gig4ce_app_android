package com.gigforce.app.modules.homescreen

import br.com.robsonldo.library.FireStoreORM
import br.com.robsonldo.library.annotations.Attribute
import br.com.robsonldo.library.annotations.Collection
import br.com.robsonldo.library.annotations.Persisted
import br.com.robsonldo.library.annotations.TypeSource
import com.google.firebase.firestore.Source

@Persisted(false)
@TypeSource(Source.SERVER)
@Collection("NewTest")
class NewTest : FireStoreORM<NewTest>(){
    @Attribute("name")
    var name: String? = null
}