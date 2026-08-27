package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.1.43 - polished finance dashboard page, direct core actions and finance-aware back navigation. */
public class MainActivityV720 extends MainActivityV719 {
    private static final int GOLD=Color.rgb(205,156,34), GREEN=Color.rgb(39,134,82), RED=Color.rgb(196,63,63), ORANGE=Color.rgb(205,132,44), BLUE=Color.rgb(72,103,132), TEXT=Color.rgb(45,45,45);
    private boolean returnToFinance720=false;

    @Override void showHome(){
        returnToFinance720=false;
        super.showHome();
        if(root!=null){root.postDelayed(this::patchHome720,12100);root.postDelayed(this::patchHome720,13200);}
    }

    private void patchHome720(){
        if(root==null||!"HOME".equalsIgnoreCase(page))return;
        removeDashboardFinanceDuplicates720(root);
        View entry=findTag720(root,"v718-finance-entry");
        if(entry!=null){
            replaceFinanceIcon720(entry);
            entry.setClickable(true);entry.setOnClickListener(v->showFinancePage720());
        }
    }

    private void replaceFinanceIcon720(View entry){
        ImageView icon=findFirstImage720(entry);if(icon==null)return;
        icon.setImageResource(android.R.drawable.ic_menu_edit);
        icon.setColorFilter(GOLD);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setRotation(0f);
    }

    private void removeDashboardFinanceDuplicates720(View v){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){View c=g.getChildAt(i);if("v718-finance-entry".equals(c.getTag()))continue;
            String n=norm720(text720(c));
            if((n.contains("ERKEN ÖDEME GİR")||n.contains("EKSİK ÖDEME GİR"))&&!n.contains("FİNANS")){g.removeViewAt(i);continue;}
            removeDashboardFinanceDuplicates720(c);
        }
    }

    private void showFinancePage720(){
        returnToFinance720=false;page="FINANCE_720";base("FİNANS",true);
        ScrollView sv=scroll();sv.setFillViewport(false);LinearLayout b=box(sv);b.setPadding(dp(8),dp(4),dp(8),dp(34));
        TextView intro=tv720("Aidat, tahsilat, vade, borç ve ödeme işlemleri",12,Color.DKGRAY,false);intro.setPadding(dp(5),dp(4),dp(5),dp(12));b.addView(intro);

        long target=scalar720("SELECT COALESCE(SUM(monthlyFee),0) FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))=''");
        b.addView(metric720("AYLIK HEDEF",money720(target),"Aktif sporcuların aylık aidat toplamı",GOLD,android.R.drawable.ic_menu_view,()->openTarget720()),wide720());

        Calendar now=Calendar.getInstance();long collected=collectionTotal720(now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1);int dueCount=countDueNow720();
        LinearLayout r1=row720();
        r1.addView(metric720("BU AYKİ TAHSİLAT",money720(collected),"Ödeme tarihine göre",GREEN,android.R.drawable.ic_menu_save,()->openViaReflection720(MainActivityV705.class,"showCollections705",new Class[]{int.class},new Object[]{0})),cell720());
        r1.addView(metric720("ÖDEME VADESİ GELENLER",String.valueOf(dueCount),dueCount+" sporcu",ORANGE,android.R.drawable.ic_menu_recent_history,()->openDueNow720()),cell720());b.addView(r1);

        b.addView(metric720("ERKEN ÖDEME GİR","+","Vadesi gelmemiş aylar için aidat kaydı",BLUE,android.R.drawable.ic_input_add,()->openViaReflection720(MainActivityV711.class,"showEarlyPayments711",new Class[0],new Object[0])),wide720());

        int[] overdue=overdueSummary720();long materialPeople=scalar720("SELECT COUNT(DISTINCT athleteId) FROM material_transactions WHERE paidAmount<total");long materialDue=scalar720("SELECT COALESCE(SUM(total-paidAmount),0) FROM material_transactions WHERE paidAmount<total");
        LinearLayout r2=row720();
        r2.addView(metric720("GECİKMİŞ",money720(overdue[1]),overdue[0]+" sporcu",RED,android.R.drawable.ic_dialog_alert,()->openOverdue720()),cell720());
        r2.addView(metric720("MALZEME BORCU",money720(materialDue),materialPeople+" sporcu",ORANGE,android.R.drawable.ic_menu_agenda,()->openMaterial720()),cell720());b.addView(r2);

        b.addView(recentCard720(),recentLp720());
    }

    private View metric720(String title,String value,String sub,int accent,int iconRes,Runnable action){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setPadding(dp(10),dp(10),dp(10),dp(10));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));c.setBackground(bg);c.setElevation(dp(2));
        ImageView icon=new ImageView(this);icon.setImageResource(iconRes);icon.setColorFilter(accent);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);c.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));
        TextView val=tv720(value,value.length()>9?22:28,accent,true);val.setGravity(Gravity.CENTER);val.setSingleLine(true);c.addView(val,new LinearLayout.LayoutParams(-1,-2));
        TextView ttl=tv720(title,11.3f,TEXT,true);ttl.setGravity(Gravity.CENTER);ttl.setMaxLines(2);c.addView(ttl,new LinearLayout.LayoutParams(-1,-2));
        TextView st=tv720(sub,9.3f,Color.rgb(105,105,105),false);st.setGravity(Gravity.CENTER);st.setMaxLines(2);c.addView(st,new LinearLayout.LayoutParams(-1,-2));
        c.setClickable(true);c.setOnClickListener(v->{returnToFinance720=true;action.run();});return c;
    }

    private View recentCard720(){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(13),dp(12),dp(13),dp(10));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));card.setBackground(bg);card.setElevation(dp(2));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView title=tv720("SON ÖDEMELER",12,TEXT,true);head.addView(title,new LinearLayout.LayoutParams(0,-2,1));TextView more=tv720("SON 20 ›",10.5f,GOLD,true);head.addView(more);card.addView(head);
        Cursor c=null;int n=0;try{c=db.recentPayments(3);while(c.moveToNext()){String name=c.getString(c.getColumnIndexOrThrow("name"));int amount=c.getInt(c.getColumnIndexOrThrow("amount"));LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(8),0,dp(8));TextView nm=tv720(name,13,TEXT,true);nm.setSingleLine(true);row.addView(nm,new LinearLayout.LayoutParams(0,-2,1));TextView am=tv720(money720(amount),13,GREEN,true);row.addView(am);card.addView(row);n++;}}catch(Exception ignored){}finally{if(c!=null)c.close();}
        if(n==0){TextView e=tv720("Henüz yeni ödeme kaydı yok.",11,Color.GRAY,false);e.setPadding(0,dp(12),0,dp(8));card.addView(e);}card.setClickable(true);card.setOnClickListener(v->{returnToFinance720=true;openViaReflection720(MainActivityV704.class,"showRecentPayments704",new Class[0],new Object[0]);});return card;
    }

    private void openTarget720(){page="FIN_TARGET_720";base("AYLIK HEDEF",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.athletes("","AKTİF");long total=0;while(c.moveToNext()){A x=a(c);row(b,x,"AYLIK AİDAT",x.fee);total+=x.fee;}c.close();TextView sum=tv720("TOPLAM AYLIK HEDEF: "+money720(total),15,Color.rgb(160,112,12),true);sum.setGravity(Gravity.CENTER);b.addView(sum,0,new LinearLayout.LayoutParams(-1,dp(52)));}
    private void openOverdue720(){page="FIN_OVERDUE_720";base("GECİKMİŞ SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){A x=a(c);int debt=overdueAmount720(x.id);if(debt<=0)continue;row(b,x,"SON VADESİ GEÇMİŞ DÖNEM BORCU",debt);n++;}c.close();if(n==0)b.addView(tv720("Gecikmiş borcu bulunan sporcu yok.",14,Color.DKGRAY,true));}
    private void openMaterial720(){page="FIN_MATERIAL_720";base("MALZEME BORCU",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT a.*,COALESCE(SUM(m.total-m.paidAmount),0) AS due FROM athletes a JOIN material_transactions m ON m.athleteId=a.id WHERE m.paidAmount<m.total GROUP BY a.id ORDER BY a.name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){A x=a(c);int due=c.getInt(c.getColumnIndexOrThrow("due"));row(b,x,"ÖDENMEMİŞ MALZEME BORCU",due);n++;}c.close();if(n==0)b.addView(tv720("Ödenmemiş malzeme borcu yok.",14,Color.DKGRAY,true));}
    private void openDueNow720(){page="FIN_DUE_720";base("ÖDEME VADESİ GELENLER",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){A x=a(c);int due=currentDue720(x.id);if(due<=0)continue;row(b,x,"ÖDEME VADESİ GELDİ",due);n++;}c.close();if(n==0)b.addView(tv720("Ödeme vadesi gelen sporcu bulunmuyor.",14,Color.DKGRAY,true));}

    private int countDueNow720(){int n=0;Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);while(c.moveToNext())if(currentDue720(c.getLong(0))>0)n++;c.close();return n;}
    private int currentDue720(long id){Cursor ac=db.athlete(id);if(!ac.moveToFirst()){ac.close();return 0;}String start=s720(ac,"startDate"),end=s720(ac,"endDate"),restart=s720(ac,"restartDate"),sib=s720(ac,"sibling");ac.close();if("BURSLU".equalsIgnoreCase(sib)||start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;Calendar today=Calendar.getInstance();int anchor=anchorDay(start),key=currentCycleKey(today,anchor);Calendar cycleStart=cycleDate(key,anchor);if(cycleStart.after(today))return 0;int y=key/100,m=key%100;if(!activeAt(y,m,start,end,restart))return 0;HashMap<Integer,PayRec> pays=paymentMap(id);PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);if("X".equals(r.marker))return 0;int expected=expectedFeeAt(id,y,m,r);if(expected<=0)return 0;return Math.max(0,expected-r.amount);}
    private int[] overdueSummary720(){int count=0,total=0;Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);while(c.moveToNext()){int d=overdueAmount720(c.getLong(0));if(d>0){count++;total+=d;}}c.close();return new int[]{count,total};}
    private int overdueAmount720(long id){Cursor ac=db.athlete(id);if(!ac.moveToFirst()){ac.close();return 0;}String start=s720(ac,"startDate"),end=s720(ac,"endDate"),restart=s720(ac,"restartDate"),sib=s720(ac,"sibling");ac.close();if("BURSLU".equalsIgnoreCase(sib)||start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;Calendar today=Calendar.getInstance();int anchor=anchorDay(start),todayKey=currentCycleKey(today,anchor);Calendar todayCycleStart=cycleDate(todayKey,anchor);int first=registrationMonth(start,id,todayKey),reg=parseMonthKey(start);if(reg>0&&reg>first)first=reg;if(first>=todayKey)return 0;HashMap<Integer,PayRec> pays=paymentMap(id);int debt=0,guard=0;for(int key=first;guard++<240;key=shiftMonth(key,1)){Calendar cycleStart=cycleDate(key,anchor);if(!cycleStart.before(todayCycleStart))break;int y=key/100,m=key%100;if(!activeAt(y,m,start,end,restart))continue;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);if("X".equals(r.marker))continue;int expected=expectedFeeAt(id,y,m,r);if(expected<=0)continue;debt+=Math.max(0,expected-r.amount);}return debt;}
    private long collectionTotal720(int year,int month){String prefix=String.format(Locale.US,"%04d-%02d",year,month);String paidExpr="CASE WHEN p.marker GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN p.marker ELSE cd.paidDate END";Cursor c=null;long sum=0;try{c=db.getReadableDatabase().rawQuery("SELECT p.amount FROM payments p LEFT JOIN payment_collection_dates cd ON cd.athleteId=p.athleteId AND cd.year=p.year AND cd.month=p.month WHERE p.amount>0 AND "+paidExpr+" LIKE ?",new String[]{prefix+"%"});while(c.moveToNext())sum+=c.getLong(0);}catch(Exception ignored){}finally{if(c!=null)c.close();}return sum;}

    private void openViaReflection720(Class<?> owner,String name,Class<?>[] sig,Object[] args){try{Method m=owner.getDeclaredMethod(name,sig);m.setAccessible(true);m.invoke(this,args);}catch(Exception e){Toast.makeText(this,"Finans işlemi açılamadı.",Toast.LENGTH_LONG).show();}}

    @Override void goBack(){
        if("FINANCE_720".equals(page)){showHome();return;}
        if(returnToFinance720&&(page!=null)&&(page.equals("FIN_TARGET_720")||page.equals("FIN_OVERDUE_720")||page.equals("FIN_MATERIAL_720")||page.equals("FIN_DUE_720")||page.equals("COLLECTION_DETAIL")||page.equals("EARLY_PAYMENT_711")||page.equals("RECENT_PAYMENTS"))){showFinancePage720();return;}
        super.goBack();
    }

    private long scalar720(String sql){Cursor c=null;try{c=db.getReadableDatabase().rawQuery(sql,null);long n=c.moveToFirst()?c.getLong(0):0;c.close();return n;}catch(Exception e){if(c!=null)c.close();return 0;}}
    private String s720(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
    private String money720(long n){return String.format(new Locale("tr","TR"),"₺%,d",n).replace(',','.');}
    private LinearLayout row720(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.TOP);r.setPadding(0,0,0,dp(9));return r;}
    private LinearLayout.LayoutParams cell720(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(132),1);lp.setMargins(dp(4),0,dp(4),0);return lp;}
    private LinearLayout.LayoutParams wide720(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(126));lp.setMargins(dp(4),0,dp(4),dp(9));return lp;}
    private LinearLayout.LayoutParams recentLp720(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(dp(4),0,dp(4),dp(12));return lp;}
    private TextView tv720(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private ImageView findFirstImage720(View v){if(v instanceof ImageView)return(ImageView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ImageView r=findFirstImage720(g.getChildAt(i));if(r!=null)return r;}}return null;}
    private View findTag720(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag720(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String text720(View v){StringBuilder s=new StringBuilder();collectText720(v,s);return s.toString();}
    private void collectText720(View v,StringBuilder s){if(v instanceof TextView)s.append(' ').append(((TextView)v).getText());if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText720(g.getChildAt(i),s);}}
    private String norm720(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
