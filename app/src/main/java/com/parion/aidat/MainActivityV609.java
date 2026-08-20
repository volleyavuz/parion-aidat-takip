package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.9 - cleaner profile periods, branded home header, readable cards and faster startup redraws. */
public class MainActivityV609 extends MainActivityV608 {
    private long lastHomeBuild609=0L;
    private boolean buildingProfile609=false;
    private String profileRestart609="";

    @Override void showHome(){
        long now=SystemClock.uptimeMillis();
        if("HOME".equals(page) && root!=null && root.getChildCount()>0 && now-lastHomeBuild609<3500L) return;
        lastHomeBuild609=now;
        super.showHome();
        patchHome609();
    }

    @Override void showProfile(long id){
        Cursor c=db.athlete(id);
        String restart="";
        if(c.moveToFirst()) restart=s609(c,"restartDate");
        c.close();
        buildingProfile609=isIso609(restart) && !isoAfterToday609(restart);
        profileRestart609=restart;
        try{ super.showProfile(id); }
        finally{ buildingProfile609=false; profileRestart609=""; }
    }

    /** During profile construction, the current/waiting cycle follows restart date once restart is effective. */
    @Override int anchorDay(String start){
        if(buildingProfile609 && isIso609(profileRestart609)) return day609(profileRestart609);
        return super.anchorDay(start);
    }

    /**
     * Hide periods for which the athlete is not responsible.
     * Before restart use the original registration-day cycle. From restart onward,
     * regenerate cycle labels using the restart day as the new anchor.
     */
    @Override void addCycleProfileRow(LinearLayout b,long id,int key,int ignoredAnchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int originalAnchor=day609(start);
        int useAnchor=originalAnchor;
        Calendar periodStart=cycleDate(key,originalAnchor);
        Calendar stop=iso609(end);
        Calendar resume=iso609(restart);

        if(stop!=null && !periodStart.before(stop)){
            if(resume==null) return;
            int restartKey=parseMonthKey(restart);
            if(key<restartKey) return;
            useAnchor=day609(restart);
            periodStart=cycleDate(key,useAnchor);
            if(periodStart.before(resume)) return;
        }

        int yr=key/100, mo=key%100;
        PayRec r=pays.get(key); if(r==null) r=new PayRec("",0);
        int expected=expectedFeeAt(id,yr,mo,r);
        String period=cycleDateLabel(key,useAnchor)+" – "+cycleDateLabel(shiftMonth(key,1),useAnchor);
        String status; int color; String detail;

        if(future){status="BEKLİYOR";color=Color.WHITE;detail=period+" • "+(expected>0?money(expected):"—");}
        else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=ORANGE;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?GREEN:ORANGE;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";}
        else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);detail=period+" • VERİ YOK";}
        else {status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}

        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));
        row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true));row.addView(tv(detail,12,Color.DKGRAY,false));
        if(!future&&yr==2026){final int mm=mo,fee=expected,amt=r.amount;final String mk=r.marker;row.setOnClickListener(v->editPayment(id,mm,fee,mk,amt));}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }

    private void patchHome609(){
        patchTopBrand609();
        ScrollView sv=findScroll609(root);if(sv==null)return;
        LinearLayout content=box(sv);
        removeLargeLegacyLogo609(content);
        moveAthletesFirst609(content);
        moveDeletedIntoScroll609(content);
        improveCardText609(content);
    }

    private void patchTopBrand609(){
        if(root==null||root.getChildCount()==0)return;
        View first=root.getChildAt(0);if(!(first instanceof LinearLayout))return;
        LinearLayout bar=(LinearLayout)first;bar.removeAllViews();bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(8),dp(5),dp(10),dp(5));bar.setBackgroundColor(Color.WHITE);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.parion_brand_mark);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);logo.setOnClickListener(v->showHome());bar.addView(logo,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);texts.setGravity(Gravity.CENTER_VERTICAL);
        TextView a=tv("PARİON SPOR KULÜBÜ",17,BLACK,true);a.setPadding(dp(5),0,0,0);
        TextView s=tv("SPORCU TAKİP SİSTEMİ",11,Color.DKGRAY,true);s.setPadding(dp(5),0,0,0);
        texts.addView(a);texts.addView(s);bar.addView(texts,new LinearLayout.LayoutParams(0,dp(58),1));
    }

    private void removeLargeLegacyLogo609(LinearLayout content){
        for(int i=content.getChildCount()-1;i>=0;i--){
            View v=content.getChildAt(i);
            if(v instanceof ImageView){content.removeViewAt(i);}
        }
    }

    private void moveAthletesFirst609(LinearLayout content){
        View athletes=findButton609(content,"SPORCULAR");if(athletes==null)return;
        ViewGroup p=(ViewGroup)athletes.getParent();if(p!=content)return;
        content.removeView(athletes);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(60));lp.setMargins(0,0,0,dp(8));content.addView(athletes,0,lp);
    }

    private void moveDeletedIntoScroll609(LinearLayout content){
        View deleted=findButton609(root,"SİLİNEN SPORCULAR");if(deleted==null)return;
        ViewParent pp=deleted.getParent();if(!(pp instanceof ViewGroup))return;ViewGroup p=(ViewGroup)pp;
        if(p==content)return;
        p.removeView(deleted);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));lp.setMargins(0,dp(8),0,dp(8));content.addView(deleted,lp);
    }

    private void improveCardText609(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String u=String.valueOf(t.getText()).toUpperCase(new Locale("tr","TR"));
            if(u.contains("TAHSİL")||u.contains("BEKLENEN")||u.contains("GECİKMİŞ")||u.contains("GELECEK")||u.contains("YAZIN")||u.contains("KIŞIN")||u.contains("FOTOĞRAFI")||u.contains("MALZEME")||u.contains("SPORCU")){
                t.setSingleLine(false);t.setMaxLines(4);t.setEllipsize(null);t.setGravity(Gravity.CENTER);t.setMinHeight(dp(34));if(t.getTextSize()/getResources().getDisplayMetrics().scaledDensity<11f)t.setTextSize(11f);
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)improveCardText609(g.getChildAt(i));}
    }

    private View findButton609(View v,String prefix){
        if(v instanceof Button){String s=String.valueOf(((Button)v).getText()).trim().toUpperCase(new Locale("tr","TR"));if(s.startsWith(prefix))return v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findButton609(g.getChildAt(i),prefix);if(r!=null)return r;}}
        return null;
    }
    private ScrollView findScroll609(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll609(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private boolean isIso609(String x){return x!=null&&x.matches("\\d{4}-\\d{2}-\\d{2}");}
    private int day609(String x){try{return isIso609(x)?Math.max(1,Math.min(31,Integer.parseInt(x.substring(8,10)))):1;}catch(Exception e){return 1;}}
    private Calendar iso609(String x){try{if(!isIso609(x))return null;Calendar c=Calendar.getInstance();c.clear();c.set(Integer.parseInt(x.substring(0,4)),Integer.parseInt(x.substring(5,7))-1,Integer.parseInt(x.substring(8,10)));return c;}catch(Exception e){return null;}}
    private boolean isoAfterToday609(String x){Calendar d=iso609(x);if(d==null)return true;Calendar n=Calendar.getInstance();n.set(Calendar.HOUR_OF_DAY,0);n.set(Calendar.MINUTE,0);n.set(Calendar.SECOND,0);n.set(Calendar.MILLISECOND,0);return d.after(n);}
    private String s609(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
