// ── Latanime/build.gradle.kts ────────────────────────────────────────────────

version = 1

cloudstream {
    pluginClassName = "Latanime"
    description     = "Anime en español latino desde Latanime.org"
    authors         = listOf("tu-usuario")
    language        = "es"
    tvTypes         = listOf("Anime", "AnimeMovie")
    iconUrl         = "https://latanime.org/favicon.ico"
    sourceUrl       = "https://latanime.org"
    status          = 1
}

dependencies {
    val cloudstream by configurations
    cloudstream("com.lagradost:cloudstream3:pre-release")
}
