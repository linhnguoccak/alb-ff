package com.alb.ff

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ), 1
            )
            return
        }

        checkShizukuAndLaunch()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && Settings.canDrawOverlays(this)) {
            checkShizukuAndLaunch()
        }
        finish()
    }

    private fun checkShizukuAndLaunch() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(1001)
                }
            }
        } catch (_: Exception) {}
        launch()
    }

    private fun launch() {
        startForegroundService(Intent(this, OverlayService::class.java))
        finish()
    }
}
