package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.OutputStream;
import java.util.*;

public class MainActivityV33 extends MainActivityV31 {
    static class PayRec { String marker=""; int amount=0; PayRec(String m,int a){marker=m==null?"":m;amount=a;} }

    @Override void showProfile(long id){
        super.showProfile(id);
        relabelReportButton(root);
    }

    void relabelReportButton(View v){
        if(v instanceof Button){Button b=(Button)v; if("YILLIK AİDAT RAPORUNU PAYLAŞ".contentEquals(b.getText())) b.setText("AİDAT BİLANÇOSU / PAYLAŞ");}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)relabelReportButton(g.getChildAt(i));}
    }

    @Override void shareReport(long id){
        String[] opts={"TÜMÜ","1 YIL","6 AY","3 AY"};
        new AlertDialog.Builder(this).setTitle("BİLANÇO ZAMAN ARALIĞI").setItems(opts,(d,w)->{
            int months=w==1?12:w==2?6:w==3?3:0;
            createRollingReport(id,months,opts[w]);
        }).setNegativeButton("İPTAL",null).show();
    }

    void createRollingReport(long id,int monthCount,String filterName){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s(a,"name"),category=s(a,"category"),sibling=s(a,"sibling"),start=s(a,"startDate"),end=s(a,"endDate"),restart=s(a,"restartDate");
        int by=a.getInt(a.getColumnIndexOrThrow("birthYear"));a.close();

        HashMap<Integer,PayRec> pays=new HashMap<>();Cursor p=db.payments(id);while(p.moveToNext()){
            int y=p.getInt(p.getColumnIndexOrThrow("year")),m=p.getInt(p.getColumnIndexOrThrow("month"));
            pays.put(y*100+m,new PayRec(s(p,"marker"),p.getInt(p.getColumnIndexOrThrow("amount"))));
        }p.close();

        Calendar now=Calendar.getInstance();int endKey=now.get(Calendar.YEAR)*100+(now.get(Calendar.MONTH)+1);
        int startKey;
        if(monthCount>0)startKey=shiftMonth(endKey,-(monthCount-1));
        else startKey=registrationMonth(start,id,endKey);
        int regKey=parseMonthKey(start);if(regKey>0&&regKey>startKey)startKey=regKey;
        if(startKey>endKey)startKey=endKey;

        ArrayList<Integer> months=new ArrayList<>();for(int k=startKey;;k=shiftMonth(k,1)){months.add(k);if(k==endKey)break;if(months.size()>240)break;}
        int rows=months.size();int W=1400,rowH=86,H=430+rows*rowH+220;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);

        q.setTypeface(Typeface.DEFAULT_BOLD);q.setColor(BLACK);q.setTextSize(52);c.drawText("PARION VOLEYBOL AKADEMİSİ",60,78,q);
        q.setColor(GOLD);q.setTextSize(38);c.drawText("AİDAT BİLANÇOSU",60,132,q);
        q.setColor(BLACK);q.setTextSize(28);c.drawText("Sporcu: "+name,60,192,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(23);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Filtre: "+filterName,60,232,q);
        c.drawText("Dönem: "+keyLabel(startKey)+" – "+keyLabel(endKey),60,268,q);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);q.setColor(Color.DKGRAY);c.drawText("AY",60,330,q);c.drawText("ÖDEME TARİHİ",300,330,q);c.drawText("BEKLENEN",610,330,q);c.drawText("ÖDENEN",830,330,q);c.drawText("DURUM",1050,330,q);
        q.setStrokeWidth(2);c.drawLine(60,350,W-60,350,q);

        int y=380,totalPaid=0,totalExpected=0;
        for(int key:months){int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeAt(yr,mo,start,end,restart);String status;int color;String date="—";
            if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;expected=0;}
            else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=ORANGE;}
            else if(isDate(r.marker)){date=dateTr(r.marker);status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?GREEN:ORANGE;}
            else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);expected=0;}
            else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);expected=0;}
            else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);}
            else {int cur=now.get(Calendar.YEAR)*100+(now.get(Calendar.MONTH)+1);if(key==cur){status="ÖDEME DÖNEMİ";color=YELLOW;}else{status="ÖDEME GECİKTİ";color=RED;}}

            if(expected>0&&active&&!"X".equals(r.marker))totalExpected+=expected;if(r.amount>0)totalPaid+=r.amount;
            q.setColor(color);c.drawRoundRect(48,y-38,W-48,y+34,16,16,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText(monthName(mo)+" "+yr,60,y,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(21);c.drawText(date,300,y,q);c.drawText(expected>0?money(expected):"—",610,y,q);c.drawText(r.amount>0?money(r.amount):"—",830,y,q);drawFit(c,q,status,1050,y,280);y+=rowH;
        }

        q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(27);c.drawText("TOPLAM BEKLENEN: "+money(totalExpected),60,y+35,q);c.drawText("TOPLAM ÖDENEN: "+money(totalPaid),520,y+35,q);int fark=totalPaid-totalExpected;q.setColor(fark<0?RED:fark>0?ORANGE:GREEN);c.drawText("FARK: "+(fark>0?"+":"")+money(fark),980,y+35,q);
        q.setColor(Color.DKGRAY);q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);c.drawText("Not: Kayıt tarihinden önceki aylar rapora dahil edilmez. Geçmiş dönemde veri bulunmayan aylar borç olarak değerlendirilmez.",60,y+90,q);
        saveAndShare(bm,name,filterName);
    }

    int currentMonthlyFee(long id){Cursor c=db.athlete(id);int x=0;if(c.moveToFirst())x=c.getInt(c.getColumnIndexOrThrow("monthlyFee"));c.close();return x;}
    int expectedFeeAt(long id,int year,int month,PayRec r){if(year==2026)return db.expectedFee(id,month);if(isDate(r.marker)&&r.amount>0)return r.amount;return 0;}
    boolean activeAt(int year,int month,String start,String end,String restart){String first=String.format(Locale.US,"%04d-%02d-01",year,month),last=String.format(Locale.US,"%04d-%02d-31",year,month);if(start!=null&&!start.isEmpty()&&start.matches("\\d{4}-\\d{2}-\\d{2}")&&start.compareTo(last)>0)return false;if(end==null||end.isEmpty()||"DEVAM".equalsIgnoreCase(end))return true;if(end.matches("\\d{4}-\\d{2}-\\d{2}")&&end.compareTo(first)>=0)return true;return restart!=null&&restart.matches("\\d{4}-\\d{2}-\\d{2}")&&restart.compareTo(last)<=0;}
    int registrationMonth(String start,long id,int fallback){int k=parseMonthKey(start);if(k>0)return k;Cursor c=db.getReadableDatabase().rawQuery("SELECT MIN(year*100+month) FROM payments WHERE athleteId=?",new String[]{String.valueOf(id)});if(c.moveToFirst()&&!c.isNull(0))k=c.getInt(0);c.close();return k>0?k:fallback;}
    int parseMonthKey(String iso){try{if(iso!=null&&iso.matches("\\d{4}-\\d{2}-\\d{2}"))return Integer.parseInt(iso.substring(0,4))*100+Integer.parseInt(iso.substring(5,7));}catch(Exception ignored){}return 0;}
    int shiftMonth(int key,int delta){Calendar c=Calendar.getInstance();c.clear();c.set(key/100,(key%100)-1,1);c.add(Calendar.MONTH,delta);return c.get(Calendar.YEAR)*100+c.get(Calendar.MONTH)+1;}
    String keyLabel(int key){return monthName(key%100)+" "+(key/100);}
    void drawFit(Canvas c,Paint p,String text,float x,float y,float max){String t=text;while(t.length()>3&&p.measureText(t)>max)t=t.substring(0,t.length()-2)+"…";c.drawText(t,x,y,p);}

    void saveAndShare(Bitmap bm,String name,String filter){try{ContentValues v=new ContentValues();String fn="Parion_Aidat_"+name.replaceAll("[^A-Za-z0-9ÇĞİÖŞÜçğıöşü]+","_")+"_"+System.currentTimeMillis()+".png";v.put(MediaStore.Images.Media.DISPLAY_NAME,fn);v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Parion");Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("Kayıt URI oluşturulamadı");try(OutputStream out=getContentResolver().openOutputStream(uri)){bm.compress(Bitmap.CompressFormat.PNG,100,out);}Intent send=new Intent(Intent.ACTION_SEND);send.setType("image/png");send.putExtra(Intent.EXTRA_STREAM,uri);send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(send,"Aidat bilançosunu paylaş"));}catch(Exception e){Toast.makeText(this,"Bilanço oluşturuldu ancak paylaşım açılamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
}
