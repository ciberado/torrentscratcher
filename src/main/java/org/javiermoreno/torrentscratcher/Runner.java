/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javiermoreno.torrentscratcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

/**
 *
 * @author ciberado
 */
public class Runner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    private final ObjectMapper om;

    public Runner() {
        this.om = new ObjectMapper();
    }

    public Movie enrichMovieWithFilmAffinity(Movie movie) {
        try {
            String url = "http://www.filmaffinity.com/es/search.php?stext={title}&stype=all";
            String title = URLEncoder.encode(movie.getTitle(), "UTF8");
            url = url.replace("{title}", title);
            Document doc = Jsoup.connect(url).get();
            if (doc.select("[property=og:title]").size() == 0) {
                // several results found, take the first
                Element firstResult = doc.select(".item-search .mc-title a").first();
                if (firstResult == null) {
                    // filmaffinity search engine failed
                    log.warn("FilmAffinity 404: " + movie.getTitle());
                    return movie;
                }
                url = "http://www.filmaffinity.com" + firstResult.attr("href");
                doc = Jsoup.connect(url).get();
            }
            movie.setFilmAffinityId(doc.select("div.rate-movie-box").attr("data-movie-id"));
            Elements movieInfo = doc.select("dl.movie-info");
            String originalTitle = movieInfo.select("dd").eq(0).text();
            originalTitle = originalTitle
                    .replaceAll("\\([^\\(]*\\)", "")
                    .replaceAll("\\[[^\\(]*\\]", "")
                    .replaceAll("aka$", "")
                    .trim();
            movie.setOriginalTitle(originalTitle);
            movie.setDescription(movieInfo.select("dd").eq(11).text());
        } catch (IOException ex) {
            log.warn(ex.getMessage());
        }
        return movie;
    }

    public Movie enrichMovieWithImdbSearch(Movie movie) {
        try {
            String url = "http://www.imdb.com/find?q={title}&s=all";
            String title = movie.getOriginalTitle() != null ? movie.getOriginalTitle() : movie.getTitle();
            url = url.replace("{title}", java.net.URLEncoder.encode(title, "UTF-8"));
            Document doc = Jsoup.connect(url).get();
            Elements results = doc.select(".result_text a");
            if (results.size() == 0) {
                log.warn("IMDB search 404: " + movie.getTitle());
                return movie;
            }
            String link = results.first().attr("href");
            String imdbId = link.substring("/title/".length(), link.indexOf("?") - 1);
            movie.setImdbId(imdbId);
            url = "http://www.imdb.com" + link;
            doc = Jsoup.connect(url).get();
            movie.setGenre(doc.select("[itemprop=genre]").eq(0).text());
            String rating = doc.select("[itemprop=aggregateRating] [itemprop=ratingValue]").text();
            if (rating.isEmpty() == false) {
                movie.setRating(Double.valueOf(rating.replace(',', '.')));
            }
        } catch (IOException ex) {
            log.warn(ex.getMessage());
        }
        return movie;
    }

    public Movie enrichMovieWithImdbAPI(Movie movie) {
        try {
            String url = "http://www.omdbapi.com/?t={title}&type=movie";
            String title = movie.getOriginalTitle() != null ? movie.getOriginalTitle() : movie.getTitle();
            url = url.replace("{title}", java.net.URLEncoder.encode(title, "UTF-8"));
            JsonNode imdb = om.readTree(new URL(url));
            if (imdb.get("Error") != null) {
                log.warn("IMDB API 404: " + movie.getTitle());
            } else {
                movie.setYear(imdb.get("Year").asText());
                movie.setGenre(imdb.get("Genre").asText());
                movie.setRating(imdb.get("imdbRating").asDouble());
                movie.setImdbId(imdb.get("imdbID").asText());
            }
        } catch (IOException ex) {
            log.warn(ex.getMessage());
        }
        return movie;
    }

    public Movie getMovie(String path) throws IOException {
        String url = "http://www.elitetorrent.net{path}";
        url = url.replace("{path}", path);
        log.debug("Retrieving " + path + ".");
        Document doc = Jsoup.connect(url).get();

        Movie movie = new Movie();
        String title = doc.select("#box-ficha > h2").text();
        // strip parentheses: http://stackoverflow.com/questions/1138552/replace-string-in-parentheses-using-regex
        title = title
                .replaceAll("\\([^\\(]*\\)", "")
                .replaceAll("\\[[^\\(]*\\]", "")
                .replaceAll("aka$", "")
                .trim();
        movie.setTitle(title);
        movie.setUrl(url);
        movie.setDescription(doc.select("p.descrip").eq(1).text());
        movie.setType("movie");
        movie.setImage("http://www.elitetorrent.net/" + doc.select("img.imagen_ficha").attr("src"));

        Torrent torrent = new Torrent();
        torrent.setMagnet(doc.select("a[href^=magnet]").attr("href"));
        torrent.setFilesize(doc.select("dl.info-tecnica dd").eq(3).text());
        movie.getTorrents().put("720p", torrent);

        return movie;
    }

    public List<String> getRecordsUrl(int page) throws IOException {
        List<String> result = new ArrayList<>();

        String url = "http://www.elitetorrent.net/categoria/13/peliculas-hdrip/modo:listado/orden:valoracion/pag:{page}";
        url = url.replace("{page}", String.valueOf(page));
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a.nombre");
        for (Element elem : links) {
            result.add(elem.attr("href"));
        }
        return result;
    }

    @Override
    public void run(String... strings) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (PrintWriter out = new PrintWriter(new FileWriter("data.json"))) {
            int page = 1;
            while (true) {
                List<String> recordsUrl = getRecordsUrl(page);
                if (recordsUrl.isEmpty()) break;
                for (String url : recordsUrl) {
                    log.info("Procesando " + url);
                    Movie movie = getMovie(url);
                    enrichMovieWithFilmAffinity(movie);
                    enrichMovieWithImdbSearch(movie);
                    out.println(mapper.writeValueAsString(movie));
                }
                page = page + 1;
                log.info("Page " + page + ".");
            }
        }
    }
}
