package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivityV391 extends MainActivityV390 {

    @Override void showAthletes(){
        page="LIST";base("SPORCULAR",true);
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(8),dp(8),dp(8),dp(6));p.setBackground(round(Color.WHITE,10));
        Button add=btn("+ YENİ KAYIT");add.setOnClickListener(v->form(-1));p.addView(add,new LinearLayout.LayoutParams(-1,dp(52)));
        EditText q=new EditText(this);q.setHint("Sporcu adı ara");p.addView(q,new LinearLayout.LayoutParams(-1,dp(48)));

        LinearLayout r1=new LinearLayout(this);
        Spinner st=sp(new String[]{"TÜM DURUMLAR","AKTİF","ARA VERDİ","BIRAKTI","ARANACAK","SAKATLANDI"},"");
        Spinner cat=sp(new String[]{"TÜM KATEGORİLER","SO 1","SO 2","SO 3","MİNİ VOLEYBOL","MİDİ","KÜÇÜK","YILDIZ","GENÇ"},"");
        r1.addView(st,new LinearLayout.LayoutParams(0,dp(48),1));r1.addView(cat,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r1);

        LinearLayout r2=new LinearLayout(this);
        ArrayList<String> years=new ArrayList<>();years.add("TÜM DOĞUM YILLARI");
        Cursor yc=db.getReadableDatabase().rawQuery("SELECT DISTINCT birthYear FROM athletes WHERE birthYear>0 AND TRIM(COALESCE(deletedAt,''))='' ORDER BY birthYear ASC",null);
        while(yc.moveToNext()) years.add(String.valueOf(yc.getInt(0)));yc.close();
        Spinner by=new Spinner(this);by.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,years));
        Spinner sort=sp(SORTS,"");
        r2.addView(by,new LinearLayout.LayoutParams(0,dp(48),1));r2.addView(sort,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r2);

        ArrayList<String> ni=new ArrayList<>();ni.add("TÜM ÖZEL NOTLAR");ni.addAll(db.uniqueNotes());
        Spinner note=new Spinner(this);note.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,ni));p.addView(note,new LinearLayout.LayoutParams(-1,dp(52)));root.addView(p);

        ScrollView sv=scroll();LinearLayout list=box(sv);
        Runnable load=()->load(list,q.getText().toString(),String.valueOf(st.getSelectedItem()),String.valueOf(cat.getSelectedItem()),yearValue(by),String.valueOf(note.getSelectedItem()),sort.getSelectedItemPosition());
        android.text.TextWatcher tw=new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){load.run();}public void afterTextChanged(android.text.Editable e){}};
        q.addTextChangedListener(tw);
        android.widget.AdapterView.OnItemSelectedListener sl=new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){load.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}};
        st.setOnItemSelectedListener(sl);cat.setOnItemSelectedListener(sl);by.setOnItemSelectedListener(sl);note.setOnItemSelectedListener(sl);sort.setOnItemSelectedListener(sl);load.run();
    }

    private String yearValue(Spinner by){String s=String.valueOf(by.getSelectedItem());return s.startsWith("TÜM")?"":s;}

    @Override void row(LinearLayout b,A x,String detail,int amount){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(7),dp(8),dp(7));r.setBackground(round(Color.WHITE,10));
        ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);av.setImageDrawable(new android.graphics.drawable.ColorDrawable(Color.rgb(230,230,230)));String photoKey=x.photo==null?"":x.photo;av.setTag(photoKey);setAthletePhoto(av,photoKey);r.addView(av,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));
        LinearLayout badges=new LinearLayout(this);badges.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        int gc=groupBadgeColor(x.cat),sc=statusBadgeColor(x.status);
        TextView g=tv(x.cat==null?"":x.cat,11,contrast(gc),true);g.setBackground(round(gc,9));g.setGravity(Gravity.CENTER);
        TextView s=tv(x.status==null?"":x.status,11,contrast(sc),true);s.setBackground(round(sc,9));s.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-2,dp(34));bp.setMargins(0,dp(2),dp(5),dp(2));badges.addView(g,bp);badges.addView(s,bp);t.addView(badges);
        TextView fee=tv(money(x.fee),12,Color.DKGRAY,false);fee.setPadding(dp(2),0,0,0);t.addView(fee);
        if(detail!=null)t.addView(tv(detail,12,Color.DKGRAY,false));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));if(amount>0)r.addView(tv(money(amount),14,BLACK,true),new LinearLayout.LayoutParams(dp(115),-2));
        final long id=x.id;r.setOnClickListener(v->showProfile(id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }

    private int contrast(int c){double y=.299*Color.red(c)+.587*Color.green(c)+.114*Color.blue(c);return y>155?Color.BLACK:Color.WHITE;}
    private int statusBadgeColor(String s){if("AKTİF".equalsIgnoreCase(s))return GREEN;if("ARA VERDİ".equalsIgnoreCase(s))return ORANGE;if("BIRAKTI".equalsIgnoreCase(s))return RED;if("SAKATLANDI".equalsIgnoreCase(s))return Color.rgb(123,31,162);if("ARANACAK".equalsIgnoreCase(s))return Color.rgb(2,119,189);return GRAY;}
    private int groupBadgeColor(String cat){String x=cat==null?"":cat.toUpperCase(new Locale("tr","TR"));if(x.contains("SO 1"))return Color.rgb(0,122,204);if(x.contains("SO 2"))return Color.rgb(126,87,194);if(x.contains("SO 3"))return Color.rgb(239,108,0);if(x.contains("MİNİ"))return Color.rgb(216,27,96);if(x.contains("MİDİ"))return Color.rgb(0,137,123);if(x.contains("KÜÇÜK"))return Color.rgb(245,166,35);if(x.contains("YILDIZ"))return Color.rgb(94,53,177);if(x.contains("GENÇ"))return Color.rgb(198,40,40);int[] p={Color.rgb(2,119,189),Color.rgb(0,121,107),Color.rgb(123,31,162),Color.rgb(230,81,0),Color.rgb(46,125,50)};return p[Math.abs(x.hashCode())%p.length];}
}
