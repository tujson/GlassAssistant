package dev.synople.glassassistant.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import dev.synople.glassassistant.R
import dev.synople.glassassistant.dataStore
import dev.synople.glassassistant.utils.GlassAssistantConstants
import dev.synople.glassassistant.utils.GlassGesture
import dev.synople.glassassistant.utils.GlassGestureDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.system.exitProcess

private val TAG = ApiKeyFragment::class.simpleName!!

class ApiKeyFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_api_key, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val flow: Flow<String> = requireContext().dataStore.data.map { preferences ->
            preferences[GlassAssistantConstants.DATASTORE_OPEN_AI_API_KEY] ?: ""
        }

        lifecycleScope.launch {
            flow.collect { apiKey ->
                if (apiKey.isEmpty()) {
                    startQrCodeScanner()
                } else {
                    view.findNavController().navigate(R.id.action_apiKeyFragment_to_cameraFragment)
                    this.coroutineContext.job.cancel()
                }
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

            GlassGestureDetector.Gesture.SWIPE_DOWN -> {
                requireActivity().finishAffinity()
                exitProcess(30000)
            }

            else -> {}
        }
    }

    private fun startQrCodeScanner() {
        IntentIntegrator.forSupportFragment((this as Fragment))
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            .setPrompt("Scan a QR code with your OpenAI API Key.")
            .setBeepEnabled(false)
            .setBarcodeImageEnabled(false)
            .setTimeout(30000)
            .initiateScan()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let { barcodeResult ->
            barcodeResult.contents?.let { barcodeContents ->
                lifecycleScope.launch {
                    requireContext().dataStore.edit { settings ->
                        settings[GlassAssistantConstants.DATASTORE_OPEN_AI_API_KEY] =
                            barcodeContents
                    }
                }
            } ?: {
                requireView().findViewById<TextView>(R.id.tvApiKey).text =
                    "Something went wrong. Exit the app and try again."
            }
        }
    }
}