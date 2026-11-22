package com.rollerskater.app.utils

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.withLink
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import android.icu.text.Transliterator
import com.rollerskater.app.data.model.BackgroundVocals
import com.rollerskater.app.data.model.Lyrics
import com.rollerskater.app.data.model.SyncedLine
import com.rollerskater.app.data.model.SyncedWord
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

object MultiLangRomanizer {
    private val anyLatinTransliterator: Transliterator? by lazy {
        try {
            Transliterator.getInstance("Any-Latin; Latin-ASCII")
        } catch (_: Throwable) {
            try {
                Transliterator.getInstance("Any-Latin")
            } catch (_: Throwable) {
                null
            }
        }
    }

    private val hanLatinTransliterator: Transliterator? by lazy {
        try {
            Transliterator.getInstance("Han-Latin; Latin-ASCII")
        } catch (_: Throwable) {
            null
        }
    }

    fun isJapanese(text: String, entireLyricsHasKana: Boolean = false): Boolean {
        if (text.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' }) return true
        return entireLyricsHasKana && text.any { it in '\u4E00'..'\u9FFF' }
    }

    fun isKorean(text: String) = text.any { it.code in 0xAC00..0xD7A3 }
    fun isHindi(text: String) = text.any { it.code in 0x0900..0x097F }
    fun isPunjabi(text: String) = text.any { it.code in 0x0A00..0x0A7F }
    fun isCyrillic(text: String) = text.any { it.code in 0x0400..0x04FF }
    fun isChinese(text: String) = text.any { it.code in 0x4E00..0x9FFF }

    fun isScriptThatNeedsRomanization(text: String): Boolean {
        return text.any { char ->
            val code = char.code
            code in 0x3040..0x309F ||
            code in 0x30A0..0x30FF ||
            code in 0x4E00..0x9FFF ||
            code in 0xAC00..0xD7A3 ||
            code in 0x0900..0x097F ||
            code in 0x0A00..0x0A7F ||
            code in 0x0400..0x04FF
        }
    }

    fun romanizeLine(line: String, entireLyricsHasKana: Boolean = false): String? {
        val transliterator = anyLatinTransliterator ?: return null
        return try {
            transliterator.transliterate(line)
                ?.replace(Regex("\\s+"), " ")
                ?.trim()
                ?.capitalizeFirstLetter()
                ?.takeIf { it.isNotEmpty() }
        } catch (e: Throwable) {
            Timber.e(e)
            null
        }
    }

    fun romanizeJapanese(japaneseText: String): String? {
        return anyLatinTransliterator?.transliterate(japaneseText)?.replace(Regex("\\s+"), " ")?.trim()
    }

    fun romanizeChinese(text: String): String? {
        return (hanLatinTransliterator ?: anyLatinTransliterator)?.transliterate(text)?.replace(Regex("\\s+"), " ")?.trim()
    }

    private val HANGUL_INITIALS = arrayOf(
        "g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s", "ss", "", "j", "jj", "ch", "k", "t", "p", "h"
    )
    private val HANGUL_VOWELS = arrayOf(
        "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa", "wae", "oe", "yo", "u", "wo", "we", "wi", "yu", "eu", "ui", "i"
    )
    private val HANGUL_FINALS = arrayOf(
        "", "k", "k", "k", "n", "n", "n", "t", "l", "k", "m", "p", "t", "t", "p", "t", "m", "p", "p", "t", "t", "ng", "t", "t", "k", "t", "p", "h"
    )

    fun romanizeKorean(text: String): String {
        val icuResult = anyLatinTransliterator?.transliterate(text)
        if (icuResult != null && icuResult != text) {
            return icuResult.replace(Regex("\\s+"), " ").trim()
        }
        val sb = StringBuilder(text.length * 2)
        for (char in text) {
            val code = char.code
            if (code in 0xAC00..0xD7A3) {
                val index = code - 0xAC00
                val initial = index / (21 * 28)
                val vowel = (index % (21 * 28)) / 28
                val final = index % 28
                sb.append(HANGUL_INITIALS[initial])
                sb.append(HANGUL_VOWELS[vowel])
                sb.append(HANGUL_FINALS[final])
            } else {
                sb.append(char)
            }
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    fun romanizeHindi(text: String): String {
        return anyLatinTransliterator?.transliterate(text)?.replace(Regex("\\s+"), " ")?.trim() ?: text
    }

    fun romanizePunjabi(text: String): String {
        return anyLatinTransliterator?.transliterate(text)?.replace(Regex("\\s+"), " ")?.trim() ?: text
    }

    fun romanizeCyrillic(text: String): String? {
        return anyLatinTransliterator?.transliterate(text)?.replace(Regex("\\s+"), " ")?.trim()
    }
}

private fun String.capitalizeFirstLetter(): String {
    if (this.isEmpty()) return this
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}

object LyricsUtils {

    private val LRC_LINE_REGEX = Pattern.compile("^\\[(\\d{2}):(\\d{2})[.:](\\d{2,3})](.*)$")
    private val LRC_WORD_REGEX = Pattern.compile("<(\\d{2}):(\\d{2})[.:](\\d{2,3})>([^<]*)")
    private val LRC_WORD_TAG_REGEX = Regex("<\\d{2}:\\d{2}[.:]\\d{2,3}>")
    private val LRC_WORD_SPLIT_REGEX = Regex("(?=<\\d{2}:\\d{2}[.:]\\d{2,3}>)")
    private val LRC_TIMESTAMP_TAG_REGEX = Regex("\\[\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?]")
    private val TRANSLATION_CREDIT_REGEX = Regex("^\\s*by\\s*[:：].+", RegexOption.IGNORE_CASE)
    private val LRC_METADATA_PATTERN = Pattern.compile("^\\[[a-zA-Z]+:.*]$")

    // Enhanced-LRC backing vocals for the line above: [bg: <00:12.00>(oh <00:12.40>yeah)]
    private val LRC_BACKGROUND_LINE_REGEX = Regex("^\\[bg:(.*)]$", RegexOption.IGNORE_CASE)

    // Translation of the line above, read only for a line with no main text: [tr: (Uh uh)]
    private val LRC_TRANSLATION_LINE_REGEX = Regex("^\\[tr:(.*)]$", RegexOption.IGNORE_CASE)

    // Enhanced-LRC singer prefix, before or right after the first word tag: "v1: Hi", "<00:01.00>v2:Hi".
    private val LRC_VOICE_PREFIX_REGEX = Regex(
        "^(<\\d{2}:\\d{2}[.:]\\d{2,3}>)?\\s*v(\\d{1,4})\\s*:\\s*",
        RegexOption.IGNORE_CASE
    )

    private val KUGOU_LINE_REGEX = Pattern.compile("^\\[(\\d+),(\\d+)](.*)$")
    private val KUGOU_WORD_PATTERN = Pattern.compile("<(\\d+),(\\d+),(\\d+)>([^<]*)")

    /**
     * Parses a String containing lyrics in LRC or plain-text format.
     * @param lyricsText The raw lyrics text to process.
     * @return A [Lyrics] object with either the 'plain' or 'synced' list populated.
     */
    fun parseLyrics(lyricsText: String?): Lyrics {
        if (lyricsText.isNullOrEmpty()) {
            return Lyrics(plain = emptyList(), synced = emptyList())
        }

        val entireLyricsHasKana = lyricsText.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' }

        // Files written on macOS and iOS often store Vietnamese and other accented text
        // decomposed, with each tone mark as its own code point that a timed token can cut off.
        val normalizedInput = stripLeadingLyricsDocumentNoise(lyricsText).toNfc()
        if (looksLikeTtmlDocument(normalizedInput)) {
            val converted = TtmlLyricsParser.parseToEnhancedLrc(normalizedInput)
                ?: return Lyrics(plain = emptyList(), synced = emptyList())
            return parseLyrics(converted)
        }

        if (looksLikeKugouFormat(normalizedInput)) {
            return parseKugouLyrics(normalizedInput)
        }

        val syncedLines = mutableListOf<SyncedLine>()
        val plainLines = mutableListOf<String>()
        var isSynced = false
        // True while the only synced line is the empty one made for backing vocals before any line.
        var holdsOpeningBackground = false

        normalizedInput.lines().forEach { rawLine ->
            val line = sanitizeLrcLine(rawLine)
            if (line.isEmpty()) return@forEach

            // Checked before the metadata pattern, which "[bg: …]" would otherwise match.
            val backgroundMatch = LRC_BACKGROUND_LINE_REGEX.matchEntire(line)
            if (backgroundMatch != null) {
                val previous = syncedLines.lastOrNull()
                val background = parseBackgroundVocals(
                    rawBody = backgroundMatch.groupValues[1],
                    anchorTimeMs = previous?.time?.toLong() ?: 0L
                ) ?: return@forEach
                if (previous != null) {
                    syncedLines[syncedLines.lastIndex] =
                        previous.copy(background = previous.background.mergedWith(background))
                } else if (!background.words.isNullOrEmpty()) {
                    // Backing vocals before the first sung line get an empty main line of their
                    // own, like a TTML paragraph sung only in the background, so they still hide
                    // with the background vocals setting.
                    isSynced = true
                    holdsOpeningBackground = true
                    syncedLines.add(
                        SyncedLine(
                            time = background.words.first().time,
                            line = "",
                            background = background
                        )
                    )
                }
                return@forEach
            }
            // Also checked before the metadata pattern. Only read under a line with no main text,
            // the one place translationLrcLine writes it; anywhere else it is skipped like a header
            // tag, since a line with text writes its translation at its own time and would not
            // read a `[tr: …]` translation back the same way.
            val translationMatch = LRC_TRANSLATION_LINE_REGEX.matchEntire(line)
            if (translationMatch != null) {
                val previous = syncedLines.lastOrNull()
                val translation = stripFormatCharacters(translationMatch.groupValues[1]).trim()
                if (previous != null && previous.line.isBlank() && translation.isNotEmpty()) {
                    syncedLines[syncedLines.lastIndex] = previous.copy(
                        translation = previous.translation?.let { "$it\n$translation" } ?: translation
                    )
                }
                return@forEach
            }
            if (LRC_METADATA_PATTERN.matcher(line).matches()) return@forEach

            val lineMatcher = LRC_LINE_REGEX.matcher(line)
            if (lineMatcher.matches()) {
                isSynced = true
                val minutes = lineMatcher.group(1)?.toLong() ?: 0
                val seconds = lineMatcher.group(2)?.toLong() ?: 0
                val fraction = lineMatcher.group(3)?.toLong() ?: 0
                val textWithTags = stripFormatCharacters(lineMatcher.group(4)?.trim() ?: "")
                val (voice, text) = splitVoicePrefix(stripLrcTimestamps(textWithTags))

                val millis = if (lineMatcher.group(3)?.length == 2) fraction * 10 else fraction
                val lineTimestamp = minutes * 60 * 1000 + seconds * 1000 + millis

                val syncedLine = if (text.contains(LRC_WORD_TAG_REGEX)) {
                    val words = parseTimedWords(text, lineTimestamp)
                    val displayText = LRC_WORD_TAG_REGEX.replace(text, "")
                    SyncedLine(
                        time = lineTimestamp.toInt(),
                        line = displayText,
                        words = words.takeIf { it.isNotEmpty() },
                        voice = voice
                    )
                } else {
                    SyncedLine(time = lineTimestamp.toInt(), line = text, voice = voice)
                }
                // Backing vocals written just before a line at that line's own time are sung with
                // it, unless a `[tr: …]` line already gave them a translation of their own.
                val opening = syncedLines.singleOrNull()?.takeIf {
                    holdsOpeningBackground &&
                        it.line.isBlank() &&
                        it.translation == null &&
                        it.time == syncedLine.time
                }
                holdsOpeningBackground = false
                if (opening != null) {
                    syncedLines[0] = syncedLine.copy(background = opening.background)
                } else {
                    syncedLines.add(syncedLine)
                }
            } else {
                val stripped = stripLrcTimestamps(stripFormatCharacters(line))
                if (isSynced && syncedLines.isNotEmpty()) {
                    val last = syncedLines.removeAt(syncedLines.lastIndex)
                    val mergedLineText = if (last.line.isEmpty()) {
                        stripped
                    } else {
                        last.line + "\n" + stripped
                    }
                    syncedLines.add(last.copy(line = mergedLineText))
                } else {
                    plainLines.add(stripped)
                }
            }
        }

        return if (isSynced && syncedLines.isNotEmpty()) {
            val sortedSyncedLines = syncedLines.sortedBy { it.time }
            val pairedLines = pairTranslationLines(sortedSyncedLines).map { line ->
                line.copy(romanization = MultiLangRomanizer.romanizeLine(line.line, entireLyricsHasKana))
            }
            val plainVersion = pairedLines.map { line ->
                buildString {
                    // A line sung only in the background reads as its backing vocals.
                    append(line.line.ifBlank { line.background?.text.orEmpty() })
                    if (!line.romanization.isNullOrEmpty()) append("\n").append(line.romanization)
                    if (!line.translation.isNullOrEmpty()) append("\n").append(line.translation)
                }
            }
            Lyrics(synced = pairedLines, plain = plainVersion)
        } else {
            Lyrics(plain = plainLines)
        }
    }

    /** Splits a leading `vN:` singer prefix off a line body, keeping a word tag that came before it. */
    private fun splitVoicePrefix(text: String): Pair<Int?, String> {
        val match = LRC_VOICE_PREFIX_REGEX.find(text) ?: return null to text
        val voice = match.groupValues[2].toIntOrNull() ?: return null to text
        return voice to match.groupValues[1] + text.substring(match.range.last + 1)
    }

    /**
     * Splits an enhanced-LRC line body into timed syllables. A syllable starts a new visual word
     * only when whitespace separates it from the one before, so `<00:45.73>whis<00:46.29>per`
     * stays one word. A tag with no text after it marks when the previous syllable ends.
     */
    private fun parseTimedWords(text: String, lineTimestampMs: Long): List<SyncedWord> {
        val words = mutableListOf<SyncedWord>()
        var pendingWordBoundary = false

        for (part in text.split(LRC_WORD_SPLIT_REGEX)) {
            if (part.isEmpty()) continue
            val wordMatcher = LRC_WORD_REGEX.matcher(part)
            if (wordMatcher.find()) {
                val wordMinutes = wordMatcher.group(1)?.toLong() ?: 0
                val wordSeconds = wordMatcher.group(2)?.toLong() ?: 0
                val wordFraction = wordMatcher.group(3)?.toLong() ?: 0
                val wordText = stripFormatCharacters(wordMatcher.group(4) ?: "")
                val timedWordTextRaw = wordText
                    .substringBefore('\n')
                    .substringBefore('\r')
                    .substringBefore("\\n")
                    .substringBefore("\\r")
                val startsNewWord = words.isEmpty() ||
                    pendingWordBoundary ||
                    timedWordTextRaw.firstOrNull()?.isWhitespace() == true
                val timedWordText = timedWordTextRaw.trim()
                val endsWithWhitespace = timedWordTextRaw.lastOrNull()?.isWhitespace() == true
                // A bare closing tag ("bones<00:12.80>") neither starts nor cancels a word break.
                pendingWordBoundary = if (timedWordText.isEmpty()) {
                    pendingWordBoundary || endsWithWhitespace
                } else {
                    endsWithWhitespace
                }
                val wordMillis = if (wordMatcher.group(3)?.length == 2) wordFraction * 10 else wordFraction
                val wordTimestamp = (wordMinutes * 60 * 1000 + wordSeconds * 1000 + wordMillis).toInt()
                if (timedWordText.isEmpty()) {
                    words.endLastSyllableAt(wordTimestamp)
                } else {
                    words.addSyllable(
                        SyncedWord(
                            time = wordTimestamp,
                            word = timedWordText,
                            startsNewWord = startsNewWord
                        )
                    )
                }
            } else {
                if (words.isEmpty()) {
                    val leading = stripFormatCharacters(part)
                    val startsNewWord = pendingWordBoundary || leading.firstOrNull()?.isWhitespace() == true
                    val visibleLeading = leading.trim()
                    pendingWordBoundary = leading.lastOrNull()?.isWhitespace() == true
                    if (visibleLeading.isNotEmpty()) {
                        words.add(
                            SyncedWord(
                                time = lineTimestampMs.toInt(),
                                word = visibleLeading,
                                startsNewWord = words.isEmpty() || startsNewWord
                            )
                        )
                    }
                } else if (part.any { it.isWhitespace() }) {
                    pendingWordBoundary = true
                }
            }
        }
        return words
    }

    private fun MutableList<SyncedWord>.endLastSyllableAt(timeMs: Int) {
        val last = lastOrNull() ?: return
        if (last.endTime == null && timeMs > last.time) {
            this[lastIndex] = last.copy(endTime = timeMs)
        }
    }

    /**
     * Adds [syllable], or folds it into the previous one when it starts with a combining mark:
     * a tone mark cut off from its letter cannot be drawn on its own (it shows as a dotted circle).
     */
    private fun MutableList<SyncedWord>.addSyllable(syllable: SyncedWord) {
        val previous = lastOrNull()
        if (previous == null || !syllable.word.startsWithCombiningMark()) {
            add(syllable)
            return
        }
        this[lastIndex] = previous.copy(
            word = (previous.word + syllable.word).toNfc(),
            endTime = listOfNotNull(previous.endTime, syllable.endTime).maxOrNull()
        )
    }

    private fun String.startsWithCombiningMark(): Boolean {
        if (isEmpty()) return false
        return when (Character.getType(codePointAt(0)).toByte()) {
            Character.NON_SPACING_MARK,
            Character.ENCLOSING_MARK,
            Character.COMBINING_SPACING_MARK -> true
            else -> false
        }
    }

    private fun parseBackgroundVocals(rawBody: String, anchorTimeMs: Long): BackgroundVocals? {
        val body = stripFormatCharacters(rawBody).trim()
        val text = LRC_WORD_TAG_REGEX.replace(body, "").trim()
        if (text.isEmpty()) return null
        val words = if (body.contains(LRC_WORD_TAG_REGEX)) parseTimedWords(body, anchorTimeMs) else emptyList()
        return BackgroundVocals(text = text, words = words.takeIf { it.isNotEmpty() })
    }

    /** Joins consecutive `[bg:]` lines for the same main line into one backing part. */
    private fun BackgroundVocals?.mergedWith(next: BackgroundVocals): BackgroundVocals {
        if (this == null) return next
        val mergedWords = if (words != null && next.words != null) {
            words + next.words.mapIndexed { index, word ->
                if (index == 0) word.copy(startsNewWord = true) else word
            }
        } else {
            // Timed and untimed parts cannot share one word list; show the joined text instead.
            null
        }
        return BackgroundVocals(text = "$text ${next.text}", words = mergedWords)
    }

    /**
     * Returns true when the raw text contains at least one line in Kugou format:
     *   [lineStartMs,lineDurationMs]…
     * where lineStartMs > 999 (milliseconds, not an LRC minute value).
     */
    private fun looksLikeKugouFormat(text: String): Boolean {
        return text.lines().any { raw ->
            val line = raw.trim()
            if (line.isEmpty()) return@any false
            if (LRC_METADATA_PATTERN.matcher(line).matches()) return@any false
            val m = KUGOU_LINE_REGEX.matcher(line)
            m.matches() && (m.group(1)?.toLongOrNull() ?: 0L) > 999L
        }
    }

    /**
     * Parses a Kugou / Paxsenix word-by-word LRC file into [Lyrics].
     *
     * Line format:
     *   [lineStartMs,lineDurationMs]<wordOffset1Ms,dur,0>word1<wordOffset2Ms,dur,0>word2…
     *
     * Word offsets are *relative* to the line start.
     * A word token with offset 0 and no preceding tokens means the token is the
     * line itself; subsequent 0-offset tokens are continuations (startsNewWord = false).
     *
     * An optional [offset:N] header (milliseconds) shifts all timestamps.
     */
    private fun parseKugouLyrics(text: String): Lyrics {
        val globalOffsetMs = text.lines()
            .firstOrNull { it.trim().startsWith("[offset:", ignoreCase = true) }
            ?.trim()
            ?.removePrefix("[offset:")
            ?.removeSuffix("]")
            ?.trim()
            ?.toLongOrNull() ?: 0L

        val syncedLines = mutableListOf<SyncedLine>()

        for (raw in text.lines()) {
            val line = raw.trim()
            if (line.isEmpty() || LRC_METADATA_PATTERN.matcher(line).matches()) continue

            val headerMatcher = KUGOU_LINE_REGEX.matcher(line)
            if (!headerMatcher.matches()) continue
            val lineStartMs = (headerMatcher.group(1)?.toLongOrNull() ?: continue) + globalOffsetMs
            if (lineStartMs <= 999L && globalOffsetMs == 0L) continue
            val body = headerMatcher.group(3) ?: ""

            val words = mutableListOf<SyncedWord>()
            val wordMatcher = KUGOU_WORD_PATTERN.matcher(body)
            var previousEndsWithSpace = true

            while (wordMatcher.find()) {
                val wordOffsetMs = wordMatcher.group(1)?.toLongOrNull() ?: 0L
                val wordDurationMs = wordMatcher.group(2)?.toLongOrNull() ?: 0L
                val rawText = wordMatcher.group(4) ?: ""

                val wordText = rawText.trim()
                val startsNew = words.isEmpty() ||
                    previousEndsWithSpace ||
                    rawText.firstOrNull()?.isWhitespace() == true ||
                    (wordOffsetMs > 0L && shouldSeparateKugouTokens(words.lastOrNull()?.word, wordText))
                previousEndsWithSpace = rawText.lastOrNull()?.isWhitespace() == true

                if (wordText.isEmpty()) continue

                val wordStartMs = lineStartMs + wordOffsetMs
                words.addSyllable(
                    SyncedWord(
                        time = wordStartMs.toInt(),
                        word = wordText,
                        startsNewWord = startsNew,
                        endTime = (wordStartMs + wordDurationMs).toInt().takeIf { wordDurationMs > 0L }
                    )
                )
            }

            val plainText = KUGOU_WORD_PATTERN.toRegex().replace(body) { it.groupValues[4] }.trim()
            if (plainText.isEmpty() && words.isEmpty()) continue

            syncedLines.add(
                SyncedLine(
                    time = lineStartMs.toInt(),
                    line = plainText,
                    words = words.takeIf { it.isNotEmpty() }
                )
            )
        }

        if (syncedLines.isEmpty()) return Lyrics(plain = emptyList(), synced = emptyList())

        val sorted = syncedLines.sortedBy { it.time }
        val paired = pairTranslationLines(sorted)
        val plainVersion = paired.map { it.line }
        return Lyrics(synced = paired, plain = plainVersion, areFromRemote = false)
    }

    /**
     * Some Kugou producers omit spaces between timed Latin-script tokens. Recover those
     * visual word boundaries without adding artificial gaps between writing systems that
     * normally do not place a space between every timed glyph.
     */
    private fun shouldSeparateKugouTokens(previous: String?, current: String): Boolean {
        val previousChar = previous?.lastOrNull { !it.isWhitespace() } ?: return false
        val currentChar = current.firstOrNull { !it.isWhitespace() } ?: return false

        if (currentChar.isLetterOrDigit()) {
            if (previousChar.isLetterOrDigit()) {
                return usesVisibleInterWordSpacing(previousChar) &&
                    usesVisibleInterWordSpacing(currentChar)
            }
            return previousChar !in KUGOU_JOINING_PUNCTUATION
        }
        return false
    }

    private fun usesVisibleInterWordSpacing(char: Char): Boolean = when (
        Character.UnicodeScript.of(char.code)
    ) {
        Character.UnicodeScript.HAN,
        Character.UnicodeScript.HIRAGANA,
        Character.UnicodeScript.KATAKANA,
        Character.UnicodeScript.HANGUL,
        Character.UnicodeScript.THAI,
        Character.UnicodeScript.LAO,
        Character.UnicodeScript.KHMER,
        Character.UnicodeScript.MYANMAR -> false
        else -> true
    }

    private val KUGOU_JOINING_PUNCTUATION = setOf('\'', '\u2019', '-', '\u2010', '\u2011')

    /**
     * Pairs consecutive synced lines that share the same timestamp.
     * The second line is treated as a translation of the first.
     * Only pairs one translation per original — a third line at the same timestamp stays separate.
     * A line marked for another singer (`v2:` after `v1:`) is a duet part, not a translation.
     * An empty line (one sung only in the background) takes no line as its translation, since the
     * next line may be sung at the same time; its translation is read from a `[tr: …]` line instead.
     */
    internal fun pairTranslationLines(lines: List<SyncedLine>): List<SyncedLine> {
        if (lines.size < 2) return lines
        val result = mutableListOf<SyncedLine>()
        var i = 0
        while (i < lines.size) {
            val current = lines[i]
            val next = lines.getOrNull(i + 1)
            if (
                next != null &&
                next.time == current.time &&
                current.translation == null &&
                current.line.isNotBlank() &&
                next.line.isNotBlank() &&
                (next.voice == null || next.voice == current.voice)
            ) {
                val translationParts = mutableListOf(next.line)
                var consumed = 2
                while (true) {
                    val trailing = lines.getOrNull(i + consumed) ?: break
                    if (trailing.time != current.time || !isTranslationCreditLine(trailing.line)) break
                    translationParts.add(trailing.line)
                    consumed++
                }
                result.add(
                    current.copy(
                        translation = translationParts.joinToString("\n"),
                        background = current.background ?: next.background
                    )
                )
                i += consumed
            } else {
                result.add(current)
                i++
            }
        }
        return result
    }

    internal fun stripLrcTimestamps(value: String): String {
        if (value.isEmpty()) return value
        val withoutTags = LRC_TIMESTAMP_TAG_REGEX.replace(value, "")
        return withoutTags.trimStart()
    }

    internal fun isTranslationCreditLine(line: String): Boolean {
        val normalized = stripLrcTimestamps(line).trim()
        return normalized.isNotEmpty() && TRANSLATION_CREDIT_REGEX.matches(normalized)
    }

    /**
     * Converts synced lyrics to LRC format string.
     * Each line is formatted as [mm:ss.xx]text, keeping `vN:` singer prefixes, `[bg: …]`
     * backing vocals and translations so the file reads back the same way.
     * @param syncedLines The list of synced lines to convert.
     * @return A string in LRC format.
     */
    fun syncedToLrcString(syncedLines: List<SyncedLine>): String =
        toEnhancedLrc(syncedLines, includeWordTimings = false)

    /**
     * Writes synced lyrics as enhanced LRC that [parseLyrics] reads back into the same lines:
     * a `vN:` singer prefix, `<mm:ss.xx>` syllable tags (plus a closing tag where a syllable's
     * end is known), a `[bg: …]` line for backing vocals, then translations at the line's time
     * (as `[tr: …]` lines under a line with no main text, see [translationLrcLine]).
     * The backing line comes before the translations so it attaches to the sung line again.
     */
    fun toEnhancedLrc(lines: List<SyncedLine>, includeWordTimings: Boolean = true): String {
        return lines.sortedBy { it.time }.flatMap { line ->
            val timestamp = "[${formatLrcTimestamp(line.time)}]"
            buildList {
                val body = line.words
                    ?.takeIf { includeWordTimings && it.isNotEmpty() }
                    ?.let(::formatTimedWords)
                    ?: line.line
                add(timestamp + line.voice?.let { "v$it:" }.orEmpty() + body)

                line.background?.let { background ->
                    val backgroundBody = background.words
                        ?.takeIf { includeWordTimings && it.isNotEmpty() }
                        ?.let(::formatTimedWords)
                        ?: background.text
                    if (backgroundBody.isNotBlank()) add("[bg:$backgroundBody]")
                }

                line.translation
                    ?.lines()
                    ?.filter { it.isNotBlank() }
                    ?.forEach { translationLine -> add(translationLrcLine(timestamp, line.line, translationLine)) }
            }
        }.joinToString("\n")
    }

    /**
     * One line of [mainLine]'s translation. A line with main text takes the next line at its own
     * time as its translation, which other players read too. An empty main line (sung only in the
     * background) cannot: a line sung at that time would read back as its translation, so its
     * translation is written as `[tr: …]` instead.
     */
    internal fun translationLrcLine(timestamp: String, mainLine: String, translationLine: String): String =
        if (mainLine.isBlank()) "[tr:$translationLine]" else timestamp + translationLine

    private fun formatTimedWords(words: List<SyncedWord>): String = buildString {
        words.forEachIndexed { index, word ->
            if (index > 0 && word.startsNewWord) append(' ')
            append('<').append(formatLrcTimestamp(word.time)).append('>').append(word.word)
            word.endTime
                ?.takeIf { it > word.time }
                ?.let { append('<').append(formatLrcTimestamp(it)).append('>') }
        }
    }

    internal fun formatLrcTimestamp(timeMs: Int): String {
        val safeTimeMs = timeMs.coerceAtLeast(0)
        val totalSeconds = safeTimeMs / 1_000
        val hundredths = (safeTimeMs % 1_000) / 10
        return String.format(Locale.US, "%02d:%02d.%02d", totalSeconds / 60, totalSeconds % 60, hundredths)
    }

    /**
     * Converts plain lyrics (list of lines) to a plain text string.
     * Strips any auto-generated romanization suffix (after '\n') before storage.
     * @param plainLines The list of plain text lines.
     * @return A string with each line separated by a newline.
     */
    fun plainToString(plainLines: List<String>): String {
        return plainLines.joinToString("\n") { it.substringBefore('\n') }
    }

    /**
     * Converts Lyrics object to LRC or plain text format based on available data.
     * Prefers synced lyrics if available.
     * @param lyrics The Lyrics object to convert.
     * @param preferSynced Whether to prefer synced lyrics over plain. Default true.
     * @return A string representation of the lyrics.
     */
    fun toLrcString(lyrics: Lyrics, preferSynced: Boolean = true): String {
        return if (preferSynced && !lyrics.synced.isNullOrEmpty()) {
            syncedToLrcString(lyrics.synced)
        } else if (!lyrics.plain.isNullOrEmpty()) {
            plainToString(lyrics.plain)
        } else if (!lyrics.synced.isNullOrEmpty()) {
            syncedToLrcString(lyrics.synced)
        } else {
            ""
        }
    }
}

private fun String.toNfc(): String =
    if (Normalizer.isNormalized(this, Normalizer.Form.NFC)) this else Normalizer.normalize(this, Normalizer.Form.NFC)

internal fun stripLeadingLyricsDocumentNoise(value: String): String {
    return value.trimStart { char ->
        char.isWhitespace() ||
            char == '\uFEFF' ||
            Character.getType(char).toByte() == Character.FORMAT
    }
}

internal fun looksLikeTtmlDocument(value: String): Boolean {
    // Match bare <tt>, namespaced <tt:tt>, and SMIL wrappers (<smil>) used
    // by some Apple Music and broadcast TTML variants.
    fun String.startsWithTtmlRoot(): Boolean =
        startsWith("<tt", ignoreCase = true) ||
            startsWith("<smil", ignoreCase = true)

    if (value.startsWithTtmlRoot()) return true

    if (!value.startsWith("<?xml", ignoreCase = true)) return false

    return value
        .substringAfter("?>", missingDelimiterValue = "")
        .trimStart()
        .startsWithTtmlRoot()
}

private fun sanitizeLrcLine(rawLine: String): String {
    if (rawLine.isEmpty()) return rawLine

    val withoutTerminators = rawLine
        .trimEnd('\r', '\n')
        .filterNot { char ->
            Character.getType(char).toByte() == Character.FORMAT ||
                (Character.isISOControl(char) && char != '\t')
        }
        .trimEnd('\uFEFF')

    val trimmedPrefix = withoutTerminators.trimStart { it.isWhitespace() }
    val firstBracket = trimmedPrefix.indexOf('[')
    return if (firstBracket > 0) {
        trimmedPrefix.substring(firstBracket)
    } else {
        trimmedPrefix
    }
}

private fun stripFormatCharacters(value: String): String {
    val cleaned = value.filterNot { char ->
        char.category == CharCategory.FORMAT ||
            (char.isISOControl() && char != '\t')
    }

    return when (cleaned) {
        "\"", "'" -> ""
        else -> cleaned
    }
}

@Composable
fun ProviderText(
    providerText: String,
    uri: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    accentColor: Color? = null
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = accentColor ?: MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = textColor)) {
            append(providerText)
        }
        withLink(
            LinkAnnotation.Url(
                url = uri,
                styles = TextLinkStyles(style = SpanStyle(color = linkColor))
            )
        ) {
            append(" LRCLIB")
        }
    }

    val baseStyle = MaterialTheme.typography.bodySmall
    val finalStyle = textAlign?.let { baseStyle.copy(textAlign = it) } ?: baseStyle

    Text(
        text = annotatedString,
        style = finalStyle,
        modifier = modifier
    )
}

/**
 * A composable that displays a row of animated bubbles that morph into
 * musical notes as they rise and back into circles as they fall.
 *
 * @param positionFlow A flow emitting the current playback position in ms.
 * @param time The start time at which these bubbles become visible.
 * @param color The base colour for the bubbles and notes.
 * @param nextTime The end time at which these bubbles disappear.
 * @param modifier Modifier applied to this layout.
 */
@Composable
fun BubblesLine(
    positionFlow: Flow<Long>,
    time: Int,
    color: Color,
    nextTime: Int,
    modifier: Modifier = Modifier,
) {
    val position by positionFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val isCurrent = position in time until nextTime
    val transition = rememberInfiniteTransition(label = "bubbles_transition")

    val animatedValue by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "bubble_animation_progress"
    )

    var show by remember { mutableStateOf(false) }
    LaunchedEffect(isCurrent) {
        show = isCurrent
    }

    if (show) {
        val density = LocalDensity.current
        val bubbleRadius = remember(density) { with(density) { 4.dp.toPx() } }

        val (morphableCircle, morphableNote) = remember(bubbleRadius) {
            val circleNodes = createCirclePathNodes(radius = bubbleRadius)
            val noteNodes = createVectorNotePathNodes(targetSize = bubbleRadius * 2.5f)

            makePathsCompatible(circleNodes, noteNodes)
            circleNodes to noteNodes
        }

        Canvas(modifier = modifier.size(64.dp, 48.dp)) {
            val bubbleCount = 3
            val bubbleColor = color.copy(alpha = 0.7f)

            for (i in 0 until bubbleCount) {
                val progress = (animatedValue + i * (1f / bubbleCount)) % 1f
                val yOffset = sin(progress * 2 * PI).toFloat() * 8.dp.toPx()

                val morphProgress = when {
                    progress in 0f..0.25f -> progress / 0.25f
                    progress in 0.25f..0.5f -> 1.0f - (progress - 0.25f) / 0.25f
                    else -> 0f
                }.coerceIn(0f, 1f)

                val scale = lerpFloat(1.0f, 1.4f, morphProgress)

                val xOffsetCorrection = lerpFloat(0f, bubbleRadius * 1.8f, morphProgress)

                val morphedPath = lerpPath(
                    start = morphableCircle,
                    stop = morphableNote,
                    fraction = morphProgress
                ).toPath()

                translate(left = (size.width / (bubbleCount + 1)) * (i + 1)) {
                    val drawOffset = Offset(x = xOffsetCorrection, y = size.height / 2 + yOffset)

                    translate(left = drawOffset.x, top = drawOffset.y) {
                        scale(scale = scale, pivot = Offset.Zero) {
                            drawPath(
                                path = morphedPath,
                                color = bubbleColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun lerpPath(start: List<PathNode>, stop: List<PathNode>, fraction: Float): List<PathNode> {
    return start.mapIndexed { index, startNode ->
        val stopNode = stop[index]
        when (startNode) {
            is PathNode.MoveTo -> {
                val stopMoveTo = stopNode as PathNode.MoveTo
                PathNode.MoveTo(
                    lerpFloat(startNode.x, stopMoveTo.x, fraction),
                    lerpFloat(startNode.y, stopMoveTo.y, fraction)
                )
            }
            is PathNode.CurveTo -> {
                val stopCurveTo = stopNode as PathNode.CurveTo
                PathNode.CurveTo(
                    lerpFloat(startNode.x1, stopCurveTo.x1, fraction),
                    lerpFloat(startNode.y1, stopCurveTo.y1, fraction),
                    lerpFloat(startNode.x2, stopCurveTo.x2, fraction),
                    lerpFloat(startNode.y2, stopCurveTo.y2, fraction),
                    lerpFloat(startNode.x3, stopCurveTo.x3, fraction),
                    lerpFloat(startNode.y3, stopCurveTo.y3, fraction)
                )
            }
            else -> startNode
        }
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

private fun List<PathNode>.toPath(): Path = Path().apply {
    this@toPath.forEach { node ->
        when (node) {
            is PathNode.MoveTo -> moveTo(node.x, node.y)
            is PathNode.LineTo -> lineTo(node.x, node.y)
            is PathNode.CurveTo -> cubicTo(node.x1, node.y1, node.x2, node.y2, node.x3, node.y3)
            is PathNode.Close -> close()
            else -> {}
        }
    }
}

private fun makePathsCompatible(nodes1: MutableList<PathNode>, nodes2: MutableList<PathNode>): Pair<MutableList<PathNode>, MutableList<PathNode>> {
    while (nodes1.size < nodes2.size) {
        nodes1.add(nodes1.size - 1, nodes1[nodes1.size - 2])
    }
    while (nodes2.size < nodes1.size) {
        nodes2.add(nodes2.size - 1, nodes2[nodes2.size - 2])
    }
    return nodes1 to nodes2
}

private fun createVectorNotePathNodes(targetSize: Float): MutableList<PathNode> {
    val pathData = "M239.5,1.9c-4.6,1.1 -8.7,3.6 -12.2,7.3 -6.7,6.9 -6.3,-2.5 -6.3,151.9 0,76.9 -0.3,140 -0.7,140.2 -0.5,0.3 -4.2,-0.9 -8.3,-2.5 -48.1,-19.3 -102.8,-8.3 -138.6,27.7 -35.8,36.1 -41.4,85.7 -13.6,120.7 18.6,23.4 52.8,37.4 86.2,35.3 34.8,-2.1 65.8,-16 89.5,-39.9 14.5,-14.6 24.9,-31.9 30.7,-50.6l2.3,-7.5 0.2,-133c0.2,-73.2 0.5,-133.6 0.8,-134.2 0.8,-2.4 62,28.5 84.3,42.4 22.4,14.1 34.1,30.4 37.2,51.9 2.4,16.5 -2.2,34.5 -13,50.9 -6,9.1 -7,12.1 -4.8,14.3 2.2,2.2 5.3,1.2 13.8,-4.5 26.4,-17.9 45.6,-48 50,-78.2 1.9,-12.9 0.8,-34.3 -2.4,-46.1 -8.7,-31.7 -30.4,-58 -64.1,-77.8 -64.3,-37.9 -116,-67.3 -119.6,-68.1 -5,-1.2 -7.1,-1.2 -11.4,-0.2z"
    val parser = PathParser().parsePathString(pathData)

    val groupScale = 0.253f
    val bounds = Path().apply { parser.toPath(this) }.getBounds()
    val maxDimension = max(bounds.width, bounds.height)
    val scale = if (maxDimension > 0f) targetSize / (maxDimension * groupScale) else 1f

    val matrix = Matrix()
    matrix.translate(x = -bounds.left, y = -bounds.top)
    matrix.scale(x = groupScale * scale, y = groupScale * scale)
    val finalWidth = bounds.width * groupScale * scale
    val finalHeight = bounds.height * groupScale * scale

    matrix.translate(x = -finalWidth / 2f, y = -finalHeight / 2f)

    return parser.toNodes().toAbsolute().transform(matrix).toCurvesOnly()
}

private fun createCirclePathNodes(radius: Float): MutableList<PathNode> {
    val kappa = 0.552284749831f
    val rk = radius * kappa
    return mutableListOf(
        PathNode.MoveTo(0f, -radius),
        PathNode.CurveTo(rk, -radius, radius, -rk, radius, 0f),
        PathNode.CurveTo(radius, rk, rk, radius, 0f, radius),
        PathNode.CurveTo(-rk, radius, -radius, rk, -radius, 0f),
        PathNode.CurveTo(-radius, -rk, -rk, -radius, 0f, -radius),
        PathNode.Close
    )
}

private fun List<PathNode>.toAbsolute(): MutableList<PathNode> {
    val absoluteNodes = mutableListOf<PathNode>()
    var currentX = 0f
    var currentY = 0f
    this.forEach { node ->
        when (node) {
            is PathNode.MoveTo -> { currentX = node.x; currentY = node.y; absoluteNodes.add(node) }
            is PathNode.RelativeMoveTo -> { currentX += node.dx; currentY += node.dy; absoluteNodes.add(PathNode.MoveTo(currentX, currentY)) }
            is PathNode.LineTo -> { currentX = node.x; currentY = node.y; absoluteNodes.add(node) }
            is PathNode.RelativeLineTo -> { currentX += node.dx; currentY += node.dy; absoluteNodes.add(PathNode.LineTo(currentX, currentY)) }
            is PathNode.CurveTo -> { currentX = node.x3; currentY = node.y3; absoluteNodes.add(node) }
            is PathNode.RelativeCurveTo -> {
                absoluteNodes.add(PathNode.CurveTo(currentX + node.dx1, currentY + node.dy1, currentX + node.dx2, currentY + node.dy2, currentX + node.dx3, currentY + node.dy3))
                currentX += node.dx3; currentY += node.dy3
            }
            is PathNode.Close -> absoluteNodes.add(node)
            else -> {}
        }
    }
    return absoluteNodes
}

private fun MutableList<PathNode>.toCurvesOnly(): MutableList<PathNode> {
    val curveNodes = mutableListOf<PathNode>()
    var lastX = 0f
    var lastY = 0f

    this.forEach { node ->
        when(node) {
            is PathNode.MoveTo -> { curveNodes.add(node); lastX = node.x; lastY = node.y }
            is PathNode.LineTo -> { curveNodes.add(PathNode.CurveTo(lastX, lastY, node.x, node.y, node.x, node.y)); lastX = node.x; lastY = node.y }
            is PathNode.CurveTo -> { curveNodes.add(node); lastX = node.x3; lastY = node.y3 }
            is PathNode.Close -> curveNodes.add(node)
            else -> {}
        }
    }
    return curveNodes
}

private fun List<PathNode>.transform(matrix: Matrix): MutableList<PathNode> {
    return this.map { node ->
        when (node) {
            is PathNode.MoveTo -> {
                val p = matrix.map(Offset(node.x, node.y))
                PathNode.MoveTo(p.x, p.y)
            }
            is PathNode.LineTo -> {
                val p = matrix.map(Offset(node.x, node.y))
                PathNode.LineTo(p.x, p.y)
            }
            is PathNode.CurveTo -> {
                val p1 = matrix.map(Offset(node.x1, node.y1))
                val p2 = matrix.map(Offset(node.x2, node.y2))
                val p3 = matrix.map(Offset(node.x3, node.y3))
                PathNode.CurveTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
            }
            else -> node
        }
    }.toMutableList()
}
