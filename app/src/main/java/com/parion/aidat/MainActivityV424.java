package com.parion.aidat;

import android.graphics.Color;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivityV424 extends MainActivityV423 {
    @Override void showAthletes(){
        page="LIST";base("SPORCULAR",true);
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(8),dp(8),dp(8),dp(6));p.setBackground(round(Color.WHITE,10));
        Button add=btn("+ YENİ KAYIT");add.setOnClickListener(v->form(-1));p.addView(add,new LinearLayout.LayoutParams(-1,dp(52)));
        EditText q=new EditText(this);q.setHint("Sporcu adı ara");p.addView(q,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout r1=new LinearLayout(this);Spinner st=sp(new String[]{"TÜM DURUMLAR","AKTİF","ARA VERDİ","BIRAKTI","ARANACAK","SAKATLANDI"},"");Spinner cat=sp(new String[]{"TÜM KATEGORİLER","SO 1","SO 2","SO 3","MİNİ VOLEYBOL","MİDİ","KÜÇÜK","YILDIZ","GENÇ"},"");r1.addView(st,new LinearLayout.LayoutParams(0,dp(48),1));r1.addView(cat,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r1);
        LinearLayout r2=new LinearLayout(this);EditText by=new EditText(this);by.setHint("Doğum yılı");by.setInputType(2);Spinner sort=sp(SORTS,"");r2.addView(by,new LinearLayout.LayoutParams(0,dp(48),1));r2.addView(sort,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r2);
        ArrayList<String> notes=db.uniqueNotes();final Spinner note;
        if(notes.isEmpty())note=null;else{ArrayList<String> ni=new ArrayList<>();ni.add("TÜM ÖZEL NOTLAR");ni.addAll(notes);note=new Spinner(this);note.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,ni));p.addView(note,new LinearLayout.LayoutParams(-1,dp(52)));}
        root.addView(p);ScrollView sv=scroll();LinearLayout list=box(sv);
        Runnable load=()->load(list,q.getText().toString(),String.valueOf(st.getSelectedItem()),String.valueOf(cat.getSelectedItem()),by.getText().toString(),note==null?"TÜM ÖZEL NOTLAR":String.valueOf(note.getSelectedItem()),sort.getSelectedItemPosition());
        android.text.TextWatcher tw=new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){load.run();}public void afterTextChanged(android.text.Editable e){}};q.addTextChangedListener(tw);by.addTextChangedListener(tw);
        android.widget.AdapterView.OnItemSelectedListener sl=new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> pp,View v,int pos,long id){load.run();}public void onNothingSelected(android.widget.AdapterView<?> pp){}};st.setOnItemSelectedListener(sl);cat.setOnItemSelectedListener(sl);sort.setOnItemSelectedListener(sl);if(note!=null)note.setOnItemSelectedListener(sl);load.run();
    }
}
