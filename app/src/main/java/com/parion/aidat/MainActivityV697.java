package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.97 - corrected absentee anchor, compact 3-row preview, guaranteed t-shirt card. */
public class MainActivityV697 extends MainActivityV694 {
    private static final int TEXT=Color.rgb(28,28,28), MUTED=Color.rgb(92,92,92), RED=Color.rgb(185,55,55), GOLD=Color.rgb(205,156,34);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::patch697);
            root.postDelayed(this::patch697,450);
        }
    }

    private void patch697(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll697(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        ensureTshirt697(box);
        ensureAbsentees697(box);
    }

    private void ensureTshirt697(LinearLayout box){
        int oldIndex=-1;
        for(int i=box.getChildCount()-1;i>=0;i--){
            View v=box.getChildAt(i);
            if(containsText697(v,"TİŞÖRT ALMAYAN")){oldIndex=i;box.removeViewAt(i);}
        }
        int count=countNoTshirt697();
        LinearLayout card=new LinearLayout(this);card.setTag("v697-tshirt");card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackground(round697(Color.WHITE,GOLD,18,1));card.setClickable(true);card.setOnClickListener(v->showNoTshirt697());
        ImageView icon=new ImageView(this);icon.setImageResource(android.R.drawable.ic_menu_agenda);icon.setColorFilter(GOLD);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);card.addView(icon,new LinearLayout.LayoutParams(dp(25),dp(25)));
        TextView title=txt697("TİŞÖRT ALMAYAN AKTİF SPORCULAR",10.5f,TEXT,true);title.setGravity(Gravity.CENTER);title.setMaxLines(2);card.addView(title);
        TextView number=txt697(String.valueOf(count),27f,GOLD,true);number.setGravity(Gravity.CENTER);card.addView(number);
        TextView sub=txt697("Tişört sayısı 0",9.5f,MUTED,false);sub.setGravity(Gravity.CENTER);card.addView(sub);

        int idx=oldIndex;
        if(idx<0){View winter=topChild697(box,"KIŞIN ARANACAK");idx=winter==null?box.getChildCount():box.indexOfChild(winter);}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(116));lp.setMargins(dp(3),dp(4),dp(3),dp(8));box.addView(card,Math.max(0,Math.min(idx,box.getChildCount())),lp);
    }

    private int countNoTshirt697(){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status='AKTİF' AND COALESCE(tshirtQty,0)=0 AND TRIM(COALESCE(deletedAt,''))=''",null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}

    private void showNoTshirt697(){
        page="NO_TSHIRT_697";base("TİŞÖRT ALMAYAN AKTİF SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear,category FROM athletes WHERE status='AKTİF' AND COALESCE(tshirtQty,0)=0 AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){
            final long id=c.getLong(0);String name=safe697(c.getString(1));int by=c.getInt(2);String group=safe697(c.getString(3));
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));row.setBackground(round697(Color.WHITE,GOLD,12,1));row.setClickable(true);row.setOnClickListener(v->showProfile(id));
            row.addView(txt697(name,14,TEXT,true));row.addView(txt697((by>0?by+" • ":"")+group+" • Tişört: 0",10.5f,MUTED,false));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);n++;
        }
        c.close();if(n==0)b.addView(txt697("Tişört almayan aktif sporcu bulunmuyor.",13,MUTED,true));
    }

    private static class Ab697{long id;String name,group;int days;Ab697(long i,String n,String g,int d){id=i;name=n;group=g;days=d;}}

    private ArrayList<Ab697> absentees697(){
        ArrayList<Ab697> out=new ArrayList<>();
        Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,category,startDate,restartDate FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(a.moveToNext()){
            long id=a.getLong(0);String name=safe697(a.getString(1)),group=safe697(a.getString(2));String start=safe697(a.getString(3)),restart=safe697(a.getString(4));
            if(group.isEmpty()||!groupEnabled697(group))continue;
            int days=absenceDays697(id,group,start,restart);if(days>=10)out.add(new Ab697(id,name,group,days));
        }
        a.close();Collections.sort(out,(x,y)->x.days!=y.days?Integer.compare(y.days,x.days):x.name.compareToIgnoreCase(y.name));return out;
    }

    private int absenceDays697(long athlete,String currentGroup,String start,String restart){
        String spellStart=currentSpellStart697(start,restart);
        if(spellStart.isEmpty())spellStart="1900-01-01";

        String lastPresent=null;
        Cursor p=db.getReadableDatabase().rawQuery(
            "SELECT MAX(s.sessionDate) FROM attendance_records r JOIN attendance_sessions s ON s.id=r.sessionId WHERE r.athleteId=? AND r.present=1 AND s.cancelled=0 AND s.sessionDate>=? AND s.sessionDate<=date('now')",
            new String[]{String.valueOf(athlete),spellStart});
        if(p.moveToFirst()&&!p.isNull(0))lastPresent=p.getString(0);p.close();

        String firstCurrent=null;
        Cursor f=db.getReadableDatabase().rawQuery(
            "SELECT MIN(sessionDate) FROM attendance_sessions WHERE groupName=? COLLATE NOCASE AND cancelled=0 AND sessionDate>=? AND sessionDate<=date('now')",
            new String[]{currentGroup,spellStart});
        if(f.moveToFirst()&&!f.isNull(0))firstCurrent=f.getString(0);f.close();

        String anchor=lastPresent!=null?lastPresent:firstCurrent;
        if(anchor==null||anchor.isEmpty())return 0;

        Cursor miss=db.getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM attendance_sessions s LEFT JOIN attendance_records r ON r.sessionId=s.id AND r.athleteId=? WHERE s.groupName=? COLLATE NOCASE AND s.cancelled=0 AND s.sessionDate>? AND s.sessionDate<=date('now') AND COALESCE(r.present,0)=0",
            new String[]{String.valueOf(athlete),currentGroup,anchor});
        int missed=0;if(miss.moveToFirst())missed=miss.getInt(0);miss.close();if(missed<=0)return 0;

        Cursor d=db.getReadableDatabase().rawQuery("SELECT CAST(julianday(date('now'))-julianday(?) AS INTEGER)",new String[]{anchor});int days=0;if(d.moveToFirst())days=Math.max(0,d.getInt(0));d.close();return days;
    }

    private String currentSpellStart697(String start,String restart){
        String today=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
        if(isIso697(restart)&&restart.compareTo(today)<=0)return restart;
        return isIso697(start)?start:"";
    }

    private void ensureAbsentees697(LinearLayout box){
        int idx=-1;
        for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(containsText697(v,"DEVAMSIZLAR")){idx=i;box.removeViewAt(i);}}
        if(idx<0)idx=box.getChildCount();
        ArrayList<Ab697> list=absentees697();
        LinearLayout card=new LinearLayout(this);card.setTag("v697-absentees");card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(12),dp(12),dp(12));card.setBackground(round697(Color.WHITE,RED,14,1));
        TextView head=txt697("DEVAMSIZLAR  •  "+list.size(),12,TEXT,true);head.setGravity(Gravity.CENTER_VERTICAL);card.addView(head);
        if(list.isEmpty())card.addView(txt697("Seçili gruplarda 10 gün ve üzeri devamsız aktif sporcu yok.",11,MUTED,false));
        else{
            ArrayList<View> extra=new ArrayList<>();
            for(int i=0;i<list.size();i++){
                Ab697 x=list.get(i);TextView r=txt697(x.name+" • "+x.days+" gündür gelmiyor • "+x.group,12,x.days>=30?Color.rgb(170,30,30):TEXT,true);r.setPadding(dp(4),dp(7),dp(4),dp(7));r.setOnClickListener(v->showProfile(x.id));card.addView(r);if(i>=3){r.setVisibility(View.GONE);extra.add(r);}
            }
            if(list.size()>3){
                TextView more=txt697("TÜMÜNÜ GÖSTER ("+list.size()+")  ▼",10.5f,RED,true);more.setGravity(Gravity.CENTER);more.setPadding(dp(4),dp(9),dp(4),dp(4));final boolean[] open={false};more.setOnClickListener(v->{open[0]=!open[0];for(View e:extra)e.setVisibility(open[0]?View.VISIBLE:View.GONE);more.setText(open[0]?"DARALT  ▲":"TÜMÜNÜ GÖSTER ("+list.size()+")  ▼");});card.addView(more);
            }
        }
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(12),0,dp(8));box.addView(card,Math.min(idx,box.getChildCount()),lp);
    }

    private boolean groupEnabled697(String group){try{Cursor c=db.getReadableDatabase().rawQuery("SELECT enabled FROM attendance_dashboard_groups WHERE groupName=?",new String[]{group});boolean on=!c.moveToFirst()||c.getInt(0)==1;c.close();return on;}catch(Exception e){return true;}}
    @Override void goBack(){if("NO_TSHIRT_697".equals(page)){showHome();return;}super.goBack();}

    private TextView txt697(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable round697(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));if(width>0)d.setStroke(dp(width),stroke);return d;}
    private String safe697(String s){return s==null?"":s.trim();}
    private boolean isIso697(String s){return s!=null&&s.matches("\\d{4}-\\d{2}-\\d{2}");}
    private String norm697(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    private boolean containsText697(View v,String needle){if(v instanceof TextView&&norm697(String.valueOf(((TextView)v).getText())).contains(norm697(needle)))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText697(g.getChildAt(i),needle))return true;}return false;}
    private View topChild697(LinearLayout box,String needle){for(int i=0;i<box.getChildCount();i++){View v=box.getChildAt(i);if(containsText697(v,needle))return v;}return null;}
    private ScrollView findScroll697(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll697(g.getChildAt(i));if(s!=null)return s;}}return null;}
}
