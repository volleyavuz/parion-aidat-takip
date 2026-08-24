package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.67 - targeted post-layout fixes only; keeps v4.0.66 ANR-safe path. */
public class MainActivityV667 extends MainActivityV666 {
    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::patch667);
    }

    private void patch667(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        View fresh=findTag667(root,"v657-fresh");
        if(fresh instanceof LinearLayout){
            LinearLayout f=(LinearLayout)fresh;
            if(f.getChildCount()>1&&f.getChildAt(1) instanceof LinearLayout){
                LinearLayout r=(LinearLayout)f.getChildAt(1);
                if(r.getChildCount()>0)hideActiveRepeats667(r.getChildAt(0));
            }
            if(f.getChildCount()>4&&f.getChildAt(4) instanceof LinearLayout){
                LinearLayout r=(LinearLayout)f.getChildAt(4);
                if(r.getChildCount()>0)hideOverdueSub667(r.getChildAt(0));
            }
        }
        restoreCurrentStarts667();
    }

    private void hideActiveRepeats667(View card){
        if(!(card instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)card;
        for(int i=0;i<g.getChildCount();i++){
            View v=g.getChildAt(i);
            if(v instanceof TextView){
                TextView t=(TextView)v;String s=norm667(String.valueOf(t.getText()));
                Object tag=t.getTag();
                if("sub".equals(tag)||"v620-active-count".equals(tag)||s.matches("\\d+ AKTİF SPORCU"))collapse667(t);
            }
        }
    }

    private void hideOverdueSub667(View card){
        TextView sub=findTaggedText667(card,"sub");
        if(sub!=null)collapse667(sub);
    }

    private void restoreCurrentStarts667(){
        View row=findTag667(root,"v662-start-row");
        if(!(row instanceof LinearLayout))return;
        LinearLayout r=(LinearLayout)row;
        if(findText667(r,"BU AY BAŞLAYANLAR","BU AY BAŞLAYAN SPORCULAR")!=null)return;
        r.addView(buildCurrentStarts667(),0,cell667());
    }

    private View buildCurrentStarts667(){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(10),dp(9),dp(10),dp(10));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));card.setBackground(bg);card.setElevation(dp(1));
        TextView h=text667("BU AY BAŞLAYANLAR",10.3f,true);h.setGravity(Gravity.CENTER);h.setPadding(dp(2),dp(2),dp(2),dp(6));card.addView(h);
        ArrayList<Long> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();
        String from=firstDay667(Calendar.getInstance());Calendar nx=Calendar.getInstance();nx.set(Calendar.DAY_OF_MONTH,1);nx.add(Calendar.MONTH,1);String to=firstDay667(nx);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND startDate>=? AND startDate<? ORDER BY startDate,name COLLATE NOCASE",new String[]{from,to});
        while(c.moveToNext()){ids.add(c.getLong(0));names.add(c.getString(1)==null?"":c.getString(1));}c.close();
        if(names.isEmpty()){
            TextView e=text667("Sporcu yok",10f,false);e.setTextColor(Color.GRAY);e.setGravity(Gravity.CENTER);card.addView(e);
        }else{
            ArrayList<TextView> hidden=new ArrayList<>();
            for(int i=0;i<names.size();i++){
                final long id=ids.get(i);TextView n=text667("• "+names.get(i),10f,false);n.setPadding(dp(3),dp(3),dp(3),dp(3));n.setMaxLines(2);n.setOnClickListener(v->showProfile(id));card.addView(n);if(i>=3){n.setVisibility(View.GONE);hidden.add(n);}
            }
            if(names.size()>3){TextView more=text667("Tümünü Gör ("+names.size()+")  ›",10f,true);more.setTextColor(Color.rgb(205,156,34));more.setGravity(Gravity.CENTER);more.setPadding(dp(3),dp(7),dp(3),dp(2));more.setOnClickListener(v->{boolean exp=!hidden.isEmpty()&&hidden.get(0).getVisibility()!=View.VISIBLE;for(TextView t:hidden)t.setVisibility(exp?View.VISIBLE:View.GONE);more.setText(exp?"Daralt  ‹":"Tümünü Gör ("+names.size()+")  ›");});card.addView(more);}
        }
        return card;
    }

    private LinearLayout.LayoutParams cell667(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);lp.setMargins(dp(3),0,dp(3),0);return lp;}
    private TextView text667(String s,float sp,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(Color.rgb(42,42,42));if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private String firstDay667(Calendar c){return String.format(Locale.US,"%04d-%02d-01",c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1);}
    private void collapse667(TextView t){t.setVisibility(View.GONE);t.setPadding(0,0,0,0);ViewGroup.LayoutParams lp=t.getLayoutParams();if(lp!=null){lp.height=0;t.setLayoutParams(lp);}}
    private TextView findTaggedText667(View v,String tag){if(v instanceof TextView&&tag.equals(v.getTag()))return(TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findTaggedText667(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private View findTag667(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag667(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private TextView findText667(View v,String... needles){if(v instanceof TextView){String u=norm667(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm667(n)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText667(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private String norm667(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
