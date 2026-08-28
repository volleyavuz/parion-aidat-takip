package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.Locale;

/** v4.2.4 - repair Settings -> Updates navigation without touching sync/data logic. */
public class MainActivityV729 extends MainActivityV728 {
    private static final int GOLD729=Color.rgb(205,156,34);

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null){
            root.post(()->hookUpdates729(root));
            root.postDelayed(()->hookUpdates729(root),180);
        }
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.postDelayed(()->hookUpdates729(root),180);
    }

    private void hookUpdates729(View v){
        if(v==null)return;
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String s=norm729(String.valueOf(t.getText()));
            if((v instanceof Button || v.isClickable() || v.hasOnClickListeners()) &&
               (s.equals("GÜNCELLEMELER") || s.contains("GÜNCELLEMELER"))){
                v.setClickable(true);
                v.setOnClickListener(x->showUpdates729());
            }
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)hookUpdates729(g.getChildAt(i));
        }
    }

    private void showUpdates729(){
        page="UPDATES_729";
        currentAthlete=-1;
        base("GÜNCELLEMELER",true);
        ScrollView sv=scroll();
        LinearLayout b=box(sv);
        b.setPadding(dp(14),dp(16),dp(14),dp(28));

        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18),dp(18),dp(18),dp(18));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),Color.rgb(224,202,142));
        card.setBackground(bg);card.setElevation(dp(2));

        TextView icon=tv("↻",34,GOLD729,true);icon.setGravity(Gravity.CENTER);card.addView(icon,new LinearLayout.LayoutParams(-1,-2));
        TextView title=tv("PARİON SPORCU TAKİP SİSTEMİ",16,Color.rgb(32,32,32),true);title.setGravity(Gravity.CENTER);title.setPadding(0,dp(8),0,dp(4));card.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView ver=tv("Mevcut sürüm: 4.2.4",14,Color.rgb(55,55,55),true);ver.setGravity(Gravity.CENTER);card.addView(ver,new LinearLayout.LayoutParams(-1,-2));
        TextView state=tv("Güncelleme sayfası aktif",12,Color.rgb(95,95,95),false);state.setGravity(Gravity.CENTER);state.setPadding(0,dp(5),0,0);card.addView(state,new LinearLayout.LayoutParams(-1,-2));
        b.addView(card,new LinearLayout.LayoutParams(-1,-2));

        TextView h=tv("BU SÜRÜMDE",13,Color.rgb(55,55,55),true);h.setPadding(dp(2),dp(20),0,dp(8));b.addView(h,new LinearLayout.LayoutParams(-1,-2));
        b.addView(info729("• Ayarlar → Güncellemeler navigasyonu onarıldı."));
        b.addView(info729("• v4.2.3 Buluttan Temiz Geri Yükle korunuyor."));
        b.addView(info729("• Güvenli çoklu cihaz senkronizasyonu ve çakışma koruması korunuyor."));
        b.addView(info729("• Sporcu, ödeme ve mevcut yerel/bulut verilerine bu düzenlemede müdahale edilmedi."));

        TextView note=tv("Yeni bir APK sürümü hazırlandığında bu sayfa sürüm kontrolü ve güncelleme bilgileri için kullanılabilir.",12,Color.DKGRAY,false);
        note.setPadding(dp(4),dp(18),dp(4),dp(8));b.addView(note,new LinearLayout.LayoutParams(-1,-2));
    }

    private TextView info729(String s){TextView t=tv(s,12.5f,Color.rgb(55,55,55),false);t.setPadding(dp(4),dp(7),dp(4),dp(7));return t;}

    @Override void goBack(){
        if("UPDATES_729".equals(page)){showSettings729();return;}
        super.goBack();
    }

    private void showSettings729(){
        // Existing settings page stays authoritative. Opening it through the inherited settings nav
        // is safer than recreating/changing any settings functionality here.
        showSettings();
        if(root!=null)root.post(()->hookUpdates729(root));
    }

    private String norm729(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
