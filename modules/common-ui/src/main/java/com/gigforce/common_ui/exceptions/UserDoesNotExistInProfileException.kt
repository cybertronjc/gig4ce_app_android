package com.gigforce.common_ui.exceptions

class UserDoesNotExistInProfileException constructor(
    uid : String
) : Exception(
    "No profile found wit $uid in profiles"
)