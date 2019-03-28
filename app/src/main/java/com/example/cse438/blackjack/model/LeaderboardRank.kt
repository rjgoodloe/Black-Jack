package com.example.cse438.blackjack.model


data class LeaderboardRank(
    val email: String?,
    val wins: Int?,
    val losses: Int?,
    var money: Long?,
    var ratio: Double?
) {
    constructor() : this(null, null, null, null, null)

    fun updateRatio() {
        if (losses == 0) {
            ratio = this.wins!!.toDouble()
        } else {
            this.ratio = wins!!.toDouble() / losses!!.toDouble()
        }
    }
}

