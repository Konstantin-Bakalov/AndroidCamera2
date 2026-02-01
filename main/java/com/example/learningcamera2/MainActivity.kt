package com.example.learningcamera2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import java.security.Permissions

class MainActivity : ComponentActivity() {
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread

    var permissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        getPermissions()

        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("camera thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        val openCameraButton = findViewById<Button>(R.id.open)

        openCameraButton.setOnClickListener @androidx.annotation.RequiresPermission(android.Manifest.permission.CAMERA) { v -> if (permissionGranted) openCamera() }
        textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
            @RequiresPermission(Manifest.permission.CAMERA)
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                if (permissionGranted)
                    openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                TODO("Not yet implemented")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice.close()
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun openCamera() {
       cameraManager.openCamera(cameraManager.cameraIdList[0], object: CameraDevice.StateCallback() {
           override fun onOpened(camera: CameraDevice) {
              cameraDevice = camera

               captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
               var surface = Surface(textureView.surfaceTexture)
               captureRequest.addTarget(surface)

               // TODO fix
               cameraDevice.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback() {
                   override fun onConfigured(session: CameraCaptureSession) {
                       cameraCaptureSession = session
                       cameraCaptureSession.setRepeatingRequest(captureRequest.build(), null, null)
                   }

                   override fun onConfigureFailed(session: CameraCaptureSession) {
                       throw IllegalAccessError("this is me")
                   }
               }, handler)
           }

           override fun onDisconnected(camera: CameraDevice) {
               TODO("Not yet implemented")
           }

           override fun onError(camera: CameraDevice, error: Int) {
               TODO("Not yet implemented")
           }
       }, handler)
    }

    private fun getPermissions() {
        var permissionsList = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(android.Manifest.permission.CAMERA)
        }

        if (permissionsList.isNotEmpty()) {
            requestPermissions(permissionsList.toTypedArray(), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                getPermissions()
            }
        }

        permissionGranted = true
    }
}
