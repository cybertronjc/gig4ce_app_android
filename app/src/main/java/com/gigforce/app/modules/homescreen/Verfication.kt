package com.gigforce.app.modules.homescreen

import br.com.robsonldo.library.FireStoreORM
import br.com.robsonldo.library.annotations.Attribute
import br.com.robsonldo.library.annotations.Collection
import br.com.robsonldo.library.annotations.Persisted
import br.com.robsonldo.library.annotations.TypeSource
import com.google.firebase.firestore.Source

@Persisted(false)
@TypeSource(Source.SERVER)
@Collection("Verification")
class Verification : FireStoreORM<Verification>(){
    @Attribute("Aadhaar")
    var aadhaarCards: ArrayList<AadhaarDetail>? = null
}

class AadhaarDetail{
    var address:String? = null;
}