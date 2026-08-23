package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/** v4.0.44 - semantic dashboard grouping; deleted athletes moved from dashboard to Settings. */
public class MainActivityV644 extends MainActivityV643 {
    private View deletedAthletesCard644;

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null) root.post(this::patchSettingsButton644);
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null) root.postDelayed(this::organize644,1230);
    }

    private void organize644(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll644(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        if("v644-organized".equals(box.getTag()))return;

        TextView deleted=findText644(box,"SİLİNEN SPORCULAR");
        if(deleted!=null){
            View top=top644(box,deleted);
            if(top!=null){deletedAthletesCard644=top;box.removeView(top);}
        }

        View active=take644(box,"AKTİF SPORCU");
        View paused=take644(box,"ARA VERDİ");
        View target=take644(box,"AYLIK HEDEF");
        View overdue=take644(box,"GECİKMİŞ");
        View material=take644(box,"MALZEME BORCU","ÖDENMEMİŞ MALZEME");
        View starts=take644(box,"BU AY BAŞLAYAN SPORCULAR");
        View previous=take644(box,"GEÇEN AY BAŞLAYAN SPORCULAR");
        View leavers=take644(box,"SON 3 AY İÇİNDE BIRAKANLAR");
        View net=take644(box,"SON 3 AYDA");
        View photo=take644(box,"FOTOĞRAF EKSİK");
        View form=take644(box,"KAYIT FORMU EKSİK");
        View tshirt=take644(box,"TİŞÖRT ALMAYAN");
        View summer=take644(box,"YAZIN ARANACAK");
        View winter=take644(box,"KIŞIN ARANACAK");
        View absent=take644(box,"DEVAMSIZLAR");

        removeSection644(box,"GENEL DURUM");removeSection644(box,"FİNANS");removeSection644(box,"TAKİP GEREKTİRENLER");removeSection644(box,"SPORCU HAREKETLERİ");

        int at=0;
        at=sectionAndCards644(box,at,"GENEL DURUM","Kulübün güncel sporcu görünümü",active,paused);
        at=sectionAndCards644(box,at,"FİNANS","Aidat, borç ve tahsilat görünümü",target,overdue,material);
        at=sectionAndCards644(box,at,"SPORCU HAREKETLERİ","Yeni başlayan ve ayrılan sporcular",starts,previous,leavers,net);
        at=sectionAndCards644(box,at,"TAKİP GEREKTİRENLER","İşlem veya iletişim gerektiren sporcular",absent,photo,form,tshirt,summer,winter);
        box.setTag("v644-organized");
    }

    private void patchSettingsButton644(){patchSettingsButton644(root);}
    private void patchSettingsButton644(View v){
        if(v==null)return;
        CharSequence d=v.getContentDescription();
        if(d!=null&&"Ayarlar".equalsIgnoreCase(d.toString())){
            v.setOnClickListener(x->{invokeSettings639(x);if(root!=null)root.postDelayed(this::injectDeletedSettings644,60);});
            return;
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchSettingsButton644(g.getChildAt(i));}
    }

    private void invokeSettings639(View anchor){
        try{Method m=MainActivityV639.class.getDeclaredMethod("showSettings639",View.class);m.setAccessible(true);m.invoke(this,anchor);}catch(Exception e){toast("Ayarlar açılamadı.");}
    }

    private void injectDeletedSettings644(){
        try{
            Field f=MainActivityV639.class.getDeclaredField("popup639");f.setAccessible(true);Object o=f.get(this);if(!(o instanceof PopupWindow))return;
            PopupWindow p=(PopupWindow)o;View c=p.getContentView();if(!(c instanceof LinearLayout))return;LinearLayout box=(LinearLayout)c;
            for(int i=0;i<box.getChildCount();i++)if(box.getChildAt(i) instanceof TextView&&norm644(String.valueOf(((TextView)box.getChildAt(i)).getText())).contains("SİLİNEN SPORCULAR"))return;
            TextView item=new TextView(this);item.setText("SİLİNEN SPORCULAR");item.setTextSize(13);item.setTextColor(Color.rgb(28,28,28));item.setTypeface(Typeface.DEFAULT,Typeface.BOLD);item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(dp(14),0,dp(14),0);
            item.setOnClickListener(v->{try{p.dismiss();}catch(Exception ignored){}openDeletedAthletes644();});
            int insert=Math.min(2,box.getChildCount());box.addView(item,insert,new LinearLayout.LayoutParams(dp(234),dp(48)));
        }catch(Exception ignored){}
    }

    private void openDeletedAthletes644(){
        if(deletedAthletesCard644!=null&&deletedAthletesCard644.hasOnClickListeners()){deletedAthletesCard644.performClick();return;}
        toast("Silinen sporcular ekranı şu anda açılamadı.");
    }

    private int sectionAndCards644(LinearLayout box,int at,String title,String sub,View... cards){
        ArrayList<View> list=new ArrayList<>();for(View v:cards)if(v!=null)list.add(v);if(list.isEmpty())return at;
        TextView h=new TextView(this);h.setText(title+"\n"+sub);h.setTextSize(12.5f);h.setTextColor(Color.rgb(28,28,28));h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setPadding(dp(4),dp(16),dp(4),dp(7));
        box.addView(h,Math.min(at++,box.getChildCount()),new LinearLayout.LayoutParams(-1,-2));
        for(int i=0;i<list.size();i+=2){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.TOP);row.setPadding(0,0,0,dp(7));
            addCell644(row,list.get(i));if(i+1<list.size())addCell644(row,list.get(i+1));else row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(1),1f));
            box.addView(row,Math.min(at++,box.getChildCount()));
        }
        return at;
    }
    private void addCell644(LinearLayout row,View v){if(v.getParent() instanceof ViewGroup)((ViewGroup)v.getParent()).removeView(v);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);lp.setMargins(dp(3),0,dp(3),0);row.addView(v,lp);}

    private View take644(LinearLayout box,String... needles){TextView t=findText644(box,needles);if(t==null)return null;View top=top644(box,t);if(top==null)return null;box.removeView(top);return top;}
    private View top644(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private void removeSection644(LinearLayout box,String title){for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(v instanceof TextView&&norm644(String.valueOf(((TextView)v).getText())).startsWith(norm644(title)))box.removeViewAt(i);}}
    private TextView findText644(View v,String... needles){if(v instanceof TextView){String u=norm644(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm644(n)))return (TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText644(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private ScrollView findScroll644(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll644(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm644(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
