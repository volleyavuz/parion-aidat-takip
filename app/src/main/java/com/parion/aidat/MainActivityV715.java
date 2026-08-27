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
import java.util.*;

/** v4.1.38 - nested finance dashboard + accounting-language payment statement + tshirt cleanup. */
public class MainActivityV715 extends MainActivityV714 {
    private static final int GOLD715=Color.rgb(205,156,34);
    private static final int TEXT715=Color.rgb(35,35,35);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(()->{organizeFinance715();cleanTshirt715();});
            root.postDelayed(()->{organizeFinance715();cleanTshirt715();},120);
        }
    }

    private void organizeFinance715(){
        if(root==null||!"HOME".equals(page))return;
        LinearLayout fresh=findFresh715(root);if(fresh==null)return;
        if(findTag715(fresh,"v715-finance-container")!=null)return;

        int insert=findFinanceHeadingIndex715(fresh);
        ArrayList<View> cards=new ArrayList<>();
        addUnique715(cards,top715(fresh,"AYLIK HEDEF"));
        addUnique715(cards,top715(fresh,"BU AYKİ TAHSİLAT"));
        addUnique715(cards,top715(fresh,"ERKEN ÖDEME GİR"));
        addUnique715(cards,top715(fresh,"GECİKMİŞ"));
        addUnique715(cards,top715(fresh,"MALZEME BORCU"));
        addUnique715(cards,top715(fresh,"SON ÖDEMELER"));
        if(cards.isEmpty())return;

        for(int i=fresh.getChildCount()-1;i>=0;i--){
            View v=fresh.getChildAt(i);
            if(isFinanceHeading715(v)){fresh.removeViewAt(i);continue;}
            if(cards.contains(v))fresh.removeViewAt(i);
        }
        if(insert<0)insert=Math.min(2,fresh.getChildCount());
        insert=Math.max(0,Math.min(insert,fresh.getChildCount()));

        LinearLayout outer=new LinearLayout(this);outer.setTag("v715-finance-container");outer.setOrientation(LinearLayout.VERTICAL);outer.setPadding(dp(10),dp(10),dp(10),dp(12));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(252,249,240));bg.setCornerRadius(dp(18));bg.setStroke(dp(1),Color.rgb(222,198,128));outer.setBackground(bg);outer.setElevation(dp(1));
        TextView title=new TextView(this);title.setText("FİNANS");title.setTextSize(14.5f);title.setTextColor(TEXT715);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setPadding(dp(4),dp(2),dp(4),0);outer.addView(title);
        TextView sub=new TextView(this);sub.setText("Aidat, tahsilat, borç ve ödeme işlemleri");sub.setTextSize(10.5f);sub.setTextColor(Color.rgb(105,105,105));sub.setPadding(dp(4),0,dp(4),dp(9));outer.addView(sub);

        for(View card:cards){
            if(card.getParent() instanceof ViewGroup)((ViewGroup)card.getParent()).removeView(card);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));outer.addView(card,lp);
        }
        LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(-1,-2);op.setMargins(dp(3),dp(8),dp(3),dp(12));fresh.addView(outer,insert,op);
    }

    private void cleanTshirt715(){
        if(root==null||!"HOME".equals(page))return;
        TextView title=findText715(root,"TİŞÖRT ALMAYAN AKTİF SPORCULAR");
        if(title==null)title=findText715(root,"TİŞÖRT ALMAYAN");
        if(title==null)return;
        View card=nearestCard715(title);if(!(card instanceof ViewGroup))return;
        cleanTshirtText715((ViewGroup)card,title);
    }
    private void cleanTshirtText715(ViewGroup g,TextView keepTitle){
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);
            if(c instanceof TextView){
                TextView t=(TextView)c;if(t==keepTitle)continue;
                String s=String.valueOf(t.getText()).trim();
                if(s.matches("[0-9]+"))continue;
                g.removeViewAt(i);continue;
            }
            if(c instanceof ViewGroup)cleanTshirtText715((ViewGroup)c,keepTitle);
        }
    }

    @Override void shareReport(long id){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s(a,"name"),category=s(a,"category"),sibling=s(a,"sibling"),start=s(a,"startDate"),end=s(a,"endDate"),restart=s(a,"restartDate");
        int by=a.getInt(a.getColumnIndexOrThrow("birthYear")),baseFee=a.getInt(a.getColumnIndexOrThrow("monthlyFee"));a.close();
        HashMap<Integer,String[]> ps=new HashMap<>();Cursor p=db.payments(id);int collected=0;
        while(p.moveToNext()){int m=p.getInt(p.getColumnIndexOrThrow("month")),amt=p.getInt(p.getColumnIndexOrThrow("amount"));ps.put(m,new String[]{s(p,"marker"),String.valueOf(amt)});collected+=amt;}p.close();
        int accrued=0;for(int m=1;m<=12;m++){String[] x=ps.get(m);String marker=x==null?"":x[0];if("X".equals(marker)||"BURSLU".equalsIgnoreCase(sibling)||!activeMonth(m,start,end,restart))continue;int ef=db.expectedFee(id,m);if(ef<=0)ef=baseFee;accrued+=Math.max(0,ef);}int balance=accrued-collected;

        int W=1080,H=1820;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setColor(BLACK);q.setTextSize(46);c.drawText("PARION VOLEYBOL AKADEMİSİ",55,78,q);
        q.setColor(GOLD715);q.setTextSize(34);c.drawText("2026 AİDAT HESAP EKSTRESİ",55,130,q);
        q.setColor(BLACK);q.setTextSize(26);c.drawText("Sporcu: "+name,55,190,q);
        q.setTypeface(Typeface.DEFAULT);q.setTextSize(22);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Güncel Aylık Aidat: "+money(baseFee),55,230,q);

        int y=300;
        for(int m=1;m<=12;m++){
            String[] x=ps.get(m);String marker=x==null?"":x[0];int amt=x==null?0:Integer.parseInt(x[1]);int expected=db.expectedFee(id,m);if(expected<=0)expected=baseFee;
            int color=paymentColor(m,expected,sibling,start,end,restart,marker,amt);q.setColor(color);c.drawRoundRect(42,y-32,1038,y+54,14,14,q);
            q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(23);c.drawText(monthName(m),60,y+3,q);
            q.setTypeface(Typeface.DEFAULT);q.setTextSize(20);c.drawText(accountingLine715(m,expected,sibling,start,end,restart,marker,amt),245,y+3,q);
            y+=94;
        }
        y+=10;q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(27);c.drawText("Dönem Tahakkuku: "+money(accrued),55,y,q);y+=42;
        q.setColor(Color.rgb(39,134,82));c.drawText("Toplam Tahsilat: "+money(collected),55,y,q);y+=42;
        q.setColor(balance>0?Color.rgb(196,63,63):BLACK);c.drawText("Aidat Bakiyesi: "+money(balance),55,y,q);y+=48;
        q.setColor(Color.DKGRAY);q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);c.drawText("Tahakkuk: dönem için hesaplanan aidat • Tahsilat: kaydedilen ödeme toplamı",55,y,q);y+=30;
        c.drawText("Bu ekstre bilgilendirme amaçlıdır; mali belge veya tahsilat makbuzu niteliğinde değildir.",55,y,q);

        try{
            ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"Parion_2026_Aidat_Hesap_Ekstresi_"+name.replace(' ','_')+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Parion");
            Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new Exception("Dosya oluşturulamadı");
            OutputStream os=getContentResolver().openOutputStream(uri);bm.compress(Bitmap.CompressFormat.PNG,100,os);if(os!=null)os.close();
            Intent sh=new Intent(Intent.ACTION_SEND);sh.setType("image/png");sh.putExtra(Intent.EXTRA_STREAM,uri);sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(sh,"Aidat hesap ekstresini paylaş"));
        }catch(Exception e){Toast.makeText(this,"Ekstre oluşturulamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}
        finally{bm.recycle();}
    }

    private String accountingLine715(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        if("X".equals(marker))return "Tahakkuk yok • Ara verilen dönem";
        if("BURSLU".equalsIgnoreCase(sibling)||fee==0)return "Tahakkuk yok • Burslu";
        if(!activeMonth(m,start,end,restart))return "Tahakkuk yok • Aktif olunmayan dönem";
        if(isDate(marker))return "Tahsil edildi • "+dateTr(marker)+" • "+money(amount)+(amount==fee?"":amount<fee?" • Eksik tahsilat":" • Fazla tahsilat");
        if(("!".equals(marker)||"!!".equals(marker))&&amount>0)return "Tahsilat kaydı • "+money(amount)+(amount<fee?" • Eksik tahsilat":amount>fee?" • Fazla tahsilat":" • Tam tahsilat");
        Calendar now=Calendar.getInstance();int year=now.get(Calendar.YEAR),cm=now.get(Calendar.MONTH)+1;
        if(year<2026||m>cm)return "Henüz tahakkuk etmedi • "+money(fee);
        if(year>2026||m<cm)return "Vadesi geçmiş • Tahsil edilmedi • "+money(fee);
        return "Tahakkuk etti • Tahsil edilmedi • "+money(fee);
    }

    private LinearLayout findFresh715(View v){if(v instanceof LinearLayout&&"v657-fresh".equals(v.getTag()))return(LinearLayout)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){LinearLayout r=findFresh715(g.getChildAt(i));if(r!=null)return r;}}return null;}
    private int findFinanceHeadingIndex715(LinearLayout fresh){for(int i=0;i<fresh.getChildCount();i++)if(isFinanceHeading715(fresh.getChildAt(i)))return i;return -1;}
    private boolean isFinanceHeading715(View v){if(!(v instanceof TextView))return false;String n=norm715(String.valueOf(((TextView)v).getText()));return n.startsWith("FİNANS")&&n.contains("AİDAT");}
    private View top715(LinearLayout fresh,String needle){for(int i=0;i<fresh.getChildCount();i++){View v=fresh.getChildAt(i);if(text715(v).contains(norm715(needle)))return v;}return null;}
    private void addUnique715(ArrayList<View> a,View v){if(v!=null&&!a.contains(v))a.add(v);}
    private String text715(View v){StringBuilder b=new StringBuilder();collect715(v,b);return norm715(b.toString());}
    private void collect715(View v,StringBuilder b){if(v instanceof TextView)b.append(' ').append(((TextView)v).getText());if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collect715(g.getChildAt(i),b);}}
    private TextView findText715(View v,String needle){if(v instanceof TextView&&norm715(String.valueOf(((TextView)v).getText())).contains(norm715(needle)))return(TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText715(g.getChildAt(i),needle);if(r!=null)return r;}}return null;}
    private View nearestCard715(View v){View cur=v,best=null;while(cur!=null&&cur!=root){if(cur.isClickable()||cur.hasOnClickListeners()||"v621-tshirt-card".equals(cur.getTag()))best=cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}return best;}
    private View findTag715(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag715(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String norm715(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
