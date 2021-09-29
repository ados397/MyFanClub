package com.ados.myfanclub

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.page.*

//class MyPagerAdapterFanClub(fa: FragmentActivity, fanClub: FanClubDTO, member: MemberDTO) : FragmentStateAdapter(fa) {
class MyPagerAdapterFanClub(fm: FragmentManager, life: Lifecycle, fanClub: FanClubDTO, member: MemberDTO) : FragmentStateAdapter(fm, life) {
    private val NUM_PAGES = 5
    private val fanClubDTO = fanClub
    private val memberDTO = member

    override fun getItemCount(): Int = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FragmentFanClubInfo.newInstance(fanClubDTO, memberDTO)
            }
            1 -> {
                FragmentFanClubMember.newInstance(fanClubDTO, memberDTO)
            }
            2 -> {
                FragmentFanClubRank.newInstance(fanClubDTO, memberDTO)
            }
            3 -> {
                FragmentFanClubManagement.newInstance(fanClubDTO, memberDTO)
            }
            else -> {
                //FragmentFanClubSchedule.newInstance(fanClubDTO!!, fanClubDTO!!)
                FragmentPageSchedule.newInstance(fanClubDTO, memberDTO)
            }
        }
    }
}