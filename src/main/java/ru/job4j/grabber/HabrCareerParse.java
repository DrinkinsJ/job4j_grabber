package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String DELIMETR = "----------------------------------";

    private static final int PAGES = 5;

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse();
        for (int i = 1; i <= PAGES; i++) {
            System.out.println("-------------PAGE:" + i + " ------------");
            Connection connection = Jsoup.connect(PAGE_LINK + "?page=" + i);
            Document document = connection.get();
            habrCareerParse.printPage(document);
        }
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

    private void printPage(Document document) {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            Element dateElement = row.select(".vacancy-card__date").first().child(0);
            String date = dateElement.attr("datetime");
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String desc = retrieveDescription(link);

            System.out.printf("%s %s%n%s%n%s%n %s%n%s%n", vacancyName, link, DELIMETR, desc, DELIMETR,
                    dateTimeParser.parse(date));
        });
    }
}