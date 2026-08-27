package com.parion.aidat;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.1.33 - pre-2026 payment cleanup, attendance group colors, nav tint fix. Dashboard early-payment card disabled at source in v4.1.46. */
public class MainActivityV711 extends MainActivityV710 {
    private static final int NAV_711=Color.rgb(78,78,78);
    private static final int EARLY_711=Color.rgb(72,103,132);
    private static final int[] GROUP_711={
        Color.rgb(217,234,247),Color.rgb(224,239,214),Color.rgb(255,239,204),Color.rgb(249,222,230),
        Color.rgb(231,221,245),Color.rgb(218,237,233),Color.rgb(244,226,207),Color.rgb(225,229,242)
    };

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->{fixNavTint711(root);if("ATTENDANCE_GROUPS_628".equals(page))colorAttendanceGroups711();});
    }

    @Override void showHome(){
        super.showHome();
        // v4.1.46: DO NOT create the standalone dashboard Early Payment card.
        // Early payment remains available only from the dedicated Finance page.
        if(root!=null)root.post(()->fixNavTint711(root));
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        if(root!=null)root.post(()->{hidePre2026PaymentViews711();fixNavTint711(root);});
    }

    @Override void goBack(){
        if("EARLY_PAYMENT_711".equals(page)){showHome();return;}
        super.goBack();
    }

    private void fixNavTint711(View v){
        if(v==null)return;
        CharSequence d=v.getContentDescription();
        if(d!=null&&(eq711(d,"Anasayfa")||eq711(d,"Yoklamalar")||eq711(d,"Sporcular")||eq711(d,"Ayarlar"))&&v instanceof ImageButton){
            ImageButton b=(ImageButton)v;
            b.setImageTintList(ColorStateList.valueOf(NAV_711));
            b.setColorFilter(NAV_711,PorterDuff.Mode.SRC_IN);
            b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            b.setPadding(dp(12),dp(12),dp(12),dp(12));
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)fixNavTint711(g.getChildAt(i));}
    }
    private boolean eq711(CharSequence a,String b){return a!=null&&b.equalsIgnoreCase(a.toString());}

    private void installEarlyPaymentCard711(){
        // Disabled permanently for HOME in v4.1.46. Kept only as dead legacy code so older
        // source layers remain binary-compatible; showHome() no longer calls this method.
    }

    private void showEarlyPayments711(){
        page="EARLY_PAYMENT_711";currentAthlete=-1;base("ERKEN ÖDEME GİR",true);ScrollView sv=scroll();LinearLayout list=box(sv);
        TextView info=new TextView(this);info.setText("Önce sporcuyu seçin. Sonraki adımda yalnızca vadesi henüz gelmemiş 2026 ayları gösterilir.");info.setTextSize(11.5f);info.setTextColor(Color.DKGRAY);info.setPadding(dp(6),dp(4),dp(6),dp(12));list.addView(info);
        Cursor c=null;try{
            c=db.getReadableDatabase().rawQuery("SELECT id,name,category,monthlyFee FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
            while(c.moveToNext()){
                long id=c.getLong(0);String name=c.getString(1),group=c.getString(2)==null?"":c.getString(2);int fee=c.getInt(3);
                LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(13),dp(11),dp(13),dp(11));GradientDrawable rbg=new GradientDrawable();rbg.setColor(Color.WHITE);rbg.setCornerRadius(dp(12));row.setBackground(rbg);row.setElevation(dp(1));
                TextView n=new TextView(this);n.setText(name);n.setTextSize(14f);n.setTextColor(Color.rgb(30,30,30));n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);row.addView(n);
                TextView sub=new TextView(this);sub.setText(group+" • "+money(fee));sub.setTextSize(10.5f);sub.setTextColor(Color.GRAY);row.addView(sub);row.setOnClickListener(v->chooseFutureMonth711(id,name));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));list.addView(row,lp);
            }
        }finally{if(c!=null)c.close();}
    }

    private void chooseFutureMonth711(long athleteId,String name){
        Calendar now=Calendar.getInstance();int year=now.get(Calendar.YEAR),cm=now.get(Calendar.MONTH)+1;
        ArrayList<Integer> months=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();
        int start=year<2026?1:(year==2026?cm+1:13);
        for(int m=start;m<=12;m++){
            String marker="";int amount=0;Cursor p=db.getReadableDatabase().rawQuery("SELECT marker,amount FROM payments WHERE athleteId=? AND year=2026 AND month=?",new String[]{String.valueOf(athleteId),String.valueOf(m)});if(p.moveToFirst()){marker=p.getString(0)==null?"":p.getString(0);amount=p.getInt(1);}p.close();
            months.add(m);labels.add(monthName(m)+" 2026"+(amount>0?" • KAYITLI "+money(amount):""));
        }
        if(months.isEmpty()){new android.app.AlertDialog.Builder(this).setTitle("ERKEN ÖDEME").setMessage("2026 için vadesi gelmemiş ay bulunmuyor.").setPositiveButton("TAMAM",null).show();return;}
        new android.app.AlertDialog.Builder(this).setTitle(name+" • AY SEÇ").setItems(labels.toArray(new String[0]),(d,w)->{
            int m=months.get(w),fee=db.expectedFee(athleteId,m);String marker="";int amount=0;Cursor p=db.getReadableDatabase().rawQuery("SELECT marker,amount FROM payments WHERE athleteId=? AND year=2026 AND month=?",new String[]{String.valueOf(athleteId),String.valueOf(m)});if(p.moveToFirst()){marker=p.getString(0)==null?"":p.getString(0);amount=p.getInt(1);}p.close();editPayment(athleteId,m,fee,marker,amount);
        }).setNegativeButton("İPTAL",null).show();
    }

    private void hidePre2026PaymentViews711(){
        LinearLayout box=findScrollBox711(root);if(box==null)return;
        for(int i=box.getChildCount()-1;i>=0;i--){View child=box.getChildAt(i);String txt=text711(child).toUpperCase(new Locale("tr","TR"));if(isOldPaymentText711(txt)){child.setVisibility(View.GONE);ViewGroup.LayoutParams lp=child.getLayoutParams();if(lp!=null){lp.height=0;child.setLayoutParams(lp);}}}
    }
    private boolean isOldPaymentText711(String s){
        boolean old=false;for(int y=2000;y<2026;y++)if(s.contains(String.valueOf(y))){old=true;break;}if(!old)return false;
        if(s.contains("AİDAT")||s.contains("ÖDEME"))return true;
        String[] ms={"OCAK","ŞUBAT","MART","NİSAN","MAYIS","HAZİRAN","TEMMUZ","AĞUSTOS","EYLÜL","EKİM","KASIM","ARALIK"};for(String m:ms)if(s.contains(m))return true;return false;
    }

    private void colorAttendanceGroups711(){
        if(root==null||!"ATTENDANCE_GROUPS_628".equals(page))return;
        HashMap<String,Integer> colors=new HashMap<>();Cursor c=null;int i=0;try{c=db.getReadableDatabase().rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE",null);while(c.moveToNext()){colors.put(c.getString(0).toUpperCase(new Locale("tr","TR")),GROUP_711[i%GROUP_711.length]);i++;}}finally{if(c!=null)c.close();}
        patchAttendanceButtons711(root,colors);
    }
    private void patchAttendanceButtons711(View v,HashMap<String,Integer> colors){
        if(v instanceof Button){Button b=(Button)v;String n=String.valueOf(b.getText()).trim().toUpperCase(new Locale("tr","TR"));Integer fill=colors.get(n);if(fill!=null){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(12));g.setStroke(dp(1),darken711(fill));b.setBackground(g);b.setTextColor(Color.rgb(35,35,35));b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchAttendanceButtons711(g.getChildAt(i),colors);}
    }
    private int darken711(int c){return Color.rgb(Math.max(0,Color.red(c)-32),Math.max(0,Color.green(c)-32),Math.max(0,Color.blue(c)-32));}

    private LinearLayout findScrollBox711(View v){if(v instanceof ScrollView){ScrollView s=(ScrollView)v;if(s.getChildCount()>0&&s.getChildAt(0) instanceof LinearLayout)return(LinearLayout)s.getChildAt(0);}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){LinearLayout r=findScrollBox711(g.getChildAt(i));if(r!=null)return r;}}return null;}
    private String text711(View v){StringBuilder s=new StringBuilder();collectText711(v,s);return s.toString();}
    private void collectText711(View v,StringBuilder s){if(v instanceof TextView)s.append(' ').append(((TextView)v).getText());if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText711(g.getChildAt(i),s);}}
}
