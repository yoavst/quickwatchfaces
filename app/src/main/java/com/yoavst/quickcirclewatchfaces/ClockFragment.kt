package com.yoavst.quickcirclewatchfaces

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.yoavst.kotlin.Bundle
import kotlinx.android.synthetic.clock_fragment.imageView
import java.io.File


public class ClockFragment : Fragment() {
    private val previewImage: String
        get() {
           return getArguments().getString(Extra_PreviewImage)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.clock_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(this).load(previewImage).into(imageView)
    }

    companion object {
        private val Extra_PreviewImage = "previewImageExtra"
        public fun newInstance(previewImage: String): Fragment {
            val fragment = ClockFragment()
            fragment.setArguments(Bundle { putString(Extra_PreviewImage, previewImage) })
            return fragment
        }
    }
}