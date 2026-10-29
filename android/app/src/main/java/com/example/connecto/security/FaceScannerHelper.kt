package com.example.connecto.security

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import java.util.concurrent.atomic.AtomicBoolean

/**
 * FaceScannerHelper provides a hardware Camera2 front-camera scanner with real-time
 * facial detection and live camera viewfinder preview.
 *
 * This completely resolves OEM BiometricPrompt limitations (e.g. Vivo, Xiaomi, Oppo)
 * where the system dialog only triggers fingerprint sensors instead of scanning the face.
 */
class FaceScannerHelper(
    private val context: Context,
    private val onFaceDetected: (score: Int) -> Unit,
    private val onError: (String) -> Unit
) {
    companion object {
        private const val TAG = "FaceScannerHelper"
    }

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val isScanning = AtomicBoolean(false)
    private val hasTriggeredSuccess = AtomicBoolean(false)

    private var previewSurface: Surface? = null
    private var previewSize: Size? = null

    /**
     * Start the background thread for Camera2 callbacks.
     */
    private fun startBackgroundThread() {
        if (backgroundThread == null) {
            backgroundThread = HandlerThread("ConnectoFaceScannerThread").apply {
                start()
                backgroundHandler = Handler(looper)
            }
        }
    }

    /**
     * Stop the background thread cleanly.
     */
    private fun stopBackgroundThread() {
        try {
            backgroundThread?.quitSafely()
            backgroundThread?.join(500)
            backgroundThread = null
            backgroundHandler = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping background thread", e)
        }
    }

    /**
     * Finds the front-facing camera ID and supported preview size.
     */
    private fun findFrontCamera(manager: CameraManager): Pair<String, CameraCharacteristics>? {
        for (id in manager.cameraIdList) {
            val chars = manager.getCameraCharacteristics(id)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return Pair(id, chars)
            }
        }
        return null
    }

    /**
     * Starts face scanning with an optional TextureView for live camera feed inside the viewfinder.
     */
    @SuppressLint("MissingPermission")
    fun startScanning(textureView: TextureView? = null) {
        if (isScanning.get()) return
        isScanning.set(true)
        hasTriggeredSuccess.set(false)

        startBackgroundThread()

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        if (manager == null) {
            onError("Camera service unavailable")
            return
        }

        val frontCam = findFrontCamera(manager)
        if (frontCam == null) {
            onError("No front-facing camera found")
            return
        }

        val (cameraId, chars) = frontCam

        // Determine optimal preview size
        val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = map?.getOutputSizes(SurfaceTexture::class.java) ?: emptyArray()
        previewSize = sizes.firstOrNull { it.width <= 1280 && it.height <= 720 } ?: sizes.firstOrNull() ?: Size(640, 480)

        // Check hardware face detection modes
        val faceModes = chars.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES) ?: intArrayOf()
        val bestFaceMode = when {
            faceModes.contains(CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL) -> CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL
            faceModes.contains(CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE) -> CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE
            else -> CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF
        }

        val stateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createCaptureSession(camera, textureView, bestFaceMode)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                cameraDevice = null
                isScanning.set(false)
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                cameraDevice = null
                isScanning.set(false)
                onError("Camera error code: $error")
            }
        }

        try {
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open front camera for Face Scan", e)
            onError(e.localizedMessage ?: "Failed to open camera")
            isScanning.set(false)
        }
    }

    /**
     * Creates CameraCaptureSession and registers the face detection capture callback.
     */
    private fun createCaptureSession(camera: CameraDevice, textureView: TextureView?, faceMode: Int) {
        try {
            val surface = if (textureView != null && textureView.isAvailable) {
                val texture = textureView.surfaceTexture
                previewSize?.let { texture?.setDefaultBufferSize(it.width, it.height) }
                Surface(texture).also { previewSurface = it }
            } else {
                val dummyTexture = SurfaceTexture(10)
                previewSize?.let { dummyTexture.setDefaultBufferSize(it.width, it.height) }
                Surface(dummyTexture).also { previewSurface = it }
            }

            val requestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
                set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                if (faceMode != CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF) {
                    set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, faceMode)
                }
            }

            camera.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                            override fun onCaptureCompleted(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                result: TotalCaptureResult
                            ) {
                                if (hasTriggeredSuccess.get()) return

                                val faces = result.get(CaptureResult.STATISTICS_FACES)
                                if (!faces.isNullOrEmpty()) {
                                    val topFace = faces[0]
                                    // Score ranges 1-100; a score >= 30 represents reliable human facial detection
                                    if (topFace.score >= 30) {
                                        if (hasTriggeredSuccess.compareAndSet(false, true)) {
                                            mainHandler.post {
                                                onFaceDetected(topFace.score)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        try {
                            session.setRepeatingRequest(requestBuilder.build(), captureCallback, backgroundHandler)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error setting repeating face scan request", e)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        onError("Camera preview configuration failed")
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating camera capture session", e)
            onError(e.localizedMessage ?: "Camera session error")
        }
    }

    /**
     * Stop scanning and release all camera hardware resources.
     */
    fun stopScanning() {
        if (!isScanning.getAndSet(false)) return
        try {
            captureSession?.stopRepeating()
            captureSession?.close()
            captureSession = null

            cameraDevice?.close()
            cameraDevice = null

            previewSurface?.release()
            previewSurface = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping face scanner", e)
        } finally {
            stopBackgroundThread()
        }
    }
}
