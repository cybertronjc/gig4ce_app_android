package com.gigforce.verification.gigerVerfication;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\u0011\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004J\u000e\u0010\n\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\u0004J\u000e\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u0004J%\u0010\u000e\u001a\u00020\u0004*\u00020\u00042\u0012\u0010\u000f\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00040\u0010\"\u00020\u0004H\u0002\u00a2\u0006\u0002\u0010\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/VerificationValidations;", "", "()V", "DL_REGEX", "", "IFSC_REGEX", "PAN_REGEX", "isDLNumberValid", "", "dlNo", "isIfSCValid", "ifsc", "isPanCardValid", "panNo", "removeAll", "c", "", "(Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/String;", "verification_debug"})
public final class VerificationValidations {
    private static final java.lang.String PAN_REGEX = "[A-Za-z]{5}[0-9]{4}[A-Za-z]{1}";
    private static final java.lang.String DL_REGEX = "[A-Za-z]{2}[0-9]{2}[A-Z0-9a-z]{3}[0-9]{8}";
    private static final java.lang.String IFSC_REGEX = "[A-Za-z]{4}0[A-Z0-9a-z]{6}";
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.verification.gigerVerfication.VerificationValidations INSTANCE = null;
    
    public final boolean isPanCardValid(@org.jetbrains.annotations.NotNull()
    java.lang.String panNo) {
        return false;
    }
    
    public final boolean isDLNumberValid(@org.jetbrains.annotations.NotNull()
    java.lang.String dlNo) {
        return false;
    }
    
    public final boolean isIfSCValid(@org.jetbrains.annotations.NotNull()
    java.lang.String ifsc) {
        return false;
    }
    
    private final java.lang.String removeAll(java.lang.String $this$removeAll, java.lang.String... c) {
        return null;
    }
    
    private VerificationValidations() {
        super();
    }
}