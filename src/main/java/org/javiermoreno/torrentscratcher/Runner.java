/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javiermoreno.torrentscratcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;

/**
 *
 * @author ciberado
 */
public class Runner implements CommandLineRunner {

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

    public MagnetLink getMagnetLink(String path) throws IOException {
        String url = "http://www.elitetorrent.net{path}";
        url = url.replace("{path}", path);
        Document doc = Jsoup.connect(url).get();
        String title = doc.select("#box-ficha > h2").text();
        String desc = doc.select("p.descrip").eq(1).text();
        String magnet = doc.select("a[href^=magnet]").attr("href");
        MagnetLink ml = new MagnetLink(title, desc, magnet);
        return ml;
    }

    @Override
    public void run(String... strings) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (PrintWriter out = new PrintWriter(new FileWriter("data.json"))) {
            int page = 1;
            while (true) {
                List<String> recordsUrl = getRecordsUrl(page);
                for (String url : recordsUrl) {
                    MagnetLink ml = getMagnetLink(url);
                    out.println(mapper.writeValueAsString(ml));
                    System.out.println(ml.getTitle());
                }            
                page = page + 1;
                System.out.println("*****************************************************************");
            }
        }
    }
}
