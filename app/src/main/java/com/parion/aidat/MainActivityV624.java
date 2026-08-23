package com.parion.aidat;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.24 - profile action placement and lighter paid-month green. */
public class MainActivityV624 extends MainActivityV623 {
    private static final int LIGHT_PAID_GREEN_624=Color.rgb(198,239,206); // #C6EFCE

    @Override void showProfile(long id){
        super.showProfile(id);
        root.post(this::reorderProfileActions624);
    }

    /** Keep the V37 edit/delete controls, but move their row above the fee/effective-month editor. */
    private void reorderProfileActions624(){
        LinearLayout box=findScrollBox624(root);if(box==null)return;
        View actionRow=findActionRow624(box);if(actionRow==null)return;
        View feeAnchor=findTopLevelByText624(box,"AİDAT ÜCRETİ","GEÇERLİ AY");
        if(feeAnchor==null||feeAnchor==actionRow)return;
        int target=box.indexOfChild(feeAnchor);if(target<0)return;
        ViewGroup.LayoutParams old=actionRow.getLayoutParams();
        box.removeView(actionRow);
        LinearLayout.LayoutParams lp;
        if(old instanceof LinearLayout.LayoutParams){LinearLayout.LayoutParams s=(LinearLayout.LayoutParams)old;lp=new LinearLayout.LayoutParams(s.width,s.height,s.weight);lp.setMargins(s.leftMargin,dp(8),s.rightMargin,dp(6));lp.gravity=s.gravity;}
        else {lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(6));}
        box.addView(actionRow,Math.min(target,box.getChildCount()),lp);
    }

    /** Paid months use the light green previously associated with completed payment status. */
    @Override int paymentColor(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        int base=super.paymentColor(m,fee,sibling,start,end,restart,marker,amount);
        boolean normalPaid=isDate(marker) && !"!".equals(marker) && !"!!".equals(marker) && !(amount>0&&fee>0&&amount!=fee);
        return normalPaid?LIGHT_PAID_GREEN_624:base;
    }

    private View findActionRow624(View v){
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;boolean edit=false,del=false;for(int i=0;i<g.getChildCount();i++){View c=g.getChildAt(i);if(c instanceof Button){String s=norm624(String.valueOf(((Button)c).getText()));if(s.contains("BİLGİLERİ DÜZENLE"))edit=true;if(s.contains("SPORCUYU SİL"))del=true;}}if(edit&&del)return v;for(int i=0;i<g.getChildCount();i++){View r=findActionRow624(g.getChildAt(i));if(r!=null)return r;}}
        return null;
    }

    private View findTopLevelByText624(LinearLayout box,String... needles){
        for(int i=0;i<box.getChildCount();i++){View child=box.getChildAt(i);if(containsAnyText624(child,needles))return child;}return null;
    }
    private boolean containsAnyText624(View v,String... needles){
        if(v instanceof TextView){String u=norm624(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm624(n)))return true;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsAnyText624(g.getChildAt(i),needles))return true;}
        return false;
    }
    private LinearLayout findScrollBox624(View v){
        if(v instanceof ScrollView){ScrollView s=(ScrollView)v;if(s.getChildCount()>0&&s.getChildAt(0) instanceof LinearLayout)return (LinearLayout)s.getChildAt(0);}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){LinearLayout r=findScrollBox624(g.getChildAt(i));if(r!=null)return r;}}
        return null;
    }
    private String norm624(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
