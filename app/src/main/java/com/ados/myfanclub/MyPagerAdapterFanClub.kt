package com.ados.myfanclub

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.page.*

//class MyPagerAdapterFanClub(fa: FragmentActivity, fanClub: FanClubDTO, member: MemberDTO) : FragmentStateAdapter(fa) {
class MyPagerAdapterFanClub(fm: FragmentManager, life: Lifecycle) : FragmentStateAdapter(fm, life) {
    private val NUM_PAGES = 5

    override fun getItemCount(): Int = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        println("탭 : position ${position}")

        return when (position) {
            0 -> {
                FragmentFanClubInfo()
            }
            1 -> {
                FragmentFanClubMember()
            }
            2 -> {
                FragmentFanClubRank()
            }
            3 -> {
                FragmentFanClubManagement()
            }
            4 -> {
                FragmentPageSchedule.newInstance("fanClub", "")
            }
            else -> {
                FragmentFanClubManagement()
            }
        }
    }
}