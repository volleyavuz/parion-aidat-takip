package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/**
 * v4.0.17 - designer dashboard refresh.
 * Reuses existing card Views/listeners so navigation/data behavior remains unchanged.
 */
public class MainActivityV617 extends MainActivityV616 {
    private static final int C_GREEN=Color.rgb(39,134,82);
    private static final int C_ORANGE=Color.rgb(205,132,44);
    private static final int C_RED=Color.rgb(196,63,63);
    private static final int C_GOLD=Color.rgb(205,156,34);
    private static final int C_BLUE=Color.rgb(55,105,160);
    private static final int C_PURPLE=Color.rgb(108,78,146);
    private static final int C_TEXT=Color.rgb(35,35,35);
    private static final int C_MUTED=Color.rgb(112,112,112);
    private static final int C_LINE=Color.rgb(229,229,229);
    private static final int C_SOFT=Color.rgb(250,250,250);

    @Override void showHome(){
        super.showHome();
        redesign617();
    }

    private void redesign617(){
        ScrollView sv=firstScroll617(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        View athletes=extract617(box,"SPORCULAR");
        View active=extract617(box,"AKTİF SPORCU");
        View paused=extract617(box,"ARA VERDİ");
        View target=extract617(box,"AYLIK HEDEF");
        View overdue=extract617(box,"GECİKMİŞ");
        View material=extract617(box,"MALZEME BORCU","ÖDENMEMİŞ MALZEME");
        View photo=extract617(box,"FOTOĞRAF EKSİK","FOTOĞRAFI OLMAYAN AKTİF SPORCULAR");
        View form=extract617(box,"KAYIT FORMU EKSİK","KAYIT FORMU OLMAYAN AKTİF SPORCULAR");
        View summer=extract617(box,"YAZIN ARANACAK");
        View winter=extract617(box,"KIŞIN ARANACAK");

        // Keep branding/logo and non-dashboard content in place, but put the redesigned dashboard before it.
        int insertAt=0;
        if(athletes!=null){
            stylePrimary617(athletes);
            box.addView(athletes,Math.min(insertAt++,box.getChildCount()));
        }

        if(active!=null||paused!=null){
            box.addView(section617("Genel Durum","Kulübün güncel sporcu görünümü"),Math.min(insertAt++,box.getChildCount()));
            LinearLayout row=row617();
            if(active!=null){decorate617(active,"Aktif Sporcu",C_GREEN,android.R.drawable.ic_menu_myplaces,false);addCell617(row,active);}
            if(paused!=null){decorate617(paused,"Ara Verdi",C_ORANGE,android.R.drawable.ic_media_pause,false);addCell617(row,paused);}
            box.addView(row,Math.min(insertAt++,box.getChildCount()));
        }

        if(target!=null||overdue!=null||material!=null){
            box.addView(section617("Finans","Aidat ve tahsilat özeti"),Math.min(insertAt++,box.getChildCount()));
            if(target!=null){
                decorate617(target,"Aylık Hedef",C_GOLD,android.R.drawable.ic_menu_view,true);
                enhanceTarget617(target);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));target.setLayoutParams(lp);
                box.addView(target,Math.min(insertAt++,box.getChildCount()));
            }
            LinearLayout row=row617();
            if(overdue!=null){decorate617(overdue,"Gecikmiş",C_RED,android.R.drawable.ic_dialog_alert,false);badge617(overdue,C_RED);addCell617(row,overdue);}
            if(material!=null){decorate617(material,"Malzeme Borcu",C_ORANGE,android.R.drawable.ic_menu_agenda,false);badge617(material,C_ORANGE);addCell617(row,material);}
            if(row.getChildCount()>0)box.addView(row,Math.min(insertAt++,box.getChildCount()));
        }

        if(photo!=null||form!=null||summer!=null||winter!=null){
            box.addView(section617("Takip Gerektirenler","Eksik kayıtlar ve aranacak sporcular"),Math.min(insertAt++,box.getChildCount()));
            ArrayList<View> cards=new ArrayList<>();
            if(photo!=null){decorate617(photo,"Fotoğraf Eksik",C_BLUE,android.R.drawable.ic_menu_camera,false);badge617(photo,C_BLUE);cards.add(photo);}
            if(form!=null){decorate617(form,"Kayıt Formu Eksik",C_PURPLE,android.R.drawable.ic_menu_edit,false);badge617(form,C_PURPLE);cards.add(form);}
            if(summer!=null){decorate617(summer,"Yazın Aranacak",C_GOLD,android.R.drawable.ic_menu_call,false);badge617(summer,C_GOLD);cards.add(summer);}
            if(winter!=null){decorate617(winter,"Kışın Aranacak",C_BLUE,android.R.drawable.ic_menu_call,false);badge617(winter,C_BLUE);cards.add(winter);}
            for(int i=0;i<cards.size();i+=2){
                LinearLayout row=row617();addCell617(row,cards.get(i));if(i+1<cards.size())addCell617(row,cards.get(i+1));
                box.addView(row,Math.min(insertAt++,box.getChildCount()));
            }
        }

        softenRemaining617(box);
    }

    private TextView section617(String title,String sub){
        TextView t=new TextView(this);
        t.setText(title+"\n"+sub);t.setTextColor(C_TEXT);t.setTextSize(15);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setPadding(dp(4),dp(18),dp(4),dp(8));t.setLineSpacing(dp(1),1.02f);
        return t;
    }

    private LinearLayout row617(){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.TOP);r.setPadding(0,0,0,dp(8));return r;
    }
    private void addCell617(LinearLayout row,View card){
        if(card.getParent() instanceof ViewGroup)((ViewGroup)card.getParent()).removeView(card);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(112),1f);lp.setMargins(dp(3),0,dp(3),0);row.addView(card,lp);
    }

    private void stylePrimary617(View v){
        if(v instanceof Button){
            Button b=(Button)v;b.setText("Sporcular");b.setAllCaps(false);b.setTextSize(16);b.setTextColor(Color.BLACK);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            b.setBackground(roundStroke617(Color.rgb(255,247,220),C_GOLD,14,1));b.setPadding(dp(14),dp(10),dp(14),dp(10));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.setMargins(0,dp(4),0,dp(8));b.setLayoutParams(lp);
        }else decorate617(v,"Sporcular",C_GOLD,android.R.drawable.ic_menu_myplaces,true);
    }

    private void decorate617(View card,String title,int accent,int iconRes,boolean wide){
        card.setBackground(roundStroke617(Color.WHITE,accent,14,1));card.setPadding(dp(10),dp(8),dp(10),dp(8));
        ViewGroup g=card instanceof ViewGroup?(ViewGroup)card:null;
        if(g!=null){
            normalizeTitles617(g,title);
            TextView titleView=findTitle617(g,title);
            if(titleView!=null){
                titleView.setText(title);titleView.setAllCaps(false);titleView.setTextSize(wide?13:12);titleView.setTextColor(C_TEXT);titleView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);titleView.setGravity(Gravity.CENTER);titleView.setMaxLines(2);
            }
            TextView number=findProminent617(g,titleView);
            if(number!=null){number.setTextSize(wide?30:28);number.setTextColor(accent);number.setTypeface(Typeface.DEFAULT,Typeface.BOLD);number.setGravity(Gravity.CENTER);}
            if(findTagIcon617(g)==null){
                ImageView icon=new ImageView(this);icon.setTag("v617-icon");icon.setImageResource(iconRes);icon.setColorFilter(accent);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(25),dp(25));ip.gravity=Gravity.CENTER_HORIZONTAL;ip.setMargins(0,0,0,dp(2));
                try{g.addView(icon,0,ip);}catch(Exception ignored){}
            }
        }
    }

    private void enhanceTarget617(View card){
        if(!(card instanceof LinearLayout))return;
        LinearLayout g=(LinearLayout)card;
        if(findTag617(g,"v617-progress")!=null)return;
        Calendar c=Calendar.getInstance();long paid=db.paidThisMonth(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1);
        long target=target617();int pct=target<=0?0:(int)Math.min(100,Math.round(paid*100.0/target));
        TextView sub=new TextView(this);sub.setTag("v617-progress-text");sub.setText("Bu ay tahsilat: "+money617(paid)+"  •  %"+pct);sub.setTextSize(11);sub.setTextColor(C_MUTED);sub.setGravity(Gravity.CENTER);sub.setPadding(dp(4),dp(3),dp(4),dp(3));g.addView(sub);
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setTag("v617-progress");p.setMax(100);p.setProgress(pct);p.getProgressDrawable().setColorFilter(C_GOLD,android.graphics.PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(7));pp.setMargins(dp(18),dp(2),dp(18),dp(2));g.addView(p,pp);
    }

    private long target617(){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(monthlyFee),0) FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))=''",null);long n=0;if(c.moveToFirst())n=c.getLong(0);c.close();return n;
    }
    private String money617(long n){return String.format(new Locale("tr","TR"),"₺%,d",n).replace(',','.');}

    private void badge617(View card,int accent){
        if(!(card instanceof ViewGroup))return;ViewGroup g=(ViewGroup)card;
        TextView n=findNumeric617(g);if(n==null)return;
        n.setBackground(roundStroke617(soft617(accent),accent,999,1));n.setTextColor(accent);n.setPadding(dp(8),dp(2),dp(8),dp(2));
    }

    private int soft617(int c){int r=Color.red(c),g=Color.green(c),b=Color.blue(c);return Color.rgb((r+255*5)/6,(g+255*5)/6,(b+255*5)/6);}
    private GradientDrawable roundStroke617(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}

    private void normalizeTitles617(View v,String wanted){
        if(v instanceof TextView){TextView t=(TextView)v;String u=norm617(String.valueOf(t.getText()));
            if(matches617(u,"AKTİF SPORCU","ARA VERDİ","AYLIK HEDEF","AYLIK HEDEF CİRO","GECİKMİŞ","MALZEME BORCU","ÖDENMEMİŞ MALZEME","FOTOĞRAF EKSİK","FOTOĞRAFI OLMAYAN AKTİF SPORCULAR","KAYIT FORMU EKSİK","KAYIT FORMU OLMAYAN AKTİF SPORCULAR","YAZIN ARANACAK","KIŞIN ARANACAK")){t.setText(wanted);}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)normalizeTitles617(g.getChildAt(i),wanted);}
    }

    private TextView findTitle617(View v,String title){
        if(v instanceof TextView && norm617(String.valueOf(((TextView)v).getText())).equals(norm617(title)))return (TextView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findTitle617(g.getChildAt(i),title);if(r!=null)return r;}}return null;
    }
    private TextView findNumeric617(View v){
        if(v instanceof TextView){String s=String.valueOf(((TextView)v).getText()).trim().replace(".","").replace("₺","").replace(" ","");if(s.matches("[0-9]+"))return (TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findNumeric617(g.getChildAt(i));if(r!=null)return r;}}return null;
    }
    private TextView findProminent617(View v,TextView title){
        ArrayList<TextView> a=new ArrayList<>();collectText617(v,a);TextView best=null;float size=-1;
        for(TextView t:a){if(t==title)continue;String s=String.valueOf(t.getText()).trim();if(s.isEmpty())continue;if(t.getTextSize()>size){best=t;size=t.getTextSize();}}
        return best;
    }
    private void collectText617(View v,List<TextView> out){if(v instanceof TextView)out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText617(g.getChildAt(i),out);}}
    private View findTagIcon617(View v){return findTag617(v,"v617-icon");}
    private View findTag617(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag617(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}

    private View extract617(LinearLayout box,String... needles){
        TextView label=findText617(box,needles);if(label==null)return null;
        View cur=label,best=label;
        while(cur.getParent() instanceof View && cur.getParent()!=box){cur=(View)cur.getParent();if(cur.hasOnClickListeners()||cur.isClickable())best=cur;}
        if(best==label){cur=label;while(cur.getParent() instanceof View && cur.getParent()!=box)cur=(View)cur.getParent();best=cur;}
        ViewParent p=best.getParent();if(p instanceof ViewGroup){ViewGroup pg=(ViewGroup)p;pg.removeView(best);cleanupEmpty617(box,pg);}return best;
    }
    private void cleanupEmpty617(LinearLayout box,ViewGroup p){if(p==box)return;if(p.getChildCount()==0&&p.getParent() instanceof ViewGroup)((ViewGroup)p.getParent()).removeView(p);}
    private TextView findText617(View v,String... needles){
        if(v instanceof TextView){String u=norm617(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm617(n)))return (TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText617(g.getChildAt(i),needles);if(r!=null)return r;}}return null;
    }
    private String norm617(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    private boolean matches617(String s,String... vals){for(String v:vals)if(s.equals(norm617(v)))return true;return false;}

    private ScrollView firstScroll617(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=firstScroll617(g.getChildAt(i));if(s!=null)return s;}}return null;}

    private void softenRemaining617(View v){
        if(v instanceof TextView){TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();if(s.equals(s.toUpperCase(new Locale("tr","TR")))&&s.length()>14&&!s.contains("PARİON")&&!s.contains("SON İŞLEMLER")){t.setAllCaps(false);}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)softenRemaining617(g.getChildAt(i));}
    }
}
