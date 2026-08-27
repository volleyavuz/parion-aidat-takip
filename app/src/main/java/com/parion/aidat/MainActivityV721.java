package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.1.44 - finance dashboard cleanup + due-date based statement accrual. */
public class MainActivityV721 extends MainActivityV720 {
    private static final int GOLD721=Color.rgb(205,156,34), BLACK721=Color.rgb(25,25,25);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::patchHome721,12250);
            root.postDelayed(this::patchHome721,13450);
            root.postDelayed(this::patchHome721,14800);
        }
    }

    private void patchHome721(){
        if(root==null||!"HOME".equalsIgnoreCase(page))return;
        removeStandaloneEarly721(root);
        View finance=findTag721(root,"v718-finance-entry");
        if(finance!=null){
            ImageView icon=findFirstImage721(finance);
            if(icon!=null){
                icon.setImageResource(android.R.drawable.ic_menu_view);
                icon.setColorFilter(GOLD721);
                icon.setRotation(0f);
                icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }
    }

    private void removeStandaloneEarly721(View v){
        if(!(v instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);
            if("v718-finance-entry".equals(c.getTag()))continue;
            String n=norm721(text721(c));
            if((n.contains("ERKEN ÖDEME GİR")||n.contains("EKSİK ÖDEME GİR"))&&!n.contains("FİNANS")){
                View target=dashboardCard721(c,g);
                if(target!=null&&target.getParent() instanceof ViewGroup){((ViewGroup)target.getParent()).removeView(target);continue;}
            }
            removeStandaloneEarly721(c);
        }
    }

    private View dashboardCard721(View v,ViewGroup fallback){
        View cur=v,best=v;
        while(cur.getParent() instanceof ViewGroup){
            ViewGroup p=(ViewGroup)cur.getParent();
            if("v718-finance-entry".equals(cur.getTag()))return null;
            if(cur.isClickable()||cur.hasOnClickListeners())best=cur;
            if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.VERTICAL&&p.getParent() instanceof ScrollView)return cur;
            if(p==fallback)break;
            cur=p;
        }
        return best;
    }

    @Override void shareReport(long id){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s721(a,"name"),category=s721(a,"category"),sibling=s721(a,"sibling"),start=s721(a,"startDate"),end=s721(a,"endDate"),restart=s721(a,"restartDate");
        int by=a.getInt(a.getColumnIndexOrThrow("birthYear")),baseFee=a.getInt(a.getColumnIndexOrThrow("monthlyFee"));a.close();
        HashMap<Integer,String[]> ps=new HashMap<>();Cursor p=db.payments(id);int collected=0;
        while(p.moveToNext()){int m=p.getInt(p.getColumnIndexOrThrow("month")),amt=p.getInt(p.getColumnIndexOrThrow("amount"));ps.put(m,new String[]{s721(p,"marker"),String.valueOf(amt)});collected+=amt;}p.close();

        Calendar now=Calendar.getInstance();int anchor=start!=null&&start.matches("\\d{4}-\\d{2}-\\d{2}")?anchorDay(start):now.get(Calendar.DAY_OF_MONTH);
        int accrued=0;
        for(int m=1;m<=12;m++){
            String[] x=ps.get(m);String marker=x==null?"":x[0];
            if("X".equals(marker)||"BURSLU".equalsIgnoreCase(sibling)||!activeMonth(m,start,end,restart))continue;
            Calendar due=cycleDate(202600+m,anchor);
            if(due.after(now))continue;
            int ef=db.expectedFee(id,m);if(ef<=0)ef=baseFee;accrued+=Math.max(0,ef);
        }
        int balance=accrued-collected;
        long[] mat=materialSummary721(id);long shirtQty=mat[0],matTotal=mat[1],matPaid=mat[2],matBalance=Math.max(0,matTotal-matPaid);
        int cycle=currentCycleKey(now,anchor);Calendar dueStart=cycleDate(cycle,anchor),dueEnd=cycleDate(shiftMonth(cycle,1),anchor);
        SimpleDateFormat df=new SimpleDateFormat("dd.MM.yyyy",TR);SimpleDateFormat longDf=new SimpleDateFormat("dd MMMM yyyy",TR);

        int W=1080,H=2080;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setColor(Color.DKGRAY);q.setTypeface(Typeface.DEFAULT);q.setTextSize(17);c.drawText("Oluşturma Tarihi: "+df.format(new Date()),52,35,q);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setColor(BLACK721);q.setTextSize(46);c.drawText("PARİON SPOR KULÜBÜ",52,86,q);
        q.setColor(GOLD721);q.setTextSize(34);c.drawText("2026 AİDAT HESAP EKSTRESİ",52,136,q);
        q.setColor(BLACK721);q.setTextSize(26);c.drawText("Sporcu: "+name,52,196,q);
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(22);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Güncel Aylık Aidat: "+money(baseFee),52,234,q);
        int y=306;
        for(int m=1;m<=12;m++){
            String[] x=ps.get(m);String marker=x==null?"":x[0];int amt=x==null?0:Integer.parseInt(x[1]);int expected=db.expectedFee(id,m);if(expected<=0)expected=baseFee;
            int color=paymentColor(m,expected,sibling,start,end,restart,marker,amt);q.setColor(color);c.drawRoundRect(40,y-34,1040,y+50,14,14,q);
            q.setColor(BLACK721);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(23);c.drawText(monthName(m),58,y+2,q);
            q.setTypeface(Typeface.DEFAULT);q.setTextSize(20);c.drawText(accountingLine721(m,expected,sibling,start,end,restart,marker,amt,anchor,now),245,y+2,q);y+=92;
        }
        y+=12;q.setColor(BLACK721);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(27);c.drawText("Dönem Tahakkuku: "+money(accrued),52,y,q);y+=40;
        q.setColor(Color.rgb(39,134,82));c.drawText("Toplam Tahsilat: "+money(collected),52,y,q);y+=40;
        q.setColor(balance>0?Color.rgb(196,63,63):BLACK721);c.drawText("Aidat Bakiyesi: "+money(balance),52,y,q);y+=58;
        q.setColor(Color.rgb(245,245,245));c.drawRoundRect(40,y-28,1040,y+108,14,14,q);q.setColor(BLACK721);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText("TİŞÖRT / MALZEME TAHSİLAT ÖZETİ",52,y,q);y+=38;
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(19);c.drawText("Verilen tişört: "+shirtQty+" adet  •  Malzeme tutarı: "+money((int)matTotal)+"  •  Tahsil edilen: "+money((int)matPaid),52,y,q);y+=34;
        q.setColor(matBalance>0?Color.rgb(196,63,63):Color.rgb(39,134,82));q.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Kalan tişört/malzeme borcu: "+money((int)matBalance),52,y,q);y+=72;
        q.setColor(BLACK721);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(20);c.drawText("Sayın Velimiz,",52,y,q);y+=34;
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);String period=monthName(cycle%100)+" ayı aidat ödeme aralığınız "+longDf.format(dueStart.getTime())+" – "+longDf.format(dueEnd.getTime())+"'dir.";c.drawText(period,52,y,q);y+=32;
        c.drawText("Aidat takibiniz sporcumuzun kulübe başlangıç gününe göre aylık olarak hesaplanmaktadır.",52,y,q);y+=54;
        q.setColor(Color.DKGRAY);q.setTextSize(17);c.drawText("Tahakkuk: vade başlangıç günü gelmiş dönem aidatı • Tahsilat: kaydedilen ödeme toplamı",52,y,q);y+=28;
        c.drawText("Bu ekstre bilgilendirme amaçlıdır; mali belge veya tahsilat makbuzu niteliğinde değildir.",52,y,q);
        try{ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"Parion_2026_Aidat_Hesap_Ekstresi_"+name.replace(' ','_')+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Parion");Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("Dosya oluşturulamadı");OutputStream os=getContentResolver().openOutputStream(uri);bm.compress(Bitmap.CompressFormat.PNG,100,os);if(os!=null)os.close();Intent sh=new Intent(Intent.ACTION_SEND);sh.setType("image/png");sh.putExtra(Intent.EXTRA_STREAM,uri);sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(sh,"Aidat hesap ekstresini paylaş"));}catch(Exception e){Toast.makeText(this,"Ekstre oluşturulamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}finally{bm.recycle();}
    }

    private String accountingLine721(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount,int anchor,Calendar now){
        if("X".equals(marker))return "Tahakkuk yok • Ara verilen dönem";
        if("BURSLU".equalsIgnoreCase(sibling)||fee==0)return "Tahakkuk yok • Burslu";
        if(!activeMonth(m,start,end,restart))return "Tahakkuk yok • Aktif olunmayan dönem";
        if(isDate(marker))return "Tahsil edildi • "+dateTr(marker)+" • "+money(amount)+(amount==fee?"":amount<fee?" • Eksik tahsilat":" • Fazla tahsilat");
        if(("!".equals(marker)||"!!".equals(marker))&&amount>0)return "Tahsilat kaydı • "+money(amount)+(amount<fee?" • Eksik tahsilat":amount>fee?" • Fazla tahsilat":" • Tam tahsilat");
        Calendar due=cycleDate(202600+m,anchor);
        if(due.after(now))return "Henüz tahakkuk etmedi • "+money(fee);
        Calendar next=cycleDate(shiftMonth(202600+m,1),anchor);
        if(!next.after(now))return "Vadesi geçmiş • Tahsil edilmedi • "+money(fee);
        return "Tahakkuk etti • Tahsil edilmedi • "+money(fee);
    }

    private long[] materialSummary721(long id){long qty=0,total=0,paid=0;Cursor c=null;try{c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(CASE WHEN UPPER(product) LIKE '%TİŞÖRT%' THEN qty ELSE 0 END),0),COALESCE(SUM(total),0),COALESCE(SUM(paidAmount),0) FROM material_transactions WHERE athleteId=?",new String[]{String.valueOf(id)});if(c.moveToFirst()){qty=c.getLong(0);total=c.getLong(1);paid=c.getLong(2);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return new long[]{qty,total,paid};}
    private String s721(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
    private View findTag721(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag721(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ImageView findFirstImage721(View v){if(v instanceof ImageView)return(ImageView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ImageView r=findFirstImage721(g.getChildAt(i));if(r!=null)return r;}}return null;}
    private String text721(View v){StringBuilder b=new StringBuilder();collectText721(v,b);return b.toString();}
    private void collectText721(View v,StringBuilder b){if(v instanceof TextView)b.append(' ').append(((TextView)v).getText());if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText721(g.getChildAt(i),b);}}
    private String norm721(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
