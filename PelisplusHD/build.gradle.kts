// ── PelisplusHD/build.gradle.kts ────────────────────────────────────────────
// Configuración de compilación específica del plugin PelisplusHD.
// El bloque cloudstream() define los metadatos que aparecen en la app.

version = 1  // Incrementar con cada actualización

cloudstream {
    // Nombre que verá el usuario en CloudStream
    pluginClassName = "PelisplusHD"

    // Metadatos del plugin
    description     = "Películas y series en español latino desde PelisplusHD"
    authors         = listOf("tu-usuario")
    language        = "es"

    // Tipo de contenido (movies, series, anime, live, nsfw, others)
    tvTypes         = listOf("Movie", "TvSeries", "Anime")

    // Ícono (URL pública a una imagen .png de 64×64 px o más)
    iconUrl         = "https://www.pelisplushd.la/favicon.ico"

    // URL del sitio fuente
    sourceUrl       = "https://www.pelisplushd.la"

    // Estado del plugin
    status          = 1  // 0 = roto, 1 = ok, 2 = slow, 3 = beta
}

dependencies {
    // CloudStream API (stubs para compilación)
    val cloudstream by configurations
    cloudstream("com.lagradost:cloudstream3:pre-release")
}
