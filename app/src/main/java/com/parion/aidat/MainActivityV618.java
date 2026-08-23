package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.18 - dashboard wording, gold canvas, monthly cohort cards and 90-day movement metric. */
public class MainActivityV618 extends MainActivityV617 {
    private static final int GOLD618=Color.rgb(218,165,32);
    private static final int GOLD_BG618=Color.rgb(246,232,181);
    private static final int GREEN618=Color.rgb(36,135,78);
    private static final int RED618=Color.rgb(196,63,63);
    private static final int TEXT618=Color.rgb(34,34,34);
    private static final int MUTED618=Color.rgb(100,100,100);

    @Override void showHome(){
        super.showHome();
        patch618();
    }

    private void patch618(){
        ScrollView sv=findScroll618(root); if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        box.setBackgroundColor(GOLD_BG618);
        box.setPadding(dp(10),dp(10),dp(10),dp(24));

        patchHeader618(root);
        patchCardTypography618(box);
        patchSeasonCards618(box);
        renameMonthlyCards618(box);
        ensureActiveCountOnTarget618(box);
        replaceExpectedWithCurrentStarts618(box);
        pairDueWithPreviousStarts618(box);
        appendNinetyDayCards618(box);
    }

    private void patchHeader618(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=norm618(String.valueOf(t.getText()));
            if(s.equals("PARION SPOR OKULU")||s.equals("PARİON SPOR OKULU")){
                t.setText("PARİON SPORCU TAKİP UYGULAMASI");t.setTextSize(15f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setMaxLines(2);t.setGravity(Gravity.CENTER_VERTICAL);
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchHeader618(g.getChildAt(i));}
    }

    private void patchCardTypography618(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();String u=norm618(s);
            boolean numeric=s.replace("₺","").replace(".","").replace(" ","").matches("[+-]?[0-9]+(%?)");
            if(!numeric && (u.contains("SPORCU")||u.contains("AİDAT")||u.contains("TAHSİL")||u.contains("ÖDEME")||u.contains("FOTOĞRAF")||u.contains("FORM")||u.contains("MALZEME")||u.contains("ARANACAK")||u.contains("HEDEF")||u.contains("GECİKMİŞ")||u.contains("AYLIK"))){
                float sp=t.getTextSize()/getResources().getDisplayMetrics().scaledDensity;
                if(sp>11f)t.setTextSize(10.5f);
                t.setSingleLine(false);t.setMaxLines(4);t.setEllipsize(null);
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchCardTypography618(g.getChildAt(i));}
    }

    private void patchSeasonCards618(LinearLayout box){
        ArrayList<TextView> season=new ArrayList<>();collectSeason618(box,season);
        if(season.size()>=1){season.get(0).setText("Yazın Aranacak");styleSeasonCard618(season.get(0),GOLD618);}
        if(season.size()>=2){season.get(1).setText("Kışın Aranacak");styleSeasonCard618(season.get(1),Color.rgb(65,105,155));}
        TextView winter=findText618(box,"KIŞIN ARANACAK");if(winter!=null)styleSeasonCard618(winter,Color.rgb(65,105,155));
    }
    private void collectSeason618(View v,List<TextView> out){
        if(v instanceof TextView){String u=norm618(String.valueOf(((TextView)v).getText()));if(u.contains("YAZIN ARANACAK")||u.contains("KIŞIN ARANACAK"))out.add((TextView)v);}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectSeason618(g.getChildAt(i),out);}
    }
    private void styleSeasonCard618(TextView t,int accent){
        View card=nearestClickable618(t);if(card==null)card=(t.getParent() instanceof View)?(View)t.getParent():null;if(card==null)return;
        GradientDrawable d=new GradientDrawable();d.setColor(Color.WHITE);d.setCornerRadius(dp(18));d.setStroke(dp(1),accent);card.setBackground(d);card.setPadding(dp(9),dp(8),dp(9),dp(8));
        t.setTextSize(10.5f);t.setGravity(Gravity.CENTER);t.setMaxLines(2);
    }

    private void renameMonthlyCards618(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String raw=String.valueOf(t.getText());String u=norm618(raw);
            if(u.contains("TAHSİL EDİLEN")&&!u.contains("BU AY TAHSİL EDİLEN"))t.setText(replacePrefix618(raw,"TAHSİL EDİLEN","Bu ay tahsil edilen"));
            if(u.contains("AY SONUNA KADAR GELECEK"))t.setText(replacePrefix618(raw,"AY SONUNA KADAR GELECEK","Bu ay sonuna kadar ödeme yapacak"));
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)renameMonthlyCards618(g.getChildAt(i));}
    }
    private String replacePrefix618(String raw,String old,String neo){
        String u=raw.toUpperCase(new Locale("tr","TR"));int p=u.indexOf(old);if(p<0)return raw;return raw.substring(0,p)+neo+raw.substring(p+old.length());
    }

    private void ensureActiveCountOnTarget618(LinearLayout box){
        TextView label=findText618(box,"AYLIK HEDEF","HEDEFLENEN CİRO");if(label==null)return;
        View card=nearestClickable618(label);if(card==null&&label.getParent() instanceof View)card=(View)label.getParent();if(!(card instanceof ViewGroup))return;
        if(findTagged618(card,"v618-active-count")!=null)return;
        int active=countStatus618("AKTİF");TextView s=new TextView(this);s.setTag("v618-active-count");s.setText(active+" aktif sporcu");s.setTextSize(10.5f);s.setTextColor(MUTED618);s.setGravity(Gravity.CENTER);s.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        ((ViewGroup)card).addView(s);
    }

    private void replaceExpectedWithCurrentStarts618(LinearLayout box){
        TextView expected=findText618(box,"BEKLENEN");if(expected==null)return;
        View card=nearestClickable618(expected);if(card==null)card=(expected.getParent() instanceof View)?(View)expected.getParent():null;
        int idx=topIndex618(box,card);removeFromParent618(card);
        View fresh=listCard618("Bu ay başlayan sporcular",queryStartsThisMonth618(),GOLD618);
        box.addView(fresh,Math.max(0,Math.min(idx,box.getChildCount())),new LinearLayout.LayoutParams(-1,-2));
    }

    private void pairDueWithPreviousStarts618(LinearLayout box){
        TextView due=findText618(box,"BU AY SONUNA KADAR ÖDEME YAPACAK","AY SONUNA KADAR GELECEK");if(due==null)return;
        View dueCard=nearestClickable618(due);if(dueCard==null)dueCard=(due.getParent() instanceof View)?(View)due.getParent():null;if(dueCard==null)return;
        int idx=topIndex618(box,dueCard);removeFromParent618(dueCard);
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.TOP);row.setPadding(0,dp(5),0,dp(5));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),0,dp(3),0);
        if(dueCard!=null){styleGenericCard618(dueCard,GOLD618);row.addView(dueCard,lp);}
        View prev=listCard618("Geçen ay başlayan sporcular",queryStartsPreviousMonth618(),Color.rgb(90,105,130));row.addView(prev,new LinearLayout.LayoutParams(0,-2,1f));
        box.addView(row,Math.max(0,Math.min(idx,box.getChildCount())));
    }

    private void appendNinetyDayCards618(LinearLayout box){
        if(findTagged618(box,"v618-90-row")!=null)return;
        LinearLayout row=new LinearLayout(this);row.setTag("v618-90-row");row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.TOP);row.setPadding(0,dp(12),0,dp(6));
        ArrayList<Person618> left=queryLeftLast90Days618();View leftCard=listCard618("Son 3 ay içinde bırakanlar",left,RED618);
        int started=countStartedLast90Days618();int leftCount=left.size();int net=started-leftCount;int accent=net>0?GREEN618:net<0?RED618:GOLD618;
        LinearLayout metric=new LinearLayout(this);metric.setOrientation(LinearLayout.VERTICAL);metric.setGravity(Gravity.CENTER);metric.setPadding(dp(10),dp(14),dp(10),dp(14));metric.setBackground(round618(Color.WHITE,accent,18));
        TextView title=text618("Son 3 ayda",10.5f,TEXT618,true);TextView num=text618((net>0?"+":"")+net,32f,accent,true);TextView detail=text618(started+" başlayan • "+leftCount+" bırakan",9.5f,MUTED618,false);metric.addView(title);metric.addView(num);metric.addView(detail);
        LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(0,-2,1f);lp1.setMargins(dp(3),0,dp(3),0);LinearLayout.LayoutParams lp2=new LinearLayout.LayoutParams(0,dp(142),1f);lp2.setMargins(dp(3),0,dp(3),0);
        row.addView(leftCard,lp1);row.addView(metric,lp2);box.addView(row);
    }

    private View listCard618(String title,ArrayList<Person618> people,int accent){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(10),dp(10),dp(10),dp(10));card.setBackground(round618(Color.WHITE,accent,18));
        TextView h=text618(title,10.5f,TEXT618,true);h.setGravity(Gravity.CENTER);h.setPadding(0,0,0,dp(5));card.addView(h);
        if(people.isEmpty()){TextView e=text618("Sporcu yok",10f,MUTED618,false);e.setGravity(Gravity.CENTER);card.addView(e);}else{
            for(Person618 p:people){TextView n=text618("• "+p.name,10f,TEXT618,false);n.setPadding(dp(3),dp(4),dp(3),dp(4));n.setMaxLines(2);n.setOnClickListener(v->showProfile(p.id));n.setClickable(true);card.addView(n);}
        }
        return card;
    }

    private ArrayList<Person618> queryStartsThisMonth618(){Calendar c=Calendar.getInstance();return queryStartRange618(firstDay618(c),firstDayNextMonth618(c));}
    private ArrayList<Person618> queryStartsPreviousMonth618(){Calendar c=Calendar.getInstance();c.add(Calendar.MONTH,-1);return queryStartRange618(firstDay618(c),firstDayNextMonth618(c));}
    private ArrayList<Person618> queryStartRange618(String from,String to){
        ArrayList<Person618> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND startDate>=? AND startDate<? ORDER BY startDate,name COLLATE NOCASE",new String[]{from,to});while(c.moveToNext())out.add(new Person618(c.getLong(0),c.getString(1)));c.close();return out;
    }
    private ArrayList<Person618> queryLeftLast90Days618(){
        String from=isoDate618(daysAgo618(90));String to=isoDate618(daysAgo618(-1));ArrayList<Person618> out=new ArrayList<>();
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM athletes WHERE status='BIRAKTI' AND TRIM(COALESCE(deletedAt,''))='' AND endDate>=? AND endDate<? ORDER BY endDate DESC,name COLLATE NOCASE",new String[]{from,to});while(c.moveToNext())out.add(new Person618(c.getLong(0),c.getString(1)));c.close();return out;
    }
    private int countStartedLast90Days618(){String from=isoDate618(daysAgo618(90));String to=isoDate618(daysAgo618(-1));Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND startDate>=? AND startDate<?",new String[]{from,to});int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}
    private int countStatus618(String status){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status=? AND TRIM(COALESCE(deletedAt,''))=''",new String[]{status});int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}

    private Calendar daysAgo618(int days){Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,-days);return c;}
    private String isoDate618(Calendar c){return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());}
    private String firstDay618(Calendar c){return String.format(Locale.US,"%04d-%02d-01",c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1);}
    private String firstDayNextMonth618(Calendar c){Calendar x=(Calendar)c.clone();x.set(Calendar.DAY_OF_MONTH,1);x.add(Calendar.MONTH,1);return firstDay618(x);}

    private void styleGenericCard618(View v,int accent){v.setBackground(round618(Color.WHITE,accent,18));v.setPadding(dp(9),dp(9),dp(9),dp(9));patchCardTypography618(v);}
    private GradientDrawable round618(int fill,int stroke,int radius){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(1),stroke);return d;}
    private TextView text618(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setGravity(Gravity.CENTER);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}

    private TextView findText618(View v,String... needles){if(v instanceof TextView){String u=norm618(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm618(n)))return (TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText618(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private View nearestClickable618(View v){View cur=v;while(cur!=null&&cur!=root){if(cur.hasOnClickListeners()||cur.isClickable())return cur;ViewParent p=cur.getParent();cur=p instanceof View?(View)p:null;}return null;}
    private int topIndex618(LinearLayout box,View v){if(v==null)return box.getChildCount();View cur=v;while(cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();int i=box.indexOfChild(cur);return i<0?box.getChildCount():i;}
    private void removeFromParent618(View v){if(v==null)return;ViewParent p=v.getParent();if(p instanceof ViewGroup){ViewGroup g=(ViewGroup)p;g.removeView(v);cleanupEmpty618(g);}}
    private void cleanupEmpty618(ViewGroup g){if(g==root)return;if(g.getChildCount()==0&&g.getParent() instanceof ViewGroup)((ViewGroup)g.getParent()).removeView(g);}
    private View findTagged618(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTagged618(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ScrollView findScroll618(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView r=findScroll618(g.getChildAt(i));if(r!=null)return r;}}return null;}
    private String norm618(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}

    static class Person618{final long id;final String name;Person618(long id,String name){this.id=id;this.name=name==null?"":name;}}
}
