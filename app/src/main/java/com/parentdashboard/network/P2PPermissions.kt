package com.parentdashboard.network

import android.Manifest
import android.os.Build
import com.parentdashboard.domain.model.P2PConnectionType

object P2PPermissions {
    fun requiredFor(type: P2PConnectionType): Array<String> {
        val list = mutableListOf<String>()
        when (type) {
            P2PConnectionType.NEARBY, P2PConnectionType.WIFI_DIRECT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    list += Manifest.permission.BLUETOOTH_CONNECT
                    list += Manifest.permission.BLUETOOTH_SCAN
                    list += Manifest.permission.NEARBY_WIFI_DEVICES
                } else {
                    list += Manifest.permission.ACCESS_FINE_LOCATION
                }
            }
            P2PConnectionType.WIFI_LAN -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    list += Manifest.permission.NEARBY_WIFI_DEVICES
                }
            }
        }
        return list.distinct().toTypedArray()
    }
}
