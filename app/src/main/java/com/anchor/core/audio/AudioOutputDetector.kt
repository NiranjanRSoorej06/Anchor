package com.anchor.core.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log

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

    private companion object {
        const val TAG = "AudioOutputDetector"
    }

    override fun isPrivateHeadsetConnected(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return false

        try {
            @Suppress("DEPRECATION")
            if (audioManager.isBluetoothA2dpOn || audioManager.isWiredHeadsetOn || audioManager.isBluetoothScoOn) {
                return true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (device in devices) {
                    when (device.type) {
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_USB_HEADSET,
                        AudioDeviceInfo.TYPE_USB_DEVICE,
                        AudioDeviceInfo.TYPE_BLE_HEADSET,
                        AudioDeviceInfo.TYPE_BLE_SPEAKER,
                        AudioDeviceInfo.TYPE_HEARING_AID -> return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying audio output devices: ${e.message}")
        }

        return false
    }
}

/**
 * Debug/fake detector for host unit testing and Compose previews.
 */
class FakeAudioOutputDetector(var headsetConnected: Boolean = false) : AudioOutputDetector {
    override fun isPrivateHeadsetConnected(): Boolean = headsetConnected
}
