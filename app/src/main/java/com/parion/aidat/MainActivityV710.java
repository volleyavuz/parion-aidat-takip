package com.parion.aidat;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;

/** v4.1.32 + v4.2.8 route fix: legacy delayed patches must open the current settings menu. */
public class MainActivityV710 extends MainActivityV709 {
    private static final int NAV_FG_710=Color.rgb(42,42,42);

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(this::patchBottomNav710);
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(()->{patchBottomNav710();root.post(this::patchBottomNav710);});
        }
    }

    private void patchBottomNav710(){
        if(root==null||"LOGIN".equals(page))return;
        LinearLayout nav=findTaggedLinear710(root,"v709-bottom-nav");
        if(nav==null)return;

        View home=findByDescription710(nav,"Anasayfa");
        View settings=findByDescription710(nav,"Ayarlar");

        if(home!=null && !(home instanceof ImageButton)){
            int idx=nav.indexOfChild(home);
            ViewGroup.LayoutParams oldLp=home.getLayoutParams();
            nav.removeView(home);
            ImageButton h=imageIcon710(R.drawable.ic_nav_home,"Anasayfa");
            h.setOnClickListener(v->showHome());
            nav.addView(h,Math.max(0,idx),oldLp);
        }else if(home instanceof ImageButton){
            ((ImageButton)home).setColorFilter(NAV_FG_710);
            home.setOnClickListener(v->showHome());
        }

        if(settings!=null){
            if(settings instanceof ImageButton)((ImageButton)settings).setColorFilter(NAV_FG_710);
            settings.setOnClickListener(this::openFullSettings710);
        }

        normalizeIcon710(findByDescription710(nav,"Yoklamalar"));
        normalizeIcon710(findByDescription710(nav,"Sporcular"));
    }

    private ImageButton imageIcon710(int res,String desc){
        ImageButton b=new ImageButton(this);
        b.setImageResource(res);b.setContentDescription(desc);
        b.setBackgroundColor(Color.TRANSPARENT);b.setColorFilter(NAV_FG_710);
        b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        b.setPadding(dp(12),dp(12),dp(12),dp(12));
        b.setClickable(true);b.setFocusable(true);
        return b;
    }

    private void normalizeIcon710(View v){
        if(v instanceof ImageButton){
            ImageButton b=(ImageButton)v;b.setColorFilter(NAV_FG_710);
            b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);b.setPadding(dp(12),dp(12),dp(12),dp(12));
        }
    }

    private void openFullSettings710(View anchor){
        // Resolve the declaring superclass directly. getClass().getDeclaredMethod()
        // fails for V730/V731/V732 because showSettings729 is private in V729.
        try{
            Method current=MainActivityV729.class.getDeclaredMethod("showSettings729",View.class);
            current.setAccessible(true);current.invoke(this,anchor);return;
        }catch(Exception ignored){}
        try{
            Method m=MainActivityV661.class.getDeclaredMethod("showSettings661",View.class);
            m.setAccessible(true);m.invoke(this,anchor);
        }catch(Exception e){toast("Ayarlar açılamadı.");}
    }

    private LinearLayout findTaggedLinear710(View v,String tag){View x=findTaggedView710(v,tag);return x instanceof LinearLayout?(LinearLayout)x:null;}
    private View findTaggedView710(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findTaggedView710(g.getChildAt(i),tag);if(x!=null)return x;}}return null;}
    private View findByDescription710(View v,String desc){CharSequence c=v.getContentDescription();if(c!=null&&desc.equalsIgnoreCase(c.toString()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findByDescription710(g.getChildAt(i),desc);if(x!=null)return x;}}return null;}
}
