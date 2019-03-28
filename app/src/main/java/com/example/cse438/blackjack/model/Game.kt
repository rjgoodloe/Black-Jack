package com.example.cse438.blackjack.model

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.example.cse438.blackjack.util.CardRandomizer
import java.util.*


/*
THIS CLASS WAS AN ATTEMPT TO  IMPLEMENT BETTER OOP
I DID NOT HAVE ENOUGH TIME TO GET IT TO WORK PROPERLY WITH GAME ACTIVITY
DISREGARD THIS FILE.
 */

data class Game(
    private var dealer: Player,
    private var player: Player,
    private var numCards: Int,
    private var numChips: Int,
    private var randomizer: CardRandomizer,
    private var cardList: ArrayList<Int>,
    private var parentContext: Context,
    private var money: Long,
    private var bettingClosed: Boolean,
    private var maxBet: Long

) {

    constructor(context: Context, money: Long) : this(
        Player(),
        Player(),
        1,
        100,
        CardRandomizer(),
        ArrayList<Int>(),
        context,
        money,
        false,
        500
    )

    fun dealInitial() {
        player.setIsTurn(true)
        dealer.setIsTurn(false)

        val dealerFaceDown = getCard()

        val playerCardOne = getCard()

        dealer.addCard(dealerFaceDown)

        player.addCard(playerCardOne)

        player.setMoney(money)
    }

    fun placeBet(): Boolean {
        return when {
            dealer.getTotal() == 21 -> {
                player.bet(50)
                true
            }
            player.getBet() == maxBet -> {
                Toast.makeText(
                    this.parentContext,
                    "Maximum bet is $500",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
            bettingClosed -> {
                Toast.makeText(
                    this.parentContext,
                    "You may only bet after the initial deal",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
            player.getBet() >= player.getMoney() -> {
                Toast.makeText(
                    this.parentContext,
                    "You have no more money to bet",
                    Toast.LENGTH_LONG
                ).show()
                false

            }
            player.getBet() < 500 -> {
                player.bet(50)
                true
            }
            else -> false

        }
    }

    private fun getCard(): Card {
        val cardList = randomizer.getIDs(parentContext)
        val rand = Random()
        val r: Int = rand.nextInt(cardList.size)
        val id: Int = cardList[r]
        val name = parentContext.resources.getResourceEntryName(id)
        return Card(name, id = numCards++)
    }

    fun getNumCards(): Int {
        return this.numCards
    }

    fun getNumChips(): Int {
        return this.numChips
    }


    fun playerHit(): Card {
        bettingClosed = true

        val card = getCard()

        player.addCard(card)


        if (player.getTotal() > 21) {
            adjustAceValues(player)
        }

        return card
    }

    private fun adjustAceValues(thisPlayer: Player) {
        for (card in thisPlayer.getCards()) {
            if (card.name.contains("ace") && card.value == 11) {
                card.value = 1
                thisPlayer.updateTotal(-10)
                break
            }
        }
    }

    private fun dealerAction() {
        while (dealer.getTotal() < 17) {

            val newCard = getCard()

            dealer.addCard(newCard)

        }
    }

    @SuppressLint("ResourceType")
    fun playerStand() {
        player.setIsTurn(false)
        dealer.setIsTurn(true)

        bettingClosed = true

        dealerAction()

        if (dealer.getTotal() > 21) {
            adjustAceValues(dealer)
            dealerAction()
        }


    }

    fun checkWin(): String {
        return when {
            dealer.getTotal() == 21 -> "losses"
            player.getTotal() > 21 && player.isTurn() -> "bust"
            dealer.getTotal() > 21 -> "wins"
            dealer.getTotal() - player.getTotal() < 0 && dealer.isTurn() -> "wins"
            dealer.getTotal() - player.getTotal() > 0 && dealer.isTurn() && dealer.getTotal() < 21 -> "losses"
            dealer.getTotal() == getPlayer().getTotal() && dealer.isTurn() -> "draw"
            else -> ""
        }
    }

    fun getPlayer(): Player {
        return this.player
    }

    fun getDealer(): Player {
        return this.dealer
    }

    fun incrementNumChips() {
        this.numChips++
    }


}
