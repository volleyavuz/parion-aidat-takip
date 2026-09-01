package com.parion.aidat;

import android.content.Context;
import java.util.Calendar;

/** Central financial-year policy. Historical UI starts at 2026 by product decision. */
final class FinancialYear {
    static final int MIN_YEAR = 2026;
    private static final String PREF = "parion_financial_year";
    private static final String KEY = "active_year";
    private FinancialYear() {}

    static int currentCalendarYear(){ return Calendar.getInstance().get(Calendar.YEAR); }
    static int get(Context c){
        int def = Math.max(MIN_YEAR, currentCalendarYear());
        int y = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY, def);
        return Math.max(MIN_YEAR, y);
    }
    static void set(Context c,int year){
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putInt(KEY, Math.max(MIN_YEAR, year)).apply();
    }
}
