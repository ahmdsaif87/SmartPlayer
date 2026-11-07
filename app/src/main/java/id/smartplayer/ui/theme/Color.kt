package id.smartplayer.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val CinemaBackground = Color(0xFF0A0A0C)
val CinemaSurface = Color(0xFF141418)
val CinemaSurfaceVariant = Color(0xFF202026)
val CinemaPrimary = Color(0xFFE50914) // Netflix Red
val CinemaSecondary = Color(0xFF38BDF8) // Cyan / Sky Blue for badges
val CinemaTextPrimary = Color(0xFFF8FAFC)
val CinemaTextSecondary = Color(0xFF94A3B8)
val CinemaProgressBg = Color(0xFF334155)

val DarkColorScheme = darkColorScheme(
    primary = CinemaPrimary,
    onPrimary = Color.White,
    secondary = CinemaSecondary,
    onSecondary = Color.Black,
    background = CinemaBackground,
    onBackground = CinemaTextPrimary,
    surface = CinemaSurface,
    onSurface = CinemaTextPrimary,
    surfaceVariant = CinemaSurfaceVariant,
    onSurfaceVariant = CinemaTextSecondary
)
