package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;
import java.util.*;

/** v4.1.42 - definitive finance page with all seven finance actions; remove stray dashboard early-payment card. */
public class MainActivityV719 extends MainActivityV718 {
    private static final int GOLD=Color.rgb(205,156,34);
    private static final int TEXT=Color.rgb(35,35,35);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::patchFinanceEntry719,12000);
            root.postDelayed(this::patchFinanceEntry719,12800);
        }
    }

    private void patchFinanceEntry719(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        removeDashboardEarly719(root);
        View entry=findTag719(root,"v718-finance-entry");
        if(entry!=null){entry.setClickable(true);entry.setOnClickListener(v->showFinancePage719());}
    }

    private void removeDashboardEarly719(View v){
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=g.getChildCount()-1;i>=0;i--){
                View c=g.getChildAt(i);
                if("v711-early-payment".equals(c.getTag())){g.removeViewAt(i);continue;}
                removeDashboardEarly719(c);
            }
        }
    }

    private void showFinancePage719(){
        page="FINANCE_719";
        base("FİNANS",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(10),dp(10),dp(10),dp(28));
        TextView intro=text719("Aidat, tahsilat, vade, borç ve ödeme işlemleri",12f,Color.rgb(90,90,90),false);intro.setPadding(dp(4),0,dp(4),dp(12));b.addView(intro);
        addFinanceCard719(b,"AYLIK HEDEF","Aktif sporcuların aylık aidat toplamı",GOLD,android.R.drawable.ic_menu_view,()->invoke719(MainActivityV650.class,"showTarget650"));
        addFinanceCard719(b,"BU AYKİ TAHSİLAT","Son 6 ayı filtreleyerek tahsilatları görüntüle",Color.rgb(39,134,82),android.R.drawable.ic_menu_save,()->invoke719(MainActivityV705.class,"showCollections705",new Class[]{int.class},new Object[]{0}));
        addFinanceCard719(b,"ERKEN ÖDEME GİR","Vadesi gelmemiş aylar için aidat kaydı",Color.rgb(72,103,132),android.R.drawable.ic_input_add,()->invoke719(MainActivityV711.class,"showEarlyPayments711"));
        addFinanceCard719(b,"ÖDEME VADESİ GELENLER","Vadesi gelmiş ve ödemesi beklenen sporcular",Color.rgb(205,132,44),android.R.drawable.ic_menu_recent_history,()->invoke719(MainActivityV668.class,"showDueNow668"));
        addFinanceCard719(b,"GECİKMİŞ","Vadesi geçmiş aidat borçları",Color.rgb(196,63,63),android.R.drawable.ic_dialog_alert,()->invoke719(MainActivityV650.class,"showOverdue650"));
        addFinanceCard719(b,"MALZEME BORCU","Ödenmemiş malzeme bakiyeleri",Color.rgb(205,132,44),android.R.drawable.ic_menu_agenda,()->invoke719(MainActivityV650.class,"showMaterial650"));
        addFinanceCard719(b,"SON ÖDEMELER","Kaydedilme zamanına göre son 20 ödeme",Color.rgb(39,134,82),android.R.drawable.ic_menu_recent_history,()->invoke719(MainActivityV704.class,"showRecentPayments704"));
    }

    private void addFinanceCard719(LinearLayout b,String title,String sub,int accent,int iconRes,Runnable action){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.HORIZONTAL);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(14),dp(12),dp(14),dp(12));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(15));bg.setStroke(dp(1),Color.rgb(230,230,230));c.setBackground(bg);c.setElevation(dp(1));
        ImageView icon=new ImageView(this);icon.setImageResource(iconRes);icon.setColorFilter(accent);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);c.addView(icon,new LinearLayout.LayoutParams(dp(30),dp(30)));
        LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);texts.setPadding(dp(13),0,dp(8),0);
        TextView h=text719(title,13.5f,TEXT,true);texts.addView(h);TextView s=text719(sub,10.5f,Color.rgb(100,100,100),false);s.setMaxLines(2);texts.addView(s);c.addView(texts,new LinearLayout.LayoutParams(0,-2,1f));
        TextView arrow=text719("›",27f,accent,false);arrow.setGravity(Gravity.CENTER);c.addView(arrow,new LinearLayout.LayoutParams(dp(28),-1));
        c.setClickable(true);c.setOnClickListener(v->action.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(dp(2),0,dp(2),dp(9));b.addView(c,lp);
    }

    private void invoke719(Class<?> owner,String name){invoke719(owner,name,new Class<?>[0],new Object[0]);}
    private void invoke719(Class<?> owner,String name,Class<?>[] sig,Object[] args){
        try{Method m=owner.getDeclaredMethod(name,sig);m.setAccessible(true);m.invoke(this,args);}catch(Exception e){Toast.makeText(this,"Finans işlemi açılamadı: "+name,Toast.LENGTH_LONG).show();}
    }

    @Override void goBack(){if("FINANCE_719".equals(page)){showHome();return;}super.goBack();}
    private TextView text719(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private View findTag719(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag719(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
}
