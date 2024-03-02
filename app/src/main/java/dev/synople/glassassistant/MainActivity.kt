package dev.synople.glassassistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat


private const val ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 101;

class MainActivity : AppCompatActivity() {

    private val ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 101;

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(PERMISSIONS, ASK_MULTIPLE_PERMISSION_REQUEST_CODE)
    }
}