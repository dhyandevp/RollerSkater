package com.rollerskater.app.ui.glancewidget

import android.content.Context
import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AlbumArtBitmapCacheTest {

    private val context = mockk<Context>(relaxed = true)

    @Test
    fun `getKey returns consistent hash code string`() {
        val bytes1 = byteArrayOf(1, 2, 3, 4, 5)
        val bytes2 = byteArrayOf(1, 2, 3, 4, 5)

        val key1 = AlbumArtBitmapCache.getKey(bytes1)
        val key2 = AlbumArtBitmapCache.getKey(bytes2)

        assertThat(key1).isEqualTo(key2)
    }

    @Test
    fun `putBitmap and getBitmap stores and retrieves bitmap`() {
        val bitmap = mockk<Bitmap>(relaxed = true)
        every { bitmap.byteCount } returns 1024

        AlbumArtBitmapCache.putBitmap("test_key_1", bitmap)
        val retrieved = AlbumArtBitmapCache.getBitmap("test_key_1")

        assertThat(retrieved).isSameInstanceAs(bitmap)
    }

    @Test
    fun `getRoundedAlbumArt returns null when data and uri are null or empty`() {
        val resultNull = AlbumArtBitmapCache.getRoundedAlbumArt(
            context = context,
            data = null,
            uri = null,
            sizePx = 64,
            cornerRadiusPx = 16f
        )
        assertThat(resultNull).isNull()

        val resultEmpty = AlbumArtBitmapCache.getRoundedAlbumArt(
            context = context,
            data = ByteArray(0),
            uri = "",
            sizePx = 64,
            cornerRadiusPx = 16f
        )
        assertThat(resultEmpty).isNull()
    }

    @Test
    fun `getRoundedAlbumArt returns cached bitmap when present for byte array`() {
        val bytes = byteArrayOf(10, 20, 30)
        val cachedBitmap = mockk<Bitmap>(relaxed = true)
        every { cachedBitmap.byteCount } returns 2048

        val key = "data_${bytes.contentHashCode()}_64_16"
        AlbumArtBitmapCache.putBitmap(key, cachedBitmap)

        val result = AlbumArtBitmapCache.getRoundedAlbumArt(
            context = context,
            data = bytes,
            uri = null,
            sizePx = 64,
            cornerRadiusPx = 16f
        )

        assertThat(result).isSameInstanceAs(cachedBitmap)
    }

    @Test
    fun `getRoundedAlbumArt returns cached bitmap when present for uri`() {
        val uri = "content://media/external/audio/albumart/42"
        val cachedBitmap = mockk<Bitmap>(relaxed = true)
        every { cachedBitmap.byteCount } returns 2048

        val key = "uri_${uri}_80_20"
        AlbumArtBitmapCache.putBitmap(key, cachedBitmap)

        val result = AlbumArtBitmapCache.getRoundedAlbumArt(
            context = context,
            data = null,
            uri = uri,
            sizePx = 80,
            cornerRadiusPx = 20f
        )

        assertThat(result).isSameInstanceAs(cachedBitmap)
    }
}
