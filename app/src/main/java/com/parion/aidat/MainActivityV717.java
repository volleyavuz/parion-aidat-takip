package com.parion.aidat;

import android.app.AlertDialog;
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

/** v4.1.40 - restore 3/6/12 month report picker and force one visible Finance container. */
public class MainActivityV717 extends MainActivityV716 {
    private static final int GOLD717=Color.rgb(205,156,34);
    private static final int TEXT717=Color.rgb(35,35,35);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::forceFinance717,1500);
            root.postDelayed(this::forceFinance717,9200);
            root.postDelayed(this::forceFinance717,10500);
        }
    }

    private void forceFinance717(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll717(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        String[] keys={"AYLIK HEDEF","BU AYKİ TAHSİLAT","ERKEN ÖDEME GİR","ÖDEME VADESİ GELENLER","GECİKMİŞ","MALZEME BORCU","SON ÖDEMELER"};
        LinkedHashMap<String,View> cards=new LinkedHashMap<>();
        int insert=box.getChildCount();
        for(String k:keys){
            TextView t=findText717(box,k);if(t==null)continue;
            View c=financeCard717(t,box);if(c==null)continue;
            cards.put(k,c);
            View top=topChild717(box,c);if(top!=null){int idx=box.indexOfChild(top);if(idx>=0)insert=Math.min(insert,idx);}
        }
        // Remove previous wrappers only after their child cards were captured.
        View old717=findTag717(box,"v717-finance-container");
        View old716=findTag717(box,"v716-finance-container");
        View old715=findTag717(box,"v715-finance-container");
        for(View c:new ArrayList<>(cards.values()))detach717(c);
        detach717(old717);detach717(old716);detach717(old715);
        removeFinanceHeading717(box);
        cleanupEmpty717(box);
        if(insert==box.getChildCount()){
            TextView movement=findText717(box,"SPORCU HAREKETLERİ");
            View top=movement==null?null:topChild717(box,movement);
            insert=top==null?Math.min(2,box.getChildCount()):Math.max(0,box.indexOfChild(top));
        }
        insert=Math.max(0,Math.min(insert,box.getChildCount()));

        LinearLayout outer=new LinearLayout(this);outer.setTag("v717-finance-container");outer.setOrientation(LinearLayout.VERTICAL);outer.setPadding(dp(10),dp(10),dp(10),dp(10));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(252,249,240));bg.setCornerRadius(dp(18));bg.setStroke(dp(1),Color.rgb(222,198,128));outer.setBackground(bg);outer.setElevation(dp(1));
        TextView h=new TextView(this);h.setText("FİNANS");h.setTextSize(15f);h.setTextColor(TEXT717);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setPadding(dp(4),dp(2),dp(4),0);outer.addView(h);
        TextView sub=new TextView(this);sub.setText("Aidat, tahsilat, vade, borç ve ödeme işlemleri");sub.setTextSize(10.5f);sub.setTextColor(Color.rgb(100,100,100));sub.setPadding(dp(4),0,dp(4),dp(9));outer.addView(sub);
        for(String k:keys){View c=cards.get(k);if(c==null)continue;LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));outer.addView(c,lp);}
        LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(-1,-2);op.setMargins(dp(3),dp(7),dp(3),dp(12));box.addView(outer,insert,op);
    }

    @Override void shareReport(long id){
        final String[] labels={"3 AY","6 AY","1 YIL"};
        final int[] months={3,6,12};
        new AlertDialog.Builder(this).setTitle("ÖDEME BİLGİSİ PAYLAŞ").setItems(labels,(d,w)->generateReport717(id,months[w],labels[w])).setNegativeButton("VAZGEÇ",null).show();
    }

    private void generateReport717(long id,int monthCount,String filterLabel){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s717(a,"name"),category=s717(a,"category"),sibling=s717(a,"sibling"),start=s717(a,"startDate"),end=s717(a,"endDate"),restart=s717(a,"restartDate");
        int by=a.getInt(a.getColumnIndexOrThrow("birthYear")),baseFee=a.getInt(a.getColumnIndexOrThrow("monthlyFee"));a.close();
        HashMap<Integer,PayRec> pays=paymentMap(id);
        Calendar now=Calendar.getInstance();Calendar first=(Calendar)now.clone();first.set(Calendar.DAY_OF_MONTH,1);first.add(Calendar.MONTH,-(monthCount-1));
        Calendar last=(Calendar)now.clone();last.set(Calendar.DAY_OF_MONTH,1);last.add(Calendar.MONTH,1);
        ArrayList<Integer> keys=new ArrayList<>();for(Calendar x=(Calendar)first.clone();!x.after(last);x.add(Calendar.MONTH,1))keys.add(x.get(Calendar.YEAR)*100+x.get(Calendar.MONTH)+1);
        int accrued=0,collected=0;
        for(int key:keys){int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);collected+=Math.max(0,r.amount);int exp=expectedFeeAt(id,yr,mo,r);if(exp<=0&&yr==2026)exp=baseFee;if("X".equals(r.marker)||!activeAt(yr,mo,start,end,restart)||"BURSLU".equalsIgnoreCase(sibling))continue;if(exp>0)accrued+=exp;}
        int balance=accrued-collected;long[] mat=materialSummary717(id);long matBalance=Math.max(0,mat[1]-mat[2]);
        int anchor=start!=null&&start.matches("\\d{4}-\\d{2}-\\d{2}")?anchorDay(start):now.get(Calendar.DAY_OF_MONTH);int cycle=currentCycleKey(now,anchor);Calendar dueStart=cycleDate(cycle,anchor),dueEnd=cycleDate(shiftMonth(cycle,1),anchor);
        SimpleDateFormat df=new SimpleDateFormat("dd.MM.yyyy",TR),longDf=new SimpleDateFormat("dd MMMM yyyy",TR);
        String periodLabel=monthYear717(keys.get(0))+" – "+monthYear717(keys.get(keys.size()-1));
        int W=1080,H=1050+keys.size()*94;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setColor(Color.DKGRAY);q.setTextSize(17);c.drawText("Oluşturma Tarihi: "+df.format(new Date()),52,34,q);
        q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(45);c.drawText("PARİON SPOR KULÜBÜ",52,84,q);
        q.setColor(GOLD717);q.setTextSize(33);c.drawText("AİDAT HESAP EKSTRESİ",52,132,q);
        q.setColor(BLACK);q.setTextSize(25);c.drawText("Sporcu: "+name,52,188,q);
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(21);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Filtre: "+filterLabel,52,226,q);c.drawText("Dönem: "+periodLabel+"   Güncel Aylık Aidat: "+money(baseFee),52,258,q);
        int y=330;
        for(int key:keys){int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);if(expected<=0&&yr==2026)expected=baseFee;int fill=reportColor717(yr,mo,expected,sibling,start,end,restart,r);
            q.setColor(fill);c.drawRoundRect(40,y-34,1040,y+50,14,14,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText(monthYear717(key),58,y+2,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(19);c.drawText(reportLine717(yr,mo,expected,sibling,start,end,restart,r),260,y+2,q);y+=92;}
        y+=14;q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(26);c.drawText("Dönem Tahakkuku: "+money(accrued),52,y,q);y+=39;q.setColor(Color.rgb(39,134,82));c.drawText("Toplam Tahsilat: "+money(collected),52,y,q);y+=39;q.setColor(balance>0?Color.rgb(196,63,63):BLACK);c.drawText("Aidat Bakiyesi: "+money(balance),52,y,q);y+=55;
        q.setColor(Color.rgb(245,245,245));c.drawRoundRect(40,y-26,1040,y+106,14,14,q);q.setColor(BLACK);q.setTextSize(21);c.drawText("TİŞÖRT / MALZEME TAHSİLAT ÖZETİ",52,y,q);y+=36;q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);c.drawText("Verilen tişört: "+mat[0]+" adet  •  Malzeme tutarı: "+money((int)mat[1])+"  •  Tahsil edilen: "+money((int)mat[2]),52,y,q);y+=32;q.setColor(matBalance>0?Color.rgb(196,63,63):Color.rgb(39,134,82));q.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Kalan tişört/malzeme borcu: "+money((int)matBalance),52,y,q);y+=68;
        q.setColor(BLACK);q.setTextSize(20);c.drawText("Sayın Velimiz,",52,y,q);y+=32;q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);c.drawText(monthName(cycle%100)+" ayı aidat ödeme aralığınız "+longDf.format(dueStart.getTime())+" – "+longDf.format(dueEnd.getTime())+"'dir.",52,y,q);y+=30;c.drawText("Aidat takibiniz sporcumuzun kulübe başlangıç gününe göre aylık olarak hesaplanmaktadır.",52,y,q);y+=50;q.setColor(Color.DKGRAY);q.setTextSize(16);c.drawText("Bu ekstre bilgilendirme amaçlıdır; mali belge veya tahsilat makbuzu niteliğinde değildir.",52,y,q);
        try{ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"Parion_Aidat_Ekstresi_"+filterLabel.replace(' ','_')+"_"+name.replace(' ','_')+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Parion");Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("Dosya oluşturulamadı");OutputStream os=getContentResolver().openOutputStream(uri);bm.compress(Bitmap.CompressFormat.PNG,100,os);if(os!=null)os.close();Intent sh=new Intent(Intent.ACTION_SEND);sh.setType("image/png");sh.putExtra(Intent.EXTRA_STREAM,uri);sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(sh,"Ödeme bilgisini paylaş"));}catch(Exception e){Toast.makeText(this,"Ekstre oluşturulamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}finally{bm.recycle();}
    }

    private String reportLine717(int yr,int mo,int fee,String sibling,String start,String end,String restart,PayRec r){
        if(yr<2026&&r.amount<=0&&(r.marker==null||r.marker.trim().isEmpty()))return "Veri yok";
        if("X".equals(r.marker))return "Tahakkuk yok • Ara verilen dönem";
        if(!activeAt(yr,mo,start,end,restart))return "Tahakkuk yok • Aktif olunmayan dönem";
        if("BURSLU".equalsIgnoreCase(sibling)||fee==0)return yr<2026?"Veri yok":"Tahakkuk yok • Burslu";
        if(isDate(r.marker))return "Tahsil edildi • "+dateTr(r.marker)+" • "+money(r.amount)+(r.amount==fee?"":r.amount<fee?" • Eksik tahsilat":" • Fazla tahsilat");
        if(("!".equals(r.marker)||"!!".equals(r.marker))&&r.amount>0)return "Tahsilat kaydı • "+money(r.amount)+(r.amount<fee?" • Eksik tahsilat":r.amount>fee?" • Fazla tahsilat":" • Tam tahsilat");
        Calendar n=Calendar.getInstance();int nk=n.get(Calendar.YEAR)*100+n.get(Calendar.MONTH)+1,k=yr*100+mo;if(k>nk)return "Henüz tahakkuk etmedi • "+money(fee);if(k<nk)return "Vadesi geçmiş • Tahsil edilmedi • "+money(fee);return "Tahakkuk etti • Tahsil edilmedi • "+money(fee);
    }
    private int reportColor717(int yr,int mo,int fee,String sibling,String start,String end,String restart,PayRec r){if(yr<2026&&r.amount<=0&&(r.marker==null||r.marker.trim().isEmpty()))return Color.rgb(230,230,230);if("X".equals(r.marker)||!activeAt(yr,mo,start,end,restart)||"BURSLU".equalsIgnoreCase(sibling)||fee==0)return Color.rgb(230,230,230);if(isDate(r.marker))return r.amount==fee?Color.rgb(232,247,236):Color.rgb(255,167,20);Calendar n=Calendar.getInstance();int nk=n.get(Calendar.YEAR)*100+n.get(Calendar.MONTH)+1,k=yr*100+mo;if(k>nk)return Color.WHITE;if(k<nk)return Color.rgb(255,230,230);return Color.rgb(255,245,190);}
    private long[] materialSummary717(long id){long qty=0,total=0,paid=0;Cursor c=null;try{c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(CASE WHEN UPPER(product) LIKE '%TİŞÖRT%' THEN qty ELSE 0 END),0),COALESCE(SUM(total),0),COALESCE(SUM(paidAmount),0) FROM material_transactions WHERE athleteId=?",new String[]{String.valueOf(id)});if(c.moveToFirst()){qty=c.getLong(0);total=c.getLong(1);paid=c.getLong(2);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return new long[]{qty,total,paid};}
    private String monthYear717(int key){return monthName(key%100)+" "+(key/100);}
    private String s717(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}

    private View financeCard717(View text,LinearLayout box){View cur=text,best=null;while(cur!=null&&cur!=box&&cur.getParent() instanceof View){if(cur.isClickable()||cur.hasOnClickListeners())best=cur;View p=(View)cur.getParent();if(p==box)break;cur=p;}return best!=null?best:topChild717(box,text);}
    private void removeFinanceHeading717(LinearLayout box){for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(v instanceof TextView){String n=norm717(String.valueOf(((TextView)v).getText()));if(n.startsWith("FİNANS")){box.removeViewAt(i);}}}}
    private ScrollView findScroll717(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll717(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private TextView findText717(View v,String needle){if(v instanceof TextView&&norm717(String.valueOf(((TextView)v).getText())).contains(norm717(needle)))return(TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText717(g.getChildAt(i),needle);if(r!=null)return r;}}return null;}
    private View topChild717(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private View findTag717(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag717(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private void detach717(View v){if(v!=null&&v.getParent() instanceof ViewGroup)((ViewGroup)v.getParent()).removeView(v);}
    private void cleanupEmpty717(ViewGroup g){for(int i=g.getChildCount()-1;i>=0;i--){View v=g.getChildAt(i);if(v instanceof ViewGroup){cleanupEmpty717((ViewGroup)v);if(((ViewGroup)v).getChildCount()==0)g.removeViewAt(i);}}}
    private String norm717(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
