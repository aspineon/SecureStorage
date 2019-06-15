package com.ehsanmashhadi.securestorage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentUtil {

    companion object {

        fun replaceFragmentWithFragment(fragmentManager: FragmentManager, fragment: Fragment, frameId: Int) {

            checkNotNull(fragmentManager)
            checkNotNull(fragment)
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(frameId, fragment)
            fragmentTransaction.commit()
        }


        fun replaceFragmentWithFragmentWithAdd(fragmentManager: FragmentManager, fragment: Fragment, frameId: Int) {

            checkNotNull(fragmentManager)
            checkNotNull(fragment)
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(frameId, fragment)
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
            fragmentTransaction.commit()
        }

        private fun <T> checkNotNull(reference: T?): T {

            if (reference == null) {
                throw NullPointerException()
            }
            return reference
        }
    }
}