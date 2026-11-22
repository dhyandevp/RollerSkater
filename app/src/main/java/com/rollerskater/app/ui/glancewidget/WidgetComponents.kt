package com.rollerskater.app.ui.glancewidget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.AndroidResourceImageProvider
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import com.rollerskater.app.R

@Composable
fun AlbumArtImage(
    modifier: GlanceModifier = GlanceModifier,
    bitmapData: ByteArray?,
    albumArtUri: String? = null,
    size: Dp,
    context: Context,
    cornerRadius: Dp
) {
    val density = context.resources.displayMetrics.density
    val targetSizePx = (size.value * density).toInt().coerceAtLeast(32)
    val cornerRadiusPx = cornerRadius.value * density

    val bitmap = AlbumArtBitmapCache.getRoundedAlbumArt(
        context = context,
        data = bitmapData,
        uri = albumArtUri,
        sizePx = targetSizePx,
        cornerRadiusPx = cornerRadiusPx
    )

    val imageProvider = bitmap?.let { ImageProvider(it) } ?: run {
        val bgArgb = GlanceTheme.colors.surfaceVariant.getColor(context).toArgb()
        val iconArgb = GlanceTheme.colors.onSurfaceVariant.getColor(context).toArgb()
        val placeholderBitmap = AlbumArtBitmapCache.getPlaceholderBitmap(
            context = context,
            sizePx = targetSizePx,
            cornerRadiusPx = cornerRadiusPx,
            backgroundColorArgb = bgArgb,
            iconColorArgb = iconArgb
        )
        ImageProvider(placeholderBitmap)
    }

    val cdAlbumArt = context.getString(R.string.widget_album_art)
    Box(
        modifier = modifier
    ) {
        Image(
            provider = imageProvider,
            contentDescription = cdAlbumArt,
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(cornerRadius),
            contentScale = ContentScale.Crop
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun WidgetIconButton(
    modifier: GlanceModifier,
    action: Action,
    backgroundColor: ColorProvider,
    iconColor: ColorProvider,
    imageProvider: ImageProvider,
    contentDescription: String,
    iconSize: Dp = 20.dp,
    buttonSize: Dp = 40.dp,
    cornerRadius: Dp
) {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
        cornerRadius > 0.dp &&
        imageProvider is AndroidResourceImageProvider
    ) {
        val density = context.resources.displayMetrics.density
        val buttonSizePx = (buttonSize.value * density).toInt().coerceAtLeast(24)
        val iconSizePx = (iconSize.value * density).toInt().coerceAtLeast(12)
        val cornerRadiusPx = cornerRadius.value * density
        val bgArgb = backgroundColor.getColor(context).toArgb()
        val iconArgb = iconColor.getColor(context).toArgb()

        val buttonBitmap = AlbumArtBitmapCache.getButtonBitmap(
            context = context,
            resId = imageProvider.resId,
            sizePx = buttonSizePx,
            iconSizePx = iconSizePx,
            cornerRadiusPx = cornerRadiusPx,
            backgroundColorArgb = bgArgb,
            iconColorArgb = iconArgb
        )

        Image(
            provider = ImageProvider(buttonBitmap),
            contentDescription = contentDescription,
            modifier = modifier.clickable(action),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(cornerRadius)
                .clickable(action),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = imageProvider,
                contentDescription = contentDescription,
                modifier = GlanceModifier.size(iconSize),
                colorFilter = ColorFilter.tint(iconColor)
            )
        }
    }
}

@Composable
fun PreviousButton(
    modifier: GlanceModifier,
    backgroundColor: ColorProvider,
    iconColor: ColorProvider,
    cornerRadius: Dp,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 20.dp
) {
    val context = LocalContext.current
    val params = actionParametersOf(PlayerActions.key to PlayerActions.PREVIOUS)
    WidgetIconButton(
        modifier = modifier,
        action = actionRunCallback<PlayerControlActionCallback>(params),
        backgroundColor = backgroundColor,
        iconColor = iconColor,
        imageProvider = ImageProvider(R.drawable.rounded_skip_previous_filled_24),
        contentDescription = context.getString(R.string.previous_track),
        iconSize = iconSize,
        buttonSize = buttonSize,
        cornerRadius = cornerRadius
    )
}

@Composable
fun NextButton(
    modifier: GlanceModifier,
    backgroundColor: ColorProvider,
    iconColor: ColorProvider,
    cornerRadius: Dp,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 20.dp
) {
    val context = LocalContext.current
    val params = actionParametersOf(PlayerActions.key to PlayerActions.NEXT)
    WidgetIconButton(
        modifier = modifier,
        action = actionRunCallback<PlayerControlActionCallback>(params),
        backgroundColor = backgroundColor,
        iconColor = iconColor,
        imageProvider = ImageProvider(R.drawable.rounded_skip_next_filled_24),
        contentDescription = context.getString(R.string.next_track),
        iconSize = iconSize,
        buttonSize = buttonSize,
        cornerRadius = cornerRadius
    )
}

@Composable
fun PlayPauseButton(
    modifier: GlanceModifier,
    isPlaying: Boolean,
    backgroundColor: ColorProvider,
    iconColor: ColorProvider,
    cornerRadius: Dp,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 22.dp
) {
    val context = LocalContext.current
    val params = actionParametersOf(PlayerActions.key to PlayerActions.PLAY_PAUSE)
    WidgetIconButton(
        modifier = modifier,
        action = actionRunCallback<PlayerControlActionCallback>(params),
        backgroundColor = backgroundColor,
        iconColor = iconColor,
        imageProvider = ImageProvider(
            if (isPlaying) R.drawable.rounded_pause_filled_24
            else R.drawable.rounded_play_arrow_filled_24
        ),
        contentDescription = context.getString(
            if (isPlaying) R.string.cd_pause else R.string.cd_play
        ),
        iconSize = iconSize,
        buttonSize = buttonSize,
        cornerRadius = cornerRadius
    )
}

@Composable
fun ShuffleButton(
    modifier: GlanceModifier,
    backgroundColor: ColorProvider,
    iconColor: ColorProvider,
    cornerRadius: Dp,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 20.dp
) {
    val context = LocalContext.current
    val params = actionParametersOf(PlayerActions.key to PlayerActions.SHUFFLE)
    WidgetIconButton(
        modifier = modifier,
        action = actionRunCallback<PlayerControlActionCallback>(params),
        backgroundColor = backgroundColor,
        iconColor = iconColor,
        imageProvider = ImageProvider(R.drawable.rounded_shuffle_24),
        contentDescription = context.getString(R.string.shortcut_shuffle_short),
        iconSize = iconSize,
        buttonSize = buttonSize,
        cornerRadius = cornerRadius
    )
}

@Composable
fun RepeatButton(
    modifier: GlanceModifier,
    backgroundColor: ColorProvider,
    iconRes: Int,
    iconColor: ColorProvider,
    cornerRadius: Dp,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 20.dp
) {
    val context = LocalContext.current
    val params = actionParametersOf(PlayerActions.key to PlayerActions.REPEAT)
    WidgetIconButton(
        modifier = modifier,
        action = actionRunCallback<PlayerControlActionCallback>(params),
        backgroundColor = backgroundColor,
        iconColor = iconColor,
        imageProvider = ImageProvider(iconRes),
        contentDescription = context.getString(R.string.cd_repeat),
        iconSize = iconSize,
        buttonSize = buttonSize,
        cornerRadius = cornerRadius
    )
}

