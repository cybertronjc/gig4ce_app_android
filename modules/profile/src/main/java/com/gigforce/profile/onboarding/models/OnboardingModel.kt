package com.gigforce.profile.onboarding.models

class OnboardingModel {
}


open class ViewType(val viewType: Int){
}

class NameGenderDM(viewType: Int) : ViewType(viewType)
{
    var name : String = ""
    var gender : String = ""
}

class AgeGroupDM(viewType: Int) : ViewType(viewType){
    var ageGroup : String = ""
}

class HighestQualificationDM(viewType: Int) : ViewType(viewType){
    var highestQualification : String = ""
}

class ExperienceDM(viewType: Int) : ViewType(viewType){
    var isExperienced : Boolean = false
    var userExperience : String = ""
}

// location pending

class DeliveryExecutiveExperienceDM(viewType: Int) : ViewType(viewType){
    var isExperience : Boolean = false
    var experienceType : String = ""
}

class CurrentJobDM(viewType: Int) : ViewType(viewType){
    var working : Boolean = false
}

class WorkingDays(viewType: Int) : ViewType(viewType){
    var day : String = ""
}

class TimingDM(viewType: Int) : ViewType(viewType){
    var timing : String = ""
}

class InterestDM(viewType: Int) : ViewType(viewType){
    var interest : String = ""
}