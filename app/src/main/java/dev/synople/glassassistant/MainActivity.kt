package dev.synople.glassassistant

import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import dev.synople.glassassistant.utils.GlassGesture
import dev.synople.glassassistant.utils.GlassGestureDetector
import org.greenrobot.eventbus.EventBus


private val TAG = MainActivity::class.simpleName!!

class MainActivity : FragmentActivity() {

    private val ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 101;

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var glassGestureDetector: GlassGestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(PERMISSIONS, ASK_MULTIPLE_PERMISSION_REQUEST_CODE)
        glassGestureDetector =
            GlassGestureDetector(this, object : GlassGestureDetector.OnGestureListener {
                override fun onGesture(gesture: GlassGestureDetector.Gesture?): Boolean {
                    val isHandled = when (gesture) {
                        GlassGestureDetector.Gesture.TWO_FINGER_TAP -> true
                        GlassGestureDetector.Gesture.TAP -> true
                        else -> false
                    }

                    if (isHandled) {
                        EventBus.getDefault().post(GlassGesture(gesture!!))
                    }

                    return isHandled
                }
            })

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return glassGestureDetector!!.onTouchEvent(ev!!) || super.dispatchTouchEvent(ev)
    }
}