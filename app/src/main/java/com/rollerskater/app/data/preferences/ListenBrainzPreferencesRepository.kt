package com.rollerskater.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-source scrobble toggles. All default to enabled — connecting a ListenBrainz account is the
 * consent act; users disable a streaming source only when their server already scrobbles
 * server-side. With no account connected these values have no effect.
 */
@Singleton
class ListenBrainzPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val SCROBBLE_LOCAL = booleanPreferencesKey("listenbrainz_scrobble_local")
    }

    val scrobbleLocalFlow: Flow<Boolean> = dataStore.data.map { it[Keys.SCROBBLE_LOCAL] ?: true }

    suspend fun setScrobbleLocal(enabled: Boolean) {
        dataStore.edit { it[Keys.SCROBBLE_LOCAL] = enabled }
    }

    suspend fun isSourceEnabled(source: String): Boolean {
        return dataStore.data.first()[Keys.SCROBBLE_LOCAL] ?: true
    }
}
