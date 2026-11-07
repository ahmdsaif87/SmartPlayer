package id.smartplayer.data.common

import java.security.MessageDigest
import java.util.Locale

object FingerprintHelper {

    fun generate(displayName: String, sizeBytes: Long): String {
        val input = "${displayName.lowercase(Locale.ROOT)}|$sizeBytes"
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
