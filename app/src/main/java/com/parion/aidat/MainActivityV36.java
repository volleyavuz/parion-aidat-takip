package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.regex.*;

public class MainActivityV36 extends MainActivityV35 {
    static class DashItem {
        long id; String name,category,photo,detail; int amount,birthYear;
        DashItem(long i,String n,String c,String p,int b){id=i;name=n;category=c;photo=p;birthYear=b;detail="";}
    }
    static class DashData {
        LinkedHashMap<Long,DashItem> collected=new LinkedHashMap<>(), expected=new LinkedHashMap<>(), overdue=new LinkedHashMap<>(), upcoming=new LinkedHashMap<>();
        int collectedTotal,expectedTotal,overdueTotal,upcomingTotal;
    }

    @Override void showHome(){
        page="HOME";currentAthlete=-1;base("PARION SPOR OKULU",false);ScrollView sv=scroll();LinearLayout b=box(sv);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.parion_logo);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);b.addView(logo,new LinearLayout.LayoutParams(-1,dp(105)));
        DashData d=dashboardData();
        LinearLayout r1=new LinearLayout(this),r2=new LinearLayout(this);r1.setOrientation(LinearLayout.HORIZONTAL);r2.setOrientation(LinearLayout.HORIZONTAL);
        View c1=dashCard("TAHSİL EDİLEN",money(d.collectedTotal),GREEN,()->showDashList("TAHSİL EDİLEN",d.collected));
        View c2=dashCard("BEKLENEN",money(d.expectedTotal),GOLD,()->showDashList("BEKLENEN",d.expected));
        View c3=dashCard("GECİKMİŞ",money(d.overdueTotal),RED,()->showDashList("GECİKMİŞ",d.overdue));
        View c4=dashCard("AY SONUNA KADAR\nGELECEK",money(d.upcomingTotal),ORANGE,()->showDashList("AY SONUNA KADAR GELECEK",d.upcoming));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(112),1);cp.setMargins(dp(3),dp(3),dp(3),dp(3));r1.addView(c1,cp);r1.addView(c2,cp);r2.addView(c3,cp);r2.addView(c4,cp);b.addView(r1);b.addView(r2);
        TextView hint=tv("Kartlara dokunarak ilgili sporcuları ve tutar detaylarını görebilirsiniz.",12,Color.DKGRAY,false);hint.setGravity(Gravity.CENTER);b.addView(hint);
        Button athletes=btn("SPORCULAR");athletes.setOnClickListener(v->showAthletes());LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(58));ap.setMargins(0,dp(8),0,0);b.addView(athletes,ap);
    }

    View dashCard(String label,String value,int color,Runnable action){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setPadding(dp(5),dp(7),dp(5),dp(7));x.setBackground(round(Color.WHITE,12));TextView v=tv(value,19,color,true);v.setGravity(Gravity.CENTER);TextView l=tv(label,10,Color.DKGRAY,true);l.setGravity(Gravity.CENTER);x.addView(v);x.addView(l);x.setClickable(true);x.setOnClickListener(z->action.run());return x;}

    DashData dashboardData(){
        DashData d=new DashData();Calendar now=Calendar.getInstance();int cy=now.get(Calendar.YEAR),cm=now.get(Calendar.MONTH)+1;int currentCalendarKey=cy*100+cm;Calendar monthStart=(Calendar)now.clone();monthStart.set(Calendar.DAY_OF_MONTH,1);zeroTime(monthStart);
        Cursor c=db.athletes("","TÜMÜ");while(c.moveToNext()){
            long id=c.getLong(c.getColumnIndexOrThrow("id"));String name=s(c,"name"),cat=s(c,"category"),photo=s(c,"photo"),start=s(c,"startDate"),end=s(c,"endDate"),restart=s(c,"restartDate"),sib=s(c,"sibling");int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
            int anchor=anchorDay(start);HashMap<Integer,PayRec> pays=paymentMap(id);
            // Bu takvim ayında gerçekten tahsil edilenler.
            int paidMonth=0;for(Map.Entry<Integer,PayRec> e:pays.entrySet()){PayRec pr=e.getValue();if(pr.amount<=0)continue;if(isDate(pr.marker)){Calendar pd=parseIsoCal(pr.marker);if(pd!=null&&pd.get(Calendar.YEAR)==cy&&pd.get(Calendar.MONTH)+1==cm&& !pd.after(now))paidMonth+=pr.amount;}else if(e.getKey()==currentCalendarKey)paidMonth+=pr.amount;}
            if(paidMonth>0){DashItem z=item(id,name,cat,photo,by);z.amount=paidMonth;z.detail="Bu ay tahsil edilen: "+money(paidMonth);d.collected.put(id,z);d.collectedTotal+=paidMonth;}
            // Bu ayın kişisel ödeme günü: beklenen veya ay sonuna kadar gelecek.
            Calendar due=cycleDate(currentCalendarKey,anchor);boolean active=activeAt(cy,cm,start,end,restart);int exp=expectedFeeAt(id,cy,cm,pays.containsKey(currentCalendarKey)?pays.get(currentCalendarKey):new PayRec("",0));if("BURSLU".equalsIgnoreCase(sib))exp=0;
            if(active&&exp>0&&!due.before(monthStart)){
                if(!due.after(now)){DashItem z=item(id,name,cat,photo,by);z.amount=exp;z.detail="Vade: "+cycleDateLabel(currentCalendarKey,anchor)+" • Beklenen: "+money(exp);d.expected.put(id,z);d.expectedTotal+=exp;}
                else {DashItem z=item(id,name,cat,photo,by);z.amount=exp;z.detail="Vade: "+cycleDateLabel(currentCalendarKey,anchor)+" • Gelecek: "+money(exp);d.upcoming.put(id,z);d.upcomingTotal+=exp;}
            }
            // Bugün itibarıyla kapanmamış tüm bilinen gecikmiş dönemler (veri olan 2026 dönemleri).
            int reg=parseMonthKey(start);if(reg==0)reg=202601;int last=currentCycleKey(now,anchor);int late=0;for(int key=Math.max(reg,202601);key<=last;key=shiftMonth(key,1)){if(key/100!=2026)continue;Calendar kd=cycleDate(key,anchor);if(!kd.before(todayStart(now)))continue;int yy=key/100,mm=key%100;if(!activeAt(yy,mm,start,end,restart))continue;PayRec pr=pays.get(key);if(pr==null)pr=new PayRec("",0);if("X".equals(pr.marker))continue;int ex=expectedFeeAt(id,yy,mm,pr);if(ex<=0||"BURSLU".equalsIgnoreCase(sib))continue;int remain=Math.max(0,ex-pr.amount);late+=remain;if(key==last)break;}
            if(late>0){DashItem z=item(id,name,cat,photo,by);z.amount=late;z.detail="Toplam gecikmiş: "+money(late);d.overdue.put(id,z);d.overdueTotal+=late;}
        }c.close();return d;
    }

    DashItem item(long id,String name,String cat,String photo,int by){return new DashItem(id,name,cat,photo,by);}
    HashMap<Integer,PayRec> paymentMap(long id){HashMap<Integer,PayRec>x=new HashMap<>();Cursor p=db.payments(id);while(p.moveToNext()){int y=p.getInt(p.getColumnIndexOrThrow("year")),m=p.getInt(p.getColumnIndexOrThrow("month"));x.put(y*100+m,new PayRec(s(p,"marker"),p.getInt(p.getColumnIndexOrThrow("amount"))));}p.close();return x;}
    void zeroTime(Calendar c){c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);}
    Calendar todayStart(Calendar n){Calendar c=(Calendar)n.clone();zeroTime(c);return c;}
    Calendar parseIsoCal(String iso){try{if(!isDate(iso))return null;Calendar c=Calendar.getInstance();c.clear();c.set(Integer.parseInt(iso.substring(0,4)),Integer.parseInt(iso.substring(5,7))-1,Integer.parseInt(iso.substring(8,10)));return c;}catch(Exception e){return null;}}

    void showDashList(String title,LinkedHashMap<Long,DashItem> map){page="LIST";base(title,true);ScrollView sv=scroll();LinearLayout b=box(sv);if(map.isEmpty()){TextView n=tv("Bu grupta sporcu bulunmuyor.",15,Color.DKGRAY,true);n.setGravity(Gravity.CENTER);b.addView(n);return;}for(DashItem a:map.values()){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),dp(8),dp(8),dp(8));row.setBackground(round(Color.WHITE,10));ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,a.photo);row.addView(av,new LinearLayout.LayoutParams(dp(58),dp(58)));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((a.birthYear>0?a.birthYear+" • ":"")+a.name,15,BLACK,true));t.addView(tv(a.category,12,Color.DKGRAY,false));t.addView(tv(a.detail,12,Color.DKGRAY,false));row.addView(t,new LinearLayout.LayoutParams(0,-2,1));TextView amt=tv(money(a.amount),14,BLACK,true);amt.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);row.addView(amt,new LinearLayout.LayoutParams(dp(115),-1));final long aid=a.id;row.setOnClickListener(v->showProfile(aid));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);}}

    @Override void showProfile(long id){super.showProfile(id);makePhonesClickable(root);}
    void makePhonesClickable(View v){if(v instanceof TextView){TextView t=(TextView)v;String text=String.valueOf(t.getText());String phone=extractPhone(text);if(phone!=null){t.setTextColor(Color.rgb(0,90,180));t.setPaintFlags(t.getPaintFlags()|Paint.UNDERLINE_TEXT_FLAG);t.setClickable(true);t.setOnClickListener(x->openDialer(phone));}}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)makePhonesClickable(g.getChildAt(i));}}
    String extractPhone(String text){if(text==null)return null;Matcher m=Pattern.compile("(?:\\+?90\\s*)?(?:0?5\\d{2})[\\s.-]*\\d{3}[\\s.-]*\\d{2}[\\s.-]*\\d{2}").matcher(text);if(!m.find())return null;String p=m.group().replaceAll("[^0-9+]","");if(p.startsWith("90")&&!p.startsWith("+"))p="+"+p;if(p.matches("5\\d{9}"))p="0"+p;return p;}
    void openDialer(String phone){try{startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phone)));}catch(Exception e){Toast.makeText(this,"Arama ekranı açılamadı.",Toast.LENGTH_SHORT).show();}}
}
