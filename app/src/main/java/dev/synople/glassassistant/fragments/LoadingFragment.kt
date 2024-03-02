package dev.synople.glassassistant.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.synople.glassassistant.R

private const val ARG_PROMPT = "argPrompt"
private const val ARG_IMAGE = "argImage"

private val TAG = CameraFragment::class.simpleName!!

class LoadingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var paramPrompt: String? = null
    private var paramImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            paramPrompt = it.getString(ARG_PROMPT)
            paramImage = it.getString(ARG_IMAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.v(TAG, "paramPrompt: $paramPrompt")
        Log.v(TAG, "paramImage: $paramImage")
    }

    companion object {
        @JvmStatic
        fun newInstance(prompt: String, image: String) =
            LoadingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROMPT, prompt)
                    putString(ARG_IMAGE, image)
                }
            }
    }
}