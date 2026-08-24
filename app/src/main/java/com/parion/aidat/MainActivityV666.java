package com.parion.aidat;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** v4.0.66 - micro dashboard text cleanup on verified v4.0.65 path. */
public class MainActivityV666 extends MainActivityV665 {

    @Override void showHome(){
        super.showHome();
        patchUpperDuplicates666();
    }

    private void patchUpperDuplicates666(){
        if(root==null)return;
        View fresh=findFresh666(root);
        if(!(fresh instanceof LinearLayout))return;
        LinearLayout f=(LinearLayout)fresh;
        if(f.getChildCount()<5)return;

        // GENEL DURUM row: [0 section, 1 row]. Active card is row child 0.
        View general=f.getChildAt(1);
        if(general instanceof LinearLayout){
            LinearLayout row=(LinearLayout)general;
            if(row.getChildCount()>0)hideMetricSub666(row.getChildAt(0));
        }

        // FİNANS row: [2 section, 3 monthly target, 4 row]. Overdue card is row child 0.
        View finance=f.getChildAt(4);
        if(finance instanceof LinearLayout){
            LinearLayout row=(LinearLayout)finance;
            if(row.getChildCount()>0)watchOverdueSub666(row.getChildAt(0));
        }
    }

    private View findFresh666(View v){
        if(v==null)return null;
        if("v657-fresh".equals(v.getTag()))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View r=findFresh666(g.getChildAt(i));
                if(r!=null)return r;
            }
        }
        return null;
    }

    private void hideMetricSub666(View card){
        TextView sub=taggedText666(card,"sub");
        if(sub!=null)collapse666(sub);
    }

    private void watchOverdueSub666(View card){
        TextView sub=taggedText666(card,"sub");
        if(sub==null)return;
        updateOverdueVisibility666(sub);
        sub.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){updateOverdueVisibility666(sub);}
            @Override public void afterTextChanged(Editable s){}
        });
    }

    private void updateOverdueVisibility666(TextView t){
        String s=String.valueOf(t.getText()).trim().toUpperCase(new java.util.Locale("tr","TR"));
        if(s.contains("SPORCU"))collapse666(t);
        else {
            t.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp=t.getLayoutParams();
            if(lp!=null){lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;t.setLayoutParams(lp);}
        }
    }

    private TextView taggedText666(View v,String tag){
        if(v instanceof TextView&&tag.equals(v.getTag()))return (TextView)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                TextView t=taggedText666(g.getChildAt(i),tag);
                if(t!=null)return t;
            }
        }
        return null;
    }

    private void collapse666(TextView t){
        t.setVisibility(View.GONE);
        t.setPadding(0,0,0,0);
        ViewGroup.LayoutParams lp=t.getLayoutParams();
        if(lp!=null){lp.height=0;t.setLayoutParams(lp);}
    }
}
