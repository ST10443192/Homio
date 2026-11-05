package com.homio.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_requests")
data class MaintenanceEntity(
    @PrimaryKey val id: String,          // same id as the Firestore document
    val tenantId: String,
    val title: String,
    val description: String,
    val category: String,                // English key, e.g. "Plumbing"
    val status: String,                  // English key, e.g. "In Progress"
    val createdDate: Long,               // milliseconds
    val pendingSync: Boolean = false     // true = changed on this phone, not uploaded yet
)