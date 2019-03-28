package com.example.cse438.blackjack.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.cse438.blackjack.R
import com.example.cse438.blackjack.model.Card
import com.example.cse438.blackjack.model.Player
import com.example.cse438.blackjack.util.CardRandomizer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDetector: GestureDetectorCompat
    private var height: Int = 0
    private var width: Int = 0
    private val database = FirebaseFirestore.getInstance()
    private lateinit var randomizer: CardRandomizer
    private lateinit var cardList: ArrayList<Int>
    private lateinit var dealer: Player
    private lateinit var player: Player
    private var numCards: Int = 1
    private var numChips: Int = 100
    private var dialogShown = false
    private var bettingClosed = false
    private var maxBet: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        mDetector = GestureDetectorCompat(this, MyGestureListener())


        // GET DISPLAY METRICS
        val metrics = this.resources.displayMetrics
        this.height = metrics.heightPixels
        this.width = metrics.widthPixels

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        randomizer = CardRandomizer()
        cardList = randomizer.getIDs(this)
    }

    private fun getCard(): Card {
        val rand = Random()
        val r: Int = rand.nextInt(cardList.size)
        val id: Int = cardList[r]
        val name = resources.getResourceEntryName(id)

        return Card(name, 0)
    }


    private fun createCardImageView(card: Card) {
        val newView = ImageView(this)
        main_layout.addView(newView)

        newView.setImageResource(resources.getIdentifier(card.name, "drawable", packageName))

        newView.layoutParams.height = 400
        newView.layoutParams.width = 400

        newView.x = -400F
        newView.y = -400F

        newView.id = this.numCards
        this.numCards++
    }

    private fun dealPlayer(card: Card) {

        createCardImageView(card)

        moveTo(
            this@MainActivity.width / 2f - 3 * findViewById<ImageView>(numCards - 1).layoutParams.width / 2 + player.getCards().size * 75F,
            this@MainActivity.height / 2f + findViewById<ImageView>(numCards - 1).layoutParams.height / 2f,
            findViewById(numCards - 1)
        )
    }

    private fun dealDealer(card: Card) {

        createCardImageView(card)

        moveTo(
            this@MainActivity.width / 2f - 3 * findViewById<ImageView>(numCards - 1).layoutParams.width / 2 + dealer.getCards().size * 75F,
            findViewById<ImageView>(numCards - 1).layoutParams.height / 2f,
            findViewById(numCards - 1)
        )
    }


    private fun playerHit() {
        bettingClosed = true

        val card = getCard()

        player.addCard(card)

        Log.d("player ${card.name}", card.value.toString())


        dealPlayer(card)

        if (player.getTotal() > 21) {
            adjustAceValues(player)
            if (player.getTotal() > 21) {
                android.os.Handler().postDelayed(
                    { displayDialog(R.layout.dialog_bust) }, 1000
                )
                updateWinLoss("losses")
            }
        }
    }

    // IF A PLAYER BUSTS, THEY MAY HAVE AN ACE THAT CAN BE VALUED AT 1 RATHER THAN 11
    private fun adjustAceValues(thisPlayer: Player) {
        for (card in thisPlayer.getCards()) {
            if (card.name.contains("ace") && card.value == 11) {
                card.value = 1
                thisPlayer.updateTotal(-10)
                break
            }
        }
    }

    // IMPLEMENTS DEALER RULES, I.E. HITTING WHEN BELOW 17
    private fun dealerAction() {
        while (dealer.getTotal() < 17) {

            val newCard = getCard()

            dealer.addCard(newCard)

            dealDealer(newCard)

        }
    }


    @SuppressLint("ResourceType")
    private fun playerStand() {
        bettingClosed = true
        val dealerFaceDown = dealer.getCards()[0]


        findViewById<ImageView>(1).setImageResource(
            resources.getIdentifier(
                dealerFaceDown.name,
                "drawable",
                packageName
            )
        )

        dealerAction()

        if (dealer.getTotal() > 21) {
            adjustAceValues(dealer)
            dealerAction()
        }

        // CHECK WIN OR LOSE AFTER DEALER HAS THEIR FINAL CARDS
        when {
            dealer.getTotal() > 21 -> {

                Log.d("dealer total", dealer.getTotal().toString())
                android.os.Handler().postDelayed(
                    { displayDialog(R.layout.dialog_win) }, 1000
                )
                updateWinLoss("wins")
            }

            dealer.getTotal() - player.getTotal() < 0 -> {
                Log.d("dealer total", dealer.getTotal().toString())

                android.os.Handler().postDelayed(
                    { displayDialog(R.layout.dialog_win) }, 1000
                )
                updateWinLoss("wins")
            }
            dealer.getTotal() - player.getTotal() > 0 -> {
                Log.d("dealer total", dealer.getTotal().toString())

                android.os.Handler().postDelayed(
                    { displayDialog(R.layout.dialog_dealer_wins) }, 1000
                )
                updateWinLoss("losses")

            }
            else -> {
                android.os.Handler().postDelayed(
                    {
                        displayDialog(R.layout.dialog_draw)
                    },
                    1000
                )
                //draw
            }
        }

    }

    // CLEAR IMAGE VIEWS FOR NEW GAME
    private fun clearImageViews() {
        for (id in 1..numCards) {
            main_layout.removeView(findViewById(id))
        }
        numCards = 1
        for (id in 100..numChips) {
            main_layout.removeView(findViewById(id))
        }
        numChips = 100
    }


    private fun initGame() {
        readData()
        bettingClosed = false

        clearImageViews()

        dealer = Player()
        player = Player()

        getPlayerMoney()

        val dealerFaceUp = getCard()
        val dealerFaceDown = getCard()

        val playerCardOne = getCard()
        val playerCardTwo = getCard()

        // DEAL THE CARD FACE DOWN
        dealer.addCard(dealerFaceDown)
        dealDealer(Card("back", 0))

        dealer.addCard(dealerFaceUp)
        dealDealer(dealerFaceUp)

        player.addCard(playerCardOne)
        dealPlayer(playerCardOne)

        player.addCard(playerCardTwo)
        dealPlayer(playerCardTwo)


        // CHECK IF DEALER WINS ON INIT DEAL
        if (dealer.getTotal() == 21) {
            playerStand()
        }


    }

    // GET PLAYER MONEY FROM DATABASE
    private fun getPlayerMoney() {
        val docRef = database.collection("users").document(auth.currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("WIN LOSS", "DocumentSnapshot data: ${document.data}")

                    player.setMoney(document.data!!["money"] as Long)
                    document.data!!["money"] as Long
                    Log.d("MONEY", player.getMoney().toString())

                    placeBet()

                } else {
                    Log.d("WIN LOSS", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("WIN LOSS", "get failed with ", exception)
            }


    }


    // UPDATE WIN, LOSS, DRAW
    private fun updateWinLoss(index: String) {
        val docRef = database.collection("users").document(auth.currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("WIN LOSS", "DocumentSnapshot data: ${document.data}")

                    var value: Long = document.data!![index] as Long
                    val user = HashMap<String, Any>()
                    user[index] = ++value
                    if (index == "wins") {
                        user["money"] = player.getMoney() + player.getBet()
                    } else {
                        user["money"] = player.getMoney() - player.getBet()
                    }

                    database.collection("users").document(auth.currentUser!!.uid)
                        .update(user)
                } else {
                    Log.d("WIN LOSS", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("WIN LOSS", "get failed with ", exception)
            }
    }

    // GET DATA TO DISPLAY WINS, LOSSES, MONEY
    @SuppressLint("SetTextI18n")
    private fun readData() {
        database.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    tv_win.text = "Wins: ${document.data!!["wins"].toString()}"
                    tv_loss.text = "Losses: ${document.data!!["losses"].toString()}"
                    tv_money.text = "Money: $${document.data!!["money"].toString()}"
                    tv_bet.text = "Current Bet: $0"
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

        newView.x = this@MainActivity.width / 2f
        newView.y = 2 * this@MainActivity.height / 2f

        newView.id = numChips
        numChips++

    }

    override fun onStart() {
        super.onStart()
        initGame()
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

        // DISABLE GESTURES
        dialog.setCanceledOnTouchOutside(false)
        dialogShown = true

        val window = dialog.window
        window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<Button>(R.id.b_new_game).setOnClickListener {
            dialog.dismiss()
            dialogShown = false
            initGame()
            // new game
        }
        dialog.show()
    }

    // BET
    private fun placeBet(): Boolean {
        when {
            // ENSURE THAT IF DEALER WINS ON INIT DEAL, ANTE IS STILL PLACED
            dealer.getTotal() == 21 -> {
                player.bet(50)
                tv_bet.text = "Current bet: $50"
            }
            // MEX BET IS $500
            player.getBet() == maxBet -> {
                Toast.makeText(
                    this@MainActivity,
                    "Maximum bet is $500",
                    Toast.LENGTH_LONG
                ).show()
                return true
            }
            // CANNOT BET AFTER CARDS HAVE BEEN DEALT
            bettingClosed -> {
                Toast.makeText(
                    this@MainActivity,
                    "You may only bet after the initial deal",
                    Toast.LENGTH_LONG
                ).show()
                return true
            }
            // CANNOT BET MORE THEN YOU OWN
            player.getBet() >= player.getMoney() -> {
                Toast.makeText(
                    this@MainActivity,
                    "You have no more money to bet",
                    Toast.LENGTH_LONG
                ).show()
                return true
            }
            player.getBet() < 500 -> {
                player.bet(50)
                tv_bet.text = "Current Bet: $${player.getBet()}"
            }

        }
        createChipImageView()
        val rand = Random()
        val floatx = rand.nextInt(100).toFloat()
        val floaty = rand.nextInt(100).toFloat()

        moveTo(
            this@MainActivity.width / 2f + floatx,
            this@MainActivity.height / 2f + floaty,
            findViewById(numChips - 1)
        )
        return true
    }


    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        private var swipedistance = 150

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            // GESTURES DISABLED WHEN DIALOG OPEN
            if (dialogShown) {
                return true
            }
            playerStand()
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            // GESTURES DISABLED WHEN DIALOG OPEN
            if (dialogShown) {
                return true
            }
            return when {
                // SWIPE RIGHT
                e2.x - e1.x > swipedistance -> {
                    Log.d("swipe", "right")
                    playerHit()
                    true
                }

                //SWIPE UP
                e1.y - e2.y > swipedistance -> {
                    Log.d("swipe", "up")
                    placeBet()
                }
                else -> false
            }
        }
    }
}