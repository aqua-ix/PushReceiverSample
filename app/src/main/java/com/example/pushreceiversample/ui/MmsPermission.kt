package com.example.pushreceiversample.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MmsPermission() {
    val context = LocalContext.current
    val mmsPermissionState = rememberPermissionState(android.Manifest.permission.RECEIVE_MMS)
    var shouldShowRationale by remember { mutableStateOf(false) }

    LaunchedEffect(mmsPermissionState.status) {
        shouldShowRationale = mmsPermissionState.status.shouldShowRationale
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                mmsPermissionState.status.isGranted -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "MMS Permission Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            PermissionStatusRow(
                icon = when {
                    mmsPermissionState.status.isGranted -> Icons.Default.CheckCircle
                    else -> Icons.Default.Warning
                },
                label = when {
                    mmsPermissionState.status.isGranted -> "RECEIVE_MMS permission granted"
                    shouldShowRationale -> "RECEIVE_MMS permission denied"
                    else -> "Permission required"
                },
                value = when {
                    mmsPermissionState.status.isGranted -> "You can receive MMS messages"
                    else -> "RECEIVE_MMS permission is needed to handle MMS messages"
                },
                tint = when {
                    mmsPermissionState.status.isGranted -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
            if (!mmsPermissionState.status.isGranted) {
                Button(
                    onClick = {
                        if (shouldShowRationale) {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            context.startActivity(intent)
                        } else {
                            mmsPermissionState.launchPermissionRequest()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        if (shouldShowRationale) "Open Settings"
                        else "Request Permission"
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionStatusRow(
    icon: ImageVector,
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Permission status icon",
            tint = tint
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = tint
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = tint.copy(alpha = 0.7f)
            )
        }
    }
}
