package dev.synople.glassassistant.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import dev.synople.glassassistant.R
import dev.synople.glassassistant.utils.GlassAssistantConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val TAG = ApiKeyFragment::class.simpleName!!

class ApiKeyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_api_key, container, false)!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val test = stringPreferencesKey(GlassAssistantConstants.DATASTORE_OPEN_AI_API_KEY)
        val flow: Flow<String> = requireContext().dataStore.data.map { preferences ->
            preferences[test] ?: ""
        }

        lifecycleScope.launch {
            flow.collect { apiKey ->
                if (apiKey.isEmpty()) {
                    // TODO: Start QR Code scanner
                } else {
                    view.findNavController().navigate(R.id.action_apiKeyFragment_to_cameraFragment)
                }
            }
        }
    }
}