package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.54 - restore clear cohort titles for current/previous month start lists. */
public class MainActivityV654 extends MainActivityV653 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::labelCohorts654,2350);
            root.postDelayed(this::labelCohorts654,2480);
        }
    }

    private void labelCohorts654(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ArrayList<TextView> mores=new ArrayList<>();
        collectMore654(root,mores);
        if(mores.size()<2)return;

        // V620 keeps current-month starts on the left and previous-month starts on the right.
        Collections.sort(mores,(a,b)->Integer.compare(screenX654(a),screenX654(b)));
        styleCohortCard654(mores.get(0),"BU AY BAŞLAYANLAR");
        styleCohortCard654(mores.get(1),"GEÇEN AY BAŞLAYANLAR");
    }

    private void styleCohortCard654(TextView more,String title){
        if(!(more.getParent() instanceof LinearLayout))return;
        LinearLayout card=(LinearLayout)more.getParent();
        TextView existing=findTitle654(card);
        if(existing==null){
            existing=new TextView(this);
            existing.setTag("v654-cohort-title");
            card.addView(existing,0,new LinearLayout.LayoutParams(-1,-2));
        }else if(card.indexOfChild(existing)>0){
            card.removeView(existing);card.addView(existing,0,new LinearLayout.LayoutParams(-1,-2));
        }
        existing.setVisibility(View.VISIBLE);
        existing.setText(title);
        existing.setTextSize(10.3f);
        existing.setTextColor(Color.rgb(48,48,48));
        existing.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        existing.setGravity(Gravity.CENTER);
        existing.setMaxLines(2);
        existing.setPadding(dp(3),dp(4),dp(3),dp(7));

        card.setPadding(dp(10),dp(8),dp(10),dp(10));
        ViewGroup.LayoutParams lp=card.getLayoutParams();
        if(lp!=null&&lp.height>0&&lp.height<dp(150)){lp.height=dp(150);card.setLayoutParams(lp);}
    }

    private TextView findTitle654(LinearLayout card){
        for(int i=0;i<card.getChildCount();i++){
            View v=card.getChildAt(i);
            if(v instanceof TextView){
                TextView t=(TextView)v;
                if("v654-cohort-title".equals(t.getTag()))return t;
                String n=norm654(String.valueOf(t.getText()));
                if(n.contains("BU AY BAŞLAYAN SPORCULAR")||n.contains("GEÇEN AY BAŞLAYAN SPORCULAR"))return t;
            }
        }
        return null;
    }

    private void collectMore654(View v,List<TextView> out){
        if(v instanceof TextView&&"v619-more".equals(v.getTag()))out.add((TextView)v);
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectMore654(g.getChildAt(i),out);}
    }
    private int screenX654(View v){int[] p=new int[2];v.getLocationOnScreen(p);return p[0];}
    private String norm654(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
