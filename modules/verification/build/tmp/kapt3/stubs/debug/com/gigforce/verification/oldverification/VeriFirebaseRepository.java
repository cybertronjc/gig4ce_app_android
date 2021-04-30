package com.gigforce.verification.oldverification;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0012\u001a\u00020\u0013J\u001e\u0010\u0014\u001a\u00020\u00152\u0016\u0010\u0016\u001a\u0012\u0012\u0004\u0012\u00020\u00180\u0017j\b\u0012\u0004\u0012\u00020\u0018`\u0019J\u000e\u0010\u001a\u001a\u00020\u00152\u0006\u0010\u001b\u001a\u00020\u0004J\u001e\u0010\u001c\u001a\u00020\u00152\u0016\u0010\u001d\u001a\u0012\u0012\u0004\u0012\u00020\u001e0\u0017j\b\u0012\u0004\u0012\u00020\u001e`\u0019J\u001e\u0010\u001f\u001a\u00020\u00152\u0016\u0010 \u001a\u0012\u0012\u0004\u0012\u00020!0\u0017j\b\u0012\u0004\u0012\u00020!`\u0019J\u001e\u0010\"\u001a\u00020\u00152\u0016\u0010#\u001a\u0012\u0012\u0004\u0012\u00020$0\u0017j\b\u0012\u0004\u0012\u00020$`\u0019J\u001e\u0010%\u001a\u00020\u00152\u0016\u0010&\u001a\u0012\u0012\u0004\u0012\u00020\'0\u0017j\b\u0012\u0004\u0012\u00020\'`\u0019R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u000f\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0006\"\u0004\b\u0011\u0010\b\u00a8\u0006("}, d2 = {"Lcom/gigforce/verification/oldverification/VeriFirebaseRepository;", "", "()V", "collection", "", "getCollection", "()Ljava/lang/String;", "setCollection", "(Ljava/lang/String;)V", "firebaseDB", "Lcom/google/firebase/firestore/FirebaseFirestore;", "getFirebaseDB", "()Lcom/google/firebase/firestore/FirebaseFirestore;", "setFirebaseDB", "(Lcom/google/firebase/firestore/FirebaseFirestore;)V", "uid", "getUid", "setUid", "getVerificationData", "Lcom/google/firebase/firestore/DocumentReference;", "setBank", "", "banks", "Ljava/util/ArrayList;", "Lcom/gigforce/core/datamodels/verification/Bank;", "Lkotlin/collections/ArrayList;", "setCardAvatar", "cardAvatarName", "setVeriContact", "contacts", "Lcom/gigforce/core/datamodels/verification/Address;", "setVeriDL", "dls", "Lcom/gigforce/core/datamodels/verification/DL;", "setVeriPassport", "passports", "Lcom/gigforce/core/datamodels/verification/Passport;", "setVeriVoterID", "voterids", "Lcom/gigforce/core/datamodels/verification/VoterID;", "verification_debug"})
public final class VeriFirebaseRepository {
    @org.jetbrains.annotations.NotNull()
    private com.google.firebase.firestore.FirebaseFirestore firebaseDB;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String uid;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String collection = "Verification";
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.FirebaseFirestore getFirebaseDB() {
        return null;
    }
    
    public final void setFirebaseDB(@org.jetbrains.annotations.NotNull()
    com.google.firebase.firestore.FirebaseFirestore p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUid() {
        return null;
    }
    
    public final void setUid(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCollection() {
        return null;
    }
    
    public final void setCollection(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final void setCardAvatar(@org.jetbrains.annotations.NotNull()
    java.lang.String cardAvatarName) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.DocumentReference getVerificationData() {
        return null;
    }
    
    public final void setVeriContact(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.Address> contacts) {
    }
    
    public final void setVeriDL(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.DL> dls) {
    }
    
    public final void setVeriVoterID(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.VoterID> voterids) {
    }
    
    public final void setVeriPassport(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.Passport> passports) {
    }
    
    public final void setBank(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.Bank> banks) {
    }
    
    public VeriFirebaseRepository() {
        super();
    }
}