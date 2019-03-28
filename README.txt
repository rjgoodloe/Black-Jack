In this file you should include:

Any information you think we should know about your submission
* Is there anything that doesn't work? Why?
		- If you go to the leaderboard activity and come back it deals you a new hand rather than picking up where you left off
		- GameActivity.kt and Game.kt were an attempt to refactor my code. They are extremely close to working, but I did not have time to finish.
			They are not integrated with any files currently.
		- If a user runs out of money there is no way to get more
* Is there anything that you did that you feel might be unclear? Explain it here.
		- Gestures: 
				swipe right: hit
				double tap: stand
				swipe up: bet


A description of the creative portion of the assignment
* Describe your feature
	I implemented the ability to place bets.
* Why did you choose this feature?
	I think that it is an important part of the game of blackjack
* How did you implement it?
	I created a new vector image asset with which to display the chip. I added a swipe up gesture that creates a chip image view and animates it so that it
	"throws the chip into a pile". Each chip is worth $50. When the user wins they are rewarded 2x their bet, losing removes their bet amount from their money, 
	and a draw reqults in no money lost or gained. There is an ante of $50 placed at the beginning of each game. If the dealer wins immediately with a 21, the ante is lost.
	There is a maximum bet of $500, and the user may not bet after they hit or stand. The users start with $1000. 
