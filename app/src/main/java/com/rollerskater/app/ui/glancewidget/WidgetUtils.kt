package com.rollerskater.app.ui.glancewidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.unit.ColorProvider
import com.rollerskater.app.R
import com.rollerskater.app.data.model.PlayerInfo
import com.rollerskater.app.data.model.WidgetThemeColors
import timber.log.Timber
import kotlin.math.max

/**
 * Transforms a Bitmap into an antialiased, center-cropped rounded rectangle Bitmap.
 * This guarantees proper rounded corners on Android 11 (API 30) where RemoteViews
 * outline clipping (setViewOutlinePreferredRadius) is not supported.
 */
fun Bitmap.toRoundedBitmap(
    cornerRadiusPx: Float,
    targetWidthPx: Int,
    targetHeightPx: Int
): Bitmap {
    val tw = targetWidthPx.coerceAtLeast(1)
    val th = targetHeightPx.coerceAtLeast(1)
    val output = Bitmap.createBitmap(tw, th, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    val scale = max(
        tw.toFloat() / width.toFloat().coerceAtLeast(1f),
        th.toFloat() / height.toFloat().coerceAtLeast(1f)
    )
    val scaledWidth = width * scale
    val scaledHeight = height * scale
    val left = (tw - scaledWidth) / 2f
    val top = (th - scaledHeight) / 2f

    val matrix = Matrix().apply {
        setScale(scale, scale)
        postTranslate(left, top)
    }

    val shader = BitmapShader(this, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
        setLocalMatrix(matrix)
    }
    paint.shader = shader

    val rect = RectF(0f, 0f, tw.toFloat(), th.toFloat())
    val safeRadius = cornerRadiusPx.coerceAtLeast(0f)
    canvas.drawRoundRect(rect, safeRadius, safeRadius, paint)
    return output
}

object AlbumArtBitmapCache {
    private const val CACHE_SIZE_BYTES = 4 * 1024 * 1024
    private val lruCache = object : LruCache<String, Bitmap>(CACHE_SIZE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    fun getBitmap(key: String): Bitmap? = lruCache.get(key)

    fun putBitmap(key: String, bitmap: Bitmap) {
        if (getBitmap(key) == null) {
            lruCache.put(key, bitmap)
        }
    }

    fun getKey(byteArray: ByteArray): String {
        return byteArray.contentHashCode().toString()
    }

    /**
     * Retrieves or decodes, center-crops, and rounds album art for widget rendering.
     */
    fun getRoundedAlbumArt(
        context: Context,
        data: ByteArray?,
        uri: String?,
        sizePx: Int,
        cornerRadiusPx: Float
    ): Bitmap? {
        val safeSize = sizePx.coerceAtLeast(32)
        val safeRadius = cornerRadiusPx.coerceAtLeast(0f)

        if (data != null && data.isNotEmpty()) {
            val key = "data_${data.contentHashCode()}_${safeSize}_${safeRadius.toInt()}"
            val cached = getBitmap(key)
            if (cached != null) return cached

            val decoded = decodeByteArrayScaled(data, safeSize, safeSize) ?: return null
            val rounded = decoded.toRoundedBitmap(safeRadius, safeSize, safeSize)
            putBitmap(key, rounded)
            return rounded
        }

        if (!uri.isNullOrBlank()) {
            val key = "uri_${uri}_${safeSize}_${safeRadius.toInt()}"
            val cached = getBitmap(key)
            if (cached != null) return cached

            val decoded = decodeWidgetAlbumArtBitmap(context, uri, safeSize, safeSize) ?: return null
            val rounded = decoded.toRoundedBitmap(safeRadius, safeSize, safeSize)
            putBitmap(key, rounded)
            return rounded
        }

        return null
    }

    /**
     * Generates or retrieves an antialiased rounded placeholder bitmap with music note icon.
     */
    fun getPlaceholderBitmap(
        context: Context,
        sizePx: Int,
        cornerRadiusPx: Float,
        backgroundColorArgb: Int,
        iconColorArgb: Int
    ): Bitmap {
        val safeSize = sizePx.coerceAtLeast(32)
        val safeRadius = cornerRadiusPx.coerceAtLeast(0f)
        val key = "placeholder_${safeSize}_${safeRadius.toInt()}_${backgroundColorArgb}_${iconColorArgb}"
        val cached = getBitmap(key)
        if (cached != null) return cached

        val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColorArgb
        }
        val rect = RectF(0f, 0f, safeSize.toFloat(), safeSize.toFloat())
        canvas.drawRoundRect(rect, safeRadius, safeRadius, paint)

        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_music_placeholder)
        if (drawable != null) {
            val iconSize = (safeSize * 0.55f).toInt().coerceAtLeast(16)
            val offset = (safeSize - iconSize) / 2
            drawable.mutate()
            drawable.setTint(iconColorArgb)
            drawable.setBounds(offset, offset, offset + iconSize, offset + iconSize)
            drawable.draw(canvas)
        }

        putBitmap(key, bitmap)
        return bitmap
    }

    /**
     * Generates or retrieves an antialiased rounded button bitmap for Android 11 compatibility.
     */
    fun getButtonBitmap(
        context: Context,
        resId: Int,
        sizePx: Int,
        iconSizePx: Int,
        cornerRadiusPx: Float,
        backgroundColorArgb: Int,
        iconColorArgb: Int
    ): Bitmap {
        val safeSize = sizePx.coerceAtLeast(24)
        val safeIconSize = iconSizePx.coerceAtMost(safeSize).coerceAtLeast(12)
        val safeRadius = cornerRadiusPx.coerceAtLeast(0f)
        val key = "btn_${resId}_${safeSize}_${safeIconSize}_${safeRadius.toInt()}_${backgroundColorArgb}_${iconColorArgb}"
        val cached = getBitmap(key)
        if (cached != null) return cached

        val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColorArgb
        }
        val rect = RectF(0f, 0f, safeSize.toFloat(), safeSize.toFloat())
        canvas.drawRoundRect(rect, safeRadius, safeRadius, paint)

        val drawable = ContextCompat.getDrawable(context, resId)
        if (drawable != null) {
            val offset = (safeSize - safeIconSize) / 2
            drawable.mutate()
            drawable.setTint(iconColorArgb)
            drawable.setBounds(offset, offset, offset + safeIconSize, offset + safeIconSize)
            drawable.draw(canvas)
        }

        putBitmap(key, bitmap)
        return bitmap
    }

    private fun decodeByteArrayScaled(data: ByteArray, targetWidthPx: Int, targetHeightPx: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            var inSampleSize = 1
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while (halfHeight / inSampleSize >= targetHeightPx && halfWidth / inSampleSize >= targetWidthPx) {
                inSampleSize *= 2
            }

            options.inSampleSize = inSampleSize
            options.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
        } catch (e: Exception) {
            Timber.tag("WidgetUtils").e(e, "Error decoding byte array for widget")
            null
        }
    }
}

data class WidgetColors(
    val surface: ColorProvider,
    val onSurface: ColorProvider,
    val artist: ColorProvider,
    val playPauseBackground: ColorProvider,
    val playPauseIcon: ColorProvider,
    val prevNextBackground: ColorProvider,
    val prevNextIcon: ColorProvider
)

@Composable
fun PlayerInfo.getWidgetColors(): WidgetColors {
    val theme = this.themeColors
    
    return if (theme != null) {
        WidgetColors(
            surface = ColorProvider(
                day = Color(theme.lightSurfaceContainer),
                night = Color(theme.darkSurfaceContainer)
            ),
            onSurface = ColorProvider(
                day = Color(theme.lightTitle),
                night = Color(theme.darkTitle)
            ),
            artist = ColorProvider(
                day = Color(theme.lightArtist),
                night = Color(theme.darkArtist)
            ),
            playPauseBackground = ColorProvider(
                day = Color(theme.lightPlayPauseBackground),
                night = Color(theme.darkPlayPauseBackground)
            ),
            playPauseIcon = ColorProvider(
                day = Color(theme.lightPlayPauseIcon),
                night = Color(theme.darkPlayPauseIcon)
            ),
            prevNextBackground = ColorProvider(
                day = Color(theme.lightPrevNextBackground),
                night = Color(theme.darkPrevNextBackground)
            ),
            prevNextIcon = ColorProvider(
                day = Color(theme.lightPrevNextIcon),
                night = Color(theme.darkPrevNextIcon)
            )
        )
    } else {
        WidgetColors(
            surface = GlanceTheme.colors.surface,
            onSurface = GlanceTheme.colors.onSurface,
            artist = GlanceTheme.colors.onSurface,
            playPauseBackground = GlanceTheme.colors.primaryContainer,
            playPauseIcon = GlanceTheme.colors.onPrimaryContainer,
            prevNextBackground = GlanceTheme.colors.secondaryContainer,
            prevNextIcon = GlanceTheme.colors.onSecondaryContainer
        )
    }
}

/**
 * Glance theme roles for widgets that read [androidx.glance.GlanceTheme] directly.
 * The mapping mirrors [getWidgetColors] so every widget agrees on what the album
 * palette looks like: surface container, title/artist text, the primary play button
 * and the onPrimary skip buttons. Roles the album palette does not carry (error,
 * outline, inverse) borrow the closest of those.
 */
fun WidgetThemeColors.toGlanceColorProviders(): ColorProviders {
    fun role(light: Int, dark: Int) = ColorProvider(day = Color(light), night = Color(dark))

    val surface = role(lightSurfaceContainer, darkSurfaceContainer)
    val onSurface = role(lightTitle, darkTitle)
    val variant = role(
        lightSurfaceContainerHigh.takeUnless { it == 0 } ?: lightSurfaceContainer,
        darkSurfaceContainerHigh.takeUnless { it == 0 } ?: darkSurfaceContainer,
    )
    val onVariant = role(lightArtist, darkArtist)
    val playBackground = role(lightPlayPauseBackground, darkPlayPauseBackground)
    val playIcon = role(lightPlayPauseIcon, darkPlayPauseIcon)
    val skipBackground = role(lightPrevNextBackground, darkPrevNextBackground)
    val skipIcon = role(lightPrevNextIcon, darkPrevNextIcon)
    val error = ColorProvider(day = Color(0xFFBA1A1A), night = Color(0xFFFFB4AB))
    val onError = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF690005))

    return colorProviders(
        primary = playBackground,
        onPrimary = playIcon,
        primaryContainer = playBackground,
        onPrimaryContainer = playIcon,
        secondary = skipBackground,
        onSecondary = skipIcon,
        secondaryContainer = skipBackground,
        onSecondaryContainer = skipIcon,
        tertiary = playBackground,
        onTertiary = playIcon,
        tertiaryContainer = playBackground,
        onTertiaryContainer = playIcon,
        error = error,
        errorContainer = error,
        onError = onError,
        onErrorContainer = onError,
        background = surface,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = variant,
        onSurfaceVariant = onVariant,
        outline = onVariant,
        inverseOnSurface = surface,
        inverseSurface = onSurface,
        inversePrimary = playBackground,
    )
}
