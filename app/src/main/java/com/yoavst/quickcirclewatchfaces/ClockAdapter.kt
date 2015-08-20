package com.yoavst.quickcirclewatchfaces

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.support.v13.app.FragmentStatePagerAdapter

public class ClockAdapter(val context: Context, val clocks: List<Clock>) : FragmentStatePagerAdapter((context as Activity).getFragmentManager()) {

    public override fun getItem(i: Int): Fragment {
        return ClockFragment.newInstance(context.getFilesDir().toString() + "/" + clocks[i].id + "/preview.png")
    }

    public override fun getCount(): Int {
        return clocks.size()
    }

}