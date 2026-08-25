package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.96 - narrow dashboard data fix: active no-tshirt + 10-day attendance absentees. */
public class MainActivityV696 extends MainActivityV694 {
    private static final int TEXT=Color.rgb(28,28,28), MUTED=Color.rgb(92,92,92), RED=Color.rgb(185,55,55), GOLD=Color.rgb(205,156,34);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::patchDashboard696);
            root.postDelayed(this::patchDashboard696,900);
            root.postDelayed(this::patchDashboard696,1800);
        }
    }

    private void patchDashboard696(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll696(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        patchTshirt696(box);
        replaceAbsentees696(box);
    }

    private void patchTshirt696(LinearLayout box){
        View card=findTag696(box,"v621-tshirt-card");
        if(card==null){TextView t=findText696(box,"TİŞÖRT ALMAYAN","TİŞÖRT ALMAYANLAR");card=t==null?null:nearestCard696(t);}
        if(card==null)return;
        TextView number=findNumeric696(card);
        if(number!=null)number.setText(String.valueOf(countNoTshirt696()));
        card.setClickable(true);
        card.setOnClickListener(v->showNoTshirt696());
    }

    private int countNoTshirt696(){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status='AKTİF' AND COALESCE(tshirtQty,0)=0 AND TRIM(COALESCE(deletedAt,''))=''",null);
        int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;
    }

    private void showNoTshirt696(){
        page="NO_TSHIRT_696";base("TİŞÖRT ALMAYAN AKTİF SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear,category FROM athletes WHERE status='AKTİF' AND COALESCE(tshirtQty,0)=0 AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){
            final long id=c.getLong(0);String name=safe696(c.getString(1));int by=c.getInt(2);String group=safe696(c.getString(3));
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));row.setBackground(round696(Color.WHITE,GOLD,12,1));row.setClickable(true);row.setOnClickListener(v->showProfile(id));
            TextView title=txt696(name,14,TEXT,true);row.addView(title);
            TextView sub=txt696((by>0?by+" • ":"")+group+" • Tişört: 0",10.5f,MUTED,false);row.addView(sub);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);n++;
        }
        c.close();if(n==0)b.addView(txt696("Tişört almayan aktif sporcu bulunmuyor.",13,MUTED,true));
    }

    private static class Ab696{long id;String name,group;int days;Ab696(long i,String n,String g,int d){id=i;name=n;group=g;days=d;}}

    private ArrayList<Ab696> absentees696(){
        ArrayList<Ab696> out=new ArrayList<>();
        Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,category,startDate,endDate,restartDate FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(a.moveToNext()){
            long id=a.getLong(0);String name=safe696(a.getString(1)),group=safe696(a.getString(2));
            String start=safe696(a.getString(3)),end=safe696(a.getString(4)),restart=safe696(a.getString(5));
            if(group.isEmpty()||!groupEnabled696(group))continue;
            int days=absenceDays696(id,group,start,end,restart);
            if(days>=10)out.add(new Ab696(id,name,group,days));
        }
        a.close();Collections.sort(out,(x,y)->x.days!=y.days?Integer.compare(y.days,x.days):x.name.compareToIgnoreCase(y.name));return out;
    }

    private int absenceDays696(long athlete,String group,String start,String end,String restart){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT s.sessionDate,COALESCE(r.present,0) FROM attendance_sessions s LEFT JOIN attendance_records r ON r.sessionId=s.id AND r.athleteId=? WHERE s.groupName=? COLLATE NOCASE AND s.cancelled=0 AND s.sessionDate<=date('now') ORDER BY s.sessionDate",new String[]{String.valueOf(athlete),group});
        String firstEligible=null,lastPresent=null;
        while(c.moveToNext()){
            String date=safe696(c.getString(0));if(!activeOn696(date,start,end,restart))continue;
            if(firstEligible==null)firstEligible=date;
            if(c.getInt(1)==1)lastPresent=date;
        }
        c.close();String anchor=lastPresent!=null?lastPresent:firstEligible;if(anchor==null)return 0;
        Cursor d=db.getReadableDatabase().rawQuery("SELECT CAST(julianday(date('now'))-julianday(?) AS INTEGER)",new String[]{anchor});int days=0;if(d.moveToFirst())days=Math.max(0,d.getInt(0));d.close();return days;
    }

    private boolean activeOn696(String date,String start,String end,String restart){
        if(date==null||date.isEmpty())return false;
        if(!start.isEmpty()&&!"DEVAM".equalsIgnoreCase(start)&&date.compareTo(start)<0)return false;
        if(end.isEmpty()||"DEVAM".equalsIgnoreCase(end))return true;
        if(date.compareTo(end)<=0)return true;
        return !restart.isEmpty()&&!"DEVAM".equalsIgnoreCase(restart)&&date.compareTo(restart)>=0;
    }

    private boolean groupEnabled696(String group){
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT enabled FROM attendance_dashboard_groups WHERE groupName=?",new String[]{group});boolean on=!c.moveToFirst()||c.getInt(0)==1;c.close();return on;}catch(Exception e){return true;}
    }

    private void replaceAbsentees696(LinearLayout box){
        int idx=-1;View old=null;
        for(int i=0;i<box.getChildCount();i++){View v=box.getChildAt(i);if(containsText696(v,"DEVAMSIZLAR")){idx=i;old=v;break;}}
        if(old!=null)box.removeView(old);if(idx<0)idx=box.getChildCount();
        ArrayList<Ab696> list=absentees696();
        LinearLayout card=new LinearLayout(this);card.setTag("v696-absentees");card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(12),dp(12),dp(12));card.setBackground(round696(Color.WHITE,RED,14,1));
        TextView head=txt696("DEVAMSIZLAR",12,TEXT,true);card.addView(head);
        if(list.isEmpty())card.addView(txt696("Seçili gruplarda 10 gün ve üzeri devamsız aktif sporcu yok.",11,MUTED,false));
        else for(Ab696 x:list){TextView r=txt696(x.name+" • "+x.days+" gündür gelmiyor • "+x.group,12,x.days>=30?Color.rgb(170,30,30):TEXT,true);r.setPadding(dp(4),dp(7),dp(4),dp(7));r.setOnClickListener(v->showProfile(x.id));card.addView(r);}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(12),0,dp(8));box.addView(card,Math.min(idx,box.getChildCount()),lp);
    }

    @Override void goBack(){if("NO_TSHIRT_696".equals(page)){showHome();return;}super.goBack();}

    private TextView txt696(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable round696(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));if(width>0)d.setStroke(dp(width),stroke);return d;}
    private String safe696(String s){return s==null?"":s.trim();}
    private boolean containsText696(View v,String needle){if(v instanceof TextView&&norm696(String.valueOf(((TextView)v).getText())).contains(norm696(needle)))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText696(g.getChildAt(i),needle))return true;}return false;}
    private TextView findText696(View v,String... needles){if(v instanceof TextView){String n=norm696(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm696(s)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText696(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private TextView findNumeric696(View v){if(v instanceof TextView){String s=String.valueOf(((TextView)v).getText()).trim();if(s.matches("[0-9]+"))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findNumeric696(g.getChildAt(i));if(r!=null)return r;}}return null;}
    private View nearestCard696(View v){View cur=v,best=v;while(cur!=null&&cur!=root){if(cur.isClickable()||cur.hasOnClickListeners())best=cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}return best;}
    private View findTag696(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag696(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ScrollView findScroll696(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll696(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm696(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
