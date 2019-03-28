package com.example.cse438.blackjack.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.widget.*
import com.example.cse438.blackjack.model.Card
import com.example.cse438.blackjack.R
import com.example.cse438.blackjack.model.Game


/*
THIS ACTIVITY WAS AN ATTEMPT TO REFACTOR MAIN ACTIVITY AND IMPLEMENT BETTER OOP
I DID NOT HAVE ENOUGH TIME TO GET IT TO WORK PROPERLY, DISREGARD THIS FILE
AND GAME.KT
 */
class GameActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDetector: GestureDetectorCompat
    private var height: Int = 0
    private var width: Int = 0
    private val database = FirebaseFirestore.getInstance()
    private var dialogShown = false
    private lateinit var game: Game
    private var winCount: Long = 0
    private var lossCount: Long = 0
    private var _money: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDetector = GestureDetectorCompat(this, MyGestureListener())

        val metrics = this.resources.displayMetrics
        this.height = metrics.heightPixels
        this.width = metrics.widthPixels

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
    }


    private fun createCardImageView(card: Card) {
        val newView = ImageView(this)
        main_layout.addView(newView)

        newView.setImageResource(resources.getIdentifier(card.name, "drawable", packageName))

        newView.layoutParams.height = 400
        newView.layoutParams.width = 400

        newView.x = -400F
        newView.y = -400F

        newView.id = card.id

        Log.d("newView", "${newView.id}")
    }

    private fun dealPlayer(card: Card) {

        Log.d("Player", game.getPlayer().getTotal().toString())
        createCardImageView(card)

        moveTo(
            this@GameActivity.width / 2f - 3 * findViewById<ImageView>(card.id).layoutParams.width / 2 + game.getPlayer().getCards().size * 75F,
            this@GameActivity.height / 2f + findViewById<ImageView>(card.id).layoutParams.height / 2f,
            findViewById(card.id)
        )
    }

    private fun dealDealer(card: Card) {

        Log.d("Dealer", game.getDealer().getTotal().toString())
        createCardImageView(card)

        moveTo(
            this@GameActivity.width / 2f - 3 * findViewById<ImageView>(card.id).layoutParams.width / 2 + game.getDealer().getCards().size * 75F,
            findViewById<ImageView>(card.id).layoutParams.height / 2f,
            findViewById(card.id)
        )
    }


    private fun clearImageViews() {
        for (id in 0..game.getNumCards()) {
            main_layout.removeView(findViewById(id))
        }
        for (id in 100..game.getNumChips()) {
            main_layout.removeView(findViewById(id))
        }
    }


    private fun updateWinLoss(tag: String) {
        if (tag != "") {
            val docRef = database.collection("users").document(auth.currentUser!!.uid)
            var index: String = tag
            when (tag) {
                "draw" -> android.os.Handler().postDelayed(
                    { displayDialog(R.layout.dialog_bust) }, 1000
                )
                "wins" -> {
                    winCount++
                    android.os.Handler().postDelayed(
                        { displayDialog(R.layout.dialog_win) }, 1000
                    )
                }
                "losses" -> {
                    lossCount++
                    android.os.Handler().postDelayed(
                        { displayDialog(R.layout.dialog_dealer_wins) }, 1000
                    )
                }
                "bust" -> {
                    lossCount++
                    android.os.Handler().postDelayed(
                        { displayDialog(R.layout.dialog_bust) }, 1000
                    )
                    index = "losses"
                }
            }
            if (index != "draw") {
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            Log.d("WIN LOSS", "DocumentSnapshot data: ${document.data}")

                            var value: Long = document.data!![index] as Long
                            val user = HashMap<String, Any>()
                            user[index] = ++value
                            if (index == "wins") {
                                _money = game.getPlayer().getMoney() + game.getPlayer().getBet()
                                user["money"] = _money

                            } else {
                                _money = game.getPlayer().getMoney() - game.getPlayer().getBet()
                                user["money"] = _money
                            }

                            database.collection("users").document(auth.currentUser!!.uid)
                                .update(user)

                            initGame(_money)


                        } else {
                            Log.d("WIN LOSS", "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("WIN LOSS", "get failed with ", exception)
                    }
            }
        }
    }

    private fun initGame(money: Long) {
        tv_win.text = "Wins:$winCount"
        tv_loss.text = "Losses: $lossCount"
        tv_money.text = "Money: $$money"
        tv_bet.text = "Current Bet: $0"

        game = Game(this, money)

        game.dealInitial()

        dealDealer(Card("back", 0))
        dealPlayer(game.getPlayer().getCards()[0])

        game.dealInitial()

        dealDealer(game.getDealer().getCards()[1])
        dealPlayer(game.getPlayer().getCards()[1])

        updateWinLoss(game.checkWin())
    }

    @SuppressLint("SetTextI18n")
    private fun readData() {


        database.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    winCount = document.data!!["wins"] as Long
                    lossCount = document.data!!["losses"] as Long
                    _money = document.data!!["money"] as Long

                    tv_win.text = "Wins:$winCount"
                    tv_loss.text = "Losses: $lossCount"
                    tv_money.text = "Money: $$_money"

                    initGame(document.data!!["money"] as Long)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("THIS", "Error getting documents.", exception)
            }
    }

    private fun createChipImageView() {
        val newView = ImageView(this)
        main_layout.addView(newView)

        newView.setImageResource(R.drawable.red_chip)

        newView.layoutParams.height = 100
        newView.layoutParams.width = 100

        newView.x = this@GameActivity.width / 2f
        newView.y = 2 * this@GameActivity.height / 2f

        newView.id = game.getNumChips()
        game.incrementNumChips()
    }

    override fun onStart() {
        super.onStart()
        readData()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.leader_board -> {
                val intent = Intent(this, LeaderboardActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun moveTo(targetX: Float, targetY: Float, thisCard: View) {

        val animSetXY = AnimatorSet()

        val x = ObjectAnimator.ofFloat(
            thisCard,
            "translationX",
            thisCard.translationX,
            targetX
        )

        val y = ObjectAnimator.ofFloat(
            thisCard,
            "translationY",
            thisCard.translationY,
            targetY
        )
        animSetXY.playTogether(x, y)
        animSetXY.duration = 450
        animSetXY.start()
    }

    @SuppressLint("SetTextI18n")
    private fun displayDialog(layout: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(layout)

        dialog.setCanceledOnTouchOutside(false)
        dialogShown = true

        val window = dialog.window
        window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<Button>(R.id.b_new_game).setOnClickListener {
            dialog.dismiss()
            dialogShown = false
            clearImageViews()
        }
        dialog.show()
    }


    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        private var swipedistance = 150

        @SuppressLint("ResourceType")
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            if (dialogShown) {
                return true
            }
            game.playerStand()

            val dealerCards = game.getDealer().getCards()
            findViewById<ImageView>(0).setImageResource(
                resources.getIdentifier(
                    dealerCards[0].name,
                    "drawable",
                    packageName
                )
            )
            for (card in 2 until dealerCards.size) {
                dealDealer(dealerCards[card])
            }

            updateWinLoss(game.checkWin())
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (dialogShown) {
                return true
            }
            return when {
                e2.x - e1.x > swipedistance -> {
                    Log.d("swipe", "right")
                    val card = game.playerHit()
                    dealPlayer(card)
                    updateWinLoss(game.checkWin())
                    true
                }
                e1.y - e2.y > swipedistance -> {
                    Log.d("swipe", "up")
                    if (game.placeBet()) {

                        tv_bet.text = "Current Bet: $${game.getPlayer().getBet()}"
                        createChipImageView()
                        val rand = Random()
                        val floatx = rand.nextInt(100).toFloat()
                        val floaty = rand.nextInt(100).toFloat()

                        moveTo(
                            this@GameActivity.width / 2f + floatx,
                            this@GameActivity.height / 2f + floaty,
                            findViewById(game.getNumChips() - 1)
                        )
                    }
                    true
                }
                else -> false
            }
        }
    }
}