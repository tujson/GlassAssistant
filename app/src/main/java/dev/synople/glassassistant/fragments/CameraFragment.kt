package dev.synople.glassassistant.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dev.synople.glassassistant.databinding.FragmentCameraBinding
import java.io.ByteArrayOutputStream
import java.io.File


private val TAG = CameraFragment::class.simpleName!!

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null

    private var capturedImage = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCamera()

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_CAMERA) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    Log.v(TAG, "Camera Key Down")
                } else if (event.action == KeyEvent.ACTION_UP) {
                    Log.v(TAG, "Camera Key Up")
                    takePhoto()
                }
                true
            } else {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val file =
            File(requireContext().externalCacheDir.toString() + File.separator + System.currentTimeMillis() + ".png")
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                file
            )
            .build()

        Log.v(TAG, "Taking picture")
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d(TAG, msg)

                    val base64Image =
                        requireContext().contentResolver.openInputStream(output.savedUri!!).use {
                            val bitmap = BitmapFactory.decodeStream(it)
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                        }
                    capturedImage = base64Image
                    Log.v(TAG, "CapturedImage: ${capturedImage.length}")
                    file.delete()

                    // TODO: Figure out audio...
                    startLoading()
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder().setResolutionStrategy(
                        ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_NONE)
                    ).build()
                )
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    /**
     * This method checks for a valid audio recording and picture,
     * then navigates to LoadingFragment.
     */
    private fun startLoading() {
        if (capturedImage != "") {
            requireView().findNavController().navigate(CameraFragmentDirections.actionCameraFragmentToLoadingFragment("In one sentence, describe the primary object in this image.", capturedImage))
        }
    }

}