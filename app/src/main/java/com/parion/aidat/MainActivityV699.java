package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;
import java.util.*;

/** v4.0.99 - profile photo zoom, clear group action, pale paid-month color. */
public class MainActivityV699 extends MainActivityV698 {
    private static final int PAID_PALE_699 = Color.rgb(232,247,236); // #E8F7EC

    @Override void showProfile(long id){
        super.showProfile(id);
        if(root!=null) root.post(()->hookProfilePhoto699(root));
    }

    private void hookProfilePhoto699(View v){
        ImageView photo=findProfilePhoto699(v);
        if(photo==null)return;
        photo.setClickable(true);
        photo.setFocusable(true);
        photo.setContentDescription("Profil fotoğrafını büyüt");
        photo.setOnClickListener(x->showProfilePhoto699(photo));
    }

    private ImageView findProfilePhoto699(View v){
        if(v instanceof ImageView){
            ImageView iv=(ImageView)v;
            if(iv.getScaleType()==ImageView.ScaleType.CENTER_CROP && iv.getDrawable()!=null) return iv;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                ImageView r=findProfilePhoto699(g.getChildAt(i));
                if(r!=null)return r;
            }
        }
        return null;
    }

    private void showProfilePhoto699(ImageView source){
        Drawable d=source.getDrawable();
        if(d==null){toast("Profil fotoğrafı bulunamadı.");return;}
        Dialog dialog=new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        FrameLayout frame=new FrameLayout(this);frame.setBackgroundColor(Color.BLACK);
        ZoomImage403 zoom=new ZoomImage403(this);zoom.setImageDrawable(d);
        frame.addView(zoom,new FrameLayout.LayoutParams(-1,-1));
        TextView close=new TextView(this);close.setText("✕");close.setTextColor(Color.WHITE);close.setTextSize(28);close.setGravity(Gravity.CENTER);close.setBackgroundColor(Color.argb(110,0,0,0));close.setOnClickListener(v->dialog.dismiss());
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(dp(54),dp(54),Gravity.TOP|Gravity.END);cp.setMargins(0,dp(18),dp(18),0);frame.addView(close,cp);
        TextView hint=new TextView(this);hint.setText("İki parmakla yakınlaştır • sürükleyerek gez");hint.setTextColor(Color.WHITE);hint.setTextSize(12);hint.setGravity(Gravity.CENTER);hint.setBackgroundColor(Color.argb(105,0,0,0));
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,dp(42),Gravity.BOTTOM);hp.setMargins(dp(18),0,dp(18),dp(16));frame.addView(hint,hp);
        dialog.setContentView(frame);dialog.show();
    }

    @Override void form(long id){
        super.form(id);
        if(id>0 && root!=null) root.post(()->addClearGroup699(id));
    }

    private void addClearGroup699(long id){
        if(root==null||findTag699(root,"v699-clear-group")!=null)return;
        Cursor c=db.athlete(id);String group="";if(c.moveToFirst())group=c.getString(c.getColumnIndexOrThrow("category"));c.close();
        if(group==null||group.trim().isEmpty())return;
        ScrollView sv=findScroll699(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout b=(LinearLayout)sv.getChildAt(0);
        Button clear=btn("GRUBU TEMİZLE");clear.setTag("v699-clear-group");
        clear.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("GRUP BİLGİSİNİ TEMİZLE")
            .setMessage("Sporcunun grup bilgisi silinsin mi? Sporcu kaydı ve diğer bilgileri korunacaktır.")
            .setPositiveButton("EVET, TEMİZLE",(d,w)->clearGroup699(id))
            .setNegativeButton("VAZGEÇ",null).show());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.setMargins(0,dp(8),0,dp(8));b.addView(clear,lp);
    }

    private void clearGroup699(long id){
        ContentValues cv=new ContentValues();cv.put("category","");
        int n=db.getWritableDatabase().update("athletes",cv,"id=?",new String[]{String.valueOf(id)});
        if(n<=0){toast("Grup bilgisi temizlenemedi.");return;}
        queueAthleteDelta699(id);
        toast("Grup bilgisi temizlendi.");
        form(id);
    }

    private void queueAthleteDelta699(long id){
        try{
            Method m=MainActivityV600.class.getDeclaredMethod("queueDelta600",long.class);
            m.setAccessible(true);m.invoke(this,id);
        }catch(Exception ignored){}
    }

    @Override int paymentColor(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        if(isDate(marker) && !(amount>0&&fee>0&&amount!=fee)) return PAID_PALE_699;
        return super.paymentColor(m,fee,sibling,start,end,restart,marker,amount);
    }

    @Override void addCycleProfileRow(LinearLayout b,long id,int key,int anchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeAt(yr,mo,start,end,restart);
        String status,detail;int color;String period=cycleDateLabel(key,anchor)+" – "+cycleDateLabel(shiftMonth(key,1),anchor);
        if(future){status="BEKLİYOR";color=Color.WHITE;detail=period+" • "+(expected>0?money(expected):"—");}
        else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=status.equals("ÖDENDİ")?PAID_PALE_699:ORANGE;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?PAID_PALE_699:ORANGE;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);detail=period+" • AKTİF DEĞİL";}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";}
        else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);detail=period+" • VERİ YOK";}
        else{status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));
        row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true));row.addView(tv(detail,12,Color.DKGRAY,false));
        if(!future&&yr==2026){final int mm=mo,fee=expected,amt=r.amount;final String mk=r.marker;row.setOnClickListener(v->editPayment(id,mm,fee,mk,amt));}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }

    private View findTag699(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag699(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ScrollView findScroll699(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll699(g.getChildAt(i));if(s!=null)return s;}}return null;}
}
