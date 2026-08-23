package com.parion.aidat;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

/** v4.0.27 - fixed left home navigation rail with athlete and attendance icons. */
public class MainActivityV627 extends MainActivityV626 {
    private static final int RAIL_BG_627=Color.rgb(250,248,241);
    private static final int RAIL_DIV_627=Color.rgb(224,214,184);

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::installHomeRail627);
    }

    private void installHomeRail627(){
        if(root==null||!"HOME".equals(page))return;
        removeHomeAthletesButton627(root);
        ScrollView sv=findHomeScroll627(root);if(sv==null)return;
        ViewParent vp=sv.getParent();if(!(vp instanceof ViewGroup))return;ViewGroup parent=(ViewGroup)vp;
        if(parent instanceof LinearLayout && "v627-home-shell".equals(((View)parent).getTag()))return;
        int idx=parent.indexOfChild(sv);if(idx<0)return;
        ViewGroup.LayoutParams old=sv.getLayoutParams();
        parent.removeView(sv);

        LinearLayout shell=new LinearLayout(this);shell.setTag("v627-home-shell");shell.setOrientation(LinearLayout.HORIZONTAL);shell.setGravity(Gravity.TOP);shell.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout rail=new LinearLayout(this);rail.setOrientation(LinearLayout.VERTICAL);rail.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);rail.setPadding(dp(3),dp(10),dp(3),dp(10));rail.setBackground(railBg627());

        ImageButton athletes=railIcon627(android.R.drawable.ic_menu_myplaces,"Sporcular");
        athletes.setOnClickListener(v->showAthletes());
        ImageButton attendance=railIcon627(android.R.drawable.ic_menu_agenda,"Yoklamalar");
        attendance.setOnClickListener(v->toast("Yoklamalar bölümü bir sonraki sürümde düzenlenecek."));
        rail.addView(athletes,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(48),dp(48));ap.setMargins(0,dp(8),0,0);rail.addView(attendance,ap);

        shell.addView(rail,new LinearLayout.LayoutParams(dp(58),-1));
        shell.addView(sv,new LinearLayout.LayoutParams(0,-1,1));
        ViewGroup.LayoutParams sp;
        if(old instanceof LinearLayout.LayoutParams){LinearLayout.LayoutParams o=(LinearLayout.LayoutParams)old;LinearLayout.LayoutParams n=new LinearLayout.LayoutParams(o.width,o.height,o.weight);n.setMargins(o.leftMargin,o.topMargin,o.rightMargin,o.bottomMargin);n.gravity=o.gravity;sp=n;}
        else sp=new ViewGroup.LayoutParams(-1,-1);
        parent.addView(shell,Math.min(idx,parent.getChildCount()),sp);
    }

    private ImageButton railIcon627(int res,String desc){ImageButton b=new ImageButton(this);b.setImageResource(res);b.setContentDescription(desc);b.setBackgroundColor(Color.TRANSPARENT);b.setPadding(dp(10),dp(10),dp(10),dp(10));b.setColorFilter(Color.rgb(35,35,35));b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);b.setFocusable(true);b.setClickable(true);return b;}
    private GradientDrawable railBg627(){GradientDrawable d=new GradientDrawable();d.setColor(RAIL_BG_627);d.setStroke(dp(1),RAIL_DIV_627);return d;}

    private void removeHomeAthletesButton627(View v){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){View c=g.getChildAt(i);if(c instanceof Button){String s=String.valueOf(((Button)c).getText()).trim();if("SPORCULAR".equalsIgnoreCase(s)){g.removeViewAt(i);continue;}}removeHomeAthletesButton627(c);}
    }
    private ScrollView findHomeScroll627(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findHomeScroll627(g.getChildAt(i));if(s!=null)return s;}}return null;}
}
