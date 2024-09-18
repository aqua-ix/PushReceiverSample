package com.example.pushreceiversample.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState

data class SimStatusData(
    val status: SimStatusEnum,
    val phoneNumber: String?,
    val simOperatorName: String?,
    val subscriptionId: Int?
)

enum class SimStatusEnum {
    READY, NOT_READY, ABSENT, NETWORK_LOCKED, PIN_REQUIRED, PUK_REQUIRED, UNKNOWN
}

@SuppressWarnings("MissingPermission")
fun getSimInfo(context: Context): SimStatusData {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val subscriptionManager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    val status = when (telephonyManager.simState) {
        TelephonyManager.SIM_STATE_READY -> SimStatusEnum.READY
        TelephonyManager.SIM_STATE_ABSENT -> SimStatusEnum.ABSENT
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> SimStatusEnum.PIN_REQUIRED
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> SimStatusEnum.PUK_REQUIRED
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> SimStatusEnum.NETWORK_LOCKED
        TelephonyManager.SIM_STATE_NOT_READY -> SimStatusEnum.NOT_READY
        else -> SimStatusEnum.UNKNOWN
    }

    val phoneNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        subscriptionManager.getPhoneNumber(SubscriptionManager.getDefaultSubscriptionId())
    } else {
        subscriptionManager.activeSubscriptionInfoList?.firstOrNull()?.number
    }

    val activeSubscriptionInfo = subscriptionManager.activeSubscriptionInfoList?.firstOrNull()

    return SimStatusData(
        status = status,
        phoneNumber = phoneNumber ?: "Unknown",
        simOperatorName = telephonyManager.simOperatorName ?: "Unknown",
        subscriptionId = activeSubscriptionInfo?.subscriptionId
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SimStatus() {
    val context = LocalContext.current
    var simInfo by remember {
        mutableStateOf(
            SimStatusData(
                SimStatusEnum.UNKNOWN,
                null,
                null,
                null
            )
        )
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS)
    } else {
        listOf(Manifest.permission.READ_PHONE_STATE)
    }

    val permissionState = rememberMultiplePermissionsState(permissions)
    var shouldShowRationale by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        shouldShowRationale = permissionState.shouldShowRationale
        if (permissionState.allPermissionsGranted) {
            simInfo = getSimInfo(context)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !permissionState.allPermissionsGranted -> MaterialTheme.colorScheme.errorContainer
                simInfo.status == SimStatusEnum.READY -> MaterialTheme.colorScheme.secondaryContainer
                simInfo.status in listOf(
                    SimStatusEnum.ABSENT,
                    SimStatusEnum.PIN_REQUIRED,
                    SimStatusEnum.PUK_REQUIRED,
                    SimStatusEnum.NETWORK_LOCKED
                ) -> MaterialTheme.colorScheme.errorContainer

                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SIM Card Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!permissionState.allPermissionsGranted) {
                SimStatusRow(
                    icon = Icons.Default.Warning,
                    label = "Permission required",
                    value = "READ_PHONE_STATE and READ_PHONE_NUMBERS permissions are needed to access SIM information"
                )
                Button(
                    onClick = {
                        if (shouldShowRationale) {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            context.startActivity(intent)
                        } else {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        if (shouldShowRationale) "Open Settings"
                        else "Request Permissions"
                    )
                }
            } else {
                SimStatusRow(
                    icon = if (simInfo.status == SimStatusEnum.READY) Icons.Default.CheckCircle else Icons.Default.Warning,
                    label = "Status",
                    value = simInfo.status.toString()
                )
                SimStatusRow(
                    icon = Icons.Default.Phone,
                    label = "Phone Number",
                    value = simInfo.phoneNumber ?: "Unknown"
                )
                SimStatusRow(
                    icon = Icons.Default.AccountCircle,
                    label = "Operator",
                    value = simInfo.simOperatorName ?: "Unknown"
                )
                SimStatusRow(
                    icon = Icons.Default.Info,
                    label = "Subscription ID",
                    value = simInfo.subscriptionId?.toString() ?: "Unknown"
                )
            }
        }
    }
}

@Composable
fun SimStatusRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}