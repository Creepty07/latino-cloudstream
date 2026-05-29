package com.latino

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class PelisplusHD : MainAPI() {

    // ── Configuración base ──────────────────────────────────────────────────
    override var mainUrl              = "https://www.pelisplushd.la"
    override var name                 = "PelisplusHD"
    override val hasMainPage          = true
    override var lang                 = "es"
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Anime,
    )

    // ── Secciones de la pantalla principal ─────────────────────────────────
    override val mainPage = mainPageOf(
        "$mainUrl/peliculas"          to "Películas",
        "$mainUrl/peliculas/estrenos" to "Últimos estrenos",
        "$mainUrl/series"             to "Series",
        "$mainUrl/series/estrenos"    to "Series – Nuevos episodios",
        "$mainUrl/animes"             to "Anime Latino",
        "$mainUrl/generos/accion"     to "Acción",
        "$mainUrl/generos/comedia"    to "Comedia",
        "$mainUrl/generos/terror"     to "Terror",
    )

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Convierte un elemento <div> de tarjeta a SearchResponse.
     * La estructura del sitio es:
     *   <div class="Num"> ... <a href="/pelicula/slug"> <img src="poster"> <p class="Title">Título</p> </a>
     */
    private fun Element.toSearchResult(): SearchResponse? {
        val anchor   = this.selectFirst("a")                        ?: return null
        val href     = fixUrl(anchor.attr("href"))
        val title    = this.selectFirst(".Title")?.text()
                    ?: anchor.attr("title")
                    ?: return null
        val poster   = fixUrlNull(
            this.selectFirst("img")?.attr("src")
                ?: this.selectFirst("img")?.attr("data-src")
        )
        // Detectar si es serie o película por la URL
        val tvType   = if (href.contains("/serie/") || href.contains("/series/"))
                           TvType.TvSeries else TvType.Movie

        return newMovieSearchResponse(title, href, tvType) {
            this.posterUrl = poster
        }
    }

    // ── Pantalla principal ──────────────────────────────────────────────────
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        // El sitio usa paginación: ?page=N
        val url      = "${request.data}?page=$page"
        val document = app.get(url).document

        // Las tarjetas están en: div.Posters > article  ó  div.TPost
        val items    = document.select("div.Posters article, div.TPost")
            .mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, items)
    }

    // ── Búsqueda ────────────────────────────────────────────────────────────
    override suspend fun search(query: String): List<SearchResponse> {
        val url      = "$mainUrl/?s=${query.replace(" ", "+")}"
        val document = app.get(url).document

        return document.select("div.Posters article, div.TPost, ul.MovieList li")
            .mapNotNull { it.toSearchResult() }
    }

    // ── Página de resultado (detalle) ────────────────────────────────────────
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title      = document.selectFirst("h1.Title")?.text()
                      ?: document.selectFirst("h1")?.text()
                      ?: throw ErrorLoadingException("Sin título")
        val poster     = fixUrlNull(document.selectFirst("div.Image img")?.attr("src"))
        val plot       = document.selectFirst("div.Description p, p.Description")?.text()
        val year       = document.selectFirst("span.Date, .Year")?.text()
                             ?.trim()?.toIntOrNull()
        val tags       = document.select("p.Genre a").map { it.text() }
        val rating     = document.selectFirst("span.Value")?.text()
                             ?.toRatingInt()

        // Detectar si es película o serie
        val isSeries   = url.contains("/serie/") || url.contains("/series/")

        return if (isSeries) {
            // ── Serie: extraer temporadas y episodios ──
            val episodes = mutableListOf<Episode>()

            document.select("section.SeasonBx").forEachIndexed { seasonIdx, season ->
                val seasonNum = season.selectFirst("span.Title")?.text()
                    ?.filter { it.isDigit() }?.toIntOrNull() ?: (seasonIdx + 1)

                season.select("table.Stbl tr").forEach { row ->
                    val epAnchor = row.selectFirst("td a") ?: return@forEach
                    val epUrl    = fixUrl(epAnchor.attr("href"))
                    val epTitle  = row.selectFirst("td.MvTbTtl a")?.text()
                    val epNum    = row.selectFirst("td.MvTbTtl span")?.text()
                        ?.filter { it.isDigit() }?.toIntOrNull()
                    val epPoster = fixUrlNull(row.selectFirst("img")?.attr("src"))

                    episodes.add(newEpisode(epUrl) {
                        this.name      = epTitle
                        this.season    = seasonNum
                        this.episode   = epNum
                        this.posterUrl = epPoster
                    })
                }
            }

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl    = poster
                this.plot         = plot
                this.year         = year
                this.tags         = tags
                this.rating       = rating
            }
        } else {
            // ── Película ──
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot      = plot
                this.year      = year
                this.tags      = tags
                this.rating    = rating
            }
        }
    }

    // ── Carga de links de video ─────────────────────────────────────────────
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        // El sitio muestra iframes y botones de servidor
        // Estrategia: extraer todos los iframes y pasarlos a loadExtractor
        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("src").takeIf { it.isNotBlank() } ?: return@forEach
            loadExtractor(fixUrl(src), data, subtitleCallback, callback)
        }

        // Botones de servidor (enlaces directos embebidos)
        document.select("li.optionBtn, .ServerButton, a.Button[data-id]").forEach { btn ->
            val serverUrl = btn.attr("data-url").takeIf { it.isNotBlank() }
                         ?: btn.attr("href").takeIf { it.isNotBlank() }
                         ?: return@forEach
            loadExtractor(fixUrl(serverUrl), data, subtitleCallback, callback)
        }

        return true
    }
}
