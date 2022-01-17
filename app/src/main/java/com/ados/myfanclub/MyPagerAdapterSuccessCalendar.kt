package com.ados.myfanclub

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.myfanclub.page.FragmentSuccessCalendarDay
import com.ados.myfanclub.page.FragmentSuccessCalendarMonth
import com.ados.myfanclub.page.FragmentSuccessCalendarWeek

//class MyPagerAdapterSuccessCalendar(fa: FragmentActivity, pageIndex: Int) : FragmentStateAdapter(fa) {
class MyPagerAdapterSuccessCalendar(fm: FragmentManager, life: Lifecycle, pageIndex: Int, param: String) : FragmentStateAdapter(fm, life) {
    private val pageCount = Int.MAX_VALUE
    val firstFragmentPosition = Int.MAX_VALUE / 2
    val type = pageIndex
    val param1 = param

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        when (type) {
            0 -> {
                val calendarFragment = FragmentSuccessCalendarDay.newInstance(param1, "")
                calendarFragment.pageIndex = position
                return calendarFragment
            }
            1 -> {
                val calendarFragment = FragmentSuccessCalendarWeek.newInstance(param1, "")
                calendarFragment.pageIndex = position
                return calendarFragment
            }
            else -> {
                val calendarFragment = FragmentSuccessCalendarMonth.newInstance(param1, "")
                calendarFragment.pageIndex = position
                return calendarFragment
            }
        }

    }
}