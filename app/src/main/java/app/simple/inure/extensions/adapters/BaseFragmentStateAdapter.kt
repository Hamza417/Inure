package app.simple.inure.extensions.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class BaseFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    abstract override fun getItemCount(): Int
    abstract override fun createFragment(position: Int): Fragment
    abstract fun getPageTitle(position: Int): String
}