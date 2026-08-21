package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.2 - overdue logic + seasonal navigation + compact header logo. */
public class MainActivityV602 extends MainActivityV601 {

    @Override void base(String title, boolean back) {
        super.base(title, back);
        patchHeaderLogo602();
    }

    @Override void showHome() {
        super.showHome();
        removeLegacyYellowBanner602();
        // v4.0.15: overdue is computed once in V608, asynchronously.
    }

    private void patchHeaderLogo602() {
        if (root == null || root.getChildCount() == 0) return;
        View first = root.getChildAt(0);
        if (!(first instanceof LinearLayout)) return;
        LinearLayout bar = (LinearLayout) first;
        ImageView home = new ImageView(this);
        home.setImageResource(R.drawable.ic_launcher);
        home.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        home.setContentDescription("ANA SAYFA");
        home.setPadding(dp(4),dp(4),dp(4),dp(4));
        home.setOnClickListener(v -> showHome());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48),dp(48));
        lp.setMargins(dp(6),0,0,0);
        bar.addView(home, lp);
    }

    private void removeLegacyYellowBanner602() {
        ScrollView sv = firstScroll602(root);
        if (sv == null || sv.getChildCount() == 0 || !(sv.getChildAt(0) instanceof LinearLayout)) return;
        LinearLayout box = (LinearLayout) sv.getChildAt(0);
        for (int i=box.getChildCount()-1;i>=0;i--) {
            View v = box.getChildAt(i);
            if (v instanceof ImageView) {
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (lp != null && lp.height >= dp(90)) box.removeViewAt(i);
            }
        }
    }

    private void patchOverdueCard602() {
        ScrollView sv = firstScroll602(root);
        if (sv == null || sv.getChildCount()==0 || !(sv.getChildAt(0) instanceof LinearLayout)) return;
        LinearLayout box = (LinearLayout) sv.getChildAt(0);
        View overdue = topChildContaining602(box,"GECİKMİŞ");
        if (overdue == null) return;
        setFirstNumber602(overdue, countOverdueAthletes602());
        overdue.setOnClickListener(v -> showOverdue602());
    }

    private int countOverdueAthletes602() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR), currentMonth = now.get(Calendar.MONTH)+1;
        if (year < 2026) return 0;
        int limit = year > 2026 ? 12 : Math.max(0,currentMonth-1);
        if (limit <= 0) return 0;
        Cursor c = db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY id",null);
        int n=0;
        while(c.moveToNext()) if(hasOverdue602(c.getLong(0),limit)) n++;
        c.close();
        return n;
    }

    private boolean hasOverdue602(long athleteId,int limitMonth) {
        Cursor a = db.athlete(athleteId);
        if(!a.moveToFirst()){a.close();return false;}
        String sibling=s602(a,"sibling"),start=s602(a,"startDate"),end=s602(a,"endDate"),restart=s602(a,"restartDate");
        int baseFee=a.getInt(a.getColumnIndexOrThrow("monthlyFee"));
        a.close();
        if("BURSLU".equalsIgnoreCase(sibling) || baseFee<=0) return false;
        HashMap<Integer,String[]> pays=new HashMap<>();
        Cursor p=db.payments(athleteId);
        while(p.moveToNext()) pays.put(p.getInt(p.getColumnIndexOrThrow("month")),new String[]{s602(p,"marker"),String.valueOf(p.getInt(p.getColumnIndexOrThrow("amount")))});
        p.close();
        for(int m=1;m<=limitMonth;m++){
            if(!activeMonth(m,start,end,restart)) continue;
            String[] x=pays.get(m);String marker=x==null?"":x[0];int amount=x==null?0:parse602(x[1]);
            if("X".equals(marker)) continue;
            int fee=expectedFee602(athleteId,m,baseFee);
            if(fee<=0) continue;
            if(amount < fee) return true;
        }
        return false;
    }

    private int expectedFee602(long athleteId,int month,int fallback){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT fee FROM fee_history WHERE athleteId=? AND year=2026 AND effectiveMonth<=? ORDER BY effectiveMonth DESC LIMIT 1",new String[]{String.valueOf(athleteId),String.valueOf(month)});
        int fee=fallback;if(c.moveToFirst())fee=c.getInt(0);c.close();return fee;
    }

    private void showOverdue602(){
        page="OVERDUE_602";base("GECİKMİŞ SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);
        Calendar now=Calendar.getInstance();int limit=now.get(Calendar.YEAR)>2026?12:Math.max(0,now.get(Calendar.MONTH));
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);int n=0;
        while(c.moveToNext()){A x=a(c);if(!hasOverdue602(x.id,limit))continue;row(b,x,null,0);n++;}
        c.close();if(n==0)b.addView(tv("VADESİ GEÇMİŞ BORCU BULUNAN AKTİF SPORCU YOK.",14,Color.DKGRAY,true));
    }

    @Override void goBack(){
        if("SUMMER_CALL_600".equals(page)||"WINTER_CALL_600".equals(page)||"OVERDUE_602".equals(page)){showHome();return;}
        super.goBack();
    }

    private ScrollView firstScroll602(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=firstScroll602(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private boolean containsText602(View v,String needle){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR")).contains(needle.toUpperCase(new Locale("tr","TR"))))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText602(g.getChildAt(i),needle))return true;}return false;}
    private View topChildContaining602(LinearLayout parent,String needle){for(int i=0;i<parent.getChildCount();i++){View v=parent.getChildAt(i);if(containsText602(v,needle))return v;}return null;}
    private void setFirstNumber602(View v,int n){ArrayList<TextView> list=new ArrayList<>();collect602(v,list);for(TextView t:list){String s=String.valueOf(t.getText()).trim();if(s.matches("\\d+")){t.setText(String.valueOf(n));return;}}}
    private void collect602(View v,ArrayList<TextView> out){if(v instanceof TextView)out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collect602(g.getChildAt(i),out);}}
    private String s602(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
    private int parse602(String s){try{return Integer.parseInt(s);}catch(Exception e){return 0;}}
}
