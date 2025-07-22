package com.nighteyecare.app.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nighteyecare.app.ui.fragments.TutorialPageFragment

class TutorialPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TutorialPageFragment.newInstance(1)
            1 -> TutorialPageFragment.newInstance(2)
            2 -> TutorialPageFragment.newInstance(3)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
