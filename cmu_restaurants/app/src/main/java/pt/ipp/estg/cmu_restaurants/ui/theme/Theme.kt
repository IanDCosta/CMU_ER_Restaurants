package pt.ipp.estg.cmu_restaurants.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define your custom colors
val BackgroundColor = Color.White
val Primary = Color(0xFFA50000)
val Secondary = Color(0xFFFF6363)
val TextColor = Color.Black
val UnfocusedColor = Color.LightGray

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = UnfocusedColor,
    onPrimary = TextColor,
    onSecondary = TextColor,
    background = BackgroundColor,
    surface = BackgroundColor
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = UnfocusedColor,
    onPrimary = TextColor,
    onSecondary = TextColor,
    background = BackgroundColor,
    surface = BackgroundColor
)

// Extension properties for custom colors
val ColorScheme.customBackground: Color
    get() = BackgroundColor

val ColorScheme.customAccent: Color
    get() = Primary

@Composable
fun Cmu_restaurantsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    /*val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }*/

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
