package com.parion.aidat;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.1.47 - restore 3/6/12 month picker and add direct parent WhatsApp sharing. */
public class MainActivityV723 extends MainActivityV722 {
    private static final int GOLD723=Color.rgb(205,156,34), BLACK723=Color.rgb(25,25,25);

    @Override void shareReport(long id){
        final String[] labels={"3 AY","6 AY","1 YIL"};
        final int[] months={3,6,12};
        new AlertDialog.Builder(this)
            .setTitle("ÖDEME BİLGİSİ PAYLAŞ")
            .setItems(labels,(d,w)->generateReport723(id,months[w],labels[w]))
            .setNegativeButton("VAZGEÇ",null)
            .show();
    }

    private void generateReport723(long id,int monthCount,String filterLabel){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s723(a,"name"),category=s723(a,"category"),sibling=s723(a,"sibling"),start=s723(a,"startDate"),end=s723(a,"endDate"),restart=s723(a,"restartDate");
        String motherPhone=s723(a,"motherPhone"),fatherPhone=s723(a,"fatherPhone");
        int by=a.getInt(a.getColumnIndexOrThrow("birthYear")),baseFee=a.getInt(a.getColumnIndexOrThrow("monthlyFee"));a.close();

        HashMap<Integer,PayRec> pays=paymentMap(id);
        Calendar now=Calendar.getInstance();
        Calendar first=(Calendar)now.clone();first.set(Calendar.DAY_OF_MONTH,1);first.add(Calendar.MONTH,-(monthCount-1));
        Calendar last=(Calendar)now.clone();last.set(Calendar.DAY_OF_MONTH,1);last.add(Calendar.MONTH,1);
        ArrayList<Integer> keys=new ArrayList<>();for(Calendar x=(Calendar)first.clone();!x.after(last);x.add(Calendar.MONTH,1))keys.add(x.get(Calendar.YEAR)*100+x.get(Calendar.MONTH)+1);
        int anchor=start!=null&&start.matches("\\d{4}-\\d{2}-\\d{2}")?anchorDay(start):now.get(Calendar.DAY_OF_MONTH);

        int accrued=0,collected=0;
        for(int key:keys){
            int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);
            collected+=Math.max(0,r.amount);
            int exp=expectedFeeAt(id,yr,mo,r);if(exp<=0&&yr==2026)exp=baseFee;
            if("X".equals(r.marker)||!activeAt(yr,mo,start,end,restart)||"BURSLU".equalsIgnoreCase(sibling)||exp<=0)continue;
            Calendar due=cycleDate(key,anchor);
            if(!due.after(now))accrued+=exp;
        }
        int balance=accrued-collected;
        long[] mat=materialSummary723(id);long matBalance=Math.max(0,mat[1]-mat[2]);
        int cycle=currentCycleKey(now,anchor);Calendar dueStart=cycleDate(cycle,anchor),dueEnd=cycleDate(shiftMonth(cycle,1),anchor);
        SimpleDateFormat df=new SimpleDateFormat("dd.MM.yyyy",TR),longDf=new SimpleDateFormat("dd MMMM yyyy",TR);
        String periodLabel=monthYear723(keys.get(0))+" – "+monthYear723(keys.get(keys.size()-1));

        int W=1080,H=1050+keys.size()*94;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setColor(Color.DKGRAY);q.setTextSize(17);c.drawText("Oluşturma Tarihi: "+df.format(new Date()),52,34,q);
        q.setColor(BLACK723);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(45);c.drawText("PARİON SPOR KULÜBÜ",52,84,q);
        q.setColor(GOLD723);q.setTextSize(33);c.drawText("AİDAT HESAP EKSTRESİ",52,132,q);
        q.setColor(BLACK723);q.setTextSize(25);c.drawText("Sporcu: "+name,52,188,q);
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(21);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Filtre: "+filterLabel,52,226,q);c.drawText("Dönem: "+periodLabel+"   Güncel Aylık Aidat: "+money(baseFee),52,258,q);
        int y=330;
        for(int key:keys){
            int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);if(expected<=0&&yr==2026)expected=baseFee;
            int fill=reportColor723(yr,mo,expected,sibling,start,end,restart,r,anchor,now);
            q.setColor(fill);c.drawRoundRect(40,y-34,1040,y+50,14,14,q);q.setColor(BLACK723);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText(monthYear723(key),58,y+2,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(19);c.drawText(reportLine723(yr,mo,expected,sibling,start,end,restart,r,anchor,now),260,y+2,q);y+=92;
        }
        y+=14;q.setColor(BLACK723);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(26);c.drawText("Dönem Tahakkuku: "+money(accrued),52,y,q);y+=39;
        q.setColor(Color.rgb(39,134,82));c.drawText("Toplam Tahsilat: "+money(collected),52,y,q);y+=39;
        q.setColor(balance>0?Color.rgb(196,63,63):BLACK723);c.drawText("Aidat Bakiyesi: "+money(balance),52,y,q);y+=55;
        q.setColor(Color.rgb(245,245,245));c.drawRoundRect(40,y-26,1040,y+106,14,14,q);q.setColor(BLACK723);q.setTextSize(21);c.drawText("TİŞÖRT / MALZEME TAHSİLAT ÖZETİ",52,y,q);y+=36;
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);c.drawText("Verilen tişört: "+mat[0]+" adet  •  Malzeme tutarı: "+money((int)mat[1])+"  •  Tahsil edilen: "+money((int)mat[2]),52,y,q);y+=32;
        q.setColor(matBalance>0?Color.rgb(196,63,63):Color.rgb(39,134,82));q.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Kalan tişört/malzeme borcu: "+money((int)matBalance),52,y,q);y+=68;
        q.setColor(BLACK723);q.setTextSize(20);c.drawText("Sayın Velimiz,",52,y,q);y+=32;q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);
        c.drawText(monthName(cycle%100)+" ayı aidat ödeme aralığınız "+longDf.format(dueStart.getTime())+" – "+longDf.format(dueEnd.getTime())+"'dir.",52,y,q);y+=30;
        c.drawText("Aidat takibiniz sporcumuzun kulübe başlangıç gününe göre aylık olarak hesaplanmaktadır.",52,y,q);y+=50;q.setColor(Color.DKGRAY);q.setTextSize(16);
        c.drawText("Bu ekstre bilgilendirme amaçlıdır; mali belge veya tahsilat makbuzu niteliğinde değildir.",52,y,q);

        try{
            ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"Parion_Aidat_Ekstresi_"+filterLabel.replace(' ','_')+"_"+name.replace(' ','_')+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Parion");
            Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("Dosya oluşturulamadı");OutputStream os=getContentResolver().openOutputStream(uri);bm.compress(Bitmap.CompressFormat.PNG,100,os);if(os!=null)os.close();
            showShareTarget723(uri,name,motherPhone,fatherPhone);
        }catch(Exception e){Toast.makeText(this,"Ekstre oluşturulamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}finally{bm.recycle();}
    }

    private void showShareTarget723(Uri uri,String athleteName,String motherPhone,String fatherPhone){
        String[] options={"ANNEYE WHATSAPP","BABAYA WHATSAPP","DİĞER PAYLAŞIM"};
        new AlertDialog.Builder(this).setTitle("PAYLAŞIM YÖNTEMİ").setItems(options,(d,w)->{
            if(w==0)shareWhatsApp723(uri,athleteName,motherPhone,"Anne");
            else if(w==1)shareWhatsApp723(uri,athleteName,fatherPhone,"Baba");
            else shareOther723(uri,athleteName);
        }).setNegativeButton("VAZGEÇ",null).show();
    }

    private void shareWhatsApp723(Uri uri,String athleteName,String rawPhone,String parentLabel){
        String phone=normalizePhone723(rawPhone);
        if(phone.isEmpty()){Toast.makeText(this,parentLabel+" telefon numarası profilde kayıtlı değil.",Toast.LENGTH_LONG).show();return;}
        Intent sh=new Intent(Intent.ACTION_SEND);sh.setType("image/png");sh.putExtra(Intent.EXTRA_STREAM,uri);sh.putExtra(Intent.EXTRA_TEXT,"PARİON SPOR KULÜBÜ - "+athleteName+" aidat hesap ekstresi");sh.putExtra("jid",phone+"@s.whatsapp.net");sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try{sh.setPackage("com.whatsapp");startActivity(sh);}catch(Exception e1){
            try{sh.setPackage("com.whatsapp.w4b");startActivity(sh);}catch(Exception e2){Toast.makeText(this,"WhatsApp açılamadı. 'Diğer Paylaşım' seçeneğini kullanabilirsiniz.",Toast.LENGTH_LONG).show();}
        }
    }

    private void shareOther723(Uri uri,String athleteName){
        Intent sh=new Intent(Intent.ACTION_SEND);sh.setType("image/png");sh.putExtra(Intent.EXTRA_STREAM,uri);sh.putExtra(Intent.EXTRA_TEXT,"PARİON SPOR KULÜBÜ - "+athleteName+" aidat hesap ekstresi");sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(sh,"Ödeme bilgisini paylaş"));
    }

    private String normalizePhone723(String raw){
        if(raw==null)return "";String d=raw.replaceAll("[^0-9]","");if(d.isEmpty())return "";
        if(d.startsWith("0090"))d=d.substring(2);if(d.startsWith("90")&&d.length()==12)return d;if(d.startsWith("0")&&d.length()==11)d=d.substring(1);if(d.length()==10&&d.startsWith("5"))return "90"+d;return d.length()>=10?d:"";
    }

    private String reportLine723(int yr,int mo,int fee,String sibling,String start,String end,String restart,PayRec r,int anchor,Calendar now){
        if(yr<2026&&r.amount<=0&&(r.marker==null||r.marker.trim().isEmpty()))return "Veri yok";
        if("X".equals(r.marker))return "Tahakkuk yok • Ara verilen dönem";
        if(!activeAt(yr,mo,start,end,restart))return "Tahakkuk yok • Aktif olunmayan dönem";
        if("BURSLU".equalsIgnoreCase(sibling)||fee==0)return yr<2026?"Veri yok":"Tahakkuk yok • Burslu";
        if(isDate(r.marker))return "Tahsil edildi • "+dateTr(r.marker)+" • "+money(r.amount)+(r.amount==fee?"":r.amount<fee?" • Eksik tahsilat":" • Fazla tahsilat");
        if(("!".equals(r.marker)||"!!".equals(r.marker))&&r.amount>0)return "Tahsilat kaydı • "+money(r.amount)+(r.amount<fee?" • Eksik tahsilat":r.amount>fee?" • Fazla tahsilat":" • Tam tahsilat");
        int key=yr*100+mo;Calendar due=cycleDate(key,anchor);if(due.after(now))return "Henüz tahakkuk etmedi • "+money(fee);Calendar next=cycleDate(shiftMonth(key,1),anchor);if(!next.after(now))return "Vadesi geçmiş • Tahsil edilmedi • "+money(fee);return "Tahakkuk etti • Tahsil edilmedi • "+money(fee);
    }

    private int reportColor723(int yr,int mo,int fee,String sibling,String start,String end,String restart,PayRec r,int anchor,Calendar now){
        if(yr<2026&&r.amount<=0&&(r.marker==null||r.marker.trim().isEmpty()))return Color.rgb(230,230,230);if("X".equals(r.marker)||!activeAt(yr,mo,start,end,restart)||"BURSLU".equalsIgnoreCase(sibling)||fee==0)return Color.rgb(230,230,230);if(isDate(r.marker))return r.amount==fee?Color.rgb(232,247,236):Color.rgb(255,167,20);Calendar due=cycleDate(yr*100+mo,anchor);if(due.after(now))return Color.WHITE;Calendar next=cycleDate(shiftMonth(yr*100+mo,1),anchor);if(!next.after(now))return Color.rgb(255,230,230);return Color.rgb(255,245,190);
    }

    private long[] materialSummary723(long id){long qty=0,total=0,paid=0;Cursor c=null;try{c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(CASE WHEN UPPER(product) LIKE '%TİŞÖRT%' THEN qty ELSE 0 END),0),COALESCE(SUM(total),0),COALESCE(SUM(paidAmount),0) FROM material_transactions WHERE athleteId=?",new String[]{String.valueOf(id)});if(c.moveToFirst()){qty=c.getLong(0);total=c.getLong(1);paid=c.getLong(2);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return new long[]{qty,total,paid};}
    private String monthYear723(int key){return monthName(key%100)+" "+(key/100);}
    private String s723(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
