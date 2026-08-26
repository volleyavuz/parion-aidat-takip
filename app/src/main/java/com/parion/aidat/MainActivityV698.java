package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.98 - dashboard fixes aligned with actual profile/material and attendance UI semantics. */
public class MainActivityV698 extends MainActivityV694 {
    private static final int TEXT=Color.rgb(28,28,28), MUTED=Color.rgb(92,92,92), RED=Color.rgb(185,55,55), GOLD=Color.rgb(205,156,34);

    @Override void showHome(){
        super.showHome();
        if(root!=null){root.post(this::patch698);root.postDelayed(this::patch698,500);}
    }

    private void patch698(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll698(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        removeCards698(box,"TİŞÖRT ALMAYAN");
        removeCards698(box,"DEVAMSIZLAR");
        addTshirt698(box);
        addAbsentees698(box);
    }

    private void addTshirt698(LinearLayout box){
        int count=countNoTshirt698();
        LinearLayout card=new LinearLayout(this);card.setTag("v698-tshirt");card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackground(round698(Color.WHITE,GOLD,18,1));card.setClickable(true);card.setOnClickListener(v->showNoTshirt698());
        ImageView icon=new ImageView(this);icon.setImageResource(android.R.drawable.ic_menu_agenda);icon.setColorFilter(GOLD);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);card.addView(icon,new LinearLayout.LayoutParams(dp(25),dp(25)));
        TextView title=txt698("TİŞÖRT ALMAYAN AKTİF SPORCULAR",10.5f,TEXT,true);title.setGravity(Gravity.CENTER);title.setMaxLines(2);card.addView(title);
        TextView num=txt698(String.valueOf(count),27f,GOLD,true);num.setGravity(Gravity.CENTER);card.addView(num);
        TextView sub=txt698("Verilen tişört adedi 0",9.5f,MUTED,false);sub.setGravity(Gravity.CENTER);card.addView(sub);
        View winter=topChild698(box,"KIŞIN ARANACAK");int idx=winter==null?box.getChildCount():box.indexOfChild(winter);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(116));lp.setMargins(dp(3),dp(4),dp(3),dp(8));box.addView(card,Math.max(0,Math.min(idx,box.getChildCount())),lp);
    }

    private int countNoTshirt698(){
        String sql="SELECT COUNT(*) FROM athletes a WHERE a.status='AKTİF' AND TRIM(COALESCE(a.deletedAt,''))='' AND COALESCE((SELECT SUM(mt.qty) FROM material_transactions mt WHERE mt.athleteId=a.id AND UPPER(mt.product) LIKE '%TİŞÖRT%'),0)=0";
        Cursor c=db.getReadableDatabase().rawQuery(sql,null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;
    }

    private void showNoTshirt698(){
        page="NO_TSHIRT_698";base("TİŞÖRT ALMAYAN AKTİF SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        String sql="SELECT a.id,a.name,a.birthYear,a.category,COALESCE((SELECT SUM(mt.qty) FROM material_transactions mt WHERE mt.athleteId=a.id AND UPPER(mt.product) LIKE '%TİŞÖRT%'),0) shirtQty FROM athletes a WHERE a.status='AKTİF' AND TRIM(COALESCE(a.deletedAt,''))='' AND COALESCE((SELECT SUM(mt.qty) FROM material_transactions mt WHERE mt.athleteId=a.id AND UPPER(mt.product) LIKE '%TİŞÖRT%'),0)=0 ORDER BY a.name COLLATE NOCASE";
        Cursor c=db.getReadableDatabase().rawQuery(sql,null);
        while(c.moveToNext()){
            final long id=c.getLong(0);String name=safe698(c.getString(1));int by=c.getInt(2);String group=safe698(c.getString(3));
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));row.setBackground(round698(Color.WHITE,GOLD,12,1));row.setClickable(true);row.setOnClickListener(v->showProfile(id));
            row.addView(txt698(name,14,TEXT,true));row.addView(txt698((by>0?by+" • ":"")+group+" • Verilen tişört: 0",10.5f,MUTED,false));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);n++;
        }
        c.close();if(n==0)b.addView(txt698("Verilen tişört adedi 0 olan aktif sporcu bulunmuyor.",13,MUTED,true));
    }

    private static class Ab698{long id;String name,group;int days;Ab698(long i,String n,String g,int d){id=i;name=n;group=g;days=d;}}

    private ArrayList<Ab698> absentees698(){
        ArrayList<Ab698> out=new ArrayList<>();Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,category,startDate,restartDate FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(a.moveToNext()){
            long id=a.getLong(0);String name=safe698(a.getString(1)),group=safe698(a.getString(2)),start=safe698(a.getString(3)),restart=safe698(a.getString(4));
            if(group.isEmpty()||!groupEnabled698(group))continue;int days=absenceDays698(id,group,start,restart);if(days>=10)out.add(new Ab698(id,name,group,days));
        }
        a.close();Collections.sort(out,(x,y)->x.days!=y.days?Integer.compare(y.days,x.days):x.name.compareToIgnoreCase(y.name));return out;
    }

    /** Attendance UI treats a missing record as present. Dashboard must use the same rule. */
    private int absenceDays698(long athlete,String group,String start,String restart){
        String spell=currentSpellStart698(start,restart);if(spell.isEmpty())spell="1900-01-01";
        String lastPresent=null;
        Cursor p=db.getReadableDatabase().rawQuery(
            "SELECT MAX(s.sessionDate) FROM attendance_sessions s LEFT JOIN attendance_records r ON r.sessionId=s.id AND r.athleteId=? WHERE s.groupName=? COLLATE NOCASE AND s.confirmed=1 AND s.cancelled=0 AND s.sessionDate>=? AND s.sessionDate<=date('now') AND COALESCE(r.present,1)=1",
            new String[]{String.valueOf(athlete),group,spell});if(p.moveToFirst()&&!p.isNull(0))lastPresent=p.getString(0);p.close();

        if(lastPresent!=null&&!lastPresent.isEmpty()){
            Cursor m=db.getReadableDatabase().rawQuery(
                "SELECT MIN(s.sessionDate) FROM attendance_sessions s JOIN attendance_records r ON r.sessionId=s.id AND r.athleteId=? WHERE s.groupName=? COLLATE NOCASE AND s.confirmed=1 AND s.cancelled=0 AND s.sessionDate>? AND s.sessionDate<=date('now') AND r.present=0",
                new String[]{String.valueOf(athlete),group,lastPresent});String firstMiss=null;if(m.moveToFirst()&&!m.isNull(0))firstMiss=m.getString(0);m.close();if(firstMiss==null)return 0;return daysSince698(lastPresent);
        }

        // No visible-present session: count only from an explicit confirmed absence, never from a generated session alone.
        Cursor m=db.getReadableDatabase().rawQuery(
            "SELECT MIN(s.sessionDate) FROM attendance_sessions s JOIN attendance_records r ON r.sessionId=s.id AND r.athleteId=? WHERE s.groupName=? COLLATE NOCASE AND s.confirmed=1 AND s.cancelled=0 AND s.sessionDate>=? AND s.sessionDate<=date('now') AND r.present=0",
            new String[]{String.valueOf(athlete),group,spell});String firstMiss=null;if(m.moveToFirst()&&!m.isNull(0))firstMiss=m.getString(0);m.close();return firstMiss==null?0:daysSince698(firstMiss);
    }

    private int daysSince698(String date){Cursor d=db.getReadableDatabase().rawQuery("SELECT CAST(julianday(date('now'))-julianday(?) AS INTEGER)",new String[]{date});int days=0;if(d.moveToFirst())days=Math.max(0,d.getInt(0));d.close();return days;}
    private String currentSpellStart698(String start,String restart){String today=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());if(isIso698(restart)&&restart.compareTo(today)<=0)return restart;return isIso698(start)?start:"";}

    private void addAbsentees698(LinearLayout box){
        ArrayList<Ab698> list=absentees698();LinearLayout card=new LinearLayout(this);card.setTag("v698-absentees");card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(12),dp(12),dp(12));card.setBackground(round698(Color.WHITE,RED,14,1));
        TextView head=txt698("DEVAMSIZLAR  •  "+list.size(),12,TEXT,true);card.addView(head);
        if(list.isEmpty())card.addView(txt698("Seçili gruplarda 10 gün ve üzeri devamsız aktif sporcu yok.",11,MUTED,false));
        else{
            ArrayList<View> extra=new ArrayList<>();for(int i=0;i<list.size();i++){Ab698 x=list.get(i);TextView r=txt698(x.name+" • "+x.days+" gündür gelmiyor • "+x.group,12,x.days>=30?Color.rgb(170,30,30):TEXT,true);r.setPadding(dp(4),dp(7),dp(4),dp(7));r.setOnClickListener(v->showProfile(x.id));card.addView(r);if(i>=3){r.setVisibility(View.GONE);extra.add(r);}}
            if(list.size()>3){TextView more=txt698("TÜMÜNÜ GÖSTER ("+list.size()+")  ▼",10.5f,RED,true);more.setGravity(Gravity.CENTER);more.setPadding(dp(4),dp(9),dp(4),dp(4));final boolean[] open={false};more.setOnClickListener(v->{open[0]=!open[0];for(View e:extra)e.setVisibility(open[0]?View.VISIBLE:View.GONE);more.setText(open[0]?"DARALT  ▲":"TÜMÜNÜ GÖSTER ("+list.size()+")  ▼");});card.addView(more);}
        }
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(12),0,dp(8));box.addView(card,lp);
    }

    private boolean groupEnabled698(String group){try{Cursor c=db.getReadableDatabase().rawQuery("SELECT enabled FROM attendance_dashboard_groups WHERE groupName=?",new String[]{group});boolean on=!c.moveToFirst()||c.getInt(0)==1;c.close();return on;}catch(Exception e){return true;}}
    @Override void goBack(){if("NO_TSHIRT_698".equals(page)){showHome();return;}super.goBack();}

    private void removeCards698(LinearLayout box,String needle){for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(containsText698(v,needle))box.removeViewAt(i);}}
    private TextView txt698(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable round698(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));if(width>0)d.setStroke(dp(width),stroke);return d;}
    private String safe698(String s){return s==null?"":s.trim();}
    private boolean isIso698(String s){return s!=null&&s.matches("\\d{4}-\\d{2}-\\d{2}");}
    private String norm698(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    private boolean containsText698(View v,String needle){if(v instanceof TextView&&norm698(String.valueOf(((TextView)v).getText())).contains(norm698(needle)))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText698(g.getChildAt(i),needle))return true;}return false;}
    private View topChild698(LinearLayout box,String needle){for(int i=0;i<box.getChildCount();i++){View v=box.getChildAt(i);if(containsText698(v,needle))return v;}return null;}
    private ScrollView findScroll698(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll698(g.getChildAt(i));if(s!=null)return s;}}return null;}
}
