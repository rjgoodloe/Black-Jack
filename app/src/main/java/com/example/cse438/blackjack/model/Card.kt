package com.example.cse438.blackjack.model


// HOLDS CARD INFORMATION
data class Card(val name: String, var id: Int) {


    var value: Int = when {
        name.contains("10") -> 10
        name.contains("jack") -> 10
        name.contains("queen") -> 10
        name.contains("king") -> 10
        name.contains("ace") -> 11
        name.contains("back") -> 0
        else -> {
            name.takeLast(1).toInt()
        }
    }

}