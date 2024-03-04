package dev.synople.glassassistant.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dev.synople.glassassistant.R
import org.json.JSONObject

private val TAG = CameraFragment::class.simpleName!!

class ResultFragment : Fragment() {

    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_result, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jsonResponse = JSONObject(args.response)
        val content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
            .getString("content")

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
    }
}