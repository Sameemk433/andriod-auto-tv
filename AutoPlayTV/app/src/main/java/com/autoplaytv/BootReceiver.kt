package com.autoplaytv

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d("BootReceiver", "Boot detected, launching AutoPlay TV")
            
            // Wait 10 seconds for USB/storage to mount
            Handler(Looper.getMainLooper()).postDelayed({
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                             Intent.FLAG_ACTIVITY_CLEAR_TOP or
                             Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("autostart", true)
                }
                try {
                    context.startActivity(launchIntent)
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to start: ${e.message}")
                }
            }, 10000) // 10 second delay for USB mount
        }
    }
}