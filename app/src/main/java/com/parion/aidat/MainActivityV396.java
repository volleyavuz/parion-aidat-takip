package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.*;

public class MainActivityV396 extends MainActivityV395 {
    private static final int PAID_GREEN=Color.rgb(9,242,153); // #09F299

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        migrateLegacyShirtDatesLocal();
    }

    private void migrateLegacyShirtDatesLocal(){
        try{
            ContentValues cv=new ContentValues();cv.put("issuedDate","2026-07-01");cv.put("paymentDate","2026-07-01");
            db.getWritableDatabase().update("material_transactions",cv,
                "issuedDate=? AND paymentDate=? AND note LIKE ?",
                new String[]{"2026-01-01","2026-01-01","2026 EXCEL Y.T.A./Y.T.%"});
        }catch(Exception ignored){}
    }

    @Override void addCycleProfileRow(LinearLayout b,long id,int key,int anchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeAt(yr,mo,start,end,restart);
        String status,detail;int color;String period=cycleDateLabel(key,anchor)+" – "+cycleDateLabel(shiftMonth(key,1),anchor);
        if(future){status="BEKLİYOR";color=Color.WHITE;detail=period+" • "+(expected>0?money(expected):"—");}
        else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=status.equals("ÖDENDİ")?PAID_GREEN:ORANGE;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?PAID_GREEN:ORANGE;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);detail=period+" • AKTİF DEĞİL";}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";}
        else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);detail=period+" • VERİ YOK";}
        else{status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));
        row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true));row.addView(tv(detail,12,Color.DKGRAY,false));
        if(!future&&yr==2026){final int mm=mo,fee=expected,amt=r.amount;final String mk=r.marker;row.setOnClickListener(v->editPayment(id,mm,fee,mk,amt));}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        renamePaymentShareButton(root);
        attachMaterialMenus(root,id);
    }

    private void renamePaymentShareButton(View v){
        if(v instanceof Button){Button b=(Button)v;String t=String.valueOf(b.getText());if(t.contains("AİDAT BİLANÇOSU"))b.setText("ÖDEME BİLGİSİ PAYLAŞ");}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)renamePaymentShareButton(g.getChildAt(i));}
    }

    private void attachMaterialMenus(View root,long athleteId){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,product,qty,unitPrice,total,issuedDate FROM material_transactions WHERE athleteId=?",new String[]{String.valueOf(athleteId)});
        while(c.moveToNext()){
            long tid=c.getLong(0);String product=c.getString(1);int qty=c.getInt(2),unit=c.getInt(3),total=c.getInt(4);
            String prefix=product+" • "+qty+" ADET × "+money(unit)+" = "+money(total);
            View row=findParentOfText(root,prefix);if(row!=null)row.setOnClickListener(v->showMaterialMenu396(athleteId,tid));
        }c.close();
    }
    private View findParentOfText(View v,String exact){
        if(v instanceof TextView&&exact.equals(String.valueOf(((TextView)v).getText())))return (View)v.getParent();
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findParentOfText(g.getChildAt(i),exact);if(x!=null)return x;}}return null;
    }

    private void showMaterialMenu396(long athleteId,long tid){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT product,total,paidAmount,cloudId FROM material_transactions WHERE id=?",new String[]{String.valueOf(tid)});if(!c.moveToFirst()){c.close();return;}
        String product=c.getString(0),cloudId=c.getString(3);int total=c.getInt(1),paid=c.getInt(2),remain=total-paid;c.close();
        ArrayList<String> opts=new ArrayList<>();opts.add("GÜNCELLE");if(remain>0){opts.add("KALANIN TAMAMINI TAHSİL ET");opts.add("KISMİ ÖDEME GİR");}opts.add("HAREKETİ SİL");
        new AlertDialog.Builder(this).setTitle(product+" • "+money(total)).setItems(opts.toArray(new String[0]),(d,w)->{
            String s=opts.get(w);if("GÜNCELLE".equals(s))editMaterial396(athleteId,tid);else if(s.startsWith("KALANIN"))setMaterialPaid396(athleteId,tid,cloudId,total);else if(s.startsWith("KISMİ"))partialMaterial396(athleteId,tid,cloudId,total,paid);else deleteMaterial396(athleteId,tid,cloudId);
        }).show();
    }

    private void editMaterial396(long athleteId,long tid){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT product,qty,unitPrice,total,paidAmount,issuedDate,paymentDate,note,cloudId FROM material_transactions WHERE id=?",new String[]{String.valueOf(tid)});if(!c.moveToFirst()){c.close();return;}
        String product=c.getString(0),issued=c.getString(5),payment=c.getString(6),note=c.getString(7),cloudId=c.getString(8);int oldQty=c.getInt(1),oldUnit=c.getInt(2),oldPaid=c.getInt(4);c.close();
        LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(18),dp(4),dp(18),0);
        EditText p=new EditText(this);p.setHint("MALZEME");p.setText(product);EditText q=new EditText(this);q.setHint("ADET");q.setInputType(2);q.setText(String.valueOf(oldQty));EditText u=new EditText(this);u.setHint("BİRİM FİYAT ₺");u.setInputType(2);u.setText(String.valueOf(oldUnit));
        EditText d=new EditText(this);d.setHint("VERİLİŞ TARİHİ");d.setText(issued);d.setFocusable(false);d.setClickable(true);d.setOnClickListener(v->pickDate396(d));
        EditText pa=new EditText(this);pa.setHint("TAHSİL EDİLEN ₺");pa.setInputType(2);pa.setText(String.valueOf(oldPaid));EditText n=new EditText(this);n.setHint("NOT");n.setText(note==null?"":note);
        x.addView(p);x.addView(q);x.addView(u);x.addView(d);x.addView(pa);x.addView(n);
        new AlertDialog.Builder(this).setTitle("MALZEME HAREKETİNİ GÜNCELLE").setView(x).setPositiveButton("KAYDET",(dd,w)->{
            String prod=p.getText().toString().trim().toUpperCase(new Locale("tr","TR"));int qty=Math.max(1,parseInt(q.getText().toString())),unit=parseInt(u.getText().toString()),total=qty*unit,paid=Math.min(total,Math.max(0,parseInt(pa.getText().toString())));String day=d.getText().toString();String payDay=paid>0?(payment==null||payment.isEmpty()?day:payment):"";
            if(prod.isEmpty()||unit<=0){toast("Malzeme adı ve birim fiyat geçerli olmalıdır.");return;}
            ContentValues cv=new ContentValues();cv.put("product",prod);cv.put("qty",qty);cv.put("unitPrice",unit);cv.put("total",total);cv.put("paidAmount",paid);cv.put("issuedDate",day);cv.put("paymentDate",payDay);cv.put("note",n.getText().toString().trim());db.getWritableDatabase().update("material_transactions",cv,"id=?",new String[]{String.valueOf(tid)});
            patchMaterialFull396(cloudId,prod,qty,unit,total,paid,day,payDay,n.getText().toString().trim());showProfile(athleteId);
        }).setNegativeButton("VAZGEÇ",null).show();
    }

    private void pickDate396(EditText e){Calendar c=Calendar.getInstance();try{String[] p=e.getText().toString().split("-");if(p.length==3)c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]));}catch(Exception ignored){}new DatePickerDialog(this,(v,y,m,d)->e.setText(String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    private void setMaterialPaid396(long aid,long tid,String cloudId,int paid){String day=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());ContentValues cv=new ContentValues();cv.put("paidAmount",paid);cv.put("paymentDate",day);db.getWritableDatabase().update("material_transactions",cv,"id=?",new String[]{String.valueOf(tid)});patchMaterialPaid396(cloudId,paid,day);showProfile(aid);}
    private void partialMaterial396(long aid,long tid,String cloudId,int total,int oldPaid){EditText e=new EditText(this);e.setInputType(2);e.setHint("EK TAHSİLAT ₺");new AlertDialog.Builder(this).setTitle("KISMİ ÖDEME").setView(e).setPositiveButton("KAYDET",(d,w)->{int add=parseInt(e.getText().toString());if(add>0)setMaterialPaid396(aid,tid,cloudId,Math.min(total,oldPaid+add));}).setNegativeButton("VAZGEÇ",null).show();}
    private void deleteMaterial396(long aid,long tid,String cloudId){new AlertDialog.Builder(this).setTitle("MALZEME HAREKETİNİ SİL").setMessage("Bu hareket silinsin mi?").setPositiveButton("SİL",(d,w)->{db.getWritableDatabase().delete("material_transactions","id=?",new String[]{String.valueOf(tid)});deleteMaterialCloud396(cloudId);showProfile(aid);}).setNegativeButton("VAZGEÇ",null).show();}

    private void patchMaterialFull396(String cloudId,String product,int qty,int unit,int total,int paid,String issued,String payment,String note){if(cloudId==null||cloudId.isEmpty())return;new Thread(()->{try{JSONObject o=new JSONObject().put("product_name",product).put("quantity",qty).put("unit_price",unit).put("total_amount",total).put("paid_amount",paid).put("issued_at",issued).put("note",note);if(payment!=null&&!payment.isEmpty())o.put("payment_date",payment);request("PATCH",SUPABASE_URL+"/rest/v1/material_transactions?id=eq."+URLEncoder.encode(cloudId,"UTF-8"),o.toString(),cloudPrefs.getString("access_token",""));}catch(Exception ignored){}}).start();}
    private void patchMaterialPaid396(String cloudId,int paid,String day){if(cloudId==null||cloudId.isEmpty())return;new Thread(()->{try{JSONObject o=new JSONObject().put("paid_amount",paid).put("payment_date",day);request("PATCH",SUPABASE_URL+"/rest/v1/material_transactions?id=eq."+URLEncoder.encode(cloudId,"UTF-8"),o.toString(),cloudPrefs.getString("access_token",""));}catch(Exception ignored){}}).start();}
    private void deleteMaterialCloud396(String cloudId){if(cloudId==null||cloudId.isEmpty())return;new Thread(()->{try{request("DELETE",SUPABASE_URL+"/rest/v1/material_transactions?id=eq."+URLEncoder.encode(cloudId,"UTF-8"),null,cloudPrefs.getString("access_token",""));}catch(Exception ignored){}}).start();}

    @Override void createRollingReport(long id,int monthCount,String filterName){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}String name=s(a,"name"),category=s(a,"category"),sibling=s(a,"sibling"),start=s(a,"startDate"),end=s(a,"endDate"),restart=s(a,"restartDate");int by=a.getInt(a.getColumnIndexOrThrow("birthYear"));a.close();
        HashMap<Integer,PayRec> pays=new HashMap<>();Cursor pc=db.payments(id);while(pc.moveToNext()){int yy=pc.getInt(pc.getColumnIndexOrThrow("year")),mm=pc.getInt(pc.getColumnIndexOrThrow("month"));pays.put(yy*100+mm,new PayRec(s(pc,"marker"),pc.getInt(pc.getColumnIndexOrThrow("amount"))));}pc.close();
        Calendar now=Calendar.getInstance();int anchor=anchorDay(start),currentKey=currentCycleKey(now,anchor),waitingKey=shiftMonth(currentKey,1),startKey=monthCount>0?shiftMonth(currentKey,-(monthCount-1)):registrationMonth(start,id,currentKey);int reg=parseMonthKey(start);if(reg>0&&reg>startKey)startKey=reg;if(startKey>currentKey)startKey=currentKey;
        ArrayList<Integer> months=new ArrayList<>();for(int k=startKey;;k=shiftMonth(k,1)){months.add(k);if(k==currentKey)break;if(months.size()>240)break;}months.add(waitingKey);
        int[] shirts=shirtSummary396(id);int W=1400,rowH=86,H=570+months.size()*rowH+360;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap logo=BitmapFactory.decodeResource(getResources(),R.drawable.parion_logo);if(logo!=null){Rect dst=new Rect(55,35,185,165);c.drawBitmap(logo,null,dst,q);}
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setColor(BLACK);q.setTextSize(50);c.drawText("PARİON SPOR KULÜBÜ",220,82,q);q.setColor(GOLD);q.setTextSize(38);c.drawText("ÖDEME BİLGİSİ",220,132,q);
        q.setColor(BLACK);q.setTextSize(28);c.drawText("Sporcu: "+name,60,205,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(23);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Filtre: "+filterName,60,245,q);c.drawText("Dönem: "+keyLabel(startKey)+" – "+keyLabel(waitingKey),60,281,q);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);q.setColor(Color.DKGRAY);c.drawText("AY",60,343,q);c.drawText("ÖDEME TARİHİ",300,343,q);c.drawText("BEKLENEN",610,343,q);c.drawText("ÖDENEN",830,343,q);c.drawText("DURUM",1050,343,q);q.setStrokeWidth(2);c.drawLine(60,363,W-60,363,q);
        int y=393,totalPaid=0,totalExpected=0;
        for(int key:months){int yr=key/100,mo=key%100;boolean future=key==waitingKey;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeAt(yr,mo,start,end,restart);String status,date="—";int color;
            if(future){status="BEKLİYOR";color=Color.WHITE;}else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;expected=0;}else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=status.equals("ÖDENDİ")?PAID_GREEN:ORANGE;}else if(isDate(r.marker)){date=dateTr(r.marker);status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?PAID_GREEN:ORANGE;}else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);expected=0;}else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);expected=0;}else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);}else{status="ÖDEME DÖNEMİ";color=YELLOW;}
            if(!future&&expected>0&&active&&!"X".equals(r.marker))totalExpected+=expected;if(!future&&r.amount>0)totalPaid+=r.amount;q.setColor(color);c.drawRoundRect(48,y-38,W-48,y+34,16,16,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText(monthName(mo)+" "+yr,60,y,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(21);c.drawText(date,300,y,q);c.drawText(expected>0?money(expected):"—",610,y,q);c.drawText(!future&&r.amount>0?money(r.amount):"—",830,y,q);drawFit(c,q,status,1050,y,280);y+=rowH;
        }
        q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(27);c.drawText("TOPLAM BEKLENEN: "+money(totalExpected),60,y+35,q);c.drawText("TOPLAM ÖDENEN: "+money(totalPaid),520,y+35,q);int diff=totalPaid-totalExpected;q.setColor(diff<0?RED:diff>0?ORANGE:PAID_GREEN);c.drawText("FARK: "+(diff>0?"+":"")+money(diff),980,y+35,q);
        y+=82;q.setColor(Color.rgb(245,245,245));c.drawRoundRect(50,y-25,W-50,y+105,18,18,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(24);c.drawText("TİŞÖRT / MALZEME TAHSİLAT ÖZETİ",70,y+8,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(22);c.drawText("Verilen tişört: "+shirts[0]+" adet   •   Tutar: "+money(shirts[1])+"   •   Tahsil edilen: "+money(shirts[2]),70,y+48,q);int due=Math.max(0,shirts[1]-shirts[2]);q.setColor(due>0?RED:PAID_GREEN);q.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Kalan tişört/malzeme borcu: "+money(due),70,y+82,q);
        y+=145;String from=cycleDateLabel(waitingKey,anchor),to=cycleDateLabel(shiftMonth(waitingKey,1),anchor);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText("Sayın Velimiz,",60,y+30,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(20);c.drawText(monthName(waitingKey%100)+" ayı aidat ödeme aralığınız "+from+" – "+to+"'dir.",60,y+64,q);c.drawText("Aidat takibiniz sporcumuzun kulübe başlangıç gününe göre aylık olarak hesaplanmaktadır.",60,y+96,q);saveAndShare(bm,name,filterName);
    }

    private int[] shirtSummary396(long athleteId){int qty=0,total=0,paid=0;try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(qty),0),COALESCE(SUM(total),0),COALESCE(SUM(paidAmount),0) FROM material_transactions WHERE athleteId=? AND UPPER(product) LIKE '%TİŞÖRT%'",new String[]{String.valueOf(athleteId)});if(c.moveToFirst()){qty=c.getInt(0);total=c.getInt(1);paid=c.getInt(2);}c.close();}catch(Exception ignored){}return new int[]{qty,total,paid};}
}
