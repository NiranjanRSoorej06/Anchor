package com.anchor.core.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

/**
 * Detects whether private listening devices (Bluetooth earbuds, A2DP, SCO,
 * BLE headset, or wired headphones) are connected to the system.
 */
interface AudioOutputDetector {
    fun isPrivateHeadsetConnected(): Boolean
}

/**
 * System implementation backed by Android [AudioManager].
 */
class SystemAudioOutputDetector(private val context: Context) : AudioOutputDetector {

    override fun isPrivateHeadsetConnected(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> return true
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    device.type == AudioDeviceInfo.TYPE_BLE_HEADSET
                ) {
                    return true
                }
            }
        }

        @Suppress("DEPRECATION")
        return audioManager.isBluetoothA2dpOn || audioManager.isWiredHeadsetOn
    }
}

/**
 * Debug/fake detector for host unit testing and Compose previews.
 */
class FakeAudioOutputDetector(var headsetConnected: Boolean = false) : AudioOutputDetector {
    override fun isPrivateHeadsetConnected(): Boolean = headsetConnected
}
