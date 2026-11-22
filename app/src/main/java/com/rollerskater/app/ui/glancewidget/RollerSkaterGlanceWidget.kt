package com.rollerskater.app.ui.glancewidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rollerskater.app.MainActivity
import com.rollerskater.app.data.model.PlayerInfo
import com.rollerskater.app.R
import androidx.core.graphics.scale
import androidx.glance.unit.ColorProvider
import com.rollerskater.app.data.model.QueueItem
import com.rollerskater.app.utils.createScalableBackgroundBitmap
import java.util.Locale
import timber.log.Timber

class RollerSkaterGlanceWidget : GlanceAppWidget() {

    companion object {
        private val VERY_THIN_LAYOUT_SIZE = DpSize(width = 200.dp, height = 60.dp)
        private val THIN_LAYOUT_SIZE = DpSize(width = 250.dp, height = 80.dp)
        private val SMALL_HORIZONTAL_LAYOUT_SIZE = DpSize(width = 110.dp, height = 60.dp)
        private val ONE_BY_ONE_LAYOUT_SIZE = DpSize(width = 110.dp, height = 110.dp)
        private val GABE_LAYOUT_SIZE = DpSize(width = 110.dp, height = 220.dp)
        private val GABE_TWO_HEIGHT_LAYOUT_SIZE = DpSize(width = 110.dp, height = 200.dp)
        private val SMALL_LAYOUT_SIZE = DpSize(width = 120.dp, height = 100.dp)
        private val MEDIUM_LAYOUT_SIZE = DpSize(width = 250.dp, height = 150.dp)
        private val LARGE_LAYOUT_SIZE = DpSize(width = 300.dp, height = 180.dp)
        private val EXTRA_LARGE_LAYOUT_SIZE = DpSize(width = 300.dp, height = 220.dp)
        private val EXTRA_LARGE_PLUS_LAYOUT_SIZE = DpSize(width = 350.dp, height = 260.dp)
        private val HUGE_LAYOUT_SIZE = DpSize(width = 400.dp, height = 300.dp)
    }

    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PlayerInfoStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val playerInfo = currentState<PlayerInfo>()
            val currentSize = LocalSize.current

            Timber.tag("RollerSkaterGlanceWidget")
                .d("Providing Glance. PlayerInfo: title='${playerInfo.songTitle}', artist='${playerInfo.artistName}', isPlaying=${playerInfo.isPlaying}, hasBitmap=${playerInfo.albumArtBitmapData != null}, progress=${playerInfo.currentPositionMs}/${playerInfo.totalDurationMs}")

            // themeColors is null when there is no palette (nothing playing, or no
            // artwork), in which case the regular dynamic Glance theme applies.
            val albumColors = remember(playerInfo.themeColors) {
                playerInfo.themeColors?.toGlanceColorProviders()
            }
            if (albumColors != null) {
                GlanceTheme(colors = albumColors) {
                    WidgetUi(playerInfo = playerInfo, size = currentSize, context = context)
                }
            } else {
                GlanceTheme {
                    WidgetUi(playerInfo = playerInfo, size = currentSize, context = context)
                }
            }
        }
    }

    @Composable
    private fun WidgetUi(
        playerInfo: PlayerInfo,
        size: DpSize,
        context: Context
    ) {
        val title = playerInfo.songTitle.ifEmpty { context.getString(R.string.app_name) }
        val artist = playerInfo.artistName.ifEmpty { context.getString(R.string.widget_tap_to_open) }
        val isPlaying = playerInfo.isPlaying
        val isFavorite = playerInfo.isFavorite
        val albumArtBitmapData = playerInfo.albumArtBitmapData
        val albumArtUri = playerInfo.albumArtUri

        Timber.tag("RollerSkaterGlanceWidget")
            .d("WidgetUi: PlayerInfo received. Title: $title, Artist: $artist, HasBitmapData: ${albumArtBitmapData != null}, BitmapDataSize: ${albumArtBitmapData?.size ?: "N/A"}")

        val actualBackgroundColor = GlanceTheme.colors.surface
        val onBackgroundColor = GlanceTheme.colors.onSurface

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .clickable(actionStartActivity<MainActivity>())

        Box(
            GlanceModifier.fillMaxSize()
        ) {
            val isOneColumn = size.width < SMALL_LAYOUT_SIZE.width
            val isSmallHeight = size.height < SMALL_LAYOUT_SIZE.height

            if (isOneColumn) {
                when {
                    size.height <= ONE_BY_ONE_LAYOUT_SIZE.height -> OneByOneWidgetLayout(
                        modifier = baseModifier,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 60.dp,
                        isPlaying = isPlaying
                    )
                    size.height <= GABE_TWO_HEIGHT_LAYOUT_SIZE.height -> GabeTwoHeightWidgetLayout(
                        modifier = baseModifier,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 60.dp,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        isPlaying = isPlaying,
                        context = context
                    )
                    else -> GabeWidgetLayout(
                        modifier = baseModifier,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 360.dp,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        isPlaying = isPlaying,
                        context = context
                    )
                }
            } else if (isSmallHeight) {
                when {
                    size.width < VERY_THIN_LAYOUT_SIZE.width -> SmallHorizontalWidgetLayout(
                        modifier = baseModifier,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 60.dp,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        isPlaying = isPlaying,
                        context = context
                    )
                    size.width < THIN_LAYOUT_SIZE.width -> VeryThinWidgetLayout(
                        modifier = baseModifier,
                        title = title,
                        artist = artist,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        isPlaying = isPlaying,
                        textColor = onBackgroundColor,
                        context = context,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 60.dp
                    )
                    else -> ThinWidgetLayout(
                        modifier = baseModifier,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 60.dp,
                        title = title,
                        artist = artist,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        isPlaying = isPlaying,
                        textColor = onBackgroundColor,
                        context = context
                    )
                }
            } else {
                when {
                    size.width < MEDIUM_LAYOUT_SIZE.width || size.height < MEDIUM_LAYOUT_SIZE.height -> SmallWidgetLayout(
                        modifier = baseModifier,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 28.dp,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        isPlaying = isPlaying,
                        context = context
                    )
                    size.width < LARGE_LAYOUT_SIZE.width || size.height < LARGE_LAYOUT_SIZE.height -> MediumWidgetLayout(
                        modifier = baseModifier,
                        title = title,
                        artist = artist,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        isPlaying = isPlaying,
                        textColor = onBackgroundColor,
                        context = context,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 28.dp
                    )
                    size.width < EXTRA_LARGE_LAYOUT_SIZE.width || size.height < EXTRA_LARGE_LAYOUT_SIZE.height -> LargeWidgetLayout(
                        modifier = baseModifier,
                        title = title,
                        artist = artist,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 28.dp,
                        isPlaying = isPlaying,
                        isFavorite = isFavorite,
                        textColor = onBackgroundColor,
                        context = context
                    )
                    else -> ExtraLargeWidgetLayout(
                        modifier = baseModifier,
                        title = title,
                        artist = artist,
                        albumArtBitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        isPlaying = isPlaying,
                        backgroundColor = actualBackgroundColor,
                        bgCornerRadius = 28.dp,
                        textColor = onBackgroundColor,
                        context = context,
                        queue = playerInfo.queue
                    )
                }
            }
        }
    }

    @Composable
    fun VeryThinWidgetLayout(
        modifier: GlanceModifier,
        title: String,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        artist: String,
        albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        isPlaying: Boolean,
        textColor: ColorProvider,
        context: Context
    ) {
        val secondaryColor = GlanceTheme.colors.secondaryContainer
        val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
        val primaryContainerColor = GlanceTheme.colors.primaryContainer
        val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
        val size = LocalSize.current
        val albumArtSize = size.height - 32.dp

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(bgCornerRadius),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {

                AlbumArtImageGlance(
                    modifier = GlanceModifier
                        .size(albumArtSize),
                    bitmapData = albumArtBitmapData,
                    albumArtUri = albumArtUri,
                    context = context,
                    cornerRadius = bgCornerRadius
                )
                Spacer(GlanceModifier.width(10.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(text = title, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor), maxLines = 1)
                    if (artist.isNotEmpty() && artist != context.getString(R.string.widget_tap_to_open)) {
                        Text(text = artist, style = TextStyle(fontSize = 14.sp, color = textColor), maxLines = 1)
                    }
                }
                Spacer(GlanceModifier.width(8.dp))
                PlayPauseButtonGlance(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .size(48.dp, 48.dp)
                        .fillMaxHeight(),
                    backgroundColor = primaryContainerColor,
                    iconColor = onPrimaryContainerColor,
                    isPlaying = isPlaying,
                    iconSize = 26.dp,
                    cornerRadius = 10.dp
                )
                Spacer(GlanceModifier.width(10.dp))
                NextButtonGlance(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .size(48.dp, 48.dp)
                        .fillMaxHeight(),
                    iconColor = onSecondaryColor,
                    iconSize = 26.dp,
                    backgroundColor = secondaryColor,
                    cornerRadius = 10.dp
                )
            }
        }
    }


    @Composable
    fun ThinWidgetLayout(
        modifier: GlanceModifier,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        title: String,
        artist: String,
        albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        isPlaying: Boolean,
        textColor: ColorProvider,
        context: Context
    ) {
        val secondaryColor = GlanceTheme.colors.secondaryContainer
        val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
        val primaryContainerColor = GlanceTheme.colors.primaryContainer
        val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
        val size = LocalSize.current
        val albumArtSize = size.height - 32.dp

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(bgCornerRadius)
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {

                AlbumArtImageGlance(
                    modifier = GlanceModifier
                        .size(albumArtSize),
                    bitmapData = albumArtBitmapData,
                    albumArtUri = albumArtUri,
                    context = context,
                    cornerRadius = bgCornerRadius
                )
                Spacer(GlanceModifier.width(14.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(text = title, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor), maxLines = 1)
                    if (artist.isNotEmpty() && artist != context.getString(R.string.widget_tap_to_open)) {
                        Text(text = artist, style = TextStyle(fontSize = 14.sp, color = textColor), maxLines = 1)
                    }
                }
                Spacer(GlanceModifier.width(8.dp))
                PlayPauseButtonGlance(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .size(48.dp, 48.dp)
                        .fillMaxHeight(),
                    backgroundColor = primaryContainerColor,
                    iconColor = onPrimaryContainerColor,
                    isPlaying = isPlaying,
                    iconSize = 26.dp,
                    cornerRadius = 10.dp
                )
                Spacer(GlanceModifier.width(10.dp))
                NextButtonGlance(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .size(48.dp, 48.dp)
                        .fillMaxHeight(),
                    iconColor = onSecondaryColor,
                    iconSize = 26.dp,
                    backgroundColor = secondaryColor,
                    cornerRadius = 10.dp
                )
            }
        }
    }

    @Composable
    fun GabeTwoHeightWidgetLayout(
        modifier: GlanceModifier,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        isPlaying: Boolean,
        context: Context
    ) {
        val secondaryColor = GlanceTheme.colors.secondaryContainer
        val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
        val primaryContainerColor = GlanceTheme.colors.primaryContainer
        val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {

                AlbumArtImageGlance(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(48.dp)
                    ,
                    bitmapData = albumArtBitmapData,
                    albumArtUri = albumArtUri,
                    context = context,
                    cornerRadius = 64.dp
                )
                Spacer(GlanceModifier.height(14.dp))
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .cornerRadius(bgCornerRadius)
                ) {
                    PlayPauseButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxWidth(),
                        backgroundColor = primaryContainerColor,
                        iconColor = onPrimaryContainerColor,
                        isPlaying = isPlaying,
                        iconSize = 26.dp,
                        cornerRadius = 10.dp
                    )
                    Spacer(GlanceModifier.height(10.dp))
                    NextButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxWidth(),
                        iconColor = onSecondaryColor,
                        iconSize = 26.dp,
                        backgroundColor = secondaryColor,
                        cornerRadius = 10.dp
                    )
                }
            }
        }
    }

    @Composable
    fun GabeWidgetLayout(
        modifier: GlanceModifier,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        isPlaying: Boolean,
        context: Context
    ) {
        val secondaryColor = GlanceTheme.colors.secondaryContainer
        val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
        val primaryContainerColor = GlanceTheme.colors.primaryContainer
        val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                AlbumArtImageGlance(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .height(48.dp),
                    bitmapData = albumArtBitmapData,
                    albumArtUri = albumArtUri,
                    context = context,
                    cornerRadius = 64.dp
                )
                Spacer(GlanceModifier.height(14.dp))
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .cornerRadius(bgCornerRadius)
                ) {
                    PreviousButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxWidth(),
                        iconColor = onSecondaryColor,
                        iconSize = 26.dp,
                        backgroundColor = secondaryColor,
                        cornerRadius = 10.dp
                    )
                    Spacer(GlanceModifier.height(10.dp))
                    PlayPauseButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxWidth(),
                        backgroundColor = primaryContainerColor,
                        iconColor = onPrimaryContainerColor,
                        isPlaying = isPlaying,
                        iconSize = 26.dp,
                        cornerRadius = 10.dp
                    )
                    Spacer(GlanceModifier.height(10.dp))
                    NextButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxWidth(),
                        iconColor = onSecondaryColor,
                        iconSize = 26.dp,
                        backgroundColor = secondaryColor,
                        cornerRadius = 10.dp
                    )
                }
            }
        }
    }

    @Composable
    fun OneByOneWidgetLayout(
        modifier: GlanceModifier,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        isPlaying: Boolean
    ) {
        val primaryContainerColor = GlanceTheme.colors.primaryContainer
        val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            PlayPauseButtonGlance(
                modifier = GlanceModifier.fillMaxSize(),
                backgroundColor = primaryContainerColor,
                iconColor = onPrimaryContainerColor,
                isPlaying = isPlaying,
                iconSize = 36.dp,
                cornerRadius = 30.dp
            )
        }
    }

    @Composable
    fun SmallHorizontalWidgetLayout(
        modifier: GlanceModifier,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        isPlaying: Boolean,
        context: Context
    ) {
        val primaryContainerColor = GlanceTheme.colors.primaryContainer
        val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(bgCornerRadius),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                AlbumArtImageGlance(
                    modifier = GlanceModifier.padding(vertical = 6.dp),
                    bitmapData = albumArtBitmapData,
                    albumArtUri = albumArtUri,
                    size = 58.dp,
                    context = context,
                    cornerRadius = 64.dp
                )
                Spacer(GlanceModifier.width(14.dp))
                PlayPauseButtonGlance(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight(),
                    backgroundColor = primaryContainerColor,
                    iconColor = onPrimaryContainerColor,
                    isPlaying = isPlaying,
                    iconSize = 26.dp,
                    cornerRadius = 10.dp
                )
            }
        }
    }

    @Composable
    fun SmallWidgetLayout(
        modifier: GlanceModifier,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        isPlaying: Boolean,
        context: Context
    ) {
        val secondaryColor = GlanceTheme.colors.secondaryContainer
        val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
        val primaryContainerColor = GlanceTheme.colors.primaryContainer
        val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
        val buttonCornerRadius = 16.dp
        val playButtonCornerRadius = if (isPlaying) 12.dp else 60.dp

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(12.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    AlbumArtImageGlance(
                        modifier = GlanceModifier.defaultWeight(),
                        bitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        context = context,
                        cornerRadius = 64.dp
                    )
                }
                Spacer(GlanceModifier.height(8.dp))
                PlayPauseButtonGlance(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .height(50.dp),
                    isPlaying = isPlaying,
                    cornerRadius = playButtonCornerRadius,
                    iconSize = 26.dp,
                    backgroundColor = primaryContainerColor,
                    iconColor = onPrimaryContainerColor
                )
                Spacer(GlanceModifier.height(8.dp))
                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PreviousButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        iconSize = 26.dp,
                        cornerRadius = buttonCornerRadius,
                        backgroundColor = secondaryColor,
                        iconColor = onSecondaryColor
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    NextButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        iconSize = 26.dp,
                        cornerRadius = buttonCornerRadius,
                        backgroundColor = secondaryColor,
                        iconColor = onSecondaryColor
                    )
                }
            }
        }
    }

    @Composable
    fun MediumWidgetLayout(
        modifier: GlanceModifier,
        title: String,
        artist: String,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        isPlaying: Boolean,
        textColor: ColorProvider,
        context: Context
    ) {
        val secondaryColor = GlanceTheme.colors.secondaryContainer
        val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
        val primaryContainerColor = GlanceTheme.colors.primaryContainer
        val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
        val buttonCornerRadius = 60.dp
        val playButtonCornerRadius = if (isPlaying) 14.dp else 60.dp

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlbumArtImageGlance(
                        bitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        size = 80.dp,
                        context = context,
                        cornerRadius = 16.dp
                    )
                    Spacer(GlanceModifier.width(12.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = title,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            ),
                            maxLines = 2
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = artist,
                            style = TextStyle(fontSize = 13.sp, color = textColor),
                            maxLines = 2
                        )
                    }
                }

                Spacer(GlanceModifier.height(12.dp))

                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PreviousButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        iconColor = onSecondaryColor,
                        backgroundColor = secondaryColor,
                        cornerRadius = buttonCornerRadius
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    PlayPauseButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        isPlaying = isPlaying,
                        iconColor = onPrimaryContainerColor,
                        backgroundColor = primaryContainerColor,
                        cornerRadius = playButtonCornerRadius
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    NextButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        iconColor = onSecondaryColor,
                        backgroundColor = secondaryColor,
                        cornerRadius = buttonCornerRadius
                    )
                }
            }
        }
    }

    @Composable
    fun LargeWidgetLayout(
        modifier: GlanceModifier,
        title: String,
        artist: String,
        albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        backgroundColor: ColorProvider,
        bgCornerRadius: Dp,
        isPlaying: Boolean,
        isFavorite: Boolean,
        textColor: ColorProvider,
        context: Context
    ) {
        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    AlbumArtImageGlance(
                        bitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        size = 64.dp,
                        context = context,
                        cornerRadius = 18.dp
                    )
                    Spacer(GlanceModifier.width(12.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(text = title, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor), maxLines = 1)
                        Text(text = artist, style = TextStyle(fontSize = 13.sp, color = textColor), maxLines = 1)
                    }
                    Spacer(GlanceModifier.width(4.dp))
                    Image(
                        provider = ImageProvider(if (isFavorite) R.drawable.round_favorite_24 else R.drawable.rounded_favorite_24),
                        contentDescription = context.getString(R.string.cd_favorite),
                        modifier = GlanceModifier
                            .size(28.dp)
                            .clickable(actionRunCallback<PlayerControlActionCallback>(actionParametersOf(PlayerActions.key to PlayerActions.FAVORITE)))
                            .padding(2.dp),
                        colorFilter = ColorFilter.tint(textColor)
                    )
                    Spacer(GlanceModifier.width(8.dp))
                }
                Spacer(GlanceModifier.height(4.dp))
                Spacer(GlanceModifier.height(10.dp))

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val secondaryColor = GlanceTheme.colors.secondaryContainer
                    val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
                    val primaryContainerColor = GlanceTheme.colors.primaryContainer
                    val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
                    val buttonCornerRadius = 60.dp
                    val playButtonCornerRadius = if (isPlaying) 14.dp else 60.dp

                    PreviousButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        iconColor = onSecondaryColor,
                        backgroundColor = secondaryColor,
                        cornerRadius = buttonCornerRadius
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    PlayPauseButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        isPlaying = isPlaying,
                        iconColor = onPrimaryContainerColor,
                        backgroundColor = primaryContainerColor,
                        cornerRadius = playButtonCornerRadius
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    NextButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        iconColor = onSecondaryColor,
                        backgroundColor = secondaryColor,
                        cornerRadius = buttonCornerRadius
                    )
                }
            }
        }
    }

    @Composable
    fun ExtraLargeWidgetLayout(
        modifier: GlanceModifier, title: String, artist: String, albumArtBitmapData: ByteArray?,
        albumArtUri: String?,
        isPlaying: Boolean, backgroundColor: ColorProvider, bgCornerRadius: Dp,
        textColor: ColorProvider,
        context: Context,
        queue: List<QueueItem>
    ) {
        val playButtonCornerRadius = if (isPlaying) 16.dp else 60.dp

        Box(
            modifier = modifier
                .background(backgroundColor)
                .cornerRadius(bgCornerRadius)
                .padding(16.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.fillMaxWidth()
                ) {
                    AlbumArtImageGlance(
                        bitmapData = albumArtBitmapData,
                        albumArtUri = albumArtUri,
                        size = 68.dp,
                        context = context,
                        cornerRadius = 16.dp
                    )
                    Spacer(GlanceModifier.width(16.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = title,
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor),
                            maxLines = 2
                        )
                        Text(
                            text = artist,
                            style = TextStyle(fontSize = 16.sp, color = textColor),
                            maxLines = 1
                        )
                    }
                }

                Spacer(GlanceModifier.height(14.dp))

                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .height(56.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val secondaryColor = GlanceTheme.colors.secondaryContainer
                    val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
                    val primaryContainerColor = GlanceTheme.colors.primaryContainer
                    val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
                    val buttonCornerRadius = 60.dp

                    PreviousButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        iconColor = onSecondaryColor,
                        backgroundColor = secondaryColor,
                        iconSize = 28.dp,
                        cornerRadius = buttonCornerRadius
                    )
                    Spacer(GlanceModifier.width(10.dp))
                    PlayPauseButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        isPlaying = isPlaying,
                        iconColor = onPrimaryContainerColor,
                        backgroundColor = primaryContainerColor,
                        iconSize = 30.dp,
                        cornerRadius = playButtonCornerRadius
                    )
                    Spacer(GlanceModifier.width(10.dp))
                    NextButtonGlance(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                        iconColor = onSecondaryColor,
                        backgroundColor = secondaryColor,
                        iconSize = 28.dp,
                        cornerRadius = buttonCornerRadius
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .background(textColor.getColor(context).copy(alpha = 0.15f))
                        .height(2.dp)
                        .cornerRadius(60.dp)
                ) {

                }

                Spacer(GlanceModifier.height(12.dp))

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(58.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = queue.take(4)
                    val itemSize = 58.dp
                    val cornerRadius = 14.dp

                    for (i in 0 until 4) {
                        Box(
                            modifier = GlanceModifier.defaultWeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (i < items.size) {
                                val queueItem = items[i]
                                AlbumArtImageGlance(
                                    modifier = GlanceModifier.clickable(
                                        actionRunCallback<PlayerControlActionCallback>(
                                            actionParametersOf(
                                                PlayerActions.key to PlayerActions.PLAY_FROM_QUEUE,
                                                PlayerActions.songIdKey to queueItem.id
                                            )
                                        )
                                    ),
                                    bitmapData = null,
                                    albumArtUri = queueItem.albumArtUri,
                                    size = itemSize,
                                    context = context,
                                    cornerRadius = cornerRadius
                                )
                            } else {
                                EndOfQueuePlaceholder(
                                    size = itemSize,
                                    cornerRadius = cornerRadius
                                )
                            }
                        }

                        if (i < 3) {
                            Spacer(GlanceModifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AlbumArtImageGlance(
        bitmapData: ByteArray?,
        albumArtUri: String? = null,
        size: Dp? = null,
        context: Context,
        modifier: GlanceModifier = GlanceModifier,
        cornerRadius: Dp = 16.dp
    ) {
        val widgetDpSize = LocalSize.current
        val density = context.resources.displayMetrics.density
        val resolvedSize = size ?: minOf(widgetDpSize.width, widgetDpSize.height)
        val resolvedSizeDp = if (resolvedSize == Dp.Unspecified || resolvedSize <= 0.dp) 64.dp else resolvedSize
        val targetSizePx = (resolvedSizeDp.value * density).toInt().coerceAtLeast(32)
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

        val sizingModifier = if (size != null) modifier.size(size) else modifier

        Box(
            modifier = sizingModifier
        ) {
            Image(
                provider = imageProvider,
                contentDescription = context.getString(R.string.widget_album_art),
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(cornerRadius),
                contentScale = ContentScale.Crop
            )
        }
    }

    @Composable
    fun PlayPauseButtonGlance(
        modifier: GlanceModifier = GlanceModifier,
        isPlaying: Boolean,
        iconColor: ColorProvider = GlanceTheme.colors.onSurfaceVariant,
        backgroundColor: ColorProvider = GlanceTheme.colors.surfaceVariant,
        iconSize: Dp = 24.dp,
        buttonSize: Dp = 48.dp,
        cornerRadius: Dp = 0.dp
    ) {
        val context = LocalContext.current
        val params = actionParametersOf(PlayerActions.key to PlayerActions.PLAY_PAUSE)
        val resId = if (isPlaying) R.drawable.rounded_pause_24 else R.drawable.rounded_play_arrow_24
        val contentDesc = context.getString(if (isPlaying) R.string.cd_pause else R.string.cd_play)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && cornerRadius > 0.dp) {
            val density = context.resources.displayMetrics.density
            val buttonSizePx = (buttonSize.value * density).toInt().coerceAtLeast(24)
            val iconSizePx = (iconSize.value * density).toInt().coerceAtLeast(12)
            val cornerRadiusPx = cornerRadius.value * density
            val bgArgb = backgroundColor.getColor(context).toArgb()
            val iconArgb = iconColor.getColor(context).toArgb()

            val bitmap = AlbumArtBitmapCache.getButtonBitmap(
                context = context,
                resId = resId,
                sizePx = buttonSizePx,
                iconSizePx = iconSizePx,
                cornerRadiusPx = cornerRadiusPx,
                backgroundColorArgb = bgArgb,
                iconColorArgb = iconArgb
            )

            Image(
                provider = ImageProvider(bitmap),
                contentDescription = contentDesc,
                modifier = modifier.clickable(actionRunCallback<PlayerControlActionCallback>(params)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = modifier
                    .background(backgroundColor)
                    .cornerRadius(cornerRadius)
                    .clickable(actionRunCallback<PlayerControlActionCallback>(params)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(resId),
                    contentDescription = contentDesc,
                    modifier = GlanceModifier.size(iconSize),
                    colorFilter = ColorFilter.tint(iconColor)
                )
            }
        }
    }

    @Composable
    fun NextButtonGlance(
        modifier: GlanceModifier = GlanceModifier,
        iconColor: ColorProvider = GlanceTheme.colors.onSurfaceVariant,
        backgroundColor: ColorProvider = GlanceTheme.colors.surfaceVariant,
        iconSize: Dp = 24.dp,
        buttonSize: Dp = 48.dp,
        cornerRadius: Dp = 8.dp
    ) {
        val context = LocalContext.current
        val params = actionParametersOf(PlayerActions.key to PlayerActions.NEXT)
        val resId = R.drawable.rounded_skip_next_24
        val contentDesc = context.getString(R.string.next_track)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && cornerRadius > 0.dp) {
            val density = context.resources.displayMetrics.density
            val buttonSizePx = (buttonSize.value * density).toInt().coerceAtLeast(24)
            val iconSizePx = (iconSize.value * density).toInt().coerceAtLeast(12)
            val cornerRadiusPx = cornerRadius.value * density
            val bgArgb = backgroundColor.getColor(context).toArgb()
            val iconArgb = iconColor.getColor(context).toArgb()

            val bitmap = AlbumArtBitmapCache.getButtonBitmap(
                context = context,
                resId = resId,
                sizePx = buttonSizePx,
                iconSizePx = iconSizePx,
                cornerRadiusPx = cornerRadiusPx,
                backgroundColorArgb = bgArgb,
                iconColorArgb = iconArgb
            )

            Image(
                provider = ImageProvider(bitmap),
                contentDescription = contentDesc,
                modifier = modifier.clickable(actionRunCallback<PlayerControlActionCallback>(params)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = modifier
                    .background(backgroundColor)
                    .cornerRadius(cornerRadius)
                    .clickable(actionRunCallback<PlayerControlActionCallback>(params)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(resId),
                    contentDescription = contentDesc,
                    modifier = GlanceModifier.size(iconSize),
                    colorFilter = ColorFilter.tint(iconColor)
                )
            }
        }
    }

    @Composable
    fun PreviousButtonGlance(
        modifier: GlanceModifier = GlanceModifier,
        iconColor: ColorProvider = GlanceTheme.colors.onSurfaceVariant,
        backgroundColor: ColorProvider = GlanceTheme.colors.surfaceVariant,
        iconSize: Dp = 24.dp,
        buttonSize: Dp = 48.dp,
        cornerRadius: Dp = 8.dp
    ) {
        val context = LocalContext.current
        val params = actionParametersOf(PlayerActions.key to PlayerActions.PREVIOUS)
        val resId = R.drawable.rounded_skip_previous_24
        val contentDesc = context.getString(R.string.previous_track)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && cornerRadius > 0.dp) {
            val density = context.resources.displayMetrics.density
            val buttonSizePx = (buttonSize.value * density).toInt().coerceAtLeast(24)
            val iconSizePx = (iconSize.value * density).toInt().coerceAtLeast(12)
            val cornerRadiusPx = cornerRadius.value * density
            val bgArgb = backgroundColor.getColor(context).toArgb()
            val iconArgb = iconColor.getColor(context).toArgb()

            val bitmap = AlbumArtBitmapCache.getButtonBitmap(
                context = context,
                resId = resId,
                sizePx = buttonSizePx,
                iconSizePx = iconSizePx,
                cornerRadiusPx = cornerRadiusPx,
                backgroundColorArgb = bgArgb,
                iconColorArgb = iconArgb
            )

            Image(
                provider = ImageProvider(bitmap),
                contentDescription = contentDesc,
                modifier = modifier.clickable(actionRunCallback<PlayerControlActionCallback>(params)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = modifier
                    .background(backgroundColor)
                    .cornerRadius(cornerRadius)
                    .clickable(actionRunCallback<PlayerControlActionCallback>(params)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(resId),
                    contentDescription = contentDesc,
                    modifier = GlanceModifier.size(iconSize),
                    colorFilter = ColorFilter.tint(iconColor)
                )
            }
        }
    }

    @Composable
    fun EndOfQueuePlaceholder(
        modifier: GlanceModifier = GlanceModifier,
        size: Dp,
        cornerRadius: Dp
    ) {
        val context = LocalContext.current
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && cornerRadius > 0.dp) {
            val density = context.resources.displayMetrics.density
            val sizePx = (size.value * density).toInt().coerceAtLeast(24)
            val cornerRadiusPx = cornerRadius.value * density
            val bgArgb = GlanceTheme.colors.surfaceVariant.getColor(context).toArgb()
            val placeholder = AlbumArtBitmapCache.getPlaceholderBitmap(
                context = context,
                sizePx = sizePx,
                cornerRadiusPx = cornerRadiusPx,
                backgroundColorArgb = bgArgb,
                iconColorArgb = 0
            )
            Image(
                provider = ImageProvider(placeholder),
                contentDescription = null,
                modifier = modifier.size(size),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = modifier
                    .size(size)
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(cornerRadius)
            ) {}
        }
    }
}

private fun formatDurationGlance(millis: Long): String {
    if (millis < 0) return "00:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
