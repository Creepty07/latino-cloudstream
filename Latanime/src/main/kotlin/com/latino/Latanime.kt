package com.latino

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class Latanime : MainAPI() {

    // ── Configuración base ──────────────────────────────────────────────────
    override var mainUrl              = "https://latanime.org"
    override var name                 = "Latanime"
    override val hasMainPage          = true
    override var lang                 = "es"
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Anime, TvType.AnimeMovie)

    // ── Secciones de la pantalla principal ─────────────────────────────────
    override val mainPage = mainPageOf(
        "$mainUrl/emision"        to "En emisión",
        "$mainUrl/animes"         to "Todos los Animes",
        "$mainUrl/animes/movie"   to "Películas Anime",
        "$mainUrl/animes/ova"     to "OVAs",
    )

    // ── Helpers ─────────────────────────────────────────────────────────────
    private fun Element.toSearchResult(): AnimeSearchResponse? {
        val anchor  = this.selectFirst("a")                     ?: return null
        val href    = fixUrl(anchor.attr("href"))
        val title   = this.selectFirst(".title, h3, .anime-title")?.text()
                   ?: anchor.attr("title")
                   ?: return null
        val poster  = fixUrlNull(
            this.selectFirst("img")?.attr("src")
                ?: this.selectFirst("img")?.attr("data-src")
        )

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = poster
            addDubStatus(dubExist = true, subExist = false) // Latino = doblado
        }
    }

    // ── Pantalla principal ──────────────────────────────────────────────────
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url      = "${request.data}?page=$page"
        val document = app.get(url).document

        val items = document
            .select("div.anime-grid article, div.ListAnimes article, .anime-card")
            .mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, items)
    }

    // ── Búsqueda ────────────────────────────────────────────────────────────
    override suspend fun search(query: String): List<SearchResponse> {
        val url      = "$mainUrl/animes?buscar=${query.replace(" ", "+")}"
        val document = app.get(url).document

        return document
            .select("div.anime-grid article, div.ListAnimes article")
            .mapNotNull { it.toSearchResult() }
    }

    // ── Página de detalle ────────────────────────────────────────────────────
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title   = document.selectFirst("h1.Title, h1.title, h1")?.text()
                   ?: throw ErrorLoadingException("Sin título")
        val poster  = fixUrlNull(document.selectFirst(".anime-poster img, .Image img")?.attr("src"))
        val plot    = document.selectFirst(".Description p, .sinopsis")?.text()
        val tags    = document.select(".genres a, .Genres a").map { it.text() }
        val year    = document.selectFirst(".Date, .year")?.text()?.trim()?.toIntOrNull()
        val status  = when {
            document.select(".anime-status").text().contains("Finalizado") ->
                ShowStatus.Completed
            else -> ShowStatus.Ongoing
        }

        // Episodios
        val episodes = document.select("ul.ListCaps li, .episodes-list li").mapNotNull { li ->
            val a       = li.selectFirst("a")                   ?: return@mapNotNull null
            val epUrl   = fixUrl(a.attr("href"))
            val epTitle = a.text().trim()
            val epNum   = Regex("\\d+").find(epTitle)?.value?.toIntOrNull()

            newEpisode(epUrl) {
                this.name    = epTitle
                this.episode = epNum
                this.season  = 1
            }
        }.reversed() // Orden ascendente

        return newAnimeLoadResponse(title, url, TvType.Anime, episodes.isNotEmpty()) {
            this.posterUrl = poster
            this.plot      = plot
            this.tags      = tags
            this.year      = year
            this.showStatus = status
            addEpisodes(DubStatus.Dubbed, episodes)
        }
    }

    // ── Links de video ────────────────────────────────────────────────────────
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        // Extraer iframes de reproductores
        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("src").takeIf { it.isNotBlank() } ?: return@forEach
            loadExtractor(fixUrl(src), data, subtitleCallback, callback)
        }

        // Botones de servidor alternativo
        document.select(".OptionsList li, .server-item").forEach { item ->
            val serverUrl = item.attr("data-video").takeIf { it.isNotBlank() }
                         ?: item.attr("data-src").takeIf { it.isNotBlank() }
                         ?: return@forEach
            loadExtractor(fixUrl(serverUrl), data, subtitleCallback, callback)
        }

        return true
    }
}
