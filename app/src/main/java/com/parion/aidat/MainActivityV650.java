package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** v4.0.50 - source-driven upper dashboard. Never reuse corrupted V617 General/Finance containers. */
public class MainActivityV650 extends MainActivityV649 {
    private final ExecutorService dashExec650=Executors.newSingleThreadExecutor();
    private volatile int dashGeneration650=0;
    private TextView overdueValue650, overdueSub650;

    @Override void showHome(){
        super.showHome();
        int gen=++dashGeneration650;
        if(root!=null){
            root.postDelayed(()->rebuildUpper650(gen),950);
            root.postDelayed(()->rebuildUpper650(gen),1220);
        }
    }

    private void rebuildUpper650(int gen){
        if(gen!=dashGeneration650||root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll650(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        hideLegacyUpper650(box);
        removeFresh650(box);

        long active=countStatus650("AKTİF");
        long paused=countStatus650("ARA VERDİ");
        long target=scalar650("SELECT COALESCE(SUM(monthlyFee),0) FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))=''");
        long materialPeople=scalar650("SELECT COUNT(DISTINCT athleteId) FROM material_transactions WHERE paidAmount<total");
        long materialDue=scalar650("SELECT COALESCE(SUM(total-paidAmount),0) FROM material_transactions WHERE paidAmount<total");

        LinearLayout fresh=new LinearLayout(this);fresh.setTag("v650-fresh");fresh.setOrientation(LinearLayout.VERTICAL);fresh.setPadding(dp(2),0,dp(2),dp(8));
        fresh.addView(section650("GENEL DURUM","Kulübün güncel sporcu görünümü"));
        LinearLayout general=row650();
        general.addView(metric650("AKTİF SPORCU",String.valueOf(active),"Aktif sporcu",Color.rgb(39,134,82),android.R.drawable.ic_menu_myplaces,()->showStatus650("AKTİF","AKTİF SPORCULAR")),cell650());
        general.addView(metric650("ARA VERDİ",String.valueOf(paused),"Ara veren sporcu",Color.rgb(205,132,44),android.R.drawable.ic_media_pause,()->showStatus650("ARA VERDİ","ARA VEREN SPORCULAR")),cell650());
        fresh.addView(general);

        fresh.addView(section650("FİNANS","Aidat, borç ve tahsilat görünümü"));
        fresh.addView(metric650("AYLIK HEDEF",money650(target),"Aktif sporcuların aylık aidat toplamı",Color.rgb(205,156,34),android.R.drawable.ic_menu_view,()->showTarget650()),wide650());
        LinearLayout finance=row650();
        View overdue=metric650("GECİKMİŞ","…","Hesaplanıyor",Color.rgb(196,63,63),android.R.drawable.ic_dialog_alert,()->showOverdue650());
        overdueValue650=findTagged650(overdue,"value");overdueSub650=findTagged650(overdue,"sub");finance.addView(overdue,cell650());
        finance.addView(metric650("MALZEME BORCU",money650(materialDue),materialPeople+" sporcu",Color.rgb(205,132,44),android.R.drawable.ic_menu_agenda,()->showMaterial650()),cell650());
        fresh.addView(finance);
        box.addView(fresh,0,new LinearLayout.LayoutParams(-1,-2));
        calculateOverdueAsync650(gen);
    }

    private void hideLegacyUpper650(LinearLayout box){
        String[] needles={"GENEL DURUM","FİNANS","AKTİF SPORCU","ARA VERDİ","AYLIK HEDEF","GECİKMİŞ","MALZEME BORCU","ÖDENMEMİŞ MALZEME"};
        for(String n:needles){
            ArrayList<View> tops=new ArrayList<>();collectTopHits650(box,box,n,tops);
            for(View v:tops){if(v!=null&&v.getTag()!=null&&"v650-fresh".equals(v.getTag()))continue;v.setVisibility(View.GONE);ViewGroup.LayoutParams lp=v.getLayoutParams();if(lp!=null){lp.height=0;v.setLayoutParams(lp);}}
        }
    }

    private void collectTopHits650(LinearLayout box,View v,String needle,ArrayList<View> out){
        if(v instanceof TextView){String n=norm650(String.valueOf(((TextView)v).getText()));if(n.contains(norm650(needle))){View top=topChild650(box,v);if(top!=null&&!out.contains(top))out.add(top);}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectTopHits650(box,g.getChildAt(i),needle,out);}
    }
    private View topChild650(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private void removeFresh650(LinearLayout box){for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if("v650-fresh".equals(v.getTag()))box.removeViewAt(i);}}

    private TextView section650(String title,String sub){TextView t=new TextView(this);t.setText(title+"\n"+sub);t.setTextColor(Color.rgb(32,32,32));t.setTextSize(14);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(dp(4),dp(15),dp(4),dp(8));t.setLineSpacing(dp(1),1f);return t;}
    private LinearLayout row650(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.TOP);r.setPadding(0,0,0,dp(9));return r;}
    private LinearLayout.LayoutParams cell650(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(132),1f);lp.setMargins(dp(4),0,dp(4),0);return lp;}
    private LinearLayout.LayoutParams wide650(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(126));lp.setMargins(dp(4),0,dp(4),dp(9));return lp;}

    private View metric650(String title,String value,String sub,int accent,int iconRes,Runnable action){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setPadding(dp(10),dp(10),dp(10),dp(10));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));c.setBackground(bg);c.setElevation(dp(2));
        ImageView icon=new ImageView(this);icon.setImageResource(iconRes);icon.setColorFilter(accent);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);c.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));
        TextView val=new TextView(this);val.setTag("value");val.setText(value);val.setTextSize(value.length()>9?24:29);val.setTextColor(accent);val.setTypeface(Typeface.DEFAULT,Typeface.BOLD);val.setGravity(Gravity.CENTER);val.setMaxLines(1);c.addView(val,new LinearLayout.LayoutParams(-1,-2));
        TextView ttl=new TextView(this);ttl.setText(title);ttl.setTextSize(11.5f);ttl.setTextColor(Color.rgb(45,45,45));ttl.setTypeface(Typeface.DEFAULT,Typeface.BOLD);ttl.setGravity(Gravity.CENTER);ttl.setMaxLines(2);c.addView(ttl,new LinearLayout.LayoutParams(-1,-2));
        TextView st=new TextView(this);st.setTag("sub");st.setText(sub);st.setTextSize(9.5f);st.setTextColor(Color.rgb(105,105,105));st.setGravity(Gravity.CENTER);st.setMaxLines(2);c.addView(st,new LinearLayout.LayoutParams(-1,-2));
        c.setClickable(true);c.setOnClickListener(v->action.run());return c;
    }
    private TextView findTagged650(View v,String tag){if(v instanceof TextView&&tag.equals(v.getTag()))return (TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView t=findTagged650(g.getChildAt(i),tag);if(t!=null)return t;}}return null;}

    private long countStatus650(String status){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status=? AND TRIM(COALESCE(deletedAt,''))=''",new String[]{status});long n=0;if(c.moveToFirst())n=c.getLong(0);c.close();return n;}
    private long scalar650(String sql){try{Cursor c=db.getReadableDatabase().rawQuery(sql,null);long n=0;if(c.moveToFirst())n=c.getLong(0);c.close();return n;}catch(Exception e){return 0;}}
    private String money650(long n){return String.format(new Locale("tr","TR"),"₺%,d",n).replace(',','.');}

    private void showStatus650(String status,String title){page="LIST";base(title,true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.athletes("",status);int n=0;while(c.moveToNext()){row(b,a(c),null,0);n++;}c.close();if(n==0)b.addView(tv("Bu grupta sporcu bulunmuyor.",14,Color.DKGRAY,true));}
    private void showTarget650(){page="LIST";base("AYLIK HEDEF",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.athletes("","AKTİF");long total=0;while(c.moveToNext()){A x=a(c);row(b,x,"AYLIK AİDAT",x.fee);total+=x.fee;}c.close();TextView sum=tv("TOPLAM AYLIK HEDEF: "+money650(total),15,Color.rgb(160,112,12),true);sum.setGravity(Gravity.CENTER);b.addView(sum,0,new LinearLayout.LayoutParams(-1,dp(52)));}

    private void calculateOverdueAsync650(int gen){dashExec650.execute(()->{int count=0,total=0;Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);while(c.moveToNext()){int d=overdueAmount650(c.getLong(0));if(d>0){count++;total+=d;}}c.close();final int fc=count,ft=total;runOnUiThread(()->{if(gen!=dashGeneration650||root==null||!"HOME".equals(page))return;if(overdueValue650!=null)overdueValue650.setText(money650(ft));if(overdueSub650!=null)overdueSub650.setText(fc+" sporcu");});});}

    private int overdueAmount650(long id){
        Cursor ac=db.athlete(id);if(!ac.moveToFirst()){ac.close();return 0;}String start=s650(ac,"startDate"),end=s650(ac,"endDate"),restart=s650(ac,"restartDate"),sib=s650(ac,"sibling");ac.close();if("BURSLU".equalsIgnoreCase(sib)||start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;
        Calendar today=Calendar.getInstance();int anchor=anchorDay(start),todayKey=currentCycleKey(today,anchor);Calendar todayCycleStart=cycleDate(todayKey,anchor);int first=registrationMonth(start,id,todayKey),reg=parseMonthKey(start);if(reg>0&&reg>first)first=reg;if(first>=todayKey)return 0;HashMap<Integer,PayRec> pays=paymentMap(id);int debt=0,guard=0;
        for(int key=first;guard++<240;key=shiftMonth(key,1)){Calendar cycleStart=cycleDate(key,anchor);if(!cycleStart.before(todayCycleStart))break;int y=key/100,m=key%100;if(!activeAt(y,m,start,end,restart))continue;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);if("X".equals(r.marker))continue;int expected=expectedFeeAt(id,y,m,r);if(expected<=0)continue;debt+=Math.max(0,expected-r.amount);}return debt;
    }
    private String s650(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}

    private void showOverdue650(){page="OVERDUE_650";base("GECİKMİŞ SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){A x=a(c);int debt=overdueAmount650(x.id);if(debt<=0)continue;row(b,x,"SON VADESİ GEÇMİŞ DÖNEM BORCU",debt);n++;}c.close();if(n==0)b.addView(tv("Gecikmiş borcu bulunan sporcu yok.",14,Color.DKGRAY,true));}
    private void showMaterial650(){page="MATERIAL_DEBT_650";base("MALZEME BORCU",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT a.*,COALESCE(SUM(m.total-m.paidAmount),0) AS due FROM athletes a JOIN material_transactions m ON m.athleteId=a.id WHERE m.paidAmount<m.total GROUP BY a.id ORDER BY a.name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){A x=a(c);int due=c.getInt(c.getColumnIndexOrThrow("due"));row(b,x,"ÖDENMEMİŞ MALZEME BORCU",due);n++;}c.close();if(n==0)b.addView(tv("Ödenmemiş malzeme borcu yok.",14,Color.DKGRAY,true));}

    @Override void goBack(){if("OVERDUE_650".equals(page)||"MATERIAL_DEBT_650".equals(page)){showHome();return;}super.goBack();}
    private ScrollView findScroll650(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll650(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm650(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    @Override protected void onDestroy(){dashGeneration650++;dashExec650.shutdownNow();super.onDestroy();}
}
