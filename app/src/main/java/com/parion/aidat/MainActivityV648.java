package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/** v4.0.48 - correct dashboard card extraction: move individual clickable cards, never whole rows. */
public class MainActivityV648 extends MainActivityV643 {
    private View deletedCard648;

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(this::patchSettings648);
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::organize648,900);
            root.postDelayed(this::organize648,1200);
        }
    }

    private void organize648(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll648(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        if("v648-organized".equals(box.getTag()))return;

        View active=detachCard648(box,"AKTİF SPORCU");
        View paused=detachCard648(box,"ARA VERDİ");
        View target=detachCard648(box,"AYLIK HEDEF");
        View overdue=detachCard648(box,"GECİKMİŞ");
        View material=detachCard648(box,"MALZEME BORCU","ÖDENMEMİŞ MALZEME");
        View starts=detachCard648(box,"BU AY BAŞLAYAN SPORCULAR");
        View previous=detachCard648(box,"GEÇEN AY BAŞLAYAN SPORCULAR");
        View leavers=detachCard648(box,"SON 3 AY İÇİNDE BIRAKANLAR");
        View net=detachCard648(box,"SON 3 AYDA");
        View photo=detachCard648(box,"FOTOĞRAF EKSİK");
        View form=detachCard648(box,"KAYIT FORMU EKSİK");
        View tshirt=detachCard648(box,"TİŞÖRT ALMAYAN");
        View summer=detachCard648(box,"YAZIN ARANACAK");
        View winter=detachCard648(box,"KIŞIN ARANACAK");
        View absent=detachCard648(box,"DEVAMSIZLAR");
        deletedCard648=detachCard648(box,"SİLİNEN SPORCULAR");

        // Remove old rows/headers after all known cards have been detached.
        for(int i=box.getChildCount()-1;i>=0;i--){
            View v=box.getChildAt(i);
            if(v instanceof LinearLayout && ((LinearLayout)v).getOrientation()==LinearLayout.HORIZONTAL){box.removeViewAt(i);continue;}
            if(v instanceof TextView){String n=norm648(String.valueOf(((TextView)v).getText()));if(n.startsWith("GENEL DURUM")||n.startsWith("FİNANS")||n.startsWith("SPORCU HAREKETLERİ")||n.startsWith("TAKİP GEREKTİRENLER"))box.removeViewAt(i);}
        }

        int at=0;
        at=section648(box,at,"GENEL DURUM","Kulübün güncel sporcu görünümü",active,paused);
        at=section648(box,at,"FİNANS","Aidat, borç ve tahsilat görünümü",target,overdue,material);
        at=section648(box,at,"SPORCU HAREKETLERİ","Yeni başlayan ve ayrılan sporcular",starts,previous,leavers,net);
        at=section648(box,at,"TAKİP GEREKTİRENLER","İşlem veya iletişim gerektiren sporcular",absent,photo,form,tshirt,summer,winter);
        box.setTag("v648-organized");
    }

    private View detachCard648(LinearLayout box,String... needles){
        TextView hit=findText648(box,needles);if(hit==null)return null;
        View card=nearestClickable648(hit,box);
        if(card==null) card=nearestCell648(hit,box);
        if(card==null)return null;
        ViewParent p=card.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(card);
        return card;
    }

    private View nearestClickable648(View v,ViewGroup stop){
        View cur=v,best=null;
        while(cur!=null&&cur!=stop){
            if(cur.isClickable()||cur.hasOnClickListeners())best=cur;
            ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;
        }
        return best;
    }

    private View nearestCell648(View v,ViewGroup stop){
        View cur=v;
        while(cur!=null&&cur!=stop){
            ViewParent p=cur.getParent();if(!(p instanceof ViewGroup))break;
            ViewGroup pg=(ViewGroup)p;
            if(pg instanceof LinearLayout && ((LinearLayout)pg).getOrientation()==LinearLayout.HORIZONTAL)return cur;
            cur=pg;
        }
        return null;
    }

    private int section648(LinearLayout box,int at,String title,String subtitle,View... cards){
        ArrayList<View> list=new ArrayList<>();for(View v:cards)if(v!=null&&!list.contains(v))list.add(v);if(list.isEmpty())return at;
        TextView h=new TextView(this);h.setText(title+"\n"+subtitle);h.setTextSize(13f);h.setTextColor(Color.rgb(28,28,28));h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setPadding(dp(4),dp(16),dp(4),dp(9));h.setLineSpacing(dp(1),1f);
        box.addView(h,Math.min(at++,box.getChildCount()),new LinearLayout.LayoutParams(-1,-2));
        for(int i=0;i<list.size();i+=2){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.TOP);row.setPadding(0,0,0,dp(10));
            addCard648(row,list.get(i));
            if(i+1<list.size())addCard648(row,list.get(i+1));else row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(1),1f));
            box.addView(row,Math.min(at++,box.getChildCount()));
        }
        return at;
    }

    private void addCard648(LinearLayout row,View card){
        if(card.getParent() instanceof ViewGroup)((ViewGroup)card.getParent()).removeView(card);
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(15));card.setBackground(bg);card.setElevation(dp(2));card.setMinimumHeight(dp(118));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);lp.setMargins(dp(4),0,dp(4),0);row.addView(card,lp);
        tuneText648(card);
    }

    private void tuneText648(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();if(s.isEmpty())return;String n=norm648(s);
            if(isTitle648(n)){t.setTextSize(11.5f);t.setTextColor(Color.rgb(48,48,48));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER);}
            else if(isValue648(s)){t.setTextSize(21f);t.setTextColor(Color.rgb(24,24,24));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER);}
            t.setLineSpacing(dp(1),1f);return;
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)tuneText648(g.getChildAt(i));}
    }

    private boolean isTitle648(String n){return n.contains("AKTİF SPORCU")||n.contains("ARA VERDİ")||n.contains("AYLIK HEDEF")||n.contains("GECİKMİŞ")||n.contains("MALZEME BORCU")||n.contains("ÖDENMEMİŞ MALZEME");}
    private boolean isValue648(String s){String x=s.replace("₺","").replace("TL","").replace(".","").replace(",","").replace(" ","").replace("+","").replace("-","");return x.matches("\\d+");}

    private void patchSettings648(){patchSettings648(root);}
    private void patchSettings648(View v){
        if(v==null)return;CharSequence d=v.getContentDescription();
        if(d!=null&&"Ayarlar".equalsIgnoreCase(d.toString())){v.setOnClickListener(x->{invokeSettings648(x);if(root!=null)root.postDelayed(this::injectDeleted648,70);});return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchSettings648(g.getChildAt(i));}
    }

    private void invokeSettings648(View anchor){try{Method m=MainActivityV639.class.getDeclaredMethod("showSettings639",View.class);m.setAccessible(true);m.invoke(this,anchor);}catch(Exception e){toast("Ayarlar açılamadı.");}}
    private void injectDeleted648(){
        try{
            Field f=MainActivityV639.class.getDeclaredField("popup639");f.setAccessible(true);Object o=f.get(this);if(!(o instanceof PopupWindow))return;PopupWindow p=(PopupWindow)o;View c=p.getContentView();if(!(c instanceof LinearLayout))return;LinearLayout box=(LinearLayout)c;
            for(int i=0;i<box.getChildCount();i++)if(box.getChildAt(i) instanceof TextView&&norm648(String.valueOf(((TextView)box.getChildAt(i)).getText())).contains("SİLİNEN SPORCULAR"))return;
            TextView item=new TextView(this);item.setText("SİLİNEN SPORCULAR");item.setTextSize(13);item.setTextColor(Color.rgb(28,28,28));item.setTypeface(Typeface.DEFAULT,Typeface.BOLD);item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(dp(14),0,dp(14),0);item.setOnClickListener(v->{try{p.dismiss();}catch(Exception ignored){}if(deletedCard648!=null&&deletedCard648.hasOnClickListeners())deletedCard648.performClick();else toast("Silinen sporcular ekranı şu anda açılamadı.");});
            box.addView(item,Math.min(2,box.getChildCount()),new LinearLayout.LayoutParams(dp(234),dp(48)));
        }catch(Exception ignored){}
    }

    private TextView findText648(View v,String... needles){
        if(v instanceof TextView){String u=norm648(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm648(n)))return (TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText648(g.getChildAt(i),needles);if(r!=null)return r;}}
        return null;
    }
    private ScrollView findScroll648(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll648(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm648(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
