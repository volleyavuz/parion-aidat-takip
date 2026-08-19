package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/**
 * v4.0.1 UI compatibility layer.
 * Keeps the v4 cloud/delta engine from V600 and restores the final v62 dashboard/navigation behaviour.
 */
public class MainActivityV601 extends MainActivityV600 {
    private static final int GOLD = Color.rgb(218,165,32);
    private static final int TEXT = Color.rgb(35,35,35);

    @Override void base(String title, boolean back) {
        super.base(title, back);
        patchHeader601(title, back);
    }

    @Override void showHome() {
        super.showHome();
        patchHome601();
    }

    private void patchHeader601(String title, boolean back) {
        if (root == null) return;
        ArrayList<TextView> all = new ArrayList<>();
        collectText601(root, all);
        for (TextView t : all) {
            String s = text601(t);
            if (back && (s.equals("GERİ") || s.equals("←") || s.equals("<"))) {
                t.setText("‹");
                t.setTextSize(28);
                t.setGravity(Gravity.CENTER);
            }
            if ("ANA SAYFA".equalsIgnoreCase(s) || "PARİON SPOR KULÜBÜ SPORCU TAKİP SİSTEMİ".equalsIgnoreCase(s)) {
                t.setText("PARİON SPOR KULÜBÜ\nAİDAT TAKİP SİSTEMİ");
                t.setGravity(Gravity.CENTER);
            }
        }
    }

    private void patchHome601() {
        ScrollView sv = firstScroll601(root);
        if (sv == null || sv.getChildCount() == 0 || !(sv.getChildAt(0) instanceof LinearLayout)) return;
        LinearLayout box = (LinearLayout) sv.getChildAt(0);

        // v62: SPORCULAR is the first actionable dashboard card.
        View athletes = topChildContaining601(box, "SPORCULAR");
        if (athletes != null) moveTo601(box, athletes, 0);

        // v62: recent activity title is short and clean.
        replaceText601(box, "SON İŞLEMLER • İSTANBUL SAATİ", "SON İŞLEMLER");

        // v62 status cards and target revenue card.
        if (!containsText601(box, "AKTİF SPORCU") && !containsText601(box, "ARA VERDİ")) {
            int at = athletes == null ? 0 : Math.min(1, box.getChildCount());
            box.addView(statusRow601(), at);
        }
        if (!containsText601(box, "AYLIK HEDEF CİRO")) {
            int at = Math.min(2, box.getChildCount());
            box.addView(targetCard601(), at);
        }

        // v62 groups the three maintenance cards compactly instead of leaving tall full-width cards.
        compactMaintenance601(box);

        // Deleted athletes belongs inside the scroll, after the normal working cards.
        View deleted = topChildContaining601(box, "SİLİNEN SPORCULAR");
        if (deleted != null) moveTo601(box, deleted, box.getChildCount()-1);
    }

    private View statusRow601() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(5), 0, dp(5));
        int active = countStatus601("AKTİF");
        int paused = countStatus601("ARA VERDİ");
        View a = miniCard601("AKTİF SPORCU", active, Color.rgb(35,135,75), () -> showStatus601("AKTİF"));
        View p = miniCard601("ARA VERDİ", paused, Color.rgb(190,120,30), () -> showStatus601("ARA VERDİ"));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, dp(92), 1f); lp1.setMargins(0,0,dp(4),0);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, dp(92), 1f); lp2.setMargins(dp(4),0,0,0);
        row.addView(a, lp1); row.addView(p, lp2);
        return row;
    }

    private View targetCard601() {
        LinearLayout card = cardShell601();
        TextView title = label601("AYLIK HEDEF CİRO", 12, true, Color.DKGRAY);
        TextView amount = label601(money601(activeTarget601()), 24, true, GOLD);
        TextView sub = label601(countStatus601("AKTİF") + " AKTİF SPORCU", 11, false, Color.GRAY);
        card.addView(title); card.addView(amount); card.addView(sub);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(104)); lp.setMargins(0,dp(5),0,dp(5)); card.setLayoutParams(lp);
        card.setOnClickListener(v -> showStatus601("AKTİF"));
        return card;
    }

    private void compactMaintenance601(LinearLayout box) {
        String[] labels = {"ÖDENMEMİŞ MALZEME", "FOTOĞRAFI OLMAYAN AKTİF SPORCULAR", "KAYIT FORMU OLMAYAN AKTİF SPORCULAR"};
        ArrayList<View> found = new ArrayList<>();
        int first = Integer.MAX_VALUE;
        for (String s : labels) {
            View v = topChildContaining601(box, s);
            if (v != null && !found.contains(v)) { found.add(v); first = Math.min(first, box.indexOfChild(v)); }
        }
        if (found.size() < 2) return;
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(0,dp(4),0,dp(4));
        for (View v : found) {
            box.removeView(v);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(96), 1f); lp.setMargins(dp(2),0,dp(2),0);
            row.addView(v, lp);
        }
        box.addView(row, Math.min(first, box.getChildCount()));
    }

    private View miniCard601(String title, int count, int accent, Runnable action) {
        LinearLayout card = cardShell601();
        TextView n = label601(String.valueOf(count), 25, true, accent);
        TextView t = label601(title, 11, true, TEXT);
        card.addView(n); card.addView(t); card.setOnClickListener(v -> action.run()); return card;
    }

    private LinearLayout cardShell601() {
        LinearLayout x = new LinearLayout(this); x.setOrientation(LinearLayout.VERTICAL); x.setGravity(Gravity.CENTER); x.setPadding(dp(8),dp(7),dp(8),dp(7));
        x.setBackground(round(Color.WHITE, 12)); return x;
    }

    private TextView label601(String s, int sp, boolean bold, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color); t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }

    private int countStatus601(String status) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status=? AND TRIM(COALESCE(deletedAt,''))=''", new String[]{status});
        int n=0; if(c.moveToFirst()) n=c.getInt(0); c.close(); return n;
    }

    private long activeTarget601() {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(monthlyFee),0) FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))=''", null);
        long n=0; if(c.moveToFirst()) n=c.getLong(0); c.close(); return n;
    }

    private String money601(long n) { return String.format(new Locale("tr","TR"), "₺%,d", n).replace(',', '.'); }

    private void showStatus601(String status) {
        page = "STATUS601:"+status;
        base(status.equals("AKTİF") ? "AKTİF SPORCULAR" : "ARA VEREN SPORCULAR", true);
        ScrollView sv = scroll(); LinearLayout b = box(sv);
        Cursor c = db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE status=? AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE", new String[]{status});
        int n=0; while(c.moveToNext()) { row(b, a(c), null, 0); n++; } c.close();
        if(n==0) b.addView(label601("BU DURUMDA SPORCU YOK.",14,true,Color.DKGRAY));
    }

    @Override void goBack() {
        if (page != null && page.startsWith("STATUS601:")) { showHome(); return; }
        super.goBack();
    }

    private ScrollView firstScroll601(View v) {
        if (v instanceof ScrollView) return (ScrollView)v;
        if (v instanceof ViewGroup) { ViewGroup g=(ViewGroup)v; for(int i=0;i<g.getChildCount();i++){ ScrollView s=firstScroll601(g.getChildAt(i)); if(s!=null)return s; } }
        return null;
    }

    private void collectText601(View v, ArrayList<TextView> out) {
        if(v instanceof TextView) out.add((TextView)v);
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText601(g.getChildAt(i),out);}
    }
    private String text601(TextView t){return String.valueOf(t.getText()).trim();}
    private boolean containsText601(View v,String needle){
        if(v instanceof TextView && text601((TextView)v).toUpperCase(new Locale("tr","TR")).contains(needle.toUpperCase(new Locale("tr","TR")))) return true;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText601(g.getChildAt(i),needle))return true;} return false;
    }
    private View topChildContaining601(LinearLayout parent,String needle){for(int i=0;i<parent.getChildCount();i++){View v=parent.getChildAt(i);if(containsText601(v,needle))return v;}return null;}
    private void moveTo601(LinearLayout p,View v,int idx){if(v==null)return;int old=p.indexOfChild(v);if(old<0)return;p.removeView(v);p.addView(v,Math.max(0,Math.min(idx,p.getChildCount())));}
    private void replaceText601(View v,String old,String neo){if(v instanceof TextView){TextView t=(TextView)v;if(text601(t).equalsIgnoreCase(old))t.setText(neo);}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)replaceText601(g.getChildAt(i),old,neo);}}
}
