package com.parion.aidat;

import android.database.Cursor;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivityV395 extends MainActivityV394 {
    private static final int PAID_GREEN_09F299=Color.rgb(9,242,153);

    @Override int paymentColor(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        int c=super.paymentColor(m,fee,sibling,start,end,restart,marker,amount);
        // V394'in açık yeşili dahil, tam ödenmiş satırları istenen #09F299 rengine sabitle.
        if(isDate(marker) && !(amount>0&&fee>0&&amount!=fee)) return PAID_GREEN_09F299;
        return c;
    }

    // Üst başlıktaki geri oku base() içinde goBack() çağırır. Bu override ile fiziksel geri tuşuyla aynı rota kullanılır.
    @Override void goBack(){
        if("MATERIAL_PRICES".equals(page)){if(currentAthlete>0)showProfile(currentAthlete);else showHome();return;}
        if("MATERIAL_DEBTS".equals(page)){showHome();return;}
        super.goBack();
    }

    @Override void showAthletes(){
        page="LIST";base("SPORCULAR",true);
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(8),dp(8),dp(8),dp(6));p.setBackground(round(Color.WHITE,10));
        Button add=btn("+ YENİ KAYIT");add.setOnClickListener(v->form(-1));p.addView(add,new LinearLayout.LayoutParams(-1,dp(52)));
        EditText q=new EditText(this);q.setHint("Sporcu adı ara");p.addView(q,new LinearLayout.LayoutParams(-1,dp(48)));

        LinearLayout r1=new LinearLayout(this);
        Spinner st=sp(new String[]{"TÜM DURUMLAR","AKTİF","ARA VERDİ","BIRAKTI","ARANACAK","SAKATLANDI"},"AKTİF");
        Spinner cat=sp(new String[]{"TÜM KATEGORİLER","SO 1","SO 2","SO 3","MİNİ VOLEYBOL","MİDİ","KÜÇÜK","YILDIZ","GENÇ"},"");
        r1.addView(st,new LinearLayout.LayoutParams(0,dp(48),1));r1.addView(cat,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r1);

        LinearLayout r2=new LinearLayout(this);
        ArrayList<String> years=new ArrayList<>();years.add("TÜM DOĞUM YILLARI");
        Cursor yc=db.getReadableDatabase().rawQuery("SELECT DISTINCT birthYear FROM athletes WHERE birthYear>0 AND TRIM(COALESCE(deletedAt,''))='' ORDER BY birthYear ASC",null);
        while(yc.moveToNext())years.add(String.valueOf(yc.getInt(0)));yc.close();
        Spinner by=new Spinner(this);by.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,years));
        Spinner sort=sp(SORTS,"");r2.addView(by,new LinearLayout.LayoutParams(0,dp(48),1));r2.addView(sort,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r2);

        ArrayList<String> ni=new ArrayList<>();ni.add("TÜM ÖZEL NOTLAR");ni.addAll(db.uniqueNotes());
        Spinner note=new Spinner(this);note.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,ni));p.addView(note,new LinearLayout.LayoutParams(-1,dp(52)));root.addView(p);

        ScrollView sv=scroll();LinearLayout list=box(sv);
        Runnable load=()->load(list,q.getText().toString(),String.valueOf(st.getSelectedItem()),String.valueOf(cat.getSelectedItem()),yearValue395(by),String.valueOf(note.getSelectedItem()),sort.getSelectedItemPosition());
        android.text.TextWatcher tw=new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){load.run();}public void afterTextChanged(android.text.Editable e){}};q.addTextChangedListener(tw);
        android.widget.AdapterView.OnItemSelectedListener sl=new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){load.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}};
        st.setOnItemSelectedListener(sl);cat.setOnItemSelectedListener(sl);by.setOnItemSelectedListener(sl);note.setOnItemSelectedListener(sl);sort.setOnItemSelectedListener(sl);load.run();
    }
    private String yearValue395(Spinner by){String s=String.valueOf(by.getSelectedItem());return s.startsWith("TÜM")?"":s;}

    @Override void showHome(){
        super.showHome();
        fixHomeCounterCards();
    }
    private void fixHomeCounterCards(){
        ScrollView sv=findFirstScroll395(root);if(sv==null)return;LinearLayout target=box(sv);
        LinearLayout photoCard=findPhotoCard395(root);
        if(photoCard!=null&&photoCard.getParent()!=target){
            ViewGroup parent=(ViewGroup)photoCard.getParent();if(parent!=null)parent.removeView(photoCard);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(8));target.addView(photoCard,lp);
        }
        tuneCounters395(root);
    }
    private ScrollView findFirstScroll395(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findFirstScroll395(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private LinearLayout findPhotoCard395(View v){
        if(v instanceof LinearLayout){LinearLayout l=(LinearLayout)v;for(int i=0;i<l.getChildCount();i++){View c=l.getChildAt(i);if(c instanceof Button&&String.valueOf(((Button)c).getText()).contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR"))return l;}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){LinearLayout x=findPhotoCard395(g.getChildAt(i));if(x!=null)return x;}}return null;
    }
    private void tuneCounters395(View v){
        if(v instanceof TextView){TextView t=(TextView)v;String s=String.valueOf(t.getText());
            if(s.matches("[0-9]+")||s.contains("₺")){t.setIncludeFontPadding(true);t.setGravity(Gravity.CENTER);t.setPadding(dp(6),dp(5),dp(6),dp(5));t.setMinHeight(dp(48));}
            if(s.startsWith("ÖDENMEMİŞ MALZEME")){t.setTextSize(11);t.setMinHeight(dp(38));t.setGravity(Gravity.CENTER);}
        }
        if(v instanceof Button&&String.valueOf(((Button)v).getText()).contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){v.setMinimumHeight(dp(58));}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)tuneCounters395(g.getChildAt(i));}
    }

    private int[] shirtSummary(long athleteId){
        int qty=0,total=0,paid=0;
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(qty),0),COALESCE(SUM(total),0),COALESCE(SUM(paidAmount),0) FROM material_transactions WHERE athleteId=? AND UPPER(product) LIKE '%TİŞÖRT%'",new String[]{String.valueOf(athleteId)});if(c.moveToFirst()){qty=c.getInt(0);total=c.getInt(1);paid=c.getInt(2);}c.close();}catch(Exception ignored){}
        return new int[]{qty,total,paid};
    }

    @Override void createRollingReport(long id,int monthCount,String filterName){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s(a,"name"),category=s(a,"category"),sibling=s(a,"sibling"),start=s(a,"startDate"),end=s(a,"endDate"),restart=s(a,"restartDate");int by=a.getInt(a.getColumnIndexOrThrow("birthYear"));a.close();
        HashMap<Integer,PayRec> pays=new HashMap<>();Cursor p=db.payments(id);while(p.moveToNext()){int yy=p.getInt(p.getColumnIndexOrThrow("year")),mm=p.getInt(p.getColumnIndexOrThrow("month"));pays.put(yy*100+mm,new PayRec(s(p,"marker"),p.getInt(p.getColumnIndexOrThrow("amount"))));}p.close();
        Calendar now=Calendar.getInstance();int anchor=anchorDay(start),currentKey=currentCycleKey(now,anchor),waitingKey=shiftMonth(currentKey,1),startKey=monthCount>0?shiftMonth(currentKey,-(monthCount-1)):registrationMonth(start,id,currentKey);int regKey=parseMonthKey(start);if(regKey>0&&regKey>startKey)startKey=regKey;if(startKey>currentKey)startKey=currentKey;
        ArrayList<Integer> months=new ArrayList<>();for(int k=startKey;;k=shiftMonth(k,1)){months.add(k);if(k==currentKey)break;if(months.size()>240)break;}months.add(waitingKey);
        int[] shirts=shirtSummary(id);int rows=months.size(),W=1400,rowH=86,H=520+rows*rowH+360;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setColor(BLACK);q.setTextSize(52);c.drawText("PARION VOLEYBOL AKADEMİSİ",60,78,q);q.setColor(GOLD);q.setTextSize(38);c.drawText("AİDAT BİLANÇOSU",60,132,q);q.setColor(BLACK);q.setTextSize(28);c.drawText("Sporcu: "+name,60,192,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(23);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Filtre: "+filterName,60,232,q);c.drawText("Dönem: "+keyLabel(startKey)+" – "+keyLabel(waitingKey),60,268,q);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);q.setColor(Color.DKGRAY);c.drawText("AY",60,330,q);c.drawText("ÖDEME TARİHİ",300,330,q);c.drawText("BEKLENEN",610,330,q);c.drawText("ÖDENEN",830,330,q);c.drawText("DURUM",1050,330,q);q.setStrokeWidth(2);c.drawLine(60,350,W-60,350,q);
        int y=380,totalPaid=0,totalExpected=0;
        for(int key:months){int yr=key/100,mo=key%100;boolean future=key==waitingKey;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeAt(yr,mo,start,end,restart);String status,date="—";int color;
            if(future){status="BEKLİYOR";color=Color.WHITE;}else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;expected=0;}else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=ORANGE;}else if(isDate(r.marker)){date=dateTr(r.marker);status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?PAID_GREEN_09F299:ORANGE;}else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);expected=0;}else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);expected=0;}else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);}else{status="ÖDEME DÖNEMİ";color=YELLOW;}
            if(!future&&expected>0&&active&&!"X".equals(r.marker))totalExpected+=expected;if(!future&&r.amount>0)totalPaid+=r.amount;
            q.setColor(color);c.drawRoundRect(48,y-38,W-48,y+34,16,16,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText(monthName(mo)+" "+yr,60,y,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(21);c.drawText(date,300,y,q);c.drawText(expected>0?money(expected):"—",610,y,q);c.drawText(!future&&r.amount>0?money(r.amount):"—",830,y,q);drawFit(c,q,status,1050,y,280);y+=rowH;
        }
        q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(27);c.drawText("TOPLAM BEKLENEN: "+money(totalExpected),60,y+35,q);c.drawText("TOPLAM ÖDENEN: "+money(totalPaid),520,y+35,q);int fark=totalPaid-totalExpected;q.setColor(fark<0?RED:fark>0?ORANGE:PAID_GREEN_09F299);c.drawText("FARK: "+(fark>0?"+":"")+money(fark),980,y+35,q);
        y+=82;q.setColor(Color.rgb(245,245,245));c.drawRoundRect(50,y-25,W-50,y+105,18,18,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(24);c.drawText("TİŞÖRT / MALZEME TAHSİLAT ÖZETİ",70,y+8,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(22);c.drawText("Verilen tişört: "+shirts[0]+" adet   •   Tutar: "+money(shirts[1])+"   •   Tahsil edilen: "+money(shirts[2]),70,y+48,q);int shirtDue=Math.max(0,shirts[1]-shirts[2]);q.setColor(shirtDue>0?RED:Color.rgb(0,130,75));q.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Kalan tişört/malzeme borcu: "+money(shirtDue),70,y+82,q);
        y+=145;String from=cycleDateLabel(waitingKey,anchor),to=cycleDateLabel(shiftMonth(waitingKey,1),anchor);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText("Sayın Velimiz,",60,y+30,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(20);c.drawText(monthName(waitingKey%100)+" ayı aidat ödeme aralığınız "+from+" – "+to+"'dir.",60,y+64,q);c.drawText("Aidat takibiniz sporcumuzun kulübe başlangıç gününe göre aylık olarak hesaplanmaktadır.",60,y+96,q);saveAndShare(bm,name,filterName);
    }
}
