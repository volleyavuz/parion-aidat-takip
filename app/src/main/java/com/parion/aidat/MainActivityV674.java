package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.74 - standardize detail headers and remove obsolete top Home shortcut. */
public class MainActivityV674 extends MainActivityV673 {

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->patchHeader674(title));
    }

    private void patchHeader674(String title){
        if(root==null||root.getChildCount()==0)return;
        View first=root.getChildAt(0);
        if(!(first instanceof ViewGroup))return;
        ViewGroup bar=(ViewGroup)first;

        // V411 added a redundant HOME button to every non-home header. The fixed navigation
        // already provides Home, and this button steals horizontal room from long titles.
        removeHomeShortcut674(bar);

        TextView heading=findHeading674(bar,title);
        if(heading==null)return;
        String text=String.valueOf(heading.getText()).trim();
        int len=text.length();
        heading.setSingleLine(false);
        heading.setMaxLines(2);
        heading.setEllipsize(null);
        heading.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        heading.setTextColor(Color.rgb(212,175,55));
        heading.setTextSize(len>34?13.5f:len>25?14.5f:len>18?15.5f:17f);
        heading.setPadding(dp(8),dp(4),dp(6),dp(4));
        ViewGroup.LayoutParams hp=heading.getLayoutParams();
        if(hp!=null){hp.height=dp(len>24?68:60);heading.setLayoutParams(hp);}

        // Give the whole top bar enough height for two-line titles without clipping.
        ViewGroup.LayoutParams bp=bar.getLayoutParams();
        if(bp!=null&&bp.height>0){bp.height=dp(len>24?72:64);bar.setLayoutParams(bp);}
    }

    private void removeHomeShortcut674(ViewGroup g){
        for(int i=g.getChildCount()-1;i>=0;i--){
            View v=g.getChildAt(i);
            if(v instanceof TextView){
                String s=norm674(String.valueOf(((TextView)v).getText()));
                if(s.contains("ANASAYFA")){
                    g.removeViewAt(i);
                    continue;
                }
            }
            if(v instanceof ViewGroup)removeHomeShortcut674((ViewGroup)v);
        }
    }

    private TextView findHeading674(View v,String wanted){
        String target=norm674(wanted);
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String s=norm674(String.valueOf(t.getText()));
            if(!target.isEmpty()&&s.equals(target))return t;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                TextView t=findHeading674(g.getChildAt(i),wanted);
                if(t!=null)return t;
            }
        }
        return null;
    }

    private String norm674(String s){
        return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));
    }
}
