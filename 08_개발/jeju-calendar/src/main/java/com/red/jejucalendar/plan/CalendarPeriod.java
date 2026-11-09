package com.red.jejucalendar.plan;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 3개월 홍보 일정표의 기간
 * 시작 월은 application.properties의 calendar.start-month (화면 시안 기준 2026-10, 즉 2026년 10월부터 12월까지)
 */
@Component
public class CalendarPeriod {

    public static final int MONTHS = 3;

    private final YearMonth startMonth;

    public CalendarPeriod(@Value("${calendar.start-month:2026-10}") String startMonth) {
        this.startMonth = YearMonth.parse(startMonth);
    }

    public List<YearMonth> months() {
        return List.of(startMonth, startMonth.plusMonths(1), startMonth.plusMonths(2));
    }

    public LocalDate start() {
        return startMonth.atDay(1);
    }

    public LocalDate end() {
        return startMonth.plusMonths(MONTHS - 1).atEndOfMonth();
    }

    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(start()) && !date.isAfter(end());
    }

    /** 화면 안내용 문구 예: 2026년 10월부터 12월까지 */
    public String label() {
        YearMonth last = startMonth.plusMonths(MONTHS - 1);
        String endText = last.getYear() == startMonth.getYear()
                ? last.getMonthValue() + "월"
                : last.getYear() + "년 " + last.getMonthValue() + "월";
        return startMonth.getYear() + "년 " + startMonth.getMonthValue() + "월부터 " + endText + "까지";
    }
}
