package com.parion.aidat;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.1.48 - restart creates a new dues anchor; second pause closes the restarted spell. */
public class MainActivityV724 extends MainActivityV723 {
    private static final int GOLD724=Color.rgb(205,156,34), BLACK724=Color.rgb(25,25,25), GREEN724=Color.rgb(39,134,82), RED724=Color.rgb(196,63,63), ORANGE724=Color.rgb(205,132,44);
    private View financeRoot724;

    @Override void base(String title,boolean back){
        super.base(title,back);
        if("FİNANS".equalsIgnoreCase(title)&&root!=null)root.postDelayed(this::patchFinance724,80);
    }

    private void patchFinance724(){
        if(root==null||!"FINANCE_720".equals(page))return;
        int dueCount=0,overCount=0,overTotal=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);
        while(c.moveToNext()){
            long id=c.getLong(0);int due=currentDue724(id),over=overdueAmount724(id);
            if(due>0)dueCount++;if(over>0){overCount++;overTotal+=over;}
        }c.close();
        View dueCard=findMetric724(root,"ÖDEME VADESİ GELENLER");
        if(dueCard!=null){setMetric724(dueCard,String.valueOf(dueCount),dueCount+" sporcu");dueCard.setOnClickListener(v->{financeRoot724=root;showDue724();});}
        View overCard=findMetric724(root,"GECİKMİŞ");
        if(overCard!=null){setMetric724(overCard,money(overTotal),overCount+" sporcu");overCard.setOnClickListener(v->{financeRoot724=root;showOverdue724();});}
    }

    private View findMetric724(View v,String exact){
        if(v instanceof TextView&&exact.equalsIgnoreCase(String.valueOf(((TextView)v).getText()).trim())){ViewParent p=v.getParent();return p instanceof View?(View)p:null;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findMetric724(g.getChildAt(i),exact);if(r!=null)return r;}}return null;
    }
    private void setMetric724(View card,String value,String sub){
        if(!(card instanceof ViewGroup))return;ArrayList<TextView> ts=new ArrayList<>();collectTexts724(card,ts);if(ts.size()>0)ts.get(0).setText(value);if(ts.size()>2)ts.get(2).setText(sub);
    }
    private void collectTexts724(View v,ArrayList<TextView> out){if(v instanceof TextView)out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectTexts724(g.getChildAt(i),out);}}

    private int currentDue724(long id){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return 0;}
        String start=s724(a,"startDate"),end=s724(a,"endDate"),restart=s724(a,"restartDate"),sib=s724(a,"sibling"),restartEnd=db.restartEndDate(id);a.close();
        if("BURSLU".equalsIgnoreCase(sib)||start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;
        Calendar now=Calendar.getInstance();int anchor=currentAnchor605(now,start,restart),key=currentCycleKey(now,anchor);Calendar due=athleteCycleDate605(key,start,restart);
        if(due.after(now)||!activeAt605(key/100,key%100,start,end,restart,restartEnd))return 0;
        PayRec r=paymentMap(id).get(key);if(r==null)r=new PayRec("",0);if("X".equals(r.marker))return 0;int expected=expectedFeeAt(id,key/100,key%100,r);return expected<=0?0:Math.max(0,expected-r.amount);
    }

    private int overdueAmount724(long id){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return 0;}
        String start=s724(a,"startDate"),end=s724(a,"endDate"),restart=s724(a,"restartDate"),sib=s724(a,"sibling"),restartEnd=db.restartEndDate(id);a.close();
        if("BURSLU".equalsIgnoreCase(sib)||start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;
        Calendar now=Calendar.getInstance();int currentAnchor=currentAnchor605(now,start,restart),currentKey=currentCycleKey(now,currentAnchor);Calendar currentStart=athleteCycleDate605(currentKey,start,restart);
        int first=registrationMonth(start,id,currentKey),reg=parseMonthKey(start);if(reg>0&&reg>first)first=reg;if(first>=currentKey)return 0;
        HashMap<Integer,PayRec> pays=paymentMap(id);int debt=0,guard=0;
        for(int key=first;guard++<240;key=shiftMonth(key,1)){
            Calendar cycleStart=athleteCycleDate605(key,start,restart);if(!cycleStart.before(currentStart))break;
            int y=key/100,m=key%100;if(!activeAt605(y,m,start,end,restart,restartEnd))continue;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);if("X".equals(r.marker))continue;int expected=expectedFeeAt(id,y,m,r);if(expected>0)debt+=Math.max(0,expected-r.amount);
        }return debt;
    }

    private void showDue724(){
        page="FIN_DUE_724";base("ÖDEME VADESİ GELENLER",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);int n=0;
        while(c.moveToNext()){A x=a(c);int due=currentDue724(x.id);if(due<=0)continue;row(b,x,"ÖDEME VADESİ GELDİ",due);n++;}c.close();if(n==0)b.addView(tv("Ödeme vadesi gelen sporcu bulunmuyor.",14,Color.DKGRAY,true));
    }
    private void showOverdue724(){
        page="FIN_OVERDUE_724";base("GECİKMİŞ SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);int n=0;
        while(c.moveToNext()){A x=a(c);int debt=overdueAmount724(x.id);if(debt<=0)continue;row(b,x,"TAMAMLANMIŞ DÖNEM BORCU",debt);n++;}c.close();if(n==0)b.addView(tv("Gecikmiş borcu bulunan sporcu yok.",14,Color.DKGRAY,true));
    }

    @Override void addCycleProfileRow(LinearLayout b,long id,int key,int ignoredAnchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int yr=key/100,mo=key%100,effectiveAnchor=cycleAnchor605(key,start,restart);PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);String restartEnd=db.restartEndDate(id);boolean active=activeAt605(yr,mo,start,end,restart,restartEnd);
        String status,detail;int color;String period=cycleDateLabel(key,effectiveAnchor)+" – "+cycleDateLabel(shiftMonth(key,1),effectiveAnchor);
        if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);detail=period+" • AKTİF DEĞİL";}
        else if(future){status="BEKLİYOR";color=Color.WHITE;detail=period+" • "+(expected>0?money(expected):"—");}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=status.equals("ÖDENDİ")?Color.rgb(9,242,153):ORANGE724;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?Color.rgb(9,242,153):ORANGE724;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";}
        else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);detail=period+" • VERİ YOK";}
        else{status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true));row.addView(tv(detail,12,Color.DKGRAY,false));
        if(!future&&yr==2026){final int mm=mo,fee=expected,amt=r.amount;final String mk=r.marker;row.setOnClickListener(v->editPayment(id,mm,fee,mk,amt));}LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }

    @Override void shareReport(long id){
        final String[] labels={"3 AY","6 AY","1 YIL"};final int[] months={3,6,12};new AlertDialog.Builder(this).setTitle("ÖDEME BİLGİSİ PAYLAŞ").setItems(labels,(d,w)->generateReport724(id,months[w],labels[w])).setNegativeButton("VAZGEÇ",null).show();
    }

    private void generateReport724(long id,int monthCount,String filterLabel){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s724(a,"name"),category=s724(a,"category"),sibling=s724(a,"sibling"),start=s724(a,"startDate"),end=s724(a,"endDate"),restart=s724(a,"restartDate"),restartEnd=db.restartEndDate(id),motherPhone=s724(a,"motherPhone"),fatherPhone=s724(a,"fatherPhone");
        int by=a.getInt(a.getColumnIndexOrThrow("birthYear")),baseFee=a.getInt(a.getColumnIndexOrThrow("monthlyFee"));a.close();
        HashMap<Integer,PayRec> pays=paymentMap(id);Calendar now=Calendar.getInstance();Calendar first=(Calendar)now.clone();first.set(Calendar.DAY_OF_MONTH,1);first.add(Calendar.MONTH,-(monthCount-1));Calendar last=(Calendar)now.clone();last.set(Calendar.DAY_OF_MONTH,1);last.add(Calendar.MONTH,1);
        ArrayList<Integer> keys=new ArrayList<>();for(Calendar x=(Calendar)first.clone();!x.after(last);x.add(Calendar.MONTH,1))keys.add(x.get(Calendar.YEAR)*100+x.get(Calendar.MONTH)+1);
        int accrued=0,collected=0;
        for(int key:keys){int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);collected+=Math.max(0,r.amount);int exp=expectedFeeAt(id,yr,mo,r);if(exp<=0&&yr==2026)exp=baseFee;if("X".equals(r.marker)||!activeAt605(yr,mo,start,end,restart,restartEnd)||"BURSLU".equalsIgnoreCase(sibling)||exp<=0)continue;Calendar due=athleteCycleDate605(key,start,restart);if(!due.after(now))accrued+=exp;}
        int balance=accrued-collected;long[] mat=materialSummary724(id);long matBalance=Math.max(0,mat[1]-mat[2]);int currentAnchor=currentAnchor605(now,start,restart),cycle=currentCycleKey(now,currentAnchor);Calendar dueStart=athleteCycleDate605(cycle,start,restart),dueEnd=athleteCycleDate605(shiftMonth(cycle,1),start,restart);
        SimpleDateFormat df=new SimpleDateFormat("dd.MM.yyyy",TR),longDf=new SimpleDateFormat("dd MMMM yyyy",TR);String periodLabel=monthYear724(keys.get(0))+" – "+monthYear724(keys.get(keys.size()-1));
        int W=1080,H=1050+keys.size()*94;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setColor(Color.DKGRAY);q.setTextSize(17);c.drawText("Oluşturma Tarihi: "+df.format(new Date()),52,34,q);q.setColor(BLACK724);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(45);c.drawText("PARİON SPOR KULÜBÜ",52,84,q);q.setColor(GOLD724);q.setTextSize(33);c.drawText("AİDAT HESAP EKSTRESİ",52,132,q);q.setColor(BLACK724);q.setTextSize(25);c.drawText("Sporcu: "+name,52,188,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(21);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Filtre: "+filterLabel,52,226,q);c.drawText("Dönem: "+periodLabel+"   Güncel Aylık Aidat: "+money(baseFee),52,258,q);
        int y=330;for(int key:keys){int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);if(expected<=0&&yr==2026)expected=baseFee;int fill=reportColor724(yr,mo,expected,sibling,start,end,restart,restartEnd,r,now);q.setColor(fill);c.drawRoundRect(40,y-34,1040,y+50,14,14,q);q.setColor(BLACK724);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText(monthYear724(key),58,y+2,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(19);c.drawText(reportLine724(yr,mo,expected,sibling,start,end,restart,restartEnd,r,now),260,y+2,q);y+=92;}
        y+=14;q.setColor(BLACK724);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(26);c.drawText("Dönem Tahakkuku: "+money(accrued),52,y,q);y+=39;q.setColor(GREEN724);c.drawText("Toplam Tahsilat: "+money(collected),52,y,q);y+=39;q.setColor(balance>0?RED724:BLACK724);c.drawText("Aidat Bakiyesi: "+money(balance),52,y,q);y+=55;
        q.setColor(Color.rgb(245,245,245));c.drawRoundRect(40,y-26,1040,y+106,14,14,q);q.setColor(BLACK724);q.setTextSize(21);c.drawText("TİŞÖRT / MALZEME TAHSİLAT ÖZETİ",52,y,q);y+=36;q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);c.drawText("Verilen tişört: "+mat[0]+" adet  •  Malzeme tutarı: "+money((int)mat[1])+"  •  Tahsil edilen: "+money((int)mat[2]),52,y,q);y+=32;q.setColor(matBalance>0?RED724:GREEN724);q.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Kalan tişört/malzeme borcu: "+money((int)matBalance),52,y,q);y+=68;
        q.setColor(BLACK724);q.setTextSize(20);c.drawText("Sayın Velimiz,",52,y,q);y+=32;q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);c.drawText(monthName(cycle%100)+" ayı aidat ödeme aralığınız "+longDf.format(dueStart.getTime())+" – "+longDf.format(dueEnd.getTime())+"'dir.",52,y,q);y+=30;c.drawText("Aidat takibiniz başlangıç veya yeniden başlama gününe göre aylık olarak hesaplanmaktadır.",52,y,q);y+=50;q.setColor(Color.DKGRAY);q.setTextSize(16);c.drawText("Bu ekstre bilgilendirme amaçlıdır; mali belge veya tahsilat makbuzu niteliğinde değildir.",52,y,q);
        try{ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"Parion_Aidat_Ekstresi_"+filterLabel.replace(' ','_')+"_"+name.replace(' ','_')+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Parion");Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("Dosya oluşturulamadı");OutputStream os=getContentResolver().openOutputStream(uri);bm.compress(Bitmap.CompressFormat.PNG,100,os);if(os!=null)os.close();showShareTarget724(uri,name,motherPhone,fatherPhone);}catch(Exception e){Toast.makeText(this,"Ekstre oluşturulamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}finally{bm.recycle();}
    }

    private String reportLine724(int yr,int mo,int fee,String sibling,String start,String end,String restart,String restartEnd,PayRec r,Calendar now){
        if(yr<2026&&r.amount<=0&&(r.marker==null||r.marker.trim().isEmpty()))return "Veri yok";if("X".equals(r.marker))return "Tahakkuk yok • Ara verilen dönem";if(!activeAt605(yr,mo,start,end,restart,restartEnd))return "Tahakkuk yok • Aktif olunmayan dönem";if("BURSLU".equalsIgnoreCase(sibling)||fee==0)return yr<2026?"Veri yok":"Tahakkuk yok • Burslu";if(isDate(r.marker))return "Tahsil edildi • "+dateTr(r.marker)+" • "+money(r.amount)+(r.amount==fee?"":r.amount<fee?" • Eksik tahsilat":" • Fazla tahsilat");if(("!".equals(r.marker)||"!!".equals(r.marker))&&r.amount>0)return "Tahsilat kaydı • "+money(r.amount)+(r.amount<fee?" • Eksik tahsilat":r.amount>fee?" • Fazla tahsilat":" • Tam tahsilat");int key=yr*100+mo;Calendar due=athleteCycleDate605(key,start,restart);if(due.after(now))return "Henüz tahakkuk etmedi • "+money(fee);Calendar next=athleteCycleDate605(shiftMonth(key,1),start,restart);if(!next.after(now))return "Vadesi geçmiş • Tahsil edilmedi • "+money(fee);return "Tahakkuk etti • Tahsil edilmedi • "+money(fee);
    }
    private int reportColor724(int yr,int mo,int fee,String sibling,String start,String end,String restart,String restartEnd,PayRec r,Calendar now){
        if(yr<2026&&r.amount<=0&&(r.marker==null||r.marker.trim().isEmpty()))return Color.rgb(230,230,230);if("X".equals(r.marker)||!activeAt605(yr,mo,start,end,restart,restartEnd)||"BURSLU".equalsIgnoreCase(sibling)||fee==0)return Color.rgb(230,230,230);if(isDate(r.marker))return r.amount==fee?Color.rgb(232,247,236):Color.rgb(255,167,20);Calendar due=athleteCycleDate605(yr*100+mo,start,restart);if(due.after(now))return Color.WHITE;Calendar next=athleteCycleDate605(shiftMonth(yr*100+mo,1),start,restart);if(!next.after(now))return Color.rgb(255,230,230);return Color.rgb(255,245,190);
    }

    private void showShareTarget724(Uri uri,String athleteName,String motherPhone,String fatherPhone){String[] options={"ANNEYE WHATSAPP","BABAYA WHATSAPP","DİĞER PAYLAŞIM"};new AlertDialog.Builder(this).setTitle("PAYLAŞIM YÖNTEMİ").setItems(options,(d,w)->{if(w==0)shareWhatsApp724(uri,athleteName,motherPhone,"Anne");else if(w==1)shareWhatsApp724(uri,athleteName,fatherPhone,"Baba");else shareOther724(uri,athleteName);}).setNegativeButton("VAZGEÇ",null).show();}
    private void shareWhatsApp724(Uri uri,String athleteName,String rawPhone,String parentLabel){String phone=normalizePhone724(rawPhone);if(phone.isEmpty()){Toast.makeText(this,parentLabel+" telefon numarası profilde kayıtlı değil.",Toast.LENGTH_LONG).show();return;}Intent sh=new Intent(Intent.ACTION_SEND);sh.setType("image/png");sh.putExtra(Intent.EXTRA_STREAM,uri);sh.putExtra(Intent.EXTRA_TEXT,"PARİON SPOR KULÜBÜ - "+athleteName+" aidat hesap ekstresi");sh.putExtra("jid",phone+"@s.whatsapp.net");sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{sh.setPackage("com.whatsapp");startActivity(sh);}catch(Exception e1){try{sh.setPackage("com.whatsapp.w4b");startActivity(sh);}catch(Exception e2){Toast.makeText(this,"WhatsApp açılamadı. 'Diğer Paylaşım' seçeneğini kullanabilirsiniz.",Toast.LENGTH_LONG).show();}}}
    private void shareOther724(Uri uri,String athleteName){Intent sh=new Intent(Intent.ACTION_SEND);sh.setType("image/png");sh.putExtra(Intent.EXTRA_STREAM,uri);sh.putExtra(Intent.EXTRA_TEXT,"PARİON SPOR KULÜBÜ - "+athleteName+" aidat hesap ekstresi");sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(sh,"Ödeme bilgisini paylaş"));}
    private String normalizePhone724(String raw){if(raw==null)return "";String d=raw.replaceAll("[^0-9]","");if(d.isEmpty())return "";if(d.startsWith("0090"))d=d.substring(2);if(d.startsWith("90")&&d.length()==12)return d;if(d.startsWith("0")&&d.length()==11)d=d.substring(1);if(d.length()==10&&d.startsWith("5"))return "90"+d;return d.length()>=10?d:"";}
    private long[] materialSummary724(long id){long qty=0,total=0,paid=0;Cursor c=null;try{c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(CASE WHEN UPPER(product) LIKE '%TİŞÖRT%' THEN qty ELSE 0 END),0),COALESCE(SUM(total),0),COALESCE(SUM(paidAmount),0) FROM material_transactions WHERE athleteId=?",new String[]{String.valueOf(id)});if(c.moveToFirst()){qty=c.getLong(0);total=c.getLong(1);paid=c.getLong(2);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return new long[]{qty,total,paid};}
    private String monthYear724(int key){return monthName(key%100)+" "+(key/100);}
    private String s724(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}

    @Override void goBack(){
        if(("FIN_DUE_724".equals(page)||"FIN_OVERDUE_724".equals(page))&&financeRoot724!=null){setContentView(financeRoot724);root=(LinearLayout)financeRoot724;page="FINANCE_720";currentAthlete=-1;root.post(this::patchFinance724);return;}
        super.goBack();
    }
}
