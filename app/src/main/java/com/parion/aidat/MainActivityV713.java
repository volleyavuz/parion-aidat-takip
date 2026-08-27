package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.1.35 - unify home icon color, remove tshirt subtitle, rebuild absentee card on modern attendance records. */
public class MainActivityV713 extends MainActivityV712 {
    private static final int NAV_713=Color.rgb(78,78,78), RED_713=Color.rgb(196,63,63);
    static class Ab713{long id;String name,group;ArrayList<String> dates=new ArrayList<>();}

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(()->{fixHomeIcon713(root);removeTshirtSubtitle713(root);replaceAbsentees713();root.post(()->{fixHomeIcon713(root);removeTshirtSubtitle713(root);replaceAbsentees713();});});
    }

    @Override void base(String title,boolean back){super.base(title,back);if(root!=null)root.post(()->fixHomeIcon713(root));}

    @Override void goBack(){if("ABSENTEES_713".equals(page)){showHome();return;}super.goBack();}

    private void fixHomeIcon713(View v){
        if(v==null)return;CharSequence cd=v.getContentDescription();
        if(cd!=null&&"Anasayfa".equalsIgnoreCase(cd.toString())&&v instanceof ImageButton){
            ImageButton b=(ImageButton)v;b.clearColorFilter();b.setImageResource(R.drawable.ic_nav_home);b.setColorFilter(NAV_713);b.setImageTintList(android.content.res.ColorStateList.valueOf(NAV_713));b.setImageAlpha(148);b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);b.setPadding(dp(12),dp(12),dp(12),dp(12));
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)fixHomeIcon713(g.getChildAt(i));}
    }

    private void removeTshirtSubtitle713(View v){
        View card=findTag713(v,"v621-tshirt-card");if(!(card instanceof ViewGroup))return;removeTshirtText713((ViewGroup)card);
    }
    private void removeTshirtText713(ViewGroup g){
        for(int i=g.getChildCount()-1;i>=0;i--){View c=g.getChildAt(i);if(c instanceof TextView){String n=norm713(String.valueOf(((TextView)c).getText()));if(n.contains("VERİLEN TİŞÖRT ADEDİ")||n.contains("TİŞÖRT ADEDİ")||n.contains("TİŞÖRT SAYISI")||n.contains("AKTİF • TİŞÖRT")){g.removeViewAt(i);continue;}}if(c instanceof ViewGroup)removeTshirtText713((ViewGroup)c);}
    }

    private void replaceAbsentees713(){
        if(root==null||!"HOME".equals(page))return;LinearLayout box=homeBox713();if(box==null)return;
        for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(contains713(v,"DEVAMSIZLAR"))box.removeViewAt(i);}
        ArrayList<Ab713> list=absentees713();
        LinearLayout card=new LinearLayout(this);card.setTag("v713-absentees");card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(16),dp(12),dp(20));card.setMinimumHeight(dp(138));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),RED_713);card.setBackground(bg);card.setElevation(dp(2));
        TextView n=tv(String.valueOf(list.size()),28,RED_713,true);n.setGravity(Gravity.CENTER);card.addView(n);TextView t=tv("DEVAMSIZLAR",11,Color.rgb(45,45,45),true);t.setGravity(Gravity.CENTER);card.addView(t);TextView s=tv("Son 4 gerçek antrenmanın tamamına gelmeyenler",9,Color.rgb(105,105,105),false);s.setGravity(Gravity.CENTER);card.addView(s);card.setOnClickListener(v->showAbsentees713());card.setClickable(true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);lp.setMargins(dp(4),dp(10),dp(4),dp(28));box.addView(card,lp);
    }

    private ArrayList<Ab713> absentees713(){
        ArrayList<Ab713> out=new ArrayList<>();Cursor a=null;try{
            a=db.getReadableDatabase().rawQuery("SELECT id,name,category,startDate,endDate,restartDate FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' AND TRIM(COALESCE(category,''))<>'' ORDER BY name COLLATE NOCASE",null);
            while(a.moveToNext()){
                long id=a.getLong(0);String name=safe713(a.getString(1)),group=safe713(a.getString(2)),start=safe713(a.getString(3)),end=safe713(a.getString(4)),restart=safe713(a.getString(5));ArrayList<Long> ids=new ArrayList<>();ArrayList<String> dates=new ArrayList<>();Cursor s=null;
                try{s=db.getReadableDatabase().rawQuery("SELECT s.id,s.sessionDate FROM attendance_sessions s WHERE s.groupName=? COLLATE NOCASE AND s.cancelled=0 AND s.sessionDate<=date('now','localtime') AND EXISTS(SELECT 1 FROM attendance_records r WHERE r.sessionId=s.id AND r.athleteId=?) ORDER BY s.sessionDate DESC,s.id DESC",new String[]{group,String.valueOf(id)});while(s.moveToNext()&&ids.size()<4){String d=safe713(s.getString(1));if(active713(d,start,end,restart)){ids.add(s.getLong(0));dates.add(d);}}}finally{if(s!=null)s.close();}
                if(ids.size()<4)continue;boolean allAbsent=true;for(long sid:ids){Cursor r=null;try{r=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=? LIMIT 1",new String[]{String.valueOf(sid),String.valueOf(id)});if(!r.moveToFirst()||r.getInt(0)!=0){allAbsent=false;break;}}finally{if(r!=null)r.close();}}
                if(allAbsent){Ab713 x=new Ab713();x.id=id;x.name=name;x.group=group;x.dates.addAll(dates);out.add(x);}
            }
        }finally{if(a!=null)a.close();}return out;
    }

    private void showAbsentees713(){page="ABSENTEES_713";base("DEVAMSIZLAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);ArrayList<Ab713> rows=absentees713();TextView info=tv("Aktif sporcuların kendi gruplarındaki, bugüne kadar gerçekleşmiş ve iptal edilmemiş son 4 yoklama kaydı değerlendirilir. Dördünde de gelmedi işaretli olanlar listelenir.",11,Color.DKGRAY,false);b.addView(info);for(Ab713 x:rows){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(12),dp(10),dp(12),dp(10));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(12));r.setBackground(bg);TextView nm=tv(x.name,14,Color.rgb(30,30,30),true);r.addView(nm);r.addView(tv(x.group+" • Üst üste 4 antrenman yok",10,RED_713,true));r.addView(tv("Tarihler: "+join713(x.dates),9,Color.GRAY,false));r.setOnClickListener(v->showProfile(x.id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,0);b.addView(r,lp);}if(rows.isEmpty())b.addView(tv("Üst üste 4 antrenmana gelmeyen aktif sporcu yok.",13,Color.DKGRAY,true));}

    private boolean active713(String d,String start,String end,String restart){if(d==null||d.length()<10)return false;if(!start.isEmpty()&&d.compareTo(start)<0)return false;if(end.isEmpty()||"DEVAM".equalsIgnoreCase(end))return true;if(d.compareTo(end)<=0)return true;return !restart.isEmpty()&&d.compareTo(restart)>=0;}
    private String join713(ArrayList<String> ds){ArrayList<String> x=new ArrayList<>();for(String d:ds)x.add(dateTr(d));return android.text.TextUtils.join(" • ",x);}private String safe713(String s){return s==null?"":s;}
    private LinearLayout homeBox713(){ScrollView s=findScroll713(root);return s!=null&&s.getChildCount()>0&&s.getChildAt(0) instanceof LinearLayout?(LinearLayout)s.getChildAt(0):null;}private ScrollView findScroll713(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll713(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag713(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag713(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}private boolean contains713(View v,String n){if(v instanceof TextView&&norm713(String.valueOf(((TextView)v).getText())).contains(norm713(n)))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(contains713(g.getChildAt(i),n))return true;}return false;}private String norm713(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
