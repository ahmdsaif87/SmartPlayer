package id.smartplayer.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playback_state",
    indices = [
        Index("updatedAtEpochMs")
    ]
)
data class PlaybackStateEntity(
    @PrimaryKey val fingerprint: String,
    val lastPlayedPosition: Long,
    val durationMs: Long,
    val isFinished: Boolean,
    val updatedAtEpochMs: Long
)
