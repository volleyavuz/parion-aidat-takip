package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.81 - dashboard consolidation: one definitive HOME header + unified card spacing/typography. */
public class MainActivityV681 extends MainActivityV680 {
    private static final int GOLD681=Color.rgb(205,156,34);
    private static final int BLACK681=Color.rgb(18,18,18);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::consolidateDashboard681);
            root.postDelayed(this::consolidateDashboard681,120);
        }
    }

    private void consolidateDashboard681(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll681(root);
        if(sv==null)return;
        View contentTop=topChild681(root,sv);
        if(contentTop==null)return;

        installDefinitiveHeader681(contentTop);
        normalizeDashboard681(sv);
    }

    /** Hide every legacy HOME-only strip above the dashboard content and install one stable header. */
    private void installDefinitiveHeader681(View contentTop){
        int contentIndex=root.indexOfChild(contentTop);
        if(contentIndex<0)return;

        // Remove a prior v681 header if showHome/async patches call us again.
        for(int i=root.getChildCount()-1;i>=0;i--){
            View v=root.getChildAt(i);
            if("v681-home-header".equals(v.getTag()))root.removeViewAt(i);
        }
        contentIndex=root.indexOfChild(contentTop);

        // Everything before the scroll content is legacy HOME chrome (title/status/account strip).
        // Collapse it instead of deleting it, so older code that still holds references cannot crash.
        for(int i=0;i<contentIndex;i++){
            View v=root.getChildAt(i);
            v.setVisibility(View.GONE);
            ViewGroup.LayoutParams lp=v.getLayoutParams();
            if(lp!=null){lp.height=0;v.setLayoutParams(lp);}
        }

        LinearLayout bar=new LinearLayout(this);
        bar.setTag("v681-home-header");
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(10),dp(6),dp(12),dp(6));
        bar.setBackgroundColor(BLACK681);

        ImageView logo=new ImageView(this);
        logo.setImageResource(R.drawable.parion_official_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setPadding(dp(3),dp(3),dp(3),dp(3));
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(42),dp(42));
        ilp.setMargins(0,0,dp(9),0);
        bar.addView(logo,ilp);

        LinearLayout titles=new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=new TextView(this);
        title.setText("PARİON SPOR KULÜBÜ");
        title.setTextSize(16.5f);
        title.setTextColor(GOLD681);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        title.setSingleLine(true);
        TextView sub=new TextView(this);
        sub.setText("SPORCU TAKİP SİSTEMİ");
        sub.setTextSize(9.5f);
        sub.setTextColor(Color.rgb(210,210,210));
        sub.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        sub.setSingleLine(true);
        titles.addView(title,new LinearLayout.LayoutParams(-1,-2));
        titles.addView(sub,new LinearLayout.LayoutParams(-1,-2));
        bar.addView(titles,new LinearLayout.LayoutParams(0,dp(48),1f));

        root.addView(bar,0,new LinearLayout.LayoutParams(-1,dp(60)));
    }

    private void normalizeDashboard681(ScrollView sv){
        if(sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        box.setPadding(dp(10),dp(8),dp(10),dp(24));

        View fresh=findTag681(box,"v657-fresh");
        if(fresh instanceof LinearLayout)normalizeFresh681((LinearLayout)fresh);

        normalizeKnownRows681(box,"v662-start-row");
        normalizeKnownRows681(box,"v663-call-row");
        normalizeTextCards681(box);
    }

    private void normalizeFresh681(LinearLayout fresh){
        fresh.setPadding(0,0,0,dp(8));
        for(int i=0;i<fresh.getChildCount();i++){
            View child=fresh.getChildAt(i);
            if(child instanceof TextView){
                styleSection681((TextView)child);
            }else if(child instanceof LinearLayout){
                LinearLayout row=(LinearLayout)child;
                if(row.getOrientation()==LinearLayout.HORIZONTAL){
                    row.setPadding(0,0,0,dp(8));
                    for(int j=0;j<row.getChildCount();j++)styleCard681(row.getChildAt(j));
                }else{
                    styleCard681(row);
                }
            }else styleCard681(child);
        }
    }

    private void normalizeKnownRows681(View rootView,String tag){
        View row=findTag681(rootView,tag);
        if(!(row instanceof LinearLayout))return;
        LinearLayout r=(LinearLayout)row;
        r.setPadding(0,dp(2),0,dp(5));
        for(int i=0;i<r.getChildCount();i++)styleCard681(r.getChildAt(i));
    }

    private void normalizeTextCards681(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String n=norm681(String.valueOf(t.getText()));
            if(isSection681(n))styleSection681(t);
            if(isFollowupTitle681(n)){
                t.setTextSize(10.5f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                t.setTextColor(Color.rgb(48,48,48));t.setGravity(Gravity.CENTER);
                t.setMaxLines(2);t.setEllipsize(null);t.setPadding(dp(3),dp(3),dp(3),dp(3));
                View card=nearestClickable681(t);
                if(card!=null)styleCard681(card);
            }
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)normalizeTextCards681(g.getChildAt(i));
        }
    }

    private boolean isSection681(String n){
        return n.startsWith("GENEL DURUM")||n.startsWith("FİNANS")||n.startsWith("SPORCU HAREKETLERİ")||n.startsWith("TAKİP GEREKTİRENLER");
    }
    private boolean isFollowupTitle681(String n){
        return n.contains("FOTOĞRAF EKSİK")||n.contains("KAYIT FORMU EKSİK")||n.contains("YAZIN ARANACAK")||n.contains("KIŞIN ARANACAK")||n.contains("KISIN ARANACAK")||n.contains("DEVAMSIZLAR")||n.contains("BU AY BAŞLAYANLAR")||n.contains("GEÇEN AY BAŞLAYANLAR");
    }

    private void styleSection681(TextView t){
        t.setTextSize(13.2f);
        t.setTextColor(Color.rgb(35,35,35));
        t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setPadding(dp(4),dp(14),dp(4),dp(7));
        t.setLineSpacing(dp(1),1f);
    }

    private void styleCard681(View card){
        if(card==null)return;
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(15));
        card.setBackground(bg);card.setElevation(dp(1));
        card.setPadding(dp(10),dp(9),dp(10),dp(9));
        // Preserve existing listener/action; only make the actual visual card receive taps when already wired.
        if(card.hasOnClickListeners())card.setClickable(true);
        ViewGroup.LayoutParams raw=card.getLayoutParams();
        if(raw instanceof LinearLayout.LayoutParams){
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)raw;
            if(lp.height>0&&lp.height<dp(115))lp.height=dp(115);
            lp.setMargins(dp(4),lp.topMargin,dp(4),Math.max(lp.bottomMargin,dp(7)));
            card.setLayoutParams(lp);
        }
    }

    private View nearestClickable681(View v){
        View cur=v,best=null;
        while(cur!=null&&cur!=root){
            if(cur.hasOnClickListeners()||cur.isClickable())best=cur;
            ViewParent p=cur.getParent();
            if(!(p instanceof View))break;
            cur=(View)p;
        }
        return best;
    }

    private ScrollView findScroll681(View v){
        if(v instanceof ScrollView)return(ScrollView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll681(g.getChildAt(i));if(s!=null)return s;}}
        return null;
    }
    private View topChild681(ViewGroup parent,View v){
        View cur=v;
        while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=parent)cur=(View)cur.getParent();
        return cur!=null&&cur.getParent()==parent?cur:null;
    }
    private View findTag681(View v,String tag){
        if(v!=null&&tag.equals(v.getTag()))return v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag681(g.getChildAt(i),tag);if(r!=null)return r;}}
        return null;
    }
    private String norm681(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
