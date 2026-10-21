package com.cyberity.cvsu

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Cyberity needs the internet: progress is saved online, and hearts refill by
 * server time. While the phone has no working connection, this covers the whole
 * app, blocks taps and back presses, and lifts by itself once it's back. The
 * screen underneath (a level in progress, say) stays as it was.
 */
@Composable
fun NoInternetCover() {
    val online = rememberIsOnline()
    // A short grace period, so switching from Wi-Fi to mobile data doesn't flash it.
    var showing by remember { mutableStateOf(false) }
    LaunchedEffect(online) {
        if (online) showing = false else { delay(1_000L); showing = true }
    }

    BackHandler(enabled = showing) { }
    AnimatedVisibility(visible = showing, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                // Swallows every touch, so nothing underneath can be used offline.
                .pointerInput(Unit) {
                    awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } }
                }
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = AppCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, AppBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).background(AppCyan.copy(alpha = 0.14f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.WifiOff, contentDescription = null, tint = AppCyan, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("No internet connection", color = AppWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Cyberity needs the internet to save your progress. Your level is paused and " +
                                "will continue when you're back online.",
                        color = AppGray, fontSize = 14.sp, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = AppCyan, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Text("Waiting for connection…", color = AppCyan, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/** True when the phone has a connection Android has confirmed reaches the internet. */
internal fun Context.isOnline(): Boolean {
    val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivity.getNetworkCapabilities(connectivity.activeNetwork)?.reachesInternet() == true
}

private fun NetworkCapabilities.reachesInternet(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

/** [isOnline], kept up to date as the connection comes and goes. */
@Composable
internal fun rememberIsOnline(): Boolean {
    val context = LocalContext.current
    val connectivity = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    var online by remember { mutableStateOf(context.isOnline()) }
    DisposableEffect(connectivity) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                online = capabilities.reachesInternet()
            }

            override fun onLost(network: Network) {
                online = false
            }
        }
        connectivity.registerDefaultNetworkCallback(callback)
        onDispose { connectivity.unregisterNetworkCallback(callback) }
    }
    return online
}
