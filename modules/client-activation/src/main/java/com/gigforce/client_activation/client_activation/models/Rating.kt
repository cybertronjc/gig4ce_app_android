package com.gigforce.client_activation.client_activation.models

data class Rating (
    var hygiene: Float = 0.0F,
    var politeness: Float = 0.0F,
    var preparedness: Float = 0.0F,
    var professionalism: Float = 0.0F,
    var punctuality: Float = 0.0F
){
    fun getTotal(): Float {
        return (this.hygiene + this.politeness + this.preparedness + this.professionalism + this.punctuality)/5
    }
}