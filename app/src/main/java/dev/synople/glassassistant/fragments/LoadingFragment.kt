package dev.synople.glassassistant.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dev.synople.glassassistant.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.Duration


private val TAG = CameraFragment::class.simpleName!!
const val OPEN_AI_API_KEY = ""

class LoadingFragment : Fragment() {
    private val args: LoadingFragmentArgs by navArgs()
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds(30))
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paramPrompt = args.prompt
        val paramImage = args.base64Image

        Log.v(TAG, "paramPrompt: $paramPrompt")
        Log.v(TAG, "paramImage: ${paramImage.substring(0, 10)}")

        view.findViewById<TextView>(R.id.tvLoading).text = paramPrompt

        // :(. String sub = better?
        val requestPayload = JSONObject()
        requestPayload.put("model", "gpt-4-vision-preview")
        val requestPayloadPrompt = JSONObject()
        requestPayloadPrompt.put("type", "text")
        requestPayloadPrompt.put("text", paramPrompt)
        val requestPayloadImage = JSONObject()
        requestPayloadImage.put("type", "image_url")
        val requestPayloadImageUrl = JSONObject()
        requestPayloadImageUrl.put("url", "data:image/jpeg;base64,$paramImage")
        requestPayloadImageUrl.put("detail", "low") // "low", "high", or "auto"
        requestPayloadImage.put("image_url", requestPayloadImageUrl)
        val requestPayloadContent = JSONArray()
        requestPayloadContent.put(requestPayloadPrompt)
        requestPayloadContent.put(requestPayloadImage)
        val requestPayloadMessage = JSONObject()
        requestPayloadMessage.put("role", "user")
        requestPayloadMessage.put("content", requestPayloadContent)
        val requestPayloadMessages = JSONArray()
        requestPayloadMessages.put(requestPayloadMessage)
        requestPayload.put("messages", requestPayloadMessages)
        requestPayload.put("max_tokens", 300)

        val openAiRequest = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $OPEN_AI_API_KEY")
            .post(requestPayload.toString().toRequestBody())
            .build()

        okHttpClient.newCall(openAiRequest).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed while calling OpenAI", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string() ?: ""
                Log.v(TAG, "OpenAI Response: $responseText")
                navigateToResult(responseText)
            }
        })
    }

    private fun navigateToResult(responseText: String) {
        requireActivity().runOnUiThread {
            requireView().findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToResultFragment(responseText))
        }
    }
}