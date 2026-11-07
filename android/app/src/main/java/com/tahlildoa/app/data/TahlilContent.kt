package com.tahlildoa.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Type of a section — drives the colored badge and accent color. */
@Serializable
enum class SectionType(val key: String, val label: String) {
    Pembukaan("pembukaan", "Pembukaan"),
    Sholawat("sholawat", "Shalawat"),
    Surah("surah", "Surat"),
    Tahlil("tahlil", "Tahlil"),
    Ayat("ayat", "Ayat"),
    Doa("doa", "Doa"),
    Dzikir("dzikir", "Dzikir");

    companion object {
        fun fromKey(key: String): SectionType =
            values().firstOrNull { it.key == key } ?: Tahlil
    }
}

/** A single block within a section — Arabic verse + transliteration + translation. */
@Serializable
data class ContentBlock(
    val arabic: String? = null,
    val transliteration: String,
    val translation: String,
    val note: String? = null,
    val repeat: Int? = null,
)

/** A numbered section of the prayer book. */
@Serializable
data class Section(
    val id: String,
    val number: Int,
    val title: String,
    val subtitle: String? = null,
    val type: SectionType,
    val blocks: List<ContentBlock>,
)

/**
 * One variant of the prayer book (e.g. "singkat", "lengkap").
 *
 * Each variant is a self-contained list of [Section]s with a human-readable
 * [name] and short [description] used by the UI.
 */
@Serializable
data class VariantData(
    val name: String,
    val description: String,
    val sections: List<Section>,
)

/**
 * Top-level shape of the `assets/tahlil-content.json` file.
 *
 * The file holds a single [source] attribution plus a [variants] map keyed
 * by the [ContentVariant.storageKey] ("singkat" / "lengkap").
 */
@Serializable
data class TahlilContentData(
    val source: String,
    val variants: Map<String, VariantData>,
)

/**
 * Parses a JSON string into a [TahlilContentData].
 *
 * Pure JVM — uses only kotlinx.serialization, so unit tests can call it
 * directly without an Android Context or Robolectric. Unknown keys are
 * ignored so the JSON file may carry extra metadata (e.g. `source_note`)
 * without breaking parsing.
 */
fun loadFromJson(json: String): TahlilContentData {
    val decoder = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    return decoder.decodeFromString(TahlilContentData.serializer(), json)
}

/** Hero copy shown at the top of the home screen. */
object HeroInfo {
    const val title = "Tahlil Singkat"
    const val subtitle = "Majelis Tahlil & Doa"
    const val description =
        "Rangkaian zikir, tahlil, dan doa untuk mendoakan para arwah kaum muslimin. " +
            "Disusun dari Al-Qur'an, hadits, dan amaliah ulama."
    const val arabicBismillah = "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ"
    const val source = "Kompilasi Tahlil dan Doa"
}

/**
 * Singleton holding the parsed content of the prayer book.
 *
 * The content lives in `assets/tahlil-content.json` and is parsed lazily on
 * first access. Because an `object` cannot reach Android's
 * [android.content.res.AssetManager] directly, the Application class must
 * call [init] (passing the application [Context]) once from its `onCreate`
 * — typically `TahlilDoaApp.onCreate()`. After [init], the parsed
 * [TahlilContentData] is cached for the lifetime of the process.
 *
 * Two variants ship:
 *  - [ContentVariant.Singkat] — 28 sections (the default).
 *  - [ContentVariant.Lengkap] — 39 sections (adds 11 Surah Yasin sections
 *    15–25 on top of the [ContentVariant.Singkat] content).
 *
 * The legacy [sections] property returns the [ContentVariant.Singkat] list
 * for backward compatibility with code that hasn't been migrated to the
 * variant-aware [sectionsFor] / [byId] / [indexOf] APIs.
 */
object TahlilContent {

    private const val ASSET_PATH = "tahlil-content.json"

    @Volatile
    private var data: TahlilContentData? = null

    /**
     * Parse the JSON asset from [context] and cache the result.
     *
     * Safe to call multiple times — only the first invocation does the
     * parsing; subsequent calls are no-ops (the cached value wins). Throws
     * [IllegalStateException] if the asset is missing or unparseable so
     * callers see the failure loudly during development.
     */
    fun init(context: Context) {
        if (data != null) return
        synchronized(this) {
            if (data != null) return
            val json = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
            data = loadFromJson(json)
        }
    }

    /**
     * The parsed content data. Visible so unit/instrumented tests can
     * inspect both variants at once.
     */
    val contentData: TahlilContentData
        get() = data ?: error(
            "TahlilContent has not been initialized — call TahlilContent.init(context) " +
                "from Application.onCreate() before reading content."
        )

    /**
     * Sections for the given [variant]. Defaults to [ContentVariant.Singkat]
     * for backward compatibility.
     */
    fun sectionsFor(variant: ContentVariant): List<Section> =
        contentData.variants[variant.storageKey]?.sections
            ?: contentData.variants[ContentVariant.Singkat.storageKey]?.sections
            ?: emptyList()

    /**
     * Look up a section by its stable id (e.g.
     * "pengantar-al-fatihah-untuk-rasulullah-saw") within [variant].
     *
     * Falls back to the [ContentVariant.Singkat] list when the id isn't
     * present in [variant] (e.g. when the user switched from Lengkap to
     * Singkat but the persisted "last read" pointer still references a
     * Yasin section) so the lookup never returns `null` for content that
     * still exists in some variant.
     */
    fun byId(id: String, variant: ContentVariant = ContentVariant.Singkat): Section? =
        sectionsFor(variant).firstOrNull { it.id == id }
            ?: sectionsFor(ContentVariant.Singkat).firstOrNull { it.id == id }

    /** Index of a section in the [variant]'s list, or -1 if not found. */
    fun indexOf(id: String, variant: ContentVariant = ContentVariant.Singkat): Int =
        sectionsFor(variant).indexOfFirst { it.id == id }

    /**
     * Backward-compatible accessor — the [ContentVariant.Singkat] list.
     *
     * Prefer [sectionsFor] in new code so the variant chosen by the user
     * is honoured.
     */
    val sections: List<Section>
        get() = sectionsFor(ContentVariant.Singkat)
}
