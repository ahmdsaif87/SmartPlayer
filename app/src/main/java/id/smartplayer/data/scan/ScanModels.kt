package id.smartplayer.data.scan

data class RawMediaFile(
    val mediaStoreId: Long,
    val contentUri: String,
    val absolutePath: String,
    val relativePath: String,
    val displayName: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val dateAddedSec: Long,
    val dateModifiedSec: Long
)

sealed interface ScanState {
    data object Idle : ScanState
    data class Scanning(val processed: Int, val total: Int) : ScanState
    data class Done(val added: Int, val updated: Int, val removed: Int) : ScanState
    data class Error(val cause: Throwable) : ScanState
}
