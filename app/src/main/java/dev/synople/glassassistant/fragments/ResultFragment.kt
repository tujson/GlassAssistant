package dev.synople.glassassistant.fragments

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dev.synople.glassassistant.R
import dev.synople.glassassistant.utils.GlassGesture
import dev.synople.glassassistant.utils.GlassGestureDetector
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject

private val TAG = ResultFragment::class.simpleName!!

class ResultFragment : Fragment() {

    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_result, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = try {
            JSONObject(args.response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: JSONException) {
            Log.w(TAG, "Unexpected response from OpenAI: ${args.response}")
            args.response
        }

        view.findViewById<TextView>(R.id.tvResult).text = content

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_CAMERA) {
                if (event.action == KeyEvent.ACTION_UP) {
                    view.findNavController().navigate(R.id.action_resultFragment_to_cameraFragment)
                }
                true
            } else {
                false
            }
        }

        EventBus.getDefault().register(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onGesture(glassGesture: GlassGesture) {
        when (glassGesture.gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                requireView().findNavController()
                    .navigate(R.id.action_resultFragment_to_cameraFragment)
            }

            GlassGestureDetector.Gesture.SWIPE_DOWN -> {
                requireView().findNavController()
                    .navigate(R.id.action_resultFragment_to_cameraFragment)
            }

            else -> {}
        }
    }
}