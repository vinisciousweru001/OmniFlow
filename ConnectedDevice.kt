package com.example.data.model

enum class DeviceType(val label: String) {
    MOBILE("Android Phone"),
    DESKTOP("Desktop PC"),
    LAPTOP("Laptop"),
    TABLET("Tablet")
}

enum class SyncState(val label: String) {
    ONLINE("Synced"),
    SYNCING("Synchronizing..."),
    PENDING("Sync Pending"),
    OFFLINE("Offline")
}

data class ConnectedDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val osDetails: String,
    val lastSyncTimeEpochMs: Long,
    val syncState: SyncState = SyncState.ONLINE,
    val isCurrentDevice: Boolean = false,
    val ipAddress: String = "192.168.1.10"
)
