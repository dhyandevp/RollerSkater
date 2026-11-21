package com.rollerskater.app.data.provider

import android.Manifest
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.ParcelFileDescriptor
import com.rollerskater.app.utils.AlbumArtUtils
import java.io.File
import java.io.FileNotFoundException

class SharedArtworkContentProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? {
        val appContext = context?.applicationContext ?: return null
        val songId = parseSongId(uri, appContext.packageName) ?: return null
        val cachedFile = AlbumArtUtils.getCachedAlbumArtFile(appContext, songId)
        return cachedFile.takeIf { it.isFile && it.canRead() }
            ?.let(::imageMimeType)
            ?: DEFAULT_CONTENT_TYPE
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") {
            throw FileNotFoundException("Shared artwork provider is read-only")
        }

        val appContext = context?.applicationContext
            ?: throw FileNotFoundException("Artwork provider is unavailable")
        enforceArtworkReadAccess(uri, appContext)

        val songId = parseSongId(uri, appContext.packageName)
            ?: throw FileNotFoundException("Unsupported artwork URI")
        val isSelfCaller = Binder.getCallingUid() == appContext.applicationInfo.uid
        val file = if (isSelfCaller) {
            AlbumArtUtils.ensureAlbumArtCachedFile(appContext, songId)
        } else {
            AlbumArtUtils.getOrWarmCachedAlbumArtFile(appContext, songId)
                ?.takeIf { isSafeForExternalArtworkReader(it) }
        }?.takeIf { it.isFile && it.canRead() }
            ?: throw FileNotFoundException("Local artwork is unavailable")

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor {
        val fileDescriptor = openFile(uri, mode)
        return AssetFileDescriptor(fileDescriptor, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
    }

    /**
     * Some vendor System UI implementations reject URI grants before attempting to open a
     * non-exported provider. The provider is therefore exported for compatibility, while every
     * file open still requires our own UID, the explicit per-item grant issued by the media
     * session, or a privileged media surface reading what the session currently publishes.
     */
    private fun enforceArtworkReadAccess(uri: Uri, appContext: Context) {
        val callingUid = Binder.getCallingUid()
        val callingPid = Binder.getCallingPid()
        val permissionResult = appContext.checkUriPermission(
            uri,
            callingPid,
            callingUid,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        val mediaControlResult = appContext.checkPermission(
            Manifest.permission.MEDIA_CONTENT_CONTROL,
            callingPid,
            callingUid,
        )
        if (
            !hasArtworkReadAccess(
                callingUid = callingUid,
                providerUid = appContext.applicationInfo.uid,
                uriPermissionResult = permissionResult,
                mediaContentControlResult = mediaControlResult,
                isPublishedSessionArtwork = isPublishedSessionArtwork(uri.toString(), appContext.packageName),
            )
        ) {
            throw FileNotFoundException("Artwork URI has not been granted to the caller")
        }
    }

    companion object {
        private const val AUTHORITY_SUFFIX = ".artwork"
        private const val PATH_SONG = "song"
        private const val DEFAULT_CONTENT_TYPE = "image/jpeg"

        internal fun imageMimeType(file: File): String? {
            val header = ByteArray(12)
            val read = runCatching {
                file.inputStream().use { it.read(header) }
            }.getOrDefault(-1)
            if (read < 4) return null
            fun matches(offset: Int, vararg expected: Int): Boolean =
                read >= offset + expected.size &&
                    expected.indices.all { header[offset + it].toInt() and 0xFF == expected[it] }

            return when {
                matches(0, 0xFF, 0xD8, 0xFF) -> "image/jpeg"
                matches(0, 0x89, 0x50, 0x4E, 0x47) -> "image/png"
                matches(0, 0x47, 0x49, 0x46, 0x38) -> "image/gif"
                matches(0, 0x52, 0x49, 0x46, 0x46) && matches(8, 0x57, 0x45, 0x42, 0x50) -> "image/webp"
                matches(4, 0x66, 0x74, 0x79, 0x70) ->
                    if (matches(8, 0x61, 0x76, 0x69, 0x66) || matches(8, 0x61, 0x76, 0x69, 0x73)) "image/avif" else "image/heif"
                matches(0, 0x42, 0x4D) -> "image/bmp"
                else -> null
            }
        }

        internal fun isSafeForExternalArtworkReader(
            file: File,
            readArtworkImageBounds: (File) -> ArtworkImageBounds? = ::decodeArtworkImageBounds,
        ): Boolean {
            if (!file.isFile || !file.canRead()) return false
            if (file.length() <= 0L) return false
            val bounds = readArtworkImageBounds(file) ?: return false
            return bounds.width > 0 && bounds.height > 0
        }

        /** Reads only the image header; null when the platform can't decode it. */
        private fun decodeArtworkImageBounds(file: File): ArtworkImageBounds? {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, options)
            return ArtworkImageBounds(options.outWidth, options.outHeight)
                .takeIf { it.width > 0 && it.height > 0 }
        }

        fun authority(packageName: String): String = packageName + AUTHORITY_SUFFIX

        fun buildSongUri(
            context: Context,
            songId: Long,
            cacheBustToken: String? = null
        ): Uri = buildSongUri(context.packageName, songId, cacheBustToken)

        internal fun buildSongUri(
            packageName: String,
            songId: Long,
            cacheBustToken: String? = null
        ): Uri {
            return Uri.parse(buildSongUriString(packageName, songId, cacheBustToken))
        }

        internal fun buildSongUriString(
            packageName: String,
            songId: Long,
            cacheBustToken: String? = null
        ): String {
            val baseUri = "content://${authority(packageName)}/$PATH_SONG/$songId"
            return cacheBustToken
                ?.takeIf { it.isNotBlank() }
                ?.let { "$baseUri?t=$it" }
                ?: baseUri
        }

        internal fun parseSongId(uri: Uri, packageName: String? = null): Long? {
            return parseSongId(uri.toString(), packageName)
        }

        internal fun parseSongId(uriString: String, packageName: String? = null): Long? {
            val expectedPrefix = packageName
                ?.let(::authority)
                ?.let { "content://$it/$PATH_SONG/" }

            if (expectedPrefix != null && !uriString.startsWith(expectedPrefix)) {
                return null
            }

            val basePrefix = expectedPrefix ?: run {
                val authoritySeparator = "://"
                val schemeSplit = uriString.indexOf(authoritySeparator)
                if (schemeSplit < 0) return null
                val pathStart = uriString.indexOf('/', schemeSplit + authoritySeparator.length)
                if (pathStart < 0) return null
                val pathPrefix = uriString.substring(pathStart)
                if (!pathPrefix.startsWith("/$PATH_SONG/")) return null
                uriString.substring(0, pathStart) + "/$PATH_SONG/"
            }

            val songIdSegment = uriString
                .removePrefix(basePrefix)
                .substringBefore('?')
                .substringBefore('/')

            if (songIdSegment.isBlank()) {
                return null
            }
            return songIdSegment.toLongOrNull()
        }

        internal fun hasArtworkReadAccess(
            callingUid: Int,
            providerUid: Int,
            uriPermissionResult: Int,
            mediaContentControlResult: Int,
            isPublishedSessionArtwork: Boolean,
        ): Boolean {
            return callingUid == providerUid ||
                uriPermissionResult == PackageManager.PERMISSION_GRANTED ||
                (mediaContentControlResult == PackageManager.PERMISSION_GRANTED && isPublishedSessionArtwork)
        }

        private const val MAX_PUBLISHED_SESSION_ARTWORK = 3
        private val publishedSessionArtwork = ArrayDeque<String>(MAX_PUBLISHED_SESSION_ARTWORK)

        /** Records artwork exposed through the session's current metadata. */
        fun publishSessionArtwork(packageName: String, artworkUri: String) {
            val key = artworkKey(artworkUri, packageName) ?: return
            synchronized(publishedSessionArtwork) {
                if (publishedSessionArtwork.lastOrNull() == key) return
                publishedSessionArtwork.remove(key)
                publishedSessionArtwork.addLast(key)
                while (publishedSessionArtwork.size > MAX_PUBLISHED_SESSION_ARTWORK) {
                    publishedSessionArtwork.removeFirst()
                }
            }
        }

        internal fun isPublishedSessionArtwork(uriString: String, packageName: String): Boolean {
            val key = artworkKey(uriString, packageName) ?: return false
            return synchronized(publishedSessionArtwork) { key in publishedSessionArtwork }
        }

        internal fun clearPublishedSessionArtwork() {
            synchronized(publishedSessionArtwork) { publishedSessionArtwork.clear() }
        }

        private fun artworkKey(uriString: String, packageName: String): String? =
            parseSongId(uriString, packageName)?.let { "song:$it" }
    }
}

internal data class ArtworkImageBounds(val width: Int, val height: Int)
