package com.gigforce.lead_management.exceptions

class UserDoesNotExistInProfileException constructor(
    uid : String
) : Exception(
    "No profile found wit $uid in profiles"
)