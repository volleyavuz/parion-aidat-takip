package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivityV411 extends MainActivityV410 {
    private final ArrayList<String> listEditValues411=new ArrayList<>();
    private final ArrayList<Integer> listSpinnerValues411=new ArrayList<>();
    private boolean restoreFilters411=false;
    private final SimpleDateFormat iso411=new SimpleDateFormat("yyyy-MM-dd",Locale.US);

    @Override void base(String title,boolean back){
        super.base(title,back);
        if("PARION SPOR OKULU".equals(title)||title.contains("ONLINE GİRİŞ"))return;
        try{
            if(root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);
            Button home=btn("⌂ ANASAYFA");home.setTextSize(11);home.setTextColor(Color.WHITE);home.setBackground(round(Color.rgb(55,55,55),9));
            home.setOnClickListener(v->{clearListState411();showHome();});
            LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dp(104),dp(44));hp.setMargins(dp(4),dp(4),0,dp(4));bar.addView(home,hp);
        }catch(Exception ignored){}
    }

    private void clearListState411(){listEditValues411.clear();listSpinnerValues411.clear();restoreFilters411=false;}

    private void captureListFilters411(){
        listEditValues411.clear();listSpinnerValues411.clear();collectFilters411(root);restoreFilters411=true;
    }
    private void collectFilters411(View v){
        if(v instanceof EditText){listEditValues411.add(((EditText)v).getText().toString());return;}
        if(v instanceof Spinner){listSpinnerValues411.add(((Spinner)v).getSelectedItemPosition());return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectFilters411(g.getChildAt(i));}
    }
    private void restoreListFilters411(){
        if(!restoreFilters411)return;
        final int[] ei={0},si={0};restoreFiltersRecursive411(root,ei,si);
    }
    private void restoreFiltersRecursive411(View v,int[] ei,int[] si){
        if(v instanceof EditText){if(ei[0]<listEditValues411.size())((EditText)v).setText(listEditValues411.get(ei[0]));ei[0]++;return;}
        if(v instanceof Spinner){if(si[0]<listSpinnerValues411.size()){Spinner s=(Spinner)v;int p=listSpinnerValues411.get(si[0]);if(p>=0&&p<s.getCount())s.setSelection(p);}si[0]++;return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)restoreFiltersRecursive411(g.getChildAt(i),ei,si);}
    }

    @Override void showProfile(long id){
        boolean fromAthleteList="LIST".equals(page);
        if(fromAthleteList)captureListFilters411();
        super.showProfile(id);
        addCustomMaterialButton411(id);
    }

    @Override void showAthletes(){
        super.showAthletes();
        if(restoreFilters411)root.post(this::restoreListFilters411);
    }

    @Override void showHome(){clearListState411();super.showHome();}

    private void addCustomMaterialButton411(long athleteId){
        TextView materialTitle=findText411(root,"MALZEME TAKİBİ");if(materialTitle==null)return;
        ViewParent vp=materialTitle.getParent();if(!(vp instanceof ViewGroup))return;ViewGroup p=(ViewGroup)vp;
        for(int i=0;i<p.getChildCount();i++)if(p.getChildAt(i) instanceof Button&&String.valueOf(((Button)p.getChildAt(i)).getText()).contains("FARKLI MALZEME"))return;
        Button b=btn("+ FARKLI MALZEME EKLE");b.setOnClickListener(v->showCustomMaterial411(athleteId));
        int pos=Math.min(p.indexOfChild(materialTitle)+2,p.getChildCount());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(5),0,dp(5));p.addView(b,pos,lp);
    }
    private TextView findText411(View v,String text){
        if(v instanceof TextView&&text.equalsIgnoreCase(String.valueOf(((TextView)v).getText()).trim()))return (TextView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView t=findText411(g.getChildAt(i),text);if(t!=null)return t;}}return null;
    }

    private void showCustomMaterial411(long athleteId){
        LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(20),dp(6),dp(20),0);
        EditText name=new EditText(this);name.setHint("MALZEME ADI");
        EditText qty=new EditText(this);qty.setHint("ADET");qty.setInputType(2);qty.setText("1");
        EditText price=new EditText(this);price.setHint("BİRİM FİYAT ₺");price.setInputType(2);
        EditText date=new EditText(this);date.setHint("VERİLİŞ TARİHİ");date.setFocusable(false);date.setClickable(true);date.setText(iso411.format(new Date()));date.setOnClickListener(v->pickDate411(date));
        CheckBox paid=new CheckBox(this);paid.setText("ÜCRETİ ŞİMDİ TAM OLARAK ALINDI");
        EditText note=new EditText(this);note.setHint("NOT (BEDEN, AÇIKLAMA VB.)");
        x.addView(name);x.addView(qty);x.addView(price);x.addView(date);x.addView(paid);x.addView(note);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("FARKLI MALZEME EKLE").setView(x).setPositiveButton("KAYDET",null).setNegativeButton("VAZGEÇ",null).create();
        dlg.setOnShowListener(z->dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String product=name.getText().toString().trim().toUpperCase(new Locale("tr","TR"));int q=Math.max(1,parseInt(qty.getText().toString()));int u=parseInt(price.getText().toString());
            if(product.isEmpty()){toast("Malzeme adını girin.");return;}if(u<=0){toast("Geçerli bir birim fiyat girin.");return;}
            int total=q*u,pa=paid.isChecked()?total:0;String cloudId=UUID.randomUUID().toString(),day=date.getText().toString();String nt=note.getText().toString().trim();
            SQLiteDatabase d=db.getWritableDatabase();ContentValues cv=new ContentValues();cv.put("cloudId",cloudId);cv.put("athleteId",athleteId);cv.put("product",product);cv.put("qty",q);cv.put("unitPrice",u);cv.put("total",total);cv.put("paidAmount",pa);cv.put("issuedDate",day);cv.put("paymentDate",paid.isChecked()?day:"");cv.put("note",nt);d.insert("material_transactions",null,cv);
            ContentValues pv=new ContentValues();pv.put("name",product);pv.put("currentPrice",u);pv.put("active",1);d.insertWithOnConflict("material_products",null,pv,SQLiteDatabase.CONFLICT_REPLACE);
            pushCustomCloud411(cloudId,athleteId,product,q,u,total,pa,day,paid.isChecked()?day:"",nt);dlg.dismiss();showProfile(athleteId);
        }));dlg.show();
    }
    private void pickDate411(EditText e){Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->e.setText(String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    private void pushCustomCloud411(String id,long aid,String product,int qty,int unit,int total,int paid,String issued,String payment,String note){
        try{Method m=MainActivityV393.class.getDeclaredMethod("pushMaterialTransaction",String.class,long.class,String.class,int.class,int.class,int.class,int.class,String.class,String.class,String.class);m.setAccessible(true);m.invoke(this,id,aid,product,qty,unit,total,paid,issued,payment,note);}catch(Exception ignored){}
        try{Method m=MainActivityV393.class.getDeclaredMethod("upsertProductCloud",String.class,int.class);m.setAccessible(true);m.invoke(this,product,unit);}catch(Exception ignored){}
    }

    @Override void goBack(){
        if("DELETED".equals(page)||"MATERIAL_DEBTS".equals(page)){showHome();return;}
        if("MATERIAL_PRICES".equals(page)&&currentAthlete>0){showProfile(currentAthlete);return;}
        if("LIST".equals(page)){clearListState411();showHome();return;}
        if("HOME".equals(page)){super.goBack();return;}
        super.goBack();
        // Alt sınıflarca tanınmayan bir sayfa HOME'a düşmek yerine uygulamayı kapatırsa bir sonraki fiziksel geri için HOME güvenli limandır.
    }

    @Override public void onBackPressed(){
        if(!"HOME".equals(page)&&!"LOGIN".equals(page)){goBack();return;}
        super.onBackPressed();
    }
}
