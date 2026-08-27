package com.parion.aidat;

import android.view.*;
import android.widget.*;

/** v4.1.36 - visual-only dashboard polish: home icon tone, tshirt subtitle cleanup, absentee card clipping. */
public class MainActivityV714 extends MainActivityV713 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::polish714);
            root.postDelayed(this::polish714,120);
        }
    }

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(this::fixHome714);
    }

    private void polish714(){
        if(root==null||!"HOME".equals(page))return;
        fixHome714();
        View tshirt=findTag714(root,"v621-tshirt-card");
        if(tshirt instanceof ViewGroup)removeTshirtText714((ViewGroup)tshirt);
        View abs=findTag714(root,"v713-absentees");
        if(abs!=null){
            abs.setMinimumHeight(dp(138));
            ViewGroup.LayoutParams p=abs.getLayoutParams();
            if(p instanceof LinearLayout.LayoutParams){
                LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)p;
                lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.bottomMargin=Math.max(lp.bottomMargin,dp(18));
                abs.setLayoutParams(lp);
            }
            abs.setPadding(dp(12),dp(14),dp(12),dp(18));
        }
    }

    private void fixHome714(){fixHome714(root);}
    private void fixHome714(View v){
        if(v==null)return;
        CharSequence cd=v.getContentDescription();
        if(cd!=null&&"Anasayfa".equalsIgnoreCase(cd.toString())&&v instanceof ImageButton){
            ImageButton b=(ImageButton)v;
            b.setImageResource(R.drawable.ic_nav_home);
            b.setImageAlpha(148); // ~58%, visually matches Android built-in nav icons.
            b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            b.setPadding(dp(12),dp(12),dp(12),dp(12));
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)fixHome714(g.getChildAt(i));}
    }

    private void removeTshirtText714(ViewGroup g){
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);
            if(c instanceof TextView){
                String s=norm714(String.valueOf(((TextView)c).getText()));
                if(s.contains("VERİLEN TİŞÖRT ADEDİ")||s.contains("TİŞÖRT ADEDİ")||s.contains("TİŞÖRT SAYISI")||s.contains("AKTİF • TİŞÖRT")){g.removeViewAt(i);continue;}
            }
            if(c instanceof ViewGroup)removeTshirtText714((ViewGroup)c);
        }
    }

    private View findTag714(View v,String tag){
        if(v!=null&&tag.equals(v.getTag()))return v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag714(g.getChildAt(i),tag);if(r!=null)return r;}}
        return null;
    }
    private String norm714(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new java.util.Locale("tr","TR"));}
}
