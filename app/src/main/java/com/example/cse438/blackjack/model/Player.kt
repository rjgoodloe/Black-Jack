package com.example.cse438.blackjack.model

import java.util.*

// HOLDS PLAYER INFORMATION
data class Player(
    private var total: Int,
    private var cards: ArrayList<Card>,
    private var money: Long,
    private var bet: Long,
    private var isTurn: Boolean,
    private var canBet: Boolean
) {

    constructor() : this(0, ArrayList<Card>(), 0, 0, true, true)

    fun getTotal(): Int {
        return this.total
    }

    fun getCards(): ArrayList<Card> {
        return this.cards
    }

    fun getMoney(): Long {
        return this.money
    }

    fun getBet(): Long {
        return this.bet
    }

    fun addCard(card: Card) {
        this.cards.add(card)
        this.total += card.value
    }

    fun bet(bet: Long) {
        this.bet += bet
    }

    fun updateTotal(value: Int) {
        this.total += value
    }

    fun setMoney(value: Long) {
        this.money = value
    }

    fun cannotBet(): Boolean {
        return this.canBet
    }

    fun isTurn(): Boolean {
        return this.isTurn
    }

    fun setIsTurn(flag: Boolean) {
        this.isTurn = flag
    }

    fun setCanNotBet() {
        this.canBet = false
    }

}










