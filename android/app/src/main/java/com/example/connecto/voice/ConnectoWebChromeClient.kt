package com.example.connecto.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import androidx.core.content.ContextCompat

/**
 * Defensive WebChromeClient that explicitly approves PermissionRequest.RESOURCE_AUDIO_CAPTURE
 * after checking Android runtime RECORD_AUDIO permission.
 */
class ConnectoWebChromeClient(private val context: Context) : WebChromeClient() {
    override fun onPermissionRequest(request: PermissionRequest) {
        val resources = request.resources
        val grantedList = mutableListOf<String>()

        for (resource in resources) {
            if (resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    grantedList.add(resource)
                }
            } else if (resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    grantedList.add(resource)
                }
            }
        }

        if (grantedList.isNotEmpty()) {
            request.grant(grantedList.toTypedArray())
        } else {
            request.deny()
        }
    }
}
