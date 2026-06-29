package com.parentdashboard.presentation.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.parentdashboard.R
import com.parentdashboard.domain.model.P2PConnectionType

@Composable
fun P2PConnectionType.displayName(): String = stringResource(
    when (this) {
        P2PConnectionType.NEARBY -> R.string.p2p_connection_nearby
        P2PConnectionType.WIFI_LAN -> R.string.p2p_connection_wifi_lan
        P2PConnectionType.WIFI_DIRECT -> R.string.p2p_connection_wifi_direct
    }
)
