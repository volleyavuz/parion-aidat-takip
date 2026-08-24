package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** v4.0.57 - single-pass dashboard built directly on stable V639; bypasses V640-V656 dashboard patch chain. */
public class MainActivityV657 extends MainActivityV639 {
    private final ExecutorService dashExec657=Executors.newSingleThreadExecutor();
    private volatile int dashGen657=0;
    private TextView overdueValue657, overdueSub657;

    @Override void showHome(){
        super.showHome();
        final int gen=++dashGen657;
        if(root==null)return;
        buildFinalDashboard657(gen);
    }

    private void buildFinalDashboard657(int gen){
        if(gen!=dashGen657||root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        removeTopStatus657(root);
        ScrollView sv=findScroll657(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        hideLegacyUpper657(box);
        removeGenerated657(box);

        long active=countStatus657("AKTİF");
        long paused=countStatus657("ARA VERDİ");
        long target=scalar657("SELECT COALESCE(SUM(monthlyFee),0) FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))=''");
        long materialPeople=scalar657("SELECT COUNT(DISTINCT athleteId) FROM material_transactions WHERE paidAmount<total");
        long materialDue=scalar657("SELECT COALESCE(SUM(total-paidAmount),0) FROM material_transactions WHERE paidAmount<total");

        LinearLayout fresh=new LinearLayout(this);fresh.setTag("v657-fresh");fresh.setOrientation(LinearLayout.VERTICAL);fresh.setPadding(dp(2),0,dp(2),dp(8));
        fresh.addView(section657("GENEL DURUM","Kulübün güncel sporcu görünümü"));
        LinearLayout general=row657();
        general.addView(metric657("AKTİF SPORCU",String.valueOf(active),"Aktif sporcu",Color.rgb(39,134,82),android.R.drawable.ic_menu_myplaces,()->showStatus657("AKTİF","AKTİF SPORCULAR")),cell657());
        general.addView(metric657("ARA VERDİ",String.valueOf(paused),"Ara veren sporcu",Color.rgb(205,132,44),android.R.drawable.ic_media_pause,()->showStatus657("ARA VERDİ","ARA VEREN SPORCULAR")),cell657());
        fresh.addView(general);

        fresh.addView(section657("FİNANS","Aidat, borç ve tahsilat görünümü"));
        fresh.addView(metric657("AYLIK HEDEF",money657(target),"Aktif sporcuların aylık aidat toplamı",Color.rgb(205,156,34),android.R.drawable.ic_menu_view,this::showTarget657),wide657());
        LinearLayout finance=row657();
        View overdue=metric657("GECİKMİŞ","…","Hesaplanıyor",Color.rgb(196,63,63),android.R.drawable.ic_dialog_alert,this::showOverdue657);
        overdueValue657=findTagged657(overdue,"value");overdueSub657=findTagged657(overdue,"sub");finance.addView(overdue,cell657());
        finance.addView(metric657("MALZEME BORCU",money657(materialDue),materialPeople+" sporcu",Color.rgb(205,132,44),android.R.drawable.ic_menu_agenda,this::showMaterial657),cell657());
        fresh.addView(finance);
        box.addView(fresh,0,new LinearLayout.LayoutParams(-1,-2));

        arrangeLower657(box);
        styleFollowup657(box,"FOTOĞRAF EKSİK");
        styleFollowup657(box,"KAYIT FORMU EKSİK");
        styleFollowup657(box,"YAZIN ARANACAK");
        styleFollowup657(box,"KIŞIN ARANACAK");
        labelCohorts657(box);
        calculateOverdueAsync657(gen);
    }

    private void removeTopStatus657(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String n=norm657(String.valueOf(t.getText()));
            if(n.contains("ÇİFT YÖNLÜ DELTA")||n.contains("CIFT YONLU DELTA")||n.contains("TOPLU SNAPSHOT KAPALI")||n.contains("BULUT GÜNCEL")||n.contains("DEĞİŞİKLİK BEKLİYOR")){
                t.setVisibility(View.GONE);ViewGroup.LayoutParams lp=t.getLayoutParams();if(lp!=null){lp.height=0;t.setLayoutParams(lp);}return;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)removeTopStatus657(g.getChildAt(i));}
    }

    private void hideLegacyUpper657(LinearLayout box){
        String[] needles={"SPORCULAR","GENEL DURUM","FİNANS","AKTİF SPORCU","ARA VERDİ","AYLIK HEDEF","GECİKMİŞ","MALZEME BORCU","ÖDENMEMİŞ MALZEME","BU AY TAHSİL EDİLEN","AY SONUNA KADAR GELECEK"};
        for(String n:needles){ArrayList<View> tops=new ArrayList<>();collectTopHits657(box,box,n,tops);for(View v:tops){if(v==null||"v657-fresh".equals(v.getTag()))continue;v.setVisibility(View.GONE);ViewGroup.LayoutParams lp=v.getLayoutParams();if(lp!=null){lp.height=0;v.setLayoutParams(lp);}}}
    }
    private void collectTopHits657(LinearLayout box,View v,String needle,ArrayList<View> out){
        if(v instanceof TextView){String n=norm657(String.valueOf(((TextView)v).getText()));if(n.contains(norm657(needle))){View top=topChild657(box,v);if(top!=null&&!out.contains(top))out.add(top);}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectTopHits657(box,g.getChildAt(i),needle,out);}
    }
    private View topChild657(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private void removeGenerated657(LinearLayout box){for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);Object tag=v.getTag();if("v657-fresh".equals(tag)||"v657-heading".equals(tag))box.removeViewAt(i);}}

    private void arrangeLower657(LinearLayout box){
        removeHeadings657(box);
        View net=topHit657(box,"SON 3 AYDA");
        if(net!=null&&net.getVisibility()!=View.GONE){int i=box.indexOfChild(net);if(i>=0)box.addView(sectionHeading657("SPORCU HAREKETLERİ","Yeni başlayan ve ayrılan sporcular"),i);}
        ArrayList<View> tracking=new ArrayList<>();
        addUnique657(tracking,topHit657(box,"FOTOĞRAF EKSİK"));
        addUnique657(tracking,topHit657(box,"KAYIT FORMU EKSİK"));
        addUnique657(tracking,topHit657(box,"YAZIN ARANACAK"));
        addUnique657(tracking,topHit657(box,"KIŞIN ARANACAK"));
        for(View v:tracking)if(v!=null&&v.getParent()==box)box.removeView(v);
        View absent=topHit657(box,"DEVAMSIZLAR");int insert=absent!=null?box.indexOfChild(absent):box.getChildCount();
        if(!tracking.isEmpty()){
            box.addView(sectionHeading657("TAKİP GEREKTİRENLER","Eksik kayıtlar ve takip edilmesi gereken sporcular"),Math.max(0,insert++));
            for(View v:tracking)if(v!=null)box.addView(v,Math.min(insert++,box.getChildCount()));
        }
    }
    private void removeHeadings657(LinearLayout box){for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);Object tag=v.getTag();if("v657-heading".equals(tag)){box.removeViewAt(i);continue;}if(v instanceof TextView){String n=norm657(String.valueOf(((TextView)v).getText()));if(n.startsWith("SPORCU HAREKETLERİ")||n.startsWith("TAKİP GEREKTİRENLER"))box.removeViewAt(i);}}}
    private View topHit657(LinearLayout box,String needle){TextView t=findText657(box,needle);if(t==null)return null;View cur=t;while(cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur.getParent()==box?cur:null;}
    private TextView sectionHeading657(String title,String sub){TextView t=section657(title,sub);t.setTag("v657-heading");t.setPadding(dp(4),dp(18),dp(4),dp(9));return t;}
    private void addUnique657(ArrayList<View>a,View v){if(v!=null&&!a.contains(v))a.add(v);}

    private void labelCohorts657(View rootView){
        ArrayList<TextView> mores=new ArrayList<>();collectMore657(rootView,mores);if(mores.size()<2)return;
        Collections.sort(mores,(a,b)->Integer.compare(screenX657(a),screenX657(b)));
        styleCohort657(mores.get(0),"BU AY BAŞLAYANLAR");styleCohort657(mores.get(1),"GEÇEN AY BAŞLAYANLAR");
    }
    private void collectMore657(View v,List<TextView> out){if(v instanceof TextView&&"v619-more".equals(v.getTag()))out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectMore657(g.getChildAt(i),out);}}
    private int screenX657(View v){int[]p=new int[2];v.getLocationOnScreen(p);return p[0];}
    private void styleCohort657(TextView more,String title){if(!(more.getParent() instanceof LinearLayout))return;LinearLayout card=(LinearLayout)more.getParent();TextView h=null;for(int i=0;i<card.getChildCount();i++){View v=card.getChildAt(i);if(v instanceof TextView&&"v657-cohort".equals(v.getTag())){h=(TextView)v;break;}}if(h==null){h=new TextView(this);h.setTag("v657-cohort");card.addView(h,0,new LinearLayout.LayoutParams(-1,-2));}h.setText(title);h.setTextSize(10.3f);h.setTextColor(Color.rgb(48,48,48));h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setGravity(Gravity.CENTER);h.setMaxLines(2);h.setPadding(dp(3),dp(4),dp(3),dp(7));card.setPadding(dp(10),dp(8),dp(10),dp(10));}

    private void styleFollowup657(View rootView,String needle){TextView label=findText657(rootView,needle);if(label==null)return;View card=nearestCard657(label);if(card==null)return;GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));card.setBackground(bg);card.setElevation(dp(1));card.setPadding(dp(10),dp(10),dp(10),dp(10));normalizeFollowup657(card,needle);}
    private View nearestCard657(View v){View cur=v,best=null;while(cur!=null&&cur!=root){if(cur.isClickable()||cur.hasOnClickListeners())best=cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}if(best!=null)return best;cur=v;while(cur!=null&&cur!=root){ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;if(cur instanceof LinearLayout)return cur;}return null;}
    private void normalizeFollowup657(View v,String needle){if(v instanceof TextView){TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();String n=norm657(s);if(n.contains(norm657(needle))){t.setTextSize(10.8f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setTextColor(Color.rgb(48,48,48));t.setGravity(Gravity.CENTER);t.setMaxLines(2);}else if(s.matches("[0-9]+\\s*")){t.setTextSize(24f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setTextColor(Color.rgb(35,35,35));t.setGravity(Gravity.CENTER);t.setBackground(null);}}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)normalizeFollowup657(g.getChildAt(i),needle);}}

    private TextView section657(String title,String sub){TextView t=new TextView(this);t.setText(title+"\n"+sub);t.setTextColor(Color.rgb(32,32,32));t.setTextSize(14);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(dp(4),dp(15),dp(4),dp(8));t.setLineSpacing(dp(1),1f);return t;}
    private LinearLayout row657(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.TOP);r.setPadding(0,0,0,dp(9));return r;}
    private LinearLayout.LayoutParams cell657(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(132),1f);lp.setMargins(dp(4),0,dp(4),0);return lp;}
    private LinearLayout.LayoutParams wide657(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(126));lp.setMargins(dp(4),0,dp(4),dp(9));return lp;}
    private View metric657(String title,String value,String sub,int accent,int iconRes,Runnable action){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setPadding(dp(10),dp(10),dp(10),dp(10));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));c.setBackground(bg);c.setElevation(dp(2));ImageView icon=new ImageView(this);icon.setImageResource(iconRes);icon.setColorFilter(accent);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);c.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));TextView val=new TextView(this);val.setTag("value");val.setText(value);val.setTextSize(value.length()>9?21.5f:value.length()>7?23.5f:27f);val.setTextColor(accent);val.setTypeface(Typeface.DEFAULT,Typeface.BOLD);val.setGravity(Gravity.CENTER);val.setSingleLine(true);c.addView(val,new LinearLayout.LayoutParams(-1,-2));TextView ttl=new TextView(this);ttl.setText(title);ttl.setTextSize(11.2f);ttl.setTextColor(Color.rgb(45,45,45));ttl.setTypeface(Typeface.DEFAULT,Typeface.BOLD);ttl.setGravity(Gravity.CENTER);ttl.setMaxLines(2);c.addView(ttl,new LinearLayout.LayoutParams(-1,-2));TextView st=new TextView(this);st.setTag("sub");st.setText(sub);st.setTextSize(9.3f);st.setTextColor(Color.rgb(105,105,105));st.setGravity(Gravity.CENTER);st.setMaxLines(2);c.addView(st,new LinearLayout.LayoutParams(-1,-2));c.setClickable(true);c.setOnClickListener(v->action.run());return c;}
    private TextView findTagged657(View v,String tag){if(v instanceof TextView&&tag.equals(v.getTag()))return(TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView t=findTagged657(g.getChildAt(i),tag);if(t!=null)return t;}}return null;}
    private TextView findText657(View v,String needle){if(v instanceof TextView){String n=norm657(String.valueOf(((TextView)v).getText()));if(n.contains(norm657(needle)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText657(g.getChildAt(i),needle);if(r!=null)return r;}}return null;}

    private long countStatus657(String status){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status=? AND TRIM(COALESCE(deletedAt,''))=''",new String[]{status});long n=0;if(c.moveToFirst())n=c.getLong(0);c.close();return n;}
    private long scalar657(String sql){try{Cursor c=db.getReadableDatabase().rawQuery(sql,null);long n=0;if(c.moveToFirst())n=c.getLong(0);c.close();return n;}catch(Exception e){return 0;}}
    private String money657(long n){return String.format(new Locale("tr","TR"),"₺%,d",n).replace(',','.');}

    private void showStatus657(String status,String title){page="LIST";base(title,true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.athletes("",status);int n=0;while(c.moveToNext()){row(b,a(c),null,0);n++;}c.close();if(n==0)b.addView(tv("Bu grupta sporcu bulunmuyor.",14,Color.DKGRAY,true));}
    private void showTarget657(){page="LIST";base("AYLIK HEDEF",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.athletes("","AKTİF");long total=0;while(c.moveToNext()){A x=a(c);row(b,x,"AYLIK AİDAT",x.fee);total+=x.fee;}c.close();TextView sum=tv("TOPLAM AYLIK HEDEF: "+money657(total),15,Color.rgb(160,112,12),true);sum.setGravity(Gravity.CENTER);b.addView(sum,0,new LinearLayout.LayoutParams(-1,dp(52)));}

    private void calculateOverdueAsync657(int gen){dashExec657.execute(()->{int count=0,total=0;Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);while(c.moveToNext()){int d=overdueAmount657(c.getLong(0));if(d>0){count++;total+=d;}}c.close();final int fc=count,ft=total;runOnUiThread(()->{if(gen!=dashGen657||root==null||!"HOME".equals(page))return;if(overdueValue657!=null)overdueValue657.setText(money657(ft));if(overdueSub657!=null)overdueSub657.setText(fc+" sporcu");});});}
    private int overdueAmount657(long id){Cursor ac=db.athlete(id);if(!ac.moveToFirst()){ac.close();return 0;}String start=s657(ac,"startDate"),end=s657(ac,"endDate"),restart=s657(ac,"restartDate"),sib=s657(ac,"sibling");ac.close();if("BURSLU".equalsIgnoreCase(sib)||start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;Calendar today=Calendar.getInstance();int anchor=anchorDay(start),todayKey=currentCycleKey(today,anchor);Calendar todayCycleStart=cycleDate(todayKey,anchor);int first=registrationMonth(start,id,todayKey),reg=parseMonthKey(start);if(reg>0&&reg>first)first=reg;if(first>=todayKey)return 0;HashMap<Integer,PayRec> pays=paymentMap(id);int debt=0,guard=0;for(int key=first;guard++<240;key=shiftMonth(key,1)){Calendar cycleStart=cycleDate(key,anchor);if(!cycleStart.before(todayCycleStart))break;int y=key/100,m=key%100;if(!activeAt(y,m,start,end,restart))continue;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);if("X".equals(r.marker))continue;int expected=expectedFeeAt(id,y,m,r);if(expected<=0)continue;debt+=Math.max(0,expected-r.amount);}return debt;}
    private String s657(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
    private void showOverdue657(){page="OVERDUE_657";base("GECİKMİŞ SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){A x=a(c);int debt=overdueAmount657(x.id);if(debt<=0)continue;row(b,x,"SON VADESİ GEÇMİŞ DÖNEM BORCU",debt);n++;}c.close();if(n==0)b.addView(tv("Gecikmiş borcu bulunan sporcu yok.",14,Color.DKGRAY,true));}
    private void showMaterial657(){page="MATERIAL_DEBT_657";base("MALZEME BORCU",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT a.*,COALESCE(SUM(m.total-m.paidAmount),0) AS due FROM athletes a JOIN material_transactions m ON m.athleteId=a.id WHERE m.paidAmount<m.total GROUP BY a.id ORDER BY a.name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){A x=a(c);int due=c.getInt(c.getColumnIndexOrThrow("due"));row(b,x,"ÖDENMEMİŞ MALZEME BORCU",due);n++;}c.close();if(n==0)b.addView(tv("Ödenmemiş malzeme borcu yok.",14,Color.DKGRAY,true));}

    @Override void goBack(){if("OVERDUE_657".equals(page)||"MATERIAL_DEBT_657".equals(page)){showHome();return;}super.goBack();}
    private ScrollView findScroll657(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll657(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm657(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    @Override protected void onDestroy(){dashGen657++;dashExec657.shutdownNow();super.onDestroy();}
}
