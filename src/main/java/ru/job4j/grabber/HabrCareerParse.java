package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final int PAGES = 1;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IllegalAccessException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        System.out.println(habrCareerParse.list(PAGE_LINK + "?page="));
    }

    private String retrieveDescription(String link) {
        Document document = null;
        try {
            document = Jsoup.connect(link).get();

        } catch (IOException e) {
            e.printStackTrace();
        }
        assert document != null;
        Elements row = document.select(".style-ugc");
        return row.text();
    }

    @Override
    public List<Post> list(String link) throws IllegalAccessException {
        List<Post> postList = new ArrayList<>();

        for (int i = 1; i <= PAGES; i++) {
            System.out.println("-------------PAGE:" + i + " ------------");
            Connection connection = Jsoup.connect(PAGE_LINK + "?page=" + i);
            Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new IllegalAccessException(e.toString());
            }
            Elements rows = document.select(".vacancy-card__inner");
            for (Element row : rows) {
                postList.add(parseRow(row));
            }
        }
        return postList;
    }

    private Post parseRow(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String titleString = linkElement.text();
        String linkString = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        LocalDateTime localDateTime = dateTimeParser.parse(row.select(".vacancy-card__date").first().child(0).attr(
                "datetime"));
        String desc = retrieveDescription(linkString);
        return new Post(titleString, linkString, desc, localDateTime);
    }
}