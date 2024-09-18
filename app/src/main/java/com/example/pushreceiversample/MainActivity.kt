package com.example.pushreceiversample

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pushreceiversample.ui.theme.PushReceiverSampleTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PushReceiverSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RequestMmsPermission()
                        ReceivedMessage()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestMmsPermission() {
    val mmsPermissionState = rememberPermissionState(android.Manifest.permission.RECEIVE_MMS)
    when {
        mmsPermissionState.status.isGranted -> Text("MMS permission granted")
        mmsPermissionState.status.shouldShowRationale -> Text("MMS permission denied")
        else -> SideEffect { mmsPermissionState.launchPermissionRequest() }
    }
}

@Composable
fun ReceivedMessage() {
    val context = LocalContext.current
    var time by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        listenToSharedPreferences(context).collectLatest { newMessage ->
            time = newMessage
        }
    }

    if (time.isNotEmpty()) {
        Text(text = "MMS Message Received at: $time")
    }
}

fun listenToSharedPreferences(context: Context) = callbackFlow {
    val sharedPref = context.getSharedPreferences("PushMessages", Context.MODE_PRIVATE)
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "lastReceived") {
            val newMessage = sharedPref.getString(key, "") ?: ""
            trySend(newMessage)
        }
    }

    sharedPref.registerOnSharedPreferenceChangeListener(listener)

    val initialMessage = sharedPref.getString("lastReceived", "") ?: ""
    trySend(initialMessage)

    awaitClose {
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
