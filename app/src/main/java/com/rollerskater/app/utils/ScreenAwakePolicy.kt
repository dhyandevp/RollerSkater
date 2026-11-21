package com.rollerskater.app.utils

internal fun shouldKeepScreenAwake(
    preferenceEnabled: Boolean,
    isPlaying: Boolean,
): Boolean = preferenceEnabled && isPlaying
