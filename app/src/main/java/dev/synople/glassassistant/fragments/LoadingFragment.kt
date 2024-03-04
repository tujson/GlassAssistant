package dev.synople.glassassistant.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dev.synople.glassassistant.R
import dev.synople.glassassistant.dataStore
import dev.synople.glassassistant.utils.GlassAssistantConstants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.time.Duration


private val TAG = CameraFragment::class.simpleName!!

class LoadingFragment : Fragment() {
    private val args: LoadingFragmentArgs by navArgs()
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds(30))
        .build()

    private var openAiApiKey = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_loading, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            requireContext().dataStore.data.firstOrNull { true }?.let { value ->
                openAiApiKey = value[GlassAssistantConstants.DATASTORE_OPEN_AI_API_KEY] ?: ""
            }

            args.recorderFile?.let {
                getSpeechResponse(it)
            } ?: getVisionResponse(GlassAssistantConstants.DEFAULT_PROMPT, args.imageFile)
        }
    }

    private fun getSpeechResponse(recorderFile: File) {
        val speechRequestPayload = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                recorderFile.name,
                recorderFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
            )
            .addFormDataPart("model", "whisper-1")
            .build()
        val openAiSpeechRequest = Request.Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .addHeader("Content-Type", "multipart/form-data")
            .addHeader("Authorization", "Bearer $openAiApiKey")
            .post(speechRequestPayload)
            .build()
        okHttpClient.newCall(openAiSpeechRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed while calling OpenAI Speech", e)
            }

            override fun onResponse(call: Call, response: Response) {
                recorderFile.delete()
                val responseText = response.body?.string() ?: ""
                Log.d(TAG, "OpenAI Speech Response: $responseText")

                val jsonResponse = JSONObject(responseText)
                val content = jsonResponse.getString("text")

                requireActivity().runOnUiThread {
                    requireView().findViewById<TextView>(R.id.tvLoading).text = content
                }

                getVisionResponse(content, args.imageFile)
            }
        })
    }

    private fun getVisionResponse(prompt: String, imageFile: File) {
        // Load imageFile to Base64 string
        val base64Image =
            requireContext().contentResolver.openInputStream(imageFile.toUri()).use {
                val bitmap = BitmapFactory.decodeStream(it)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            }
        imageFile.delete()

        // :(. String sub = better?
        val visionRequestPayload = JSONObject()
        visionRequestPayload.put("model", "gpt-4-vision-preview")
        val requestPayloadPrompt = JSONObject()
        requestPayloadPrompt.put("type", "text")
        requestPayloadPrompt.put("text", prompt)
        val requestPayloadImage = JSONObject()
        requestPayloadImage.put("type", "image_url")
        val requestPayloadImageUrl = JSONObject()
        requestPayloadImageUrl.put("url", "data:image/jpeg;base64,$base64Image")
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
        visionRequestPayload.put("messages", requestPayloadMessages)
        visionRequestPayload.put("max_tokens", 300)

        val openAiVisionRequest = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $openAiApiKey")
            .post(visionRequestPayload.toString().toRequestBody())
            .build()

        okHttpClient.newCall(openAiVisionRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed while calling OpenAI vision", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string() ?: ""
                Log.d(TAG, "OpenAI Vision Response: $responseText")

                requireActivity().runOnUiThread {
                    requireView().findNavController().navigate(
                        LoadingFragmentDirections.actionLoadingFragmentToResultFragment(responseText)
                    )
                }
            }
        })
    }
}