package com.example.cse438.blackjack.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.cse438.blackjack.R
import com.example.cse438.blackjack.model.LeaderboardRank
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_leaderboard.*
import kotlinx.android.synthetic.main.leaderboardrank_list_item.view.*
import viewmodel.LeaderboardRankViewModel
import java.text.DecimalFormat

class LeaderboardActivity : AppCompatActivity() {

    private val database = FirebaseFirestore.getInstance()
    private lateinit var viewModel: LeaderboardRankViewModel
    private var adapter = RankListAdapter()
    private var rankList: ArrayList<LeaderboardRank> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

    }

    override fun onStart() {
        super.onStart()

        // set up recyclerView
        leader_board.layoutManager = LinearLayoutManager(this)
        viewModel = ViewModelProviders.of(this).get(LeaderboardRankViewModel::class.java)

        // observe updates to recyclerView as the come from async task
        val observer = Observer<ArrayList<LeaderboardRank>> {
            leader_board.adapter = adapter
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(p0: Int, p1: Int): Boolean {
                    return rankList[p0].email == rankList[p1].email
                }

                override fun getOldListSize(): Int {
                    return rankList.size
                }

                override fun getNewListSize(): Int {
                    if (it == null) {
                        return 0
                    }
                    return it.size
                }

                override fun areContentsTheSame(p0: Int, p1: Int): Boolean {
                    return rankList[p0] == rankList[p1]
                }
            })
            result.dispatchUpdatesTo(adapter)
            rankList = it ?: ArrayList()
        }

        viewModel.getLeaderBoardData(database).observe(this, observer)

    }


    inner class RankListAdapter : RecyclerView.Adapter<RankListAdapter.RankListViewHolder>() {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RankListViewHolder {
            val itemView = LayoutInflater.from(p0.context).inflate(R.layout.leaderboardrank_list_item, p0, false)
            return RankListViewHolder(itemView)
        }

        override fun onBindViewHolder(listItem: RankListViewHolder, p1: Int) {
            val rank = rankList[p1]

            listItem.userEmail.text = rank.email!!.substringBeforeLast("@")
            listItem.userMoney.text = "$${(rank.money!!)}"
            var num = (rank.wins!!.toDouble() / (rank.losses!!.toDouble()))
            if (rank.losses == 0) {
                num = rank.wins.toDouble()
            }
            val df = DecimalFormat("#.##")
            listItem.userWinLossRatio.text = df.format(num)

        }

        override fun getItemCount(): Int {
            return rankList.size
        }


        inner class RankListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var userEmail: TextView = itemView.email
            var userMoney: TextView = itemView.money
            var userWinLossRatio: TextView = itemView.win_loss_ratio
        }
    }
}
