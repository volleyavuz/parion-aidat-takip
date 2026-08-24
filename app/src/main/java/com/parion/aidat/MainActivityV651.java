package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.51 - visual-only dashboard polish on top of stable v4.0.50 data/click logic. */
public class MainActivityV651 extends MainActivityV650 {
    @Override void showHome(){
        super.showHome();
        polish651();
    }

    private void polish651(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        styleFresh651(root);
        styleLegacyFollowup651(root,"FOTOĞRAF EKSİK");
        styleLegacyFollowup651(root,"KAYIT FORMU EKSİK");
        styleLegacyFollowup651(root,"YAZIN ARANACAK");
        styleLegacyFollowup651(root,"KIŞIN ARANACAK");
    }

    private void styleFresh651(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();String n=norm651(s);
            Object tag=t.getTag();
            if("value".equals(tag)){
                int len=s.length();
                t.setTextSize(len>=9?21.5f:len>=7?23.5f:27f);
                t.setSingleLine(true);t.setMaxLines(1);t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(false);
                t.setPadding(dp(2),0,dp(2),0);
            }else if("sub".equals(tag)){
                t.setTextSize(9.3f);t.setMaxLines(2);t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(false);
            }else if(n.equals("GECİKMİŞ")||n.equals("MALZEME BORCU")||n.equals("AKTİF SPORCU")||n.equals("ARA VERDİ")||n.equals("AYLIK HEDEF")){
                t.setTextSize(11.2f);t.setMaxLines(2);t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(false);
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)styleFresh651(g.getChildAt(i));}
    }

    private void styleLegacyFollowup651(View rootView,String needle){
        TextView label=findText651(rootView,needle);if(label==null)return;
        View card=nearestCard651(label);if(card==null)return;
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));
        card.setBackground(bg);card.setElevation(dp(1));card.setPadding(dp(10),dp(10),dp(10),dp(10));
        ViewGroup.LayoutParams vp=card.getLayoutParams();if(vp!=null&&vp.height>0&&vp.height<dp(118)){vp.height=dp(118);card.setLayoutParams(vp);}
        normalizeFollowupTexts651(card,needle);
    }

    private void normalizeFollowupTexts651(View v,String needle){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();String n=norm651(s);
            if(n.contains(norm651(needle))){
                t.setTextSize(10.8f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setTextColor(Color.rgb(48,48,48));
                t.setGravity(Gravity.CENTER);t.setMaxLines(2);t.setIncludeFontPadding(false);t.setPadding(dp(2),dp(2),dp(2),dp(2));
            }else if(isPlainNumber651(s)){
                t.setTextSize(24f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setTextColor(Color.rgb(35,35,35));
                t.setGravity(Gravity.CENTER);t.setMaxLines(1);t.setIncludeFontPadding(false);t.setBackground(null);t.setPadding(dp(2),dp(2),dp(2),dp(2));
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)normalizeFollowupTexts651(g.getChildAt(i),needle);}
    }

    private View nearestCard651(View v){
        View cur=v,best=null;
        while(cur!=null&&cur!=root){
            if(cur.isClickable()||cur.hasOnClickListeners())best=cur;
            ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;
        }
        if(best!=null)return best;
        cur=v;
        while(cur!=null&&cur!=root){ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;if(cur instanceof LinearLayout)return cur;}
        return null;
    }

    private TextView findText651(View v,String needle){
        if(v instanceof TextView){String n=norm651(String.valueOf(((TextView)v).getText()));if(n.contains(norm651(needle)))return (TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText651(g.getChildAt(i),needle);if(r!=null)return r;}}return null;
    }
    private boolean isPlainNumber651(String s){return s!=null&&s.trim().matches("[0-9]+\\s*");}
    private String norm651(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
