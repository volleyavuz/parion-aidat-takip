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

/** v4.1.39 - final finance regroup + complete accounting payment statement + tshirt subtitle cleanup. */
public class MainActivityV716 extends MainActivityV715 {
    private static final int GOLD716=Color.rgb(205,156,34);
    private static final int TEXT716=Color.rgb(35,35,35);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::finalHome716,320);
            root.postDelayed(this::finalHome716,1200);
            root.postDelayed(this::finalHome716,8400);
        }
    }

    private void finalHome716(){
        if(root==null||!"HOME".equals(page))return;
        hideTshirtSubtitle716(root);
        regroupFinance716();
    }

    private void hideTshirtSubtitle716(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String n=norm716(String.valueOf(t.getText()));
            if(n.equals("VERİLEN TİŞÖRT ADEDİ 0")||n.equals("VERİLEN TİŞÖRT ADEDİ: 0")||n.equals("TİŞÖRT SAYISI 0")){
                t.setVisibility(View.GONE);t.setPadding(0,0,0,0);ViewGroup.LayoutParams lp=t.getLayoutParams();if(lp!=null){lp.height=0;t.setLayoutParams(lp);}return;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hideTshirtSubtitle716(g.getChildAt(i));}
    }

    private void regroupFinance716(){
        ScrollView sv=findScroll716(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        View old=findTag716(box,"v716-finance-container");
        if(old!=null&&old.getParent() instanceof ViewGroup)((ViewGroup)old.getParent()).removeView(old);
        View old715=findTag716(box,"v715-finance-container");
        if(old715!=null&&old715.getParent() instanceof ViewGroup)((ViewGroup)old715.getParent()).removeView(old715);

        LinkedHashMap<String,View> cards=new LinkedHashMap<>();
        String[] keys={"AYLIK HEDEF","BU AYKİ TAHSİLAT","ERKEN ÖDEME GİR","GECİKMİŞ","MALZEME BORCU","SON ÖDEMELER"};
        for(String k:keys){TextView t=findText716(box,k);if(t!=null){View c=financeCard716(t,box);if(c!=null)cards.put(k,c);}}
        if(cards.isEmpty())return;

        int insert=0;TextView gen=findText716(box,"GENEL DURUM");if(gen!=null){View top=topChild716(box,gen);if(top!=null){int i=box.indexOfChild(top);if(i>=0)insert=i+1;}}
        for(View c:new ArrayList<>(cards.values()))detach716(c);
        cleanupEmpty716(box);
        insert=Math.max(0,Math.min(insert,box.getChildCount()));

        LinearLayout outer=new LinearLayout(this);outer.setTag("v716-finance-container");outer.setOrientation(LinearLayout.VERTICAL);outer.setPadding(dp(10),dp(10),dp(10),dp(10));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(252,249,240));bg.setCornerRadius(dp(18));bg.setStroke(dp(1),Color.rgb(222,198,128));outer.setBackground(bg);outer.setElevation(dp(1));
        TextView h=new TextView(this);h.setText("FİNANS");h.setTextSize(14.5f);h.setTextColor(TEXT716);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setPadding(dp(4),0,dp(4),0);outer.addView(h);
        TextView s=new TextView(this);s.setText("Aidat, tahsilat, borç ve ödeme işlemleri");s.setTextSize(10.5f);s.setTextColor(Color.rgb(105,105,105));s.setPadding(dp(4),0,dp(4),dp(8));outer.addView(s);
        for(String k:keys){View c=cards.get(k);if(c==null)continue;LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));outer.addView(c,lp);}
        LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(-1,-2);op.setMargins(dp(3),dp(6),dp(3),dp(12));box.addView(outer,insert,op);
        removeFinanceHeadings716(box,outer);
    }

    private View financeCard716(View text,LinearLayout box){
        View cur=text,best=null;
        while(cur!=null&&cur!=box&&cur.getParent() instanceof View){
            if(cur.isClickable()||cur.hasOnClickListeners())best=cur;
            View p=(View)cur.getParent();
            if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.HORIZONTAL&&best!=null)return best;
            if(p==box)break;cur=p;
        }
        if(best!=null)return best;
        return topChild716(box,text);
    }
    private void removeFinanceHeadings716(LinearLayout box,View keep){for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(v==keep)continue;if(v instanceof TextView){String n=norm716(String.valueOf(((TextView)v).getText()));if(n.startsWith("FİNANS")&&n.contains("AİDAT")){box.removeViewAt(i);}}}}

    @Override void shareReport(long id){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s(a,"name"),category=s(a,"category"),sibling=s(a,"sibling"),start=s(a,"startDate"),end=s(a,"endDate"),restart=s(a,"restartDate");
        int by=a.getInt(a.getColumnIndexOrThrow("birthYear")),baseFee=a.getInt(a.getColumnIndexOrThrow("monthlyFee"));a.close();
        HashMap<Integer,String[]> ps=new HashMap<>();Cursor p=db.payments(id);int collected=0;
        while(p.moveToNext()){int m=p.getInt(p.getColumnIndexOrThrow("month")),amt=p.getInt(p.getColumnIndexOrThrow("amount"));ps.put(m,new String[]{s(p,"marker"),String.valueOf(amt)});collected+=amt;}p.close();
        int accrued=0;for(int m=1;m<=12;m++){String[] x=ps.get(m);String marker=x==null?"":x[0];if("X".equals(marker)||"BURSLU".equalsIgnoreCase(sibling)||!activeMonth(m,start,end,restart))continue;int ef=db.expectedFee(id,m);if(ef<=0)ef=baseFee;accrued+=Math.max(0,ef);}int balance=accrued-collected;

        long[] mat=materialSummary716(id);long shirtQty=mat[0],matTotal=mat[1],matPaid=mat[2],matBalance=Math.max(0,matTotal-matPaid);
        Calendar now=Calendar.getInstance();int anchor=start!=null&&start.matches("\\d{4}-\\d{2}-\\d{2}")?anchorDay(start):now.get(Calendar.DAY_OF_MONTH);int cycle=currentCycleKey(now,anchor);Calendar dueStart=cycleDate(cycle,anchor),dueEnd=cycleDate(shiftMonth(cycle,1),anchor);
        SimpleDateFormat df=new SimpleDateFormat("dd.MM.yyyy",TR);SimpleDateFormat longDf=new SimpleDateFormat("dd MMMM yyyy",TR);

        int W=1080,H=2080;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setColor(Color.DKGRAY);q.setTypeface(Typeface.DEFAULT);q.setTextSize(17);c.drawText("Oluşturma Tarihi: "+df.format(new Date()),52,35,q);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setColor(BLACK);q.setTextSize(46);c.drawText("PARİON SPOR KULÜBÜ",52,86,q);
        q.setColor(GOLD716);q.setTextSize(34);c.drawText("2026 AİDAT HESAP EKSTRESİ",52,136,q);
        q.setColor(BLACK);q.setTextSize(26);c.drawText("Sporcu: "+name,52,196,q);
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(22);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Güncel Aylık Aidat: "+money(baseFee),52,234,q);

        int y=306;
        for(int m=1;m<=12;m++){
            String[] x=ps.get(m);String marker=x==null?"":x[0];int amt=x==null?0:Integer.parseInt(x[1]);int expected=db.expectedFee(id,m);if(expected<=0)expected=baseFee;
            int color=paymentColor(m,expected,sibling,start,end,restart,marker,amt);q.setColor(color);c.drawRoundRect(40,y-34,1040,y+50,14,14,q);
            q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(23);c.drawText(monthName(m),58,y+2,q);
            q.setTypeface(Typeface.DEFAULT);q.setTextSize(20);c.drawText(accountingLine716(m,expected,sibling,start,end,restart,marker,amt),245,y+2,q);y+=92;
        }
        y+=12;q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(27);c.drawText("Dönem Tahakkuku: "+money(accrued),52,y,q);y+=40;
        q.setColor(Color.rgb(39,134,82));c.drawText("Toplam Tahsilat: "+money(collected),52,y,q);y+=40;
        q.setColor(balance>0?Color.rgb(196,63,63):BLACK);c.drawText("Aidat Bakiyesi: "+money(balance),52,y,q);y+=58;

        q.setColor(Color.rgb(245,245,245));c.drawRoundRect(40,y-28,1040,y+108,14,14,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText("TİŞÖRT / MALZEME TAHSİLAT ÖZETİ",52,y,q);y+=38;
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(19);c.drawText("Verilen tişört: "+shirtQty+" adet  •  Malzeme tutarı: "+money((int)matTotal)+"  •  Tahsil edilen: "+money((int)matPaid),52,y,q);y+=34;
        q.setColor(matBalance>0?Color.rgb(196,63,63):Color.rgb(39,134,82));q.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Kalan tişört/malzeme borcu: "+money((int)matBalance),52,y,q);y+=72;

        q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(20);c.drawText("Sayın Velimiz,",52,y,q);y+=34;
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);String period=monthName(cycle%100)+" ayı aidat ödeme aralığınız "+longDf.format(dueStart.getTime())+" – "+longDf.format(dueEnd.getTime())+"'dir.";c.drawText(period,52,y,q);y+=32;
        c.drawText("Aidat takibiniz sporcumuzun kulübe başlangıç gününe göre aylık olarak hesaplanmaktadır.",52,y,q);y+=54;
        q.setColor(Color.DKGRAY);q.setTextSize(17);c.drawText("Tahakkuk: dönem için hesaplanan aidat • Tahsilat: kaydedilen ödeme toplamı",52,y,q);y+=28;
        c.drawText("Bu ekstre bilgilendirme amaçlıdır; mali belge veya tahsilat makbuzu niteliğinde değildir.",52,y,q);

        try{ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"Parion_2026_Aidat_Hesap_Ekstresi_"+name.replace(' ','_')+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Parion");Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("Dosya oluşturulamadı");OutputStream os=getContentResolver().openOutputStream(uri);bm.compress(Bitmap.CompressFormat.PNG,100,os);if(os!=null)os.close();Intent sh=new Intent(Intent.ACTION_SEND);sh.setType("image/png");sh.putExtra(Intent.EXTRA_STREAM,uri);sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(sh,"Aidat hesap ekstresini paylaş"));}catch(Exception e){Toast.makeText(this,"Ekstre oluşturulamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}finally{bm.recycle();}
    }

    private long[] materialSummary716(long id){long qty=0,total=0,paid=0;Cursor c=null;try{c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(CASE WHEN UPPER(product) LIKE '%TİŞÖRT%' THEN qty ELSE 0 END),0),COALESCE(SUM(total),0),COALESCE(SUM(paidAmount),0) FROM material_transactions WHERE athleteId=?",new String[]{String.valueOf(id)});if(c.moveToFirst()){qty=c.getLong(0);total=c.getLong(1);paid=c.getLong(2);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return new long[]{qty,total,paid};}
    private String accountingLine716(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){if("X".equals(marker))return "Tahakkuk yok • Ara verilen dönem";if("BURSLU".equalsIgnoreCase(sibling)||fee==0)return "Tahakkuk yok • Burslu";if(!activeMonth(m,start,end,restart))return "Tahakkuk yok • Aktif olunmayan dönem";if(isDate(marker))return "Tahsil edildi • "+dateTr(marker)+" • "+money(amount)+(amount==fee?"":amount<fee?" • Eksik tahsilat":" • Fazla tahsilat");if(("!".equals(marker)||"!!".equals(marker))&&amount>0)return "Tahsilat kaydı • "+money(amount)+(amount<fee?" • Eksik tahsilat":amount>fee?" • Fazla tahsilat":" • Tam tahsilat");Calendar now=Calendar.getInstance();int year=now.get(Calendar.YEAR),cm=now.get(Calendar.MONTH)+1;if(year<2026||m>cm)return "Henüz tahakkuk etmedi • "+money(fee);if(year>2026||m<cm)return "Vadesi geçmiş • Tahsil edilmedi • "+money(fee);return "Tahakkuk etti • Tahsil edilmedi • "+money(fee);}

    private ScrollView findScroll716(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll716(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private TextView findText716(View v,String needle){if(v instanceof TextView&&norm716(String.valueOf(((TextView)v).getText())).contains(norm716(needle)))return(TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText716(g.getChildAt(i),needle);if(r!=null)return r;}}return null;}
    private View topChild716(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private View findTag716(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag716(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private void detach716(View v){if(v!=null&&v.getParent() instanceof ViewGroup)((ViewGroup)v.getParent()).removeView(v);}
    private void cleanupEmpty716(ViewGroup g){for(int i=g.getChildCount()-1;i>=0;i--){View v=g.getChildAt(i);if(v instanceof ViewGroup){cleanupEmpty716((ViewGroup)v);if(((ViewGroup)v).getChildCount()==0)g.removeViewAt(i);}}}
    private String norm716(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
