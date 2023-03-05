package com.example.whisper.ui.basecontacts

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whisper.ProfileFragment
import com.example.whisper.ui.contacts.ContactsFragment
import com.example.whisper.ui.recentchats.RecentChatsFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = FRAGMENTS_COUNT

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> RecentChatsFragment()
            1 -> ContactsFragment()
            else -> ProfileFragment()
        }

    companion object {
        private const val FRAGMENTS_COUNT = 3
    }
}
