package com.matadesigns.spotlightandroid.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.matadesigns.spotlight.SpotlightBuilder
import com.matadesigns.spotlight.abstraction.SpotlightListener
import com.matadesigns.spotlight.config.SpotlightMessageGravity
import com.matadesigns.spotlightandroid.R

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var builder: SpotlightBuilder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.main_fragment, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()
        val center = requireView().findViewById<View>(R.id.message_center)

        val bottomRight = requireView().findViewById<View>(R.id.message_bottom_right)
        val bottomLeft = requireView().findViewById<View>(R.id.message_bottom_left)
        val topLeft = requireView().findViewById<View>(R.id.message_top_left)
        val topRight = requireView().findViewById<View>(R.id.message_top_right)

        val leftContainer = requireView().findViewById<View>(R.id.left_container)

        builder = SpotlightBuilder(requireContext())
            .setTargetView(center)
            .setInset(20)
            .setListener(object : SpotlightListener {
                override fun onEnd(targetView: View?) {
                    when (targetView) {
                        center -> {
                            builder.setTargetView(bottomRight)
                        }
                        bottomRight -> {
                            builder.setTargetView(bottomLeft)
                        }
                        bottomLeft -> {
                            builder.setTargetView(topLeft)
                        }
                        topLeft -> {
                            builder.setTargetView(topRight)
                        }
                        topRight -> {
                            builder.setTargetView(leftContainer)
                        }
                        leftContainer -> {
                            builder.setTargetView(center)
                        }
                    }
                    builder.build().startSpotlight()
                }

                override fun onStart(targetView: View?) {

                }

            })
        val spotlightView = builder.build()
        spotlightView.startSpotlight()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}