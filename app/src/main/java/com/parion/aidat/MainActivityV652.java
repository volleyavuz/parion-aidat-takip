package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.52 - lower dashboard category/order cleanup; preserves existing card views and listeners. */
public class MainActivityV652 extends MainActivityV651 {
    @Override void showHome(){
        super.showHome();
        layout652();
    }

    private void layout652(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll652(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        removeOldHeadings652(box);
        hideTop652(box,"AY SONUNA KADAR GELECEK");
        hideTop652(box,"BU AY TAHSİL EDİLEN");

        View net=topHit652(box,"SON 3 AYDA");
        if(net!=null&&net.getVisibility()!=View.GONE){
            int i=box.indexOfChild(net);
            if(i>=0)box.addView(section652("SPORCU HAREKETLERİ","Yeni başlayan ve ayrılan sporcular"),i);
        }

        ArrayList<View> tracking=new ArrayList<>();
        addUnique652(tracking,topHit652(box,"FOTOĞRAF EKSİK"));
        addUnique652(tracking,topHit652(box,"KAYIT FORMU EKSİK"));
        addUnique652(tracking,topHit652(box,"YAZIN ARANACAK"));
        addUnique652(tracking,topHit652(box,"KIŞIN ARANACAK"));
        for(View v:tracking)if(v!=null&&v.getParent()==box)box.removeView(v);

        View absent=topHit652(box,"DEVAMSIZLAR");
        int insert=absent!=null?box.indexOfChild(absent):box.getChildCount();
        if(!tracking.isEmpty()){
            box.addView(section652("TAKİP GEREKTİRENLER","Eksik kayıtlar ve takip edilmesi gereken sporcular"),Math.max(0,insert++));
            for(View v:tracking){
                if(v==null)continue;
                box.addView(v,Math.min(insert++,box.getChildCount()));
            }
        }
        if(absent!=null)styleAbsent652(absent);
    }

    private TextView section652(String title,String sub){
        TextView t=new TextView(this);t.setTag("v652-heading");t.setText(title+"\n"+sub);t.setTextColor(Color.rgb(32,32,32));t.setTextSize(14);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(dp(4),dp(18),dp(4),dp(9));t.setLineSpacing(dp(1),1f);return t;
    }

    private void removeOldHeadings652(LinearLayout box){
        for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if("v652-heading".equals(v.getTag()))box.removeViewAt(i);}
        for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(v instanceof TextView){String n=norm652(String.valueOf(((TextView)v).getText()));if(n.startsWith("SPORCU HAREKETLERİ")||n.startsWith("TAKİP GEREKTİRENLER"))box.removeViewAt(i);}}
    }

    private void hideTop652(LinearLayout box,String needle){View v=topHit652(box,needle);if(v==null)return;v.setVisibility(View.GONE);ViewGroup.LayoutParams lp=v.getLayoutParams();if(lp!=null){lp.height=0;v.setLayoutParams(lp);}}

    private View topHit652(LinearLayout box,String needle){
        TextView t=findText652(box,needle);if(t==null)return null;View cur=t;while(cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur.getParent()==box?cur:null;
    }
    private TextView findText652(View v,String needle){
        if(v instanceof TextView){String n=norm652(String.valueOf(((TextView)v).getText()));if(n.contains(norm652(needle)))return (TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText652(g.getChildAt(i),needle);if(r!=null)return r;}}return null;
    }
    private void addUnique652(ArrayList<View> a,View v){if(v!=null&&!a.contains(v))a.add(v);}
    private void styleAbsent652(View v){if(v instanceof ViewGroup){v.setPadding(dp(14),dp(14),dp(14),dp(14));ViewGroup.LayoutParams lp=v.getLayoutParams();if(lp!=null&&lp.height>0&&lp.height<dp(118)){lp.height=dp(118);v.setLayoutParams(lp);}}}
    private ScrollView findScroll652(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll652(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm652(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
