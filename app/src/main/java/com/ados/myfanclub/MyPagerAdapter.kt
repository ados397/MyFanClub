package com.ados.myfanclub

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.page.*

class MyPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val NUM_PAGES = 4

    override fun getItemCount(): Int  = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FragmentPageDashboard.newInstance("", "day")
            }
            1 -> {
                FragmentPageFanClub()
            }
            2 -> {
                FragmentPageSchedule.newInstance("personal", "")
            }
            else -> {
                FragmentPageAccount()
            }
        }
    }
}