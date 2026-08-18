package com.parion.aidat;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivityV433 extends MainActivityV432 {
    private String q433="",status433="TÜM DURUMLAR",cat433="TÜM KATEGORİLER",birth433="",note433="TÜM ÖZEL NOTLAR";
    private int sort433=0,scroll433=0;
    private boolean returnAthletes433=false;

    @Override void base(String title,boolean back){
        super.base(title,back);
        patchOfficialLogo433();
    }

    private void patchOfficialLogo433(){
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);
            for(int i=bar.getChildCount()-1;i>=0;i--){
                View v=bar.getChildAt(i);
                Object tag=v.getTag();
                if("PARION_HOME_LOGO_432".equals(tag)||"PARION_HEADER_LOGO_431".equals(tag)||"PARION_OFFICIAL_LOGO_433".equals(tag))bar.removeViewAt(i);
            }
            ImageView logo=new ImageView(this);
            logo.setTag("PARION_OFFICIAL_LOGO_433");
            logo.setImageBitmap(ClubLogoAsset.bitmap());
            logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            logo.setContentDescription("Ana Sayfa");
            logo.setClickable(true);logo.setFocusable(true);
            logo.setOnClickListener(v->showHome());
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(42),dp(42));
            lp.setMargins(dp(2),0,dp(6),0);
            bar.addView(logo,0,lp);
        }catch(Exception ignored){}
    }

    @Override void showAthletes(){
        page="ATHLETES433";
        base("SPORCULAR",true);
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(8),dp(8),dp(8),dp(6));p.setBackground(round(Color.WHITE,10));
        Button add=btn("+ YENİ KAYIT");add.setOnClickListener(v->form(-1));p.addView(add,new LinearLayout.LayoutParams(-1,dp(52)));

        EditText q=new EditText(this);q.setHint("Sporcu adı ara");q.setText(q433);p.addView(q,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout r1=new LinearLayout(this);
        Spinner st=sp(new String[]{"TÜM DURUMLAR","AKTİF","ARA VERDİ","BIRAKTI","ARANACAK","SAKATLANDI"},status433);
        Spinner cat=sp(new String[]{"TÜM KATEGORİLER","SO 1","SO 2","SO 3","MİNİ VOLEYBOL","MİDİ","KÜÇÜK","YILDIZ","GENÇ"},cat433);
        r1.addView(st,new LinearLayout.LayoutParams(0,dp(48),1));r1.addView(cat,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r1);

        LinearLayout r2=new LinearLayout(this);EditText by=new EditText(this);by.setHint("Doğum yılı");by.setInputType(2);by.setText(birth433);
        Spinner sort=sp(SORTS,"");if(sort433>=0&&sort433<SORTS.length)sort.setSelection(sort433);
        r2.addView(by,new LinearLayout.LayoutParams(0,dp(48),1));r2.addView(sort,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r2);

        ArrayList<String> notes=db.uniqueNotes();final Spinner note;
        if(notes.isEmpty()){note=null;note433="TÜM ÖZEL NOTLAR";}
        else{
            ArrayList<String> ni=new ArrayList<>();ni.add("TÜM ÖZEL NOTLAR");ni.addAll(notes);note=new Spinner(this);note.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,ni));
            boolean found=false;for(int i=0;i<ni.size();i++)if(ni.get(i).equalsIgnoreCase(note433)){note.setSelection(i);found=true;break;}if(!found){note433="TÜM ÖZEL NOTLAR";note.setSelection(0);}p.addView(note,new LinearLayout.LayoutParams(-1,dp(52)));
        }
        root.addView(p);

        ScrollView sv=scroll();LinearLayout list=box(sv);
        Runnable load=()->load(list,q.getText().toString(),String.valueOf(st.getSelectedItem()),String.valueOf(cat.getSelectedItem()),by.getText().toString(),note==null?"TÜM ÖZEL NOTLAR":String.valueOf(note.getSelectedItem()),sort.getSelectedItemPosition());
        android.text.TextWatcher tw=new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){q433=q.getText().toString();birth433=by.getText().toString();load.run();}public void afterTextChanged(android.text.Editable e){}};
        q.addTextChangedListener(tw);by.addTextChangedListener(tw);
        android.widget.AdapterView.OnItemSelectedListener sl=new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> pp,View v,int pos,long id){status433=String.valueOf(st.getSelectedItem());cat433=String.valueOf(cat.getSelectedItem());sort433=sort.getSelectedItemPosition();note433=note==null?"TÜM ÖZEL NOTLAR":String.valueOf(note.getSelectedItem());load.run();}public void onNothingSelected(android.widget.AdapterView<?> pp){}};
        st.setOnItemSelectedListener(sl);cat.setOnItemSelectedListener(sl);sort.setOnItemSelectedListener(sl);if(note!=null)note.setOnItemSelectedListener(sl);
        sv.setOnScrollChangeListener((View v,int sx,int sy,int ox,int oy)->scroll433=sy);
        load.run();sv.post(()->sv.scrollTo(0,Math.max(0,scroll433)));
    }

    @Override void showProfile(long id){
        if("ATHLETES433".equals(page))returnAthletes433=true;
        super.showProfile(id);
    }

    @Override void goBack(){
        if("PROFILE".equals(page)&&returnAthletes433){returnAthletes433=false;showAthletes();return;}
        super.goBack();
    }

    @Override void showHome(){returnAthletes433=false;super.showHome();}
}
