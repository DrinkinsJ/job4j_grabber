package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
class HabrCareerDateTimeParserTest {

    @Test
    void whenDateTimeParserReturnLocalDayTime() {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        String expected = "2022-12-11T16:27:56";
        assertThat(dateTimeParser.parse("2022-12-11T16:27:56+03:00")).isEqualTo(expected);
    }
}
