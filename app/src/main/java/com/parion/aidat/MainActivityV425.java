package com.parion.aidat;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivityV425 extends MainActivityV424 {
    private final LinkedHashMap<Long,Fix425> paymentFix425=new LinkedHashMap<>();
    private volatile boolean paymentFixLoading425=false;

    static class Fix425{
        long id;String name;int dup;
        Fix425(long i,String n,int d){id=i;name=n;dup=d;}
    }

    @Override void showHome(){
        super.showHome();
        regroupHomeCards425();
        addPaymentFixCard425();
        refreshPaymentFix425();
    }

    private void regroupHomeCards425(){
        ScrollView sv=findScroll425(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        View material=findCard425(root,"ÖDENMEMİŞ MALZEME");
        View photo=findCard425(root,"FOTOĞRAFI OLMAYAN AKTİF SPORCULAR");
        View form=findCard425(root,"KAYIT FORMU OLMAYAN AKTİF SPORCULAR");
        ArrayList<View> cards=new ArrayList<>();if(material!=null)cards.add(material);if(photo!=null&&photo!=material)cards.add(photo);if(form!=null&&form!=photo&&form!=material)cards.add(form);
        if(cards.isEmpty())return;
        for(View c:cards){ViewParent p=c.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(c);}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER);
        for(View c:cards){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(118),1f);lp.setMargins(dp(3),dp(4),dp(3),dp(4));row.addView(c,lp);shrinkCard425(c);}
        int at=findDashboardEnd425(box);box.addView(row,Math.min(at,box.getChildCount()),new LinearLayout.LayoutParams(-1,dp(126)));
    }

    private int findDashboardEnd425(LinearLayout box){
        for(int i=0;i<box.getChildCount();i++){View v=box.getChildAt(i);if(containsText425(v,"GECİKMİŞ")||containsText425(v,"AY SONUNA KADAR"))return i+1;}
        return Math.min(3,box.getChildCount());
    }
    private void shrinkCard425(View v){
        if(v instanceof TextView){TextView t=(TextView)v;t.setTextSize(Math.min(t.getTextSize()/getResources().getDisplayMetrics().scaledDensity,12f));t.setGravity(Gravity.CENTER);t.setPadding(dp(3),dp(3),dp(3),dp(3));}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)shrinkCard425(g.getChildAt(i));}
    }
    private View findCard425(View v,String term){
        if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR")).contains(term)){
            View p=(View)v.getParent();return p==null?v:p;
        }
        if(v instanceof Button&&String.valueOf(((Button)v).getText()).toUpperCase(new Locale("tr","TR")).contains(term)){
            View p=(View)v.getParent();return p==null?v:p;
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findCard425(g.getChildAt(i),term);if(x!=null)return x;}}
        return null;
    }
    private boolean containsText425(View v,String term){
        if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).contains(term))return true;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText425(g.getChildAt(i),term))return true;}
        return false;
    }
    private ScrollView findScroll425(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll425(g.getChildAt(i));if(s!=null)return s;}}return null;}

    private void addPaymentFixCard425(){
        ScrollView sv=findScroll425(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;LinearLayout box=(LinearLayout)sv.getChildAt(0);
        View old=findCard425(root,"DÜZELTİLECEK ÖDEMELER");if(old!=null){ViewParent p=old.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(old);}
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(8),dp(8),dp(8),dp(8));card.setBackground(round(Color.rgb(255,238,210),12));
        TextView n=tv(paymentFixLoading425?"…":String.valueOf(paymentFix425.size()),24,RED,true);n.setGravity(Gravity.CENTER);card.addView(n,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView l=tv("DÜZELTİLECEK ÖDEMELER",12,Color.DKGRAY,true);l.setGravity(Gravity.CENTER);card.addView(l,new LinearLayout.LayoutParams(-1,dp(38)));
        card.setOnClickListener(v->showPaymentFix425());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(94));lp.setMargins(0,dp(6),0,dp(8));box.addView(card,lp);
    }

    private void refreshPaymentFix425(){
        if(paymentFixLoading425||cloudPrefs==null)return;String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;paymentFixLoading425=true;
        new Thread(()->{try{
            HttpResult r=request("GET",SUPABASE_URL+"/rest/v1/payment_duplicate_athletes?select=legacy_id,display_name,duplicate_excess&order=display_name.asc",null,token);
            if(r.code==401&&refreshSession())r=request("GET",SUPABASE_URL+"/rest/v1/payment_duplicate_athletes?select=legacy_id,display_name,duplicate_excess&order=display_name.asc",null,cloudPrefs.getString("access_token",""));
            if(r.code>=200&&r.code<300){JSONArray a=new JSONArray(r.body);paymentFix425.clear();for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)paymentFix425.put(id,new Fix425(id,o.optString("display_name","SPORCU"),o.optInt("duplicate_excess",0)));}}
        }catch(Exception ignored){}finally{paymentFixLoading425=false;runOnUiThread(()->{if("HOME".equals(page)){addPaymentFixCard425();}});}},"payment-fix-425").start();
    }

    private void showPaymentFix425(){
        page="PAYMENT_FIX_425";base("DÜZELTİLECEK ÖDEMELER",true);TextView info=tv("Bulutta aynı sporcu ve aynı ay için birden fazla eski ödeme satırı bulunan sporcular. Sporcuya dokunarak profilindeki ödeme dönemlerini kontrol edip düzenleyebilirsiniz.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);if(paymentFix425.isEmpty()){b.addView(tv(paymentFixLoading425?"LİSTE YÜKLENİYOR...":"DÜZELTİLECEK ÖDEME KAYDI BULUNMUYOR.",14,Color.DKGRAY,true));return;}
        for(Fix425 x:paymentFix425.values()){
            LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(10),dp(8),dp(10),dp(8));r.setBackground(round(Color.WHITE,10));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv(x.name,15,BLACK,true));t.addView(tv("Mükerrer eski ödeme satırı: "+x.dup,12,Color.DKGRAY,false));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));TextView a=tv("DÜZENLE ›",12,RED,true);a.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);r.addView(a,new LinearLayout.LayoutParams(dp(95),-1));r.setOnClickListener(v->showProfile(x.id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(r,lp);
        }
    }

    @Override void goBack(){if("PAYMENT_FIX_425".equals(page)){showHome();return;}super.goBack();}
}
