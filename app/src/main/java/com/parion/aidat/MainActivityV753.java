package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * v4.3.5
 * - Attendance is generated/displayed only through today. Future sessions do not exist yet.
 * - Current month opens by default; past months remain selectable.
 * - Cloud photo map is reconciled into local athlete.photo before profile render.
 */
public class MainActivityV753 extends MainActivityV752 {
    private final SimpleDateFormat iso753 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat tr753 = new SimpleDateFormat("dd.MM.yyyy", new Locale("tr", "TR"));
    private final Handler ui753 = new Handler(Looper.getMainLooper());
    private String attGroup753 = "";
    private int attYear753 = -1, attMonth753 = -1;
    private boolean destroyed753 = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ui753.postDelayed(this::repairAllMappedPhotos753, 900);
        ui753.postDelayed(this::repairAllMappedPhotos753, 2800);
        scheduleNextDay753();
    }

    @Override protected void onResume() {
        super.onResume();
        ui753.postDelayed(this::repairAllMappedPhotos753, 700);
        ui753.postDelayed(this::ensureTodayForAllGroups753, 120);
    }

    @Override void showHome() {
        super.showHome();
        if (root != null) root.post(() -> hookAttendance753(root));
    }

    private void hookAttendance753(View v) {
        if (v instanceof ImageButton) {
            ImageButton b = (ImageButton) v;
            CharSequence cd = b.getContentDescription();
            if (cd != null && "Yoklamalar".equalsIgnoreCase(cd.toString())) {
                b.setOnClickListener(x -> showAttendanceGroups753());
                return;
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) hookAttendance753(g.getChildAt(i));
        }
    }

    private void showAttendanceGroups753() {
        page = "ATTENDANCE_GROUPS_753";
        currentAthlete = -1;
        base("YOKLAMALAR", true);
        ScrollView sv = scroll();
        LinearLayout b = box(sv);
        b.setPadding(dp(12), dp(12), dp(12), dp(24));
        ArrayList<String> groups = groups753();
        if (groups.isEmpty()) {
            b.addView(tv("Tanımlı grup bulunamadı.", 14, Color.DKGRAY, true));
            return;
        }
        for (String g : groups) {
            Button x = btn(g);
            x.setOnClickListener(v -> openGroup753(g, currentYear753(), currentMonth753()));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(56));
            lp.setMargins(0, dp(8), 0, 0);
            b.addView(x, lp);
        }
    }

    private ArrayList<String> groups753() {
        ArrayList<String> out = new ArrayList<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE", null);
        while (c.moveToNext()) out.add(c.getString(0));
        c.close();
        return out;
    }

    private void openGroup753(String group, int year, int month) {
        attGroup753 = group;
        attYear753 = year;
        attMonth753 = month;
        if (!hasSchedule753(group)) {
            tryInvoke628753("askSchedule628", new Class<?>[]{String.class, boolean.class}, group, true);
            return;
        }
        ensureMonth753(group, year, month);
        page = "ATTENDANCE_GROUP_753:" + group + ":" + year + ":" + month;
        base(group + " • YOKLAMA", true);
        ScrollView sv = scroll();
        LinearLayout b = box(sv);
        b.setPadding(dp(12), dp(12), dp(12), dp(24));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        Button monthBtn = btn(monthName753(month) + " " + year);
        monthBtn.setOnClickListener(v -> chooseMonth753(group));
        Button program = btn("PROGRAM");
        program.setOnClickListener(v -> tryInvoke628753("askSchedule628", new Class<?>[]{String.class, boolean.class}, group, false));
        top.addView(monthBtn, new LinearLayout.LayoutParams(0, dp(50), 1.35f));
        top.addView(program, new LinearLayout.LayoutParams(0, dp(50), 1f));
        b.addView(top);

        String first = String.format(Locale.US, "%04d-%02d-01", year, month + 1);
        Calendar next = Calendar.getInstance();
        next.clear(); next.set(year, month, 1); next.add(Calendar.MONTH, 1);
        String nextFirst = iso753.format(next.getTime());
        String today = iso753.format(new Date());
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT id,sessionDate,cancelled,confirmed FROM attendance_sessions WHERE groupName=? AND sessionDate>=? AND sessionDate<? AND sessionDate<=? ORDER BY sessionDate DESC",
                new String[]{group, first, nextFirst, today});
        int count = 0;
        while (c.moveToNext()) {
            long sid = c.getLong(0);
            String date = c.getString(1);
            boolean cancelled = c.getInt(2) == 1;
            boolean confirmed = c.getInt(3) == 1;
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(7), dp(8), dp(7));
            CheckBox ok = new CheckBox(this);
            ok.setChecked(confirmed);
            ok.setEnabled(!cancelled);
            ok.setText((cancelled ? "İPTAL • " : "") + trDate753(date));
            ok.setOnCheckedChangeListener((x, on) -> setSessionConfirmed753(sid, on));
            row.addView(ok, new LinearLayout.LayoutParams(0, -2, 1));
            Button open = btn("AÇ");
            open.setEnabled(!cancelled);
            open.setOnClickListener(v -> showSession753(group, sid, date));
            row.addView(open, new LinearLayout.LayoutParams(dp(82), dp(42)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, dp(7), 0, 0);
            b.addView(row, lp);
            count++;
        }
        c.close();
        if (count == 0) b.addView(tv("Bu ay için bugüne kadar yoklama günü bulunmuyor.", 14, Color.DKGRAY, true));
    }

    private void chooseMonth753(String group) {
        Calendar cur = Calendar.getInstance();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> ys = new ArrayList<>(), ms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            labels.add(monthName753(cur.get(Calendar.MONTH)) + " " + cur.get(Calendar.YEAR));
            ys.add(cur.get(Calendar.YEAR)); ms.add(cur.get(Calendar.MONTH));
            cur.add(Calendar.MONTH, -1);
        }
        new AlertDialog.Builder(this).setTitle("AY SEÇ")
                .setItems(labels.toArray(new String[0]), (d, which) -> openGroup753(group, ys.get(which), ms.get(which)))
                .show();
    }

    private boolean hasSchedule753(String group) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT 1 FROM attendance_schedule WHERE groupName=? LIMIT 1", new String[]{group});
        boolean ok = c.moveToFirst(); c.close(); return ok;
    }

    /** Past months are fully generated; current month is generated only through today. Future months are never offered/generated. */
    private void ensureMonth753(String group, int year, int month) {
        Calendar now = Calendar.getInstance();
        int nowY = now.get(Calendar.YEAR), nowM = now.get(Calendar.MONTH);
        if (year > nowY || (year == nowY && month > nowM)) return;
        Calendar c = Calendar.getInstance(); c.clear(); c.set(year, month, 1);
        int last = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        int maxDay = (year == nowY && month == nowM) ? now.get(Calendar.DAY_OF_MONTH) : last;
        SQLiteDatabase d = db.getWritableDatabase();
        for (int day = 1; day <= maxDay; day++) {
            c.set(Calendar.DAY_OF_MONTH, day);
            String date = iso753.format(c.getTime());
            String weekdays = weekdaysForDate753(group, date);
            int wd = weekday753(c.get(Calendar.DAY_OF_WEEK));
            if (containsWeekday753(weekdays, wd)) {
                ContentValues v = new ContentValues();
                v.put("groupName", group); v.put("sessionDate", date);
                d.insertWithOnConflict("attendance_sessions", null, v, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
        // Defensive cleanup: old versions may already have generated future rows in the current month.
        if (year == nowY && month == nowM) {
            String today = iso753.format(now.getTime());
            d.delete("attendance_records", "sessionId IN (SELECT id FROM attendance_sessions WHERE groupName=? AND sessionDate>?)", new String[]{group, today});
            d.delete("attendance_sessions", "groupName=? AND sessionDate>?", new String[]{group, today});
        }
    }

    private String weekdaysForDate753(String group, String date) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? AND effectiveFrom<=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1", new String[]{group, date});
        String s = c.moveToFirst() ? c.getString(0) : null; c.close(); return s;
    }
    private boolean containsWeekday753(String s, int n) {
        if (s == null || s.trim().isEmpty()) return false;
        for (String x : s.split(",")) if (String.valueOf(n).equals(x.trim())) return true;
        return false;
    }
    private int weekday753(int d) {
        return d == Calendar.MONDAY ? 1 : d == Calendar.TUESDAY ? 2 : d == Calendar.WEDNESDAY ? 3 : d == Calendar.THURSDAY ? 4 : d == Calendar.FRIDAY ? 5 : d == Calendar.SATURDAY ? 6 : 7;
    }

    private void showSession753(String group, long sid, String date) {
        page = "ATTENDANCE_SESSION_753:" + group + ":" + sid;
        base(group + " • " + trDate753(date), true);
        ScrollView sv = scroll(); LinearLayout b = box(sv);
        Cursor a = db.getReadableDatabase().rawQuery("SELECT id,name,birthYear FROM athletes WHERE category=? COLLATE NOCASE AND status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE", new String[]{group});
        while (a.moveToNext()) {
            long athlete = a.getLong(0); String name = a.getString(1); int by = a.getInt(2);
            ensureRecord753(sid, athlete);
            CheckBox cb = new CheckBox(this);
            cb.setChecked(recordPresent753(sid, athlete));
            cb.setText((by > 0 ? by + " • " : "") + name);
            cb.setTextSize(14); cb.setPadding(dp(8), dp(6), dp(8), dp(6));
            cb.setOnCheckedChangeListener((v, on) -> setRecord753(sid, athlete, on));
            b.addView(cb, new LinearLayout.LayoutParams(-1, dp(52)));
        }
        a.close();
        b.addView(tv("İşaretli = geldi • İşaretsiz = gelmedi.", 11, Color.DKGRAY, false));
    }

    private void ensureRecord753(long sid, long athlete) {
        ContentValues v = new ContentValues(); v.put("sessionId", sid); v.put("athleteId", athlete); v.put("present", 1);
        db.getWritableDatabase().insertWithOnConflict("attendance_records", null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }
    private boolean recordPresent753(long sid, long athlete) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?", new String[]{String.valueOf(sid), String.valueOf(athlete)});
        boolean x = !c.moveToFirst() || c.getInt(0) == 1; c.close(); return x;
    }
    private void setRecord753(long sid, long athlete, boolean on) {
        ensureRecord753(sid, athlete);
        ContentValues v = new ContentValues(); v.put("present", on ? 1 : 0);
        db.getWritableDatabase().update("attendance_records", v, "sessionId=? AND athleteId=?", new String[]{String.valueOf(sid), String.valueOf(athlete)});
        String key = sessionKey753(sid); if (!key.isEmpty()) enqueue753("ATT_RECORD", key + "|" + athlete);
        syncFromCloud(false);
    }
    private void setSessionConfirmed753(long sid, boolean on) {
        ContentValues v = new ContentValues(); v.put("confirmed", on ? 1 : 0);
        db.getWritableDatabase().update("attendance_sessions", v, "id=?", new String[]{String.valueOf(sid)});
        String key = sessionKey753(sid); if (!key.isEmpty()) enqueue753("ATT_SESSION", key);
        syncFromCloud(false);
    }
    private String sessionKey753(long sid) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT groupName,sessionDate FROM attendance_sessions WHERE id=? LIMIT 1", new String[]{String.valueOf(sid)});
        String k = c.moveToFirst() ? c.getString(0) + "|" + c.getString(1) : ""; c.close(); return k;
    }
    private void enqueue753(String kind, String key) {
        ContentValues p = new ContentValues(); p.put("kind", kind); p.put("entity_key", key); p.put("created_at", System.currentTimeMillis());
        db.getWritableDatabase().insertWithOnConflict("pending_sync", null, p, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void ensureTodayForAllGroups753() {
        Calendar n = Calendar.getInstance();
        for (String g : groups753()) if (hasSchedule753(g)) ensureMonth753(g, n.get(Calendar.YEAR), n.get(Calendar.MONTH));
    }

    private void scheduleNextDay753() {
        Calendar next = Calendar.getInstance();
        next.add(Calendar.DAY_OF_MONTH, 1); next.set(Calendar.HOUR_OF_DAY, 0); next.set(Calendar.MINUTE, 0); next.set(Calendar.SECOND, 2); next.set(Calendar.MILLISECOND, 0);
        long delay = Math.max(1000L, next.getTimeInMillis() - System.currentTimeMillis());
        ui753.postDelayed(() -> {
            if (destroyed753) return;
            ensureTodayForAllGroups753();
            if (!attGroup753.isEmpty() && attYear753 == currentYear753() && attMonth753 == currentMonth753() && page != null && page.startsWith("ATTENDANCE_GROUP_753:"))
                openGroup753(attGroup753, attYear753, attMonth753);
            scheduleNextDay753();
        }, delay);
    }

    @Override void showProfile(long id) {
        boolean fixed = syncLocalPhoto753(id);
        super.showProfile(id);
        if (!fixed) ui753.postDelayed(() -> {
            if (syncLocalPhoto753(id) && "PROFILE".equals(page) && currentAthlete == id) redrawProfile753(id);
        }, 850);
    }
    private void redrawProfile753(long id) { super.showProfile(id); }

    private boolean syncLocalPhoto753(long id) {
        try {
            String path = photoMap413().get(id);
            if (path == null || path.trim().isEmpty()) return false;
            String wanted = "CLOUD:" + path.trim();
            Cursor c = db.athlete(id); String local = "";
            if (c.moveToFirst()) { int i = c.getColumnIndex("photo"); if (i >= 0 && !c.isNull(i)) local = c.getString(i); }
            c.close();
            if (wanted.equals(local)) return false;
            ContentValues v = new ContentValues(); v.put("photo", wanted);
            db.getWritableDatabase().update("athletes", v, "id=?", new String[]{String.valueOf(id)});
            return true;
        } catch (Exception e) { return false; }
    }

    private void repairAllMappedPhotos753() {
        try {
            Map<Long, String> map = photoMap413();
            if (map == null || map.isEmpty()) return;
            SQLiteDatabase d = db.getWritableDatabase();
            d.beginTransaction();
            try {
                for (Map.Entry<Long, String> e : map.entrySet()) {
                    if (e.getKey() == null || e.getKey() <= 0 || e.getValue() == null || e.getValue().trim().isEmpty()) continue;
                    ContentValues v = new ContentValues(); v.put("photo", "CLOUD:" + e.getValue().trim());
                    d.update("athletes", v, "id=? AND COALESCE(photo,'')<>?", new String[]{String.valueOf(e.getKey()), "CLOUD:" + e.getValue().trim()});
                }
                d.setTransactionSuccessful();
            } finally { d.endTransaction(); }
        } catch (Exception ignored) {}
    }

    @Override protected void onRemoteApplied750(boolean athleteChanged, boolean attendanceChanged, boolean materialChanged, boolean membershipChanged) {
        super.onRemoteApplied750(athleteChanged, false, materialChanged, membershipChanged);
        if (!attendanceChanged) return;
        runOnUiThread(() -> {
            try {
                if (page == null) return;
                if (page.equals("ATTENDANCE_GROUPS_753")) showAttendanceGroups753();
                else if (page.startsWith("ATTENDANCE_GROUP_753:") && !attGroup753.isEmpty()) openGroup753(attGroup753, attYear753, attMonth753);
                else if (page.startsWith("ATTENDANCE_SESSION_753:")) {
                    String rest = page.substring("ATTENDANCE_SESSION_753:".length()); int k = rest.lastIndexOf(':');
                    if (k > 0) { String g = rest.substring(0, k); long sid = Long.parseLong(rest.substring(k + 1));
                        Cursor c = db.getReadableDatabase().rawQuery("SELECT sessionDate FROM attendance_sessions WHERE id=? LIMIT 1", new String[]{String.valueOf(sid)});
                        String dt = c.moveToFirst() ? c.getString(0) : ""; c.close(); if (!dt.isEmpty()) showSession753(g, sid, dt); }
                }
            } catch (Exception ignored) {}
        });
    }

    private void tryInvoke628753(String name, Class<?>[] sig, Object... args) {
        try { java.lang.reflect.Method m = MainActivityV628.class.getDeclaredMethod(name, sig); m.setAccessible(true); m.invoke(this, args); }
        catch (Exception ignored) {}
    }

    private int currentYear753() { return Calendar.getInstance().get(Calendar.YEAR); }
    private int currentMonth753() { return Calendar.getInstance().get(Calendar.MONTH); }
    private String trDate753(String iso) { try { return tr753.format(iso753.parse(iso)); } catch (Exception e) { return iso; } }
    private String monthName753(int m) {
        String[] a = {"OCAK","ŞUBAT","MART","NİSAN","MAYIS","HAZİRAN","TEMMUZ","AĞUSTOS","EYLÜL","EKİM","KASIM","ARALIK"};
        return m >= 0 && m < 12 ? a[m] : "AY";
    }

    @Override protected void onDestroy() { destroyed753 = true; ui753.removeCallbacksAndMessages(null); super.onDestroy(); }
}
