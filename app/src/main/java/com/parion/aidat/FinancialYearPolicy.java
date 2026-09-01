package com.parion.aidat;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Central financial-year policy.
 * Aidat history intentionally starts at 2026; pre-2026 monthly cards are never exposed.
 */
final class FinancialYearPolicy {
    static final int MIN_YEAR = 2026;
    private static final String PREF = "parion_financial_year";
    private static final String KEY_ACTIVE = "active_year";

    private FinancialYearPolicy() {}

    static int currentCalendarYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    static int activeYear(Context context) {
        int now = Math.max(MIN_YEAR, currentCalendarYear());
        SharedPreferences p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        int saved = p.getInt(KEY_ACTIVE, now);
        if (saved < MIN_YEAR) saved = MIN_YEAR;
        if (saved > now) saved = now;
        return saved;
    }

    static void setActiveYear(Context context, int year) {
        int now = Math.max(MIN_YEAR, currentCalendarYear());
        int safe = Math.max(MIN_YEAR, Math.min(year, now));
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putInt(KEY_ACTIVE, safe).apply();
    }

    static List<Integer> availableYears(Context context) {
        int now = Math.max(MIN_YEAR, currentCalendarYear());
        ArrayList<Integer> years = new ArrayList<>();
        for (int y = MIN_YEAR; y <= now; y++) years.add(y);
        return years;
    }

    static boolean isVisibleYear(int year) {
        return year >= MIN_YEAR;
    }
}
