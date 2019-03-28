package viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.example.cse438.blackjack.model.LeaderboardRank
import com.google.firebase.firestore.FirebaseFirestore

class LeaderboardRankViewModel(application: Application) : AndroidViewModel(application) {

    private var _rankList: MutableLiveData<ArrayList<LeaderboardRank>> = MutableLiveData()


    @SuppressLint("SetTextI18n")
    fun getLeaderBoardData(database: FirebaseFirestore): MutableLiveData<ArrayList<LeaderboardRank>> {
        val array = ArrayList<LeaderboardRank>()
        database.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val leaderboardRank = document.toObject(LeaderboardRank::class.java)
                    Log.d("LEADERBOARD", "${document.id} => ${document.data}")
                    leaderboardRank.updateRatio()
                    Log.d("RATIO", "${leaderboardRank.ratio}")
                    array.add(element = leaderboardRank)
                }

                _rankList.value = ArrayList(array.sortedWith(compareBy { it.ratio }).reversed())
            }
            .addOnFailureListener { exception ->
                Log.w("THIS", "Error getting documents.", exception)
            }

        return _rankList
    }
}