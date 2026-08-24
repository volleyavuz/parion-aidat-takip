package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.72 - preserve v4.0.71 navigation fix; remove the actual top-level account row from HOME. */
public class MainActivityV672 extends MainActivityV671 {

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::removeAccountRow672);
    }

    private void removeAccountRow672(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        TextView account=findAccount672(root);
        if(account==null)return;

        View top=account;
        while(top.getParent() instanceof View && top.getParent()!=root){
            top=(View)top.getParent();
        }

        // The legacy cloud/account strip is a top-level child of root. Never remove the
        // dashboard ScrollView/shell or the activity root itself; in those unlikely cases
        // collapse only the account text instead.
        if(top!=root && top.getParent()==root && !(top instanceof ScrollView) && !"v627-home-shell".equals(top.getTag())){
            collapse672(top);
        }else{
            ViewParent p=account.getParent();
            if(p instanceof View && p!=root && !(p instanceof ScrollView))collapse672((View)p);
            else collapse672(account);
        }
    }

    private TextView findAccount672(View v){
        if(v instanceof TextView){
            String s=norm672(String.valueOf(((TextView)v).getText()));
            if(s.contains("VOLLEYAVUZ"))return (TextView)v;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                TextView r=findAccount672(g.getChildAt(i));
                if(r!=null)return r;
            }
        }
        return null;
    }

    private void collapse672(View v){
        if(v==null)return;
        v.setVisibility(View.GONE);
        v.setPadding(0,0,0,0);
        ViewGroup.LayoutParams lp=v.getLayoutParams();
        if(lp!=null){lp.height=0;v.setLayoutParams(lp);}
    }

    private String norm672(String s){
        return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));
    }
}
