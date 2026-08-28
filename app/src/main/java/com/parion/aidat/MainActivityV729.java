package com.parion.aidat;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;

/** v4.2.4 - repair Settings -> Updates navigation while preserving all current settings actions. */
public class MainActivityV729 extends MainActivityV728 {
    private static final int GOLD729=Color.rgb(205,156,34);
    private PopupWindow settings729;

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null){root.post(()->patchSettingsIcon729(root));root.postDelayed(()->patchSettingsIcon729(root),180);}
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null){root.post(()->patchSettingsIcon729(root));root.postDelayed(()->patchSettingsIcon729(root),180);}
    }

    private void patchSettingsIcon729(View v){
        if(v==null)return;
        CharSequence d=v.getContentDescription();
        if(d!=null&&"Ayarlar".equalsIgnoreCase(d.toString())){v.setOnClickListener(this::showSettings729);return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchSettingsIcon729(g.getChildAt(i));}
    }

    private void showSettings729(View anchor){
        dismissSettings729();
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(8),dp(8),dp(8),dp(8));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(14));bg.setStroke(dp(1),Color.rgb(215,205,175));p.setBackground(bg);

        addItem729(p,"GRUPLARI DÜZENLE",()->invokeNoArg729(MainActivityV625.class,"showGroups625"));
        addItem729(p,"YOKLAMA AYARLARI",()->invokeNoArg729(MainActivityV630.class,"showAttendanceSettings630"));
        addItem729(p,"SİLİNEN SPORCULAR",()->invokeNoArg729(MainActivityV661.class,"showDeleted661"));
        addItem729(p,"SENKRONİZASYON",this::showCloudMenu);
        addItem729(p,"GÜNCELLEMELER",this::showUpdates729);
        addDivider729(p);
        addItem729(p,"OTURUMU KAPAT",this::confirmLogout729);
        addItem729(p,"ÇIKIŞ",this::finishAffinity);

        settings729=new PopupWindow(p,dp(258),WindowManager.LayoutParams.WRAP_CONTENT,true);
        settings729.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));settings729.setOutsideTouchable(true);settings729.setElevation(dp(8));
        int[] loc=new int[2];anchor.getLocationOnScreen(loc);p.measure(View.MeasureSpec.makeMeasureSpec(dp(258),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        int x=Math.max(dp(8),getResources().getDisplayMetrics().widthPixels-dp(270));int y=Math.max(dp(8),loc[1]-p.getMeasuredHeight()-dp(8));
        settings729.showAtLocation(root,Gravity.TOP|Gravity.START,x,y);
    }

    private void showUpdates729(){
        page="UPDATES_729";currentAthlete=-1;base("GÜNCELLEMELER",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(14),dp(16),dp(14),dp(28));
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(18),dp(18),dp(18),dp(18));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),Color.rgb(224,202,142));card.setBackground(bg);card.setElevation(dp(2));
        TextView icon=tv("↻",34,GOLD729,true);icon.setGravity(Gravity.CENTER);card.addView(icon,new LinearLayout.LayoutParams(-1,-2));
        TextView title=tv("PARİON SPORCU TAKİP SİSTEMİ",16,Color.rgb(32,32,32),true);title.setGravity(Gravity.CENTER);title.setPadding(0,dp(8),0,dp(4));card.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView ver=tv("Mevcut sürüm: 4.2.4",14,Color.rgb(55,55,55),true);ver.setGravity(Gravity.CENTER);card.addView(ver,new LinearLayout.LayoutParams(-1,-2));
        TextView state=tv("Güncelleme sayfası aktif",12,Color.rgb(95,95,95),false);state.setGravity(Gravity.CENTER);state.setPadding(0,dp(5),0,0);card.addView(state,new LinearLayout.LayoutParams(-1,-2));
        b.addView(card,new LinearLayout.LayoutParams(-1,-2));
        TextView h=tv("BU SÜRÜMDE",13,Color.rgb(55,55,55),true);h.setPadding(dp(2),dp(20),0,dp(8));b.addView(h,new LinearLayout.LayoutParams(-1,-2));
        b.addView(info729("• Ayarlar → Güncellemeler navigasyonu onarıldı."));
        b.addView(info729("• Güvenli Senkronizasyon ve Buluttan Temiz Geri Yükle korunuyor."));
        b.addView(info729("• Eski snapshot tabanlı güncelleme/senkronizasyon dialogu artık kullanılmıyor."));
        b.addView(info729("• Sporcu, ödeme ve bulut verilerine bu düzenlemede müdahale edilmedi."));
        TextView note=tv("Yeni APK sürümleri için sürüm bilgileri bu sayfada gösterilecek.",12,Color.DKGRAY,false);note.setPadding(dp(4),dp(18),dp(4),dp(8));b.addView(note,new LinearLayout.LayoutParams(-1,-2));
    }

    private void confirmLogout729(){
        new AlertDialog.Builder(this).setTitle("OTURUMU KAPAT").setMessage("Bulut oturumu kapatılsın mı? Yerel veriler cihazda kalır.")
            .setPositiveButton("OTURUMU KAPAT",(d,w)->{if(cloudPrefs!=null)cloudPrefs.edit().clear().apply();showLogin();})
            .setNegativeButton("VAZGEÇ",null).show();
    }

    private void addItem729(LinearLayout p,String text,Runnable action){TextView t=new TextView(this);t.setText(text);t.setTextSize(13);t.setTextColor(Color.rgb(28,28,28));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(14),0,dp(14),0);t.setClickable(true);t.setOnClickListener(v->{dismissSettings729();action.run();});p.addView(t,new LinearLayout.LayoutParams(dp(242),dp(48)));}
    private void addDivider729(LinearLayout p){View d=new View(this);d.setBackgroundColor(Color.rgb(232,228,216));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.setMargins(dp(8),dp(3),dp(8),dp(3));p.addView(d,lp);}
    private TextView info729(String s){TextView t=tv(s,12.5f,Color.rgb(55,55,55),false);t.setPadding(dp(4),dp(7),dp(4),dp(7));return t;}
    private void invokeNoArg729(Class<?> cls,String name){try{Method m=cls.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("İşlem açılamadı.");}}

    @Override void goBack(){
        if("UPDATES_729".equals(page)){showHome();root.postDelayed(()->{View s=findSettings729(root);if(s!=null)showSettings729(s);},120);return;}
        super.goBack();
    }

    private View findSettings729(View v){if(v==null)return null;CharSequence d=v.getContentDescription();if(d!=null&&"Ayarlar".equalsIgnoreCase(d.toString()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findSettings729(g.getChildAt(i));if(x!=null)return x;}}return null;}
    private void dismissSettings729(){if(settings729!=null){try{settings729.dismiss();}catch(Exception ignored){}settings729=null;}}
    @Override protected void onDestroy(){dismissSettings729();super.onDestroy();}
}
