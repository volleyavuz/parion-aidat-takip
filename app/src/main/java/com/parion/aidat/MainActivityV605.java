package com.parion.aidat;

import java.util.*;

/** v4.0.5 - aidat activity is determined by real personal cycle boundaries, not calendar months. */
public class MainActivityV605 extends MainActivityV604 {

    /**
     * A cycle is active only if its real start date belongs to an active spell.
     * Example: start 12 Mar, end 12 Jul => 12 Jun-12 Jul is the last active cycle;
     * 12 Jul-12 Aug and every later cycle are inactive until a restart.
     */
    @Override boolean activeAt(int year, int month, String start, String end, String restart) {
        int key = year * 100 + month;
        int anchor = anchorDay(start);
        Calendar cycleStart = cycleDate(key, anchor);

        Calendar firstStart = iso605(start);
        if (firstStart != null && cycleStart.before(firstStart)) return false;

        Calendar stop = iso605(end);
        boolean hasStop = stop != null && end != null && !"DEVAM".equalsIgnoreCase(end);
        if (!hasStop) return true;

        // The period beginning exactly on the stop date is already inactive.
        if (cycleStart.before(stop)) return true;

        Calendar resume = iso605(restart);
        if (resume == null) return false;

        // Do not open a partial dues period. Reactivate from the first personal
        // period whose start is on/after the recorded restart date.
        return !cycleStart.before(resume);
    }

    private Calendar iso605(String iso) {
        try {
            if (iso == null || !iso.matches("\\d{4}-\\d{2}-\\d{2}")) return null;
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Integer.parseInt(iso.substring(0,4)), Integer.parseInt(iso.substring(5,7)) - 1, Integer.parseInt(iso.substring(8,10)));
            return c;
        } catch (Exception e) {
            return null;
        }
    }
}
