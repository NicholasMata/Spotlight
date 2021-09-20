package com.matadesigns.spotlightandroid.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.HandlerCompat.postDelayed
import androidx.lifecycle.ViewModelProvider
import com.matadesigns.spotlight.SpotlightBuilder
import com.matadesigns.spotlight.abstraction.SpotlightListener
import com.matadesigns.spotlight.config.SpotlightDismissType
import com.matadesigns.spotlight.config.SpotlightMessageGravity
import com.matadesigns.spotlightandroid.MainActivity
import com.matadesigns.spotlightandroid.R
import kotlinx.android.synthetic.main.main_fragment.view.*
import com.matadesigns.spotlight.utils.*

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
        val refreshLayout = root.left_container
        refreshLayout.setOnRefreshListener {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val number: Int = root.left_container_text.text.toString().toInt()
                root.left_container_text.text = (number + 1).toString()
                refreshLayout.isRefreshing = false
                handler.postDelayed({
                    refreshLayout.spotlightView?.endSpotlight()
                }, 1000)
            }, 1000)
        }
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
        val rightContainer = requireView().findViewById<View>(R.id.right_container)

        builder = SpotlightBuilder(requireContext())
            .setTargetView(center)
            .setInset(20)
            .setTitle("TAP")
            .setDescription("on the text")
            .setDismissType(SpotlightDismissType.targetView)
            .setListener(object : SpotlightListener {
                override fun onEnd(targetView: View?) {
                    when (targetView) {
                        center -> {
                            builder
                                .setTargetView(bottomRight)
                        }
                        bottomRight -> {
                            builder
                                .setTargetView(bottomLeft)
                        }
                        bottomLeft -> {
                            builder
                                .setTargetView(topLeft)
                        }
                        topLeft -> {
                            builder
                                .setTargetView(topRight)
                                .setPassThrough(true)
                        }
                        topRight -> {
                            builder
                                .setPassThrough(false)
                                .setTitle("PULL DOWN")
                                .setDescription("to refresh the value")
                                .setMessageLayout(R.layout.spotlight_skippable_message_view)
                                .setTargetView(leftContainer)
                        }
                        leftContainer -> {
                            builder
                                .setMessageLayout(com.matadesigns.spotlight.R.layout.simple_message)
                                .setDismissType(SpotlightDismissType.anywhere)
                                .setTitle("TAP")
                                .setDescription("anywhere")
                                .setTargetView(rightContainer)
                        }
                        rightContainer -> {
                            builder
                                .setDismissType(SpotlightDismissType.targetView)
                                .setTitle("TAP")
                                .setDescription("on the text")
                                .setTargetView(center)
                        }
                    }
                    builder.build().startSpotlight(animate = false)
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
        requireView().message_top_right.isClickable = true
        requireView().message_top_right.setOnClickListenerWithDebounce({
            Log.i("DebounceOnClickListener", "Clicked")
            builder.current?.endSpotlight()
        })
    }

}