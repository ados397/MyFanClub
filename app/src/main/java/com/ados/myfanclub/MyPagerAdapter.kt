package com.ados.myfanclub

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.page.*

class MyPagerAdapter(fa: FragmentActivity, fanClub: FanClubDTO?, member: MemberDTO?) : FragmentStateAdapter(fa) {
    private val NUM_PAGES = 4
    private val fanClubDTO = fanClub
    private val memberDTO = member

    override fun getItemCount(): Int  = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FragmentPageDashboard.newInstance(fanClubDTO, memberDTO)
            }
            1 -> {
                FragmentPageFanClub.newInstance(fanClubDTO, memberDTO)
            }
            2 -> {
                FragmentPageSchedule.newInstance(null, null)
            }
            else -> {
                FragmentPageAccount.newInstance(fanClubDTO, memberDTO)
            }
        }
    }
}