package com.parion.aidat;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.database.Cursor;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.text.NumberFormat;
import java.util.*;

public class MainActivity extends Activity {
    private DbHelper db;
    private LinearLayout root, content;
    private int year, month;
    private final int GOLD=Color.rgb(242,201,76), BLACK=Color.rgb(17,17,17), BG=Color.rgb(247,247,247);
    private NumberFormat money=NumberFormat.getCurrencyInstance(new Locale("tr","TR"));
    private final String[] months={"Ocak","Şubat","Mart","Nisan","Mayıs","Haziran","Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"};

    @Override public void onCreate(Bundle b){ super.onCreate(b); db=new DbHelper(this); Calendar cal=Calendar.getInstance();year=cal.get(Calendar.YEAR);month=cal.get(Calendar.MONTH)+1; buildShell(); showDashboard(); }
    private TextView tv(String s,int sp,boolean bold){ TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(BLACK);t.setPadding(dp(12),dp(8),dp(12),dp(8)); if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t; }
    private Button btn(String s){ Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b; }
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private void buildShell(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        TextView head=tv("PARION  •  AİDAT TAKİP",22,true);head.setTextColor(GOLD);head.setGravity(Gravity.CENTER_VERTICAL);head.setBackgroundColor(BLACK);head.setPadding(dp(18),dp(18),dp(18),dp(18));root.addView(head,new LinearLayout.LayoutParams(-1,-2));
        ScrollView sv=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(12),dp(12),dp(12),dp(90));sv.addView(content);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this);nav.setBackgroundColor(BLACK);String[] labs={"Özet","Sporcular","Borçlular","+ Sporcu"};for(String s:labs){Button b=btn(s);b.setTextColor(s.equals("+ Sporcu")?BLACK:GOLD);if(s.equals("+ Sporcu"))b.setBackgroundColor(GOLD);b.setOnClickListener(v->{if(s.equals("Özet"))showDashboard();else if(s.equals("Sporcular"))showAthletes(false);else if(s.equals("Borçlular"))showAthletes(true);else addAthleteDialog();});nav.addView(b,new LinearLayout.LayoutParams(0,dp(62),1));}root.addView(nav);setContentView(root);
    }
    private void clear(){content.removeAllViews();}
    private void monthHeader(){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER);Button prev=btn("‹");Button next=btn("›");TextView title=tv(months[month-1]+" "+year,20,true);title.setGravity(Gravity.CENTER);prev.setOnClickListener(v->{month--;if(month==0){month=12;year--;}showDashboard();});next.setOnClickListener(v->{month++;if(month==13){month=1;year++;}showDashboard();});r.addView(prev,new LinearLayout.LayoutParams(dp(56),-2));r.addView(title,new LinearLayout.LayoutParams(0,-2,1));r.addView(next,new LinearLayout.LayoutParams(dp(56),-2));content.addView(r);
    }
    private void card(String title,String value){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(14),dp(10),dp(14),dp(10));c.setBackgroundColor(Color.WHITE);c.addView(tv(title,13,false));TextView val=tv(value,24,true);c.addView(val);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));content.addView(c,p);}
    private void showDashboard(){clear();monthHeader();double exp=db.expected(), col=db.collected(year,month);int total=db.activeCount(),paid=db.paidCount(year,month);card("Beklenen aidat",money.format(exp));card("Tahsil edilen",money.format(col));card("Kalan",money.format(Math.max(0,exp-col)));card("Ödeme yapmayan",Math.max(0,total-paid)+" sporcu");Button b=btn("Bu ay ödeme yapmayanları göster");b.setOnClickListener(v->showAthletes(true));content.addView(b);TextView info=tv("Pilot v0.1 • Veriler yalnızca bu cihazda saklanır.",12,false);info.setGravity(Gravity.CENTER);content.addView(info);}
    private void showAthletes(boolean debtOnly){clear();TextView h=tv((debtOnly?months[month-1]+" "+year+" Borçluları":"Sporcular"),22,true);content.addView(h);Cursor c=debtOnly?db.debtors(year,month):db.athletes();if(!c.moveToFirst()){content.addView(tv(debtOnly?"Bu ay borçlu sporcu yok.":"Henüz sporcu eklenmedi.",16,false));c.close();return;}do{long id=c.getLong(c.getColumnIndexOrThrow("id"));String name=c.getString(c.getColumnIndexOrThrow("name"));String group=c.getString(c.getColumnIndexOrThrow("groupName"));double fee=c.getDouble(c.getColumnIndexOrThrow("monthlyFee"));boolean paid=db.isPaid(id,year,month);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));row.setBackgroundColor(Color.WHITE);TextView n=tv(name,18,true);row.addView(n);row.addView(tv((group==null?"":group+" • ")+money.format(fee)+" / ay",13,false));Button pay=btn(paid?"✓ Ödendi — geri al":"Aidatı ödendi işaretle");pay.setOnClickListener(v->{if(paid){db.removePayment(id,year,month);showAthletes(debtOnly);}else paymentDialog(id,name,fee,debtOnly);});row.addView(pay);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));content.addView(row,p);}while(c.moveToNext());c.close();}
    private EditText field(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setInputType(type);e.setPadding(dp(8),dp(8),dp(8),dp(8));return e;}
    private void addAthleteDialog(){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(dp(20),0,dp(20),0);EditText name=field("Sporcu adı soyadı",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS), birth=field("Doğum yılı",InputType.TYPE_CLASS_NUMBER), parent=field("Veli adı soyadı",InputType.TYPE_CLASS_TEXT), phone=field("Telefon",InputType.TYPE_CLASS_PHONE), group=field("Grup (örn. Spor Okulu A)",InputType.TYPE_CLASS_TEXT), fee=field("Aylık aidat (₺)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);for(EditText e:new EditText[]{name,birth,parent,phone,group,fee})f.addView(e);new AlertDialog.Builder(this).setTitle("Yeni Sporcu").setView(f).setNegativeButton("Vazgeç",null).setPositiveButton("Kaydet",(d,w)->{if(name.getText().toString().trim().isEmpty())return;int by=parseInt(birth.getText().toString(),0);double mf=parseDouble(fee.getText().toString(),0);db.addAthlete(name.getText().toString().trim(),by,parent.getText().toString().trim(),phone.getText().toString().trim(),group.getText().toString().trim(),mf);showAthletes(false);}).show();}
    private void paymentDialog(long id,String name,double defaultFee,boolean debtOnly){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(dp(20),0,dp(20),0);EditText amount=field("Tutar",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);amount.setText(String.valueOf((int)defaultFee));Spinner method=new Spinner(this);String[] methods={"Nakit","Havale / EFT","Kart","Diğer"};method.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,methods));f.addView(tv(name+" • "+months[month-1]+" "+year,15,true));f.addView(amount);f.addView(method);new AlertDialog.Builder(this).setTitle("Ödeme Kaydı").setView(f).setNegativeButton("Vazgeç",null).setPositiveButton("Kaydet",(d,w)->{db.markPaid(id,year,month,parseDouble(amount.getText().toString(),defaultFee),methods[method.getSelectedItemPosition()]);showAthletes(debtOnly);}).show();}
    private int parseInt(String s,int def){try{return Integer.parseInt(s);}catch(Exception e){return def;}}
    private double parseDouble(String s,double def){try{return Double.parseDouble(s.replace(',','.'));}catch(Exception e){return def;}}
}
