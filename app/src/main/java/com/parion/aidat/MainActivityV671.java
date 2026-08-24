package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/**
 * v4.0.71 - first stabilization pass from the frozen v4.0.70 recovery baseline.
 *
 * - Prevent the athlete edit FORM from being captured as PROFILE back-source after Save.
 * - Give FORM a deterministic back target.
 * - Remove the obsolete dashboard account/sync strip; sync remains under Settings > Updates.
 * - No database, payment, attendance or cloud-sync data logic is changed here.
 */
public class MainActivityV671 extends MainActivityV670 {

    @Override void showProfile(long id){
        // V616 remembers the screen that opened PROFILE. After saving an edit, showProfile()
        // is called while page == FORM, which made FORM the remembered back target and
        // produced PROFILE <-> FORM bouncing. Pretend we are already in PROFILE only for
        // this transition so V616 does not capture the editor as navigation history.
        if("FORM".equals(page)){
            page="PROFILE";
            currentAthlete=id;
        }
        super.showProfile(id);
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::patchHome671);
    }

    private void patchHome671(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        removeLegacyCloudStrip671(root);
    }

    /** Remove only a compact ancestor that contains BOTH account identity and cloud status text. */
    private void removeLegacyCloudStrip671(View tree){
        TextView account=findText671(tree,"VOLLEYAVUZ@","VOLLEYAVUZ");
        if(account==null)return;
        View candidate=account;
        View cur=account;
        while(cur!=null&&cur!=root){
            ViewParent p=cur.getParent();
            if(!(p instanceof View))break;
            View parent=(View)p;
            if(parent instanceof ViewGroup && containsCloudStatus671(parent)){
                candidate=parent;
                // Prefer the nearest self-contained row/card; do not climb into root.
                if(parent.getParent()==root)break;
            }
            cur=parent;
        }
        if(candidate==account){
            collapse671(account);
            return;
        }
        // Safety: never collapse the whole activity root or the home scroll/shell.
        if(candidate==root || candidate instanceof ScrollView || "v627-home-shell".equals(candidate.getTag()))return;
        collapse671(candidate);
    }

    private boolean containsCloudStatus671(View v){
        if(v instanceof TextView){
            String s=norm671(String.valueOf(((TextView)v).getText()));
            return s.contains("ONLINE")||s.contains("BULUT")||s.contains("SENKRON")||s.contains("DEĞİŞİKLİK BEKLİYOR")||s.contains("DEGISIKLIK BEKLIYOR");
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)if(containsCloudStatus671(g.getChildAt(i)))return true;
        }
        return false;
    }

    private TextView findText671(View v,String... needles){
        if(v instanceof TextView){
            String s=norm671(String.valueOf(((TextView)v).getText()));
            for(String n:needles)if(s.contains(norm671(n)))return (TextView)v;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                TextView r=findText671(g.getChildAt(i),needles);
                if(r!=null)return r;
            }
        }
        return null;
    }

    private void collapse671(View v){
        if(v==null)return;
        v.setVisibility(View.GONE);
        ViewGroup.LayoutParams lp=v.getLayoutParams();
        if(lp!=null){lp.height=0;v.setLayoutParams(lp);}
        v.setPadding(0,0,0,0);
    }

    @Override void goBack(){
        if("FORM".equals(page)){
            long id=currentAthlete;
            if(id>0){
                // Suppress V616 source capture for this direct editor -> profile return.
                page="PROFILE";
                showProfile(id);
            }else showAthletes();
            return;
        }
        super.goBack();
    }

    private String norm671(String s){
        return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));
    }
}
