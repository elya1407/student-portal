package com.studentportal.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;

public final class WeekTypeUtil {
    public static final String NUMERATOR = "числитель";
    public static final String DENOMINATOR = "знаменатель";

    // Задаётся старостой из админки: "на дату X идёт неделя Y", дальше считается автоматически.
    private static volatile LocalDate anchorDate;
    private static volatile String anchorType;

    private WeekTypeUtil() {}

    public static void setAnchor(LocalDate date, String weekType) {
        anchorDate = date;
        anchorType = weekType;
    }

    public static LocalDate getAnchorDate() {
        return anchorDate;
    }

    public static String getAnchorType() {
        return anchorType;
    }

    /**
     * Если староста задал опорную дату — считаем чередование от неё.
     * Иначе (пока не задано ни разу) — старое поведение по номеру недели в году.
     */
    public static String getWeekType(LocalDate date) {
        if (anchorDate != null && anchorType != null) {
            LocalDate anchorWeekMonday = anchorDate.with(DayOfWeek.MONDAY);
            LocalDate dateWeekMonday = date.with(DayOfWeek.MONDAY);
            long weeksBetween = ChronoUnit.WEEKS.between(anchorWeekMonday, dateWeekMonday);
            boolean sameParity = Math.floorMod(weeksBetween, 2) == 0;
            if (sameParity) {
                return anchorType;
            }
            return anchorType.equals(NUMERATOR) ? DENOMINATOR : NUMERATOR;
        }
        int week = date.get(WeekFields.of(Locale.forLanguageTag("ru-RU")).weekOfWeekBasedYear());
        return week % 2 == 1 ? NUMERATOR : DENOMINATOR;
    }

    public static String getDayCode(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "ПН";
            case TUESDAY -> "ВТ";
            case WEDNESDAY -> "СР";
            case THURSDAY -> "ЧТ";
            case FRIDAY -> "ПТ";
            case SATURDAY -> "СБ";
            case SUNDAY -> "ВС";
        };
    }

    public static int dayOrder(String dayCode) {
        return switch (dayCode) {
            case "ПН" -> 1;
            case "ВТ" -> 2;
            case "СР" -> 3;
            case "ЧТ" -> 4;
            case "ПТ" -> 5;
            case "СБ" -> 6;
            case "ВС" -> 7;
            default -> 99;
        };
    }
}

