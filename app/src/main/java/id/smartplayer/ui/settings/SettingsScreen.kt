package id.smartplayer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.smartplayer.data.scan.ScanState
import id.smartplayer.ui.theme.CinemaBackground
import id.smartplayer.ui.theme.CinemaPrimary
import id.smartplayer.ui.theme.CinemaSecondary
import id.smartplayer.ui.theme.CinemaSurface
import id.smartplayer.ui.theme.CinemaSurfaceVariant
import id.smartplayer.ui.theme.CinemaTextPrimary
import id.smartplayer.ui.theme.CinemaTextSecondary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val syncOnlyWifi by viewModel.syncOnlyWifi.collectAsState()
    val autoEnrich by viewModel.autoEnrich.collectAsState()

    var inputKey by remember(apiKey) { mutableStateOf(apiKey) }
    var saveFeedback by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pengaturan & AI",
                color = CinemaTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI SECTION
        Text(
            text = "KECERDASAN BUATAN (AI ENRICHMENT)",
            color = CinemaPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Google Gemini API Key",
                    color = CinemaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dibutuhkan untuk otomatis mencari poster resolusi tinggi, sinopsis berbahasa Indonesia, dan genre film/serial.",
                    color = CinemaTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it
                        saveFeedback = ""
                    },
                    placeholder = { Text("Masukkan API Key Gemini...", color = CinemaTextSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary,
                        focusedContainerColor = CinemaSurfaceVariant,
                        unfocusedContainerColor = CinemaSurfaceVariant,
                        focusedBorderColor = CinemaPrimary,
                        unfocusedBorderColor = CinemaSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (saveFeedback.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = saveFeedback,
                                color = Color(0xFF22C55E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.saveApiKey(inputKey)
                            saveFeedback = "Tersimpan"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan Key", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NETWORK TOGGLES
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hanya Sync Lewat Wi-Fi",
                            color = CinemaTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Hemat kuota seluler saat mengunduh poster & sinopsis.",
                            color = CinemaTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = syncOnlyWifi,
                        onCheckedChange = { viewModel.setSyncOnlyWifi(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CinemaPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Enrich Metadata",
                            color = CinemaTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Otomatis panggil AI untuk video baru yang didownload.",
                            color = CinemaTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = autoEnrich,
                        onCheckedChange = { viewModel.setAutoEnrich(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CinemaPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // STORAGE & SCANNING SECTION
        Text(
            text = "PENYIMPANAN & PEMINDAIAN",
            color = CinemaPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lokasi yang Dipantau:",
                    color = CinemaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "• /Download", color = CinemaTextSecondary, fontSize = 12.sp)
                Text(text = "• /Movies", color = CinemaTextSecondary, fontSize = 12.sp)
                Text(text = "• /Video & /Videos", color = CinemaTextSecondary, fontSize = 12.sp)
                Text(text = "• /DCIM", color = CinemaTextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                val scanState by viewModel.scanState.collectAsState()
                val isScanning = scanState is ScanState.Scanning

                Button(
                    onClick = { viewModel.triggerRescan() },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaPrimary,
                        disabledContainerColor = CinemaSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val s = scanState as ScanState.Scanning
                        Text(text = "Sedang Memindai (${s.processed}/${s.total})...", color = Color.White, fontSize = 13.sp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Pindai Ulang Penyimpanan Sekarang", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (val state = scanState) {
                    is ScanState.Done -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Selesai: ${state.added} baru ditambahkan, ${state.updated} diperbarui.",
                                color = Color(0xFF22C55E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    is ScanState.Error -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gagal: ${state.cause.localizedMessage}",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
