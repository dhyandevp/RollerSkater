package com.rollerskater.app.data.repository

import com.rollerskater.app.data.database.TransitionDao
import com.rollerskater.app.data.database.TransitionRuleEntity
import com.rollerskater.app.data.model.TransitionResolution
import com.rollerskater.app.data.model.TransitionRule
import com.rollerskater.app.data.model.TransitionSettings
import com.rollerskater.app.data.model.TransitionSource
import com.rollerskater.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing transition rules and settings.
 * This is the single source of truth for transition-related data.
 */
@Singleton
class TransitionRepository @Inject constructor(
    private val transitionDao: TransitionDao,
    private val userPreferences: UserPreferencesRepository
) {

    /**
     * Resolves the effective transition settings for a given playback context.
     * It checks for rules in the following priority order:
     * 1. A specific rule for the given fromTrackId and toTrackId in the playlist.
     * 2. The default rule for the given playlistId.
     * 3. The global default settings from user preferences.
     *
     * Pass null track ids to skip the per-pair rule, e.g. when the queue no longer plays the
     * pair the way the playlist has it.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun resolveTransitionSettings(
        playlistId: String,
        fromTrackId: String?,
        toTrackId: String?
    ): Flow<TransitionResolution> {
        // Chain the lookups according to priority: specific -> playlist -> global
        if (fromTrackId == null || toTrackId == null) return resolvePlaylistDefault(playlistId)
        return transitionDao.getSpecificRule(playlistId, fromTrackId, toTrackId)
            .flatMapLatest { specificRule ->
                if (specificRule != null) {
                    flowOf(
                        TransitionResolution(
                            settings = specificRule.settings,
                            source = TransitionSource.PLAYLIST_SPECIFIC,
                        )
                    )
                } else {
                    resolvePlaylistDefault(playlistId)
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun resolvePlaylistDefault(playlistId: String): Flow<TransitionResolution> =
        transitionDao.getPlaylistDefaultRule(playlistId).flatMapLatest { playlistRule ->
            if (playlistRule != null) {
                flowOf(
                    TransitionResolution(
                        settings = playlistRule.settings,
                        source = TransitionSource.PLAYLIST_DEFAULT,
                    )
                )
            } else {
                userPreferences.globalTransitionSettingsFlow.map { settings ->
                    TransitionResolution(
                        settings = settings,
                        source = TransitionSource.GLOBAL_DEFAULT,
                    )
                }
            }
        }

    /**
     * Gets all rules defined for a specific playlist.
     */
    fun getAllRulesForPlaylist(playlistId: String): Flow<List<TransitionRule>> {
        return transitionDao.getAllRulesForPlaylist(playlistId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    /**
     * Gets the default rule for a playlist. Does not fall back to global settings.
     * Returns a flow that may emit null if no specific default rule exists for the playlist.
     */
    fun getPlaylistDefaultRule(playlistId: String): Flow<TransitionRule?> {
        return transitionDao.getPlaylistDefaultRule(playlistId).map { entity ->
            entity?.toModel()
        }
    }

    /**
     * Saves a transition rule. If a rule for the given context
     * (playlistId, fromTrackId, toTrackId) already exists, it will be updated.
     * Otherwise, a new rule will be created.
     */
    suspend fun saveRule(rule: TransitionRule) {
        transitionDao.setRule(rule.toEntity())
    }

    /**
     * Deletes a specific rule by its unique ID.
     */
    suspend fun deleteRule(ruleId: Long) {
        transitionDao.deleteRule(ruleId)
    }

    /**
     * Deletes the default transition rule for a given playlist.
     */
    suspend fun deletePlaylistDefaultRule(playlistId: String) {
        transitionDao.deletePlaylistDefaultRule(playlistId)
    }

    /**
     * Gets the flow of global transition settings.
     */
    fun getGlobalSettings(): Flow<TransitionSettings> {
        return userPreferences.globalTransitionSettingsFlow
    }

    /**
     * Saves the global transition settings.
     */
    suspend fun saveGlobalSettings(settings: TransitionSettings) {
        userPreferences.saveGlobalTransitionSettings(settings)
    }

    private fun TransitionRuleEntity.toModel(): TransitionRule {
        return TransitionRule(
            id = this.id,
            playlistId = this.playlistId,
            fromTrackId = this.fromTrackId,
            toTrackId = this.toTrackId,
            settings = this.settings
        )
    }

    private fun TransitionRule.toEntity(): TransitionRuleEntity {
        return TransitionRuleEntity(
            id = this.id,
            playlistId = this.playlistId,
            fromTrackId = this.fromTrackId,
            toTrackId = this.toTrackId,
            settings = this.settings
        )
    }
}
