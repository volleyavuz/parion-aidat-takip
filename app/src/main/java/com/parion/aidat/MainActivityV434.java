package com.parion.aidat;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivityV434 extends MainActivityV432 {
    private String q434="",status434="TÜM DURUMLAR",cat434="TÜM KATEGORİLER",birth434="",note434="TÜM ÖZEL NOTLAR";
    private int sort434=0,scroll434=0;
    private boolean returnAthletes434=false;

    @Override void base(String title,boolean back){
        super.base(title,back);
        patchRealLogo434();
    }

    private void patchRealLogo434(){
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);
            for(int i=bar.getChildCount()-1;i>=0;i--){
                Object tag=bar.getChildAt(i).getTag();
                if("PARION_HOME_LOGO_432".equals(tag)||"PARION_HEADER_LOGO_431".equals(tag)||"PARION_REAL_LOGO_434".equals(tag))bar.removeViewAt(i);
            }
            ImageView logo=new ImageView(this);
            logo.setTag("PARION_REAL_LOGO_434");
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
        page="ATHLETES434";
        base("SPORCULAR",true);
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(8),dp(8),dp(8),dp(6));p.setBackground(round(Color.WHITE,10));
        Button add=btn("+ YENİ KAYIT");add.setOnClickListener(v->form(-1));p.addView(add,new LinearLayout.LayoutParams(-1,dp(52)));
        EditText q=new EditText(this);q.setHint("Sporcu adı ara");q.setText(q434);p.addView(q,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout r1=new LinearLayout(this);
        Spinner st=sp(new String[]{"TÜM DURUMLAR","AKTİF","ARA VERDİ","BIRAKTI","ARANACAK","SAKATLANDI"},status434);
        Spinner cat=sp(new String[]{"TÜM KATEGORİLER","SO 1","SO 2","SO 3","MİNİ VOLEYBOL","MİDİ","KÜÇÜK","YILDIZ","GENÇ"},cat434);
        r1.addView(st,new LinearLayout.LayoutParams(0,dp(48),1));r1.addView(cat,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r1);
        LinearLayout r2=new LinearLayout(this);
        EditText by=new EditText(this);by.setHint("Doğum yılı");by.setInputType(2);by.setText(birth434);
        Spinner sort=sp(SORTS,"");if(sort434>=0&&sort434<SORTS.length)sort.setSelection(sort434);
        r2.addView(by,new LinearLayout.LayoutParams(0,dp(48),1));r2.addView(sort,new LinearLayout.LayoutParams(0,dp(48),1));p.addView(r2);
        ArrayList<String> notes=db.uniqueNotes();final Spinner note;
        if(notes.isEmpty()){note=null;note434="TÜM ÖZEL NOTLAR";}
        else{
            ArrayList<String> ni=new ArrayList<>();ni.add("TÜM ÖZEL NOTLAR");ni.addAll(notes);note=new Spinner(this);note.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,ni));
            boolean found=false;for(int i=0;i<ni.size();i++)if(ni.get(i).equalsIgnoreCase(note434)){note.setSelection(i);found=true;break;}if(!found){note434="TÜM ÖZEL NOTLAR";note.setSelection(0);}p.addView(note,new LinearLayout.LayoutParams(-1,dp(52)));
        }
        root.addView(p);
        ScrollView sv=scroll();LinearLayout list=box(sv);
        Runnable loadNow=()->load(list,q.getText().toString(),String.valueOf(st.getSelectedItem()),String.valueOf(cat.getSelectedItem()),by.getText().toString(),note==null?"TÜM ÖZEL NOTLAR":String.valueOf(note.getSelectedItem()),sort.getSelectedItemPosition());
        android.text.TextWatcher tw=new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){q434=q.getText().toString();birth434=by.getText().toString();loadNow.run();}public void afterTextChanged(android.text.Editable e){}};
        q.addTextChangedListener(tw);by.addTextChangedListener(tw);
        android.widget.AdapterView.OnItemSelectedListener sl=new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> pp,View v,int pos,long id){status434=String.valueOf(st.getSelectedItem());cat434=String.valueOf(cat.getSelectedItem());sort434=sort.getSelectedItemPosition();note434=note==null?"TÜM ÖZEL NOTLAR":String.valueOf(note.getSelectedItem());loadNow.run();}public void onNothingSelected(android.widget.AdapterView<?> pp){}};
        st.setOnItemSelectedListener(sl);cat.setOnItemSelectedListener(sl);sort.setOnItemSelectedListener(sl);if(note!=null)note.setOnItemSelectedListener(sl);
        sv.setOnScrollChangeListener((View v,int sx,int sy,int ox,int oy)->scroll434=sy);
        loadNow.run();sv.post(()->sv.scrollTo(0,Math.max(0,scroll434)));
    }

    @Override void showProfile(long id){
        boolean fromAthletes="ATHLETES434".equals(page);
        if(fromAthletes)returnAthletes434=true;
        super.showProfile(id);
        attachProfilePhoto434(id);
    }

    private void attachProfilePhoto434(long id){
        try{
            Cursor c=db.athlete(id);String photo="";if(c.moveToFirst())photo=s(c,"photo");c.close();
            ScrollView sv=findScroll434(root);if(sv==null)return;ImageView athlete=findFirstImage434(sv);if(athlete==null)return;final String ph=photo;
            athlete.setClickable(true);athlete.setOnClickListener(v->showFullPhoto434(ph));
        }catch(Exception ignored){}
    }

    private void showFullPhoto434(String photo){
        Dialog d=new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView im=new ImageView(this);im.setBackgroundColor(Color.BLACK);im.setScaleType(ImageView.ScaleType.FIT_CENTER);setAthletePhoto(im,photo);im.setOnClickListener(v->d.dismiss());d.setContentView(im);d.show();
    }

    @Override void goBack(){
        if("PROFILE".equals(page)&&returnAthletes434){returnAthletes434=false;showAthletes();return;}
        if("ATHLETES434".equals(page)){showHome();return;}
        super.goBack();
    }

    @Override void showHome(){returnAthletes434=false;super.showHome();}

    private ScrollView findScroll434(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll434(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private ImageView findFirstImage434(View v){if(v instanceof ImageView)return (ImageView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ImageView im=findFirstImage434(g.getChildAt(i));if(im!=null)return im;}}return null;}
}
