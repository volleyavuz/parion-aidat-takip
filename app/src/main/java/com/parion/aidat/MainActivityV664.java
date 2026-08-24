package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.64 - visual-only dashboard tightening. No DB/sync/calculation changes. */
public class MainActivityV664 extends MainActivityV663 {

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::polish664);
    }

    private void polish664(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll664(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        removeActiveDuplicate664(box);
        removeOverdueDuplicate664(box);

        compactCardByText664(box,"SON 3 AYDA");
        compactCardByText664(box,"BU AY BAŞLAYANLAR","BU AY BAŞLAYAN SPORCULAR");
        compactCardByText664(box,"GEÇEN AY BAŞLAYANLAR","GEÇEN AY BAŞLAYAN SPORCULAR");
        compactCardByText664(box,"SON 3 AY İÇİNDE BIRAKANLAR");

        compactFollowup664(box,"FOTOĞRAF EKSİK","FOTOĞRAFI OLMAYAN AKTİF SPORCULAR");
        compactFollowup664(box,"KAYIT FORMU EKSİK","KAYIT FORMU OLMAYAN AKTİF SPORCULAR");
        compactFollowup664(box,"YAZIN ARANACAK");
        compactFollowup664(box,"KIŞIN ARANACAK");
        compactFollowup664(box,"TİŞÖRT ALMAYANLAR","TİŞÖRT ALMAYAN SPORCULAR","TİŞÖRT ALMAYAN");
        compactFollowup664(box,"DEVAMSIZLAR");

        tightenSections664(box);
    }

    private void removeActiveDuplicate664(View rootView){
        TextView label=findText664(rootView,"AKTİF SPORCU");if(label==null)return;
        View card=nearestVertical664(label,rootView);if(!(card instanceof ViewGroup))return;
        ArrayList<TextView> all=new ArrayList<>();collectText664(card,all);
        boolean kept=false;
        for(TextView t:all){
            String n=norm664(String.valueOf(t.getText()));
            if("AKTİF SPORCU".equals(n)){
                if(!kept){kept=true;continue;}
                collapse664(t);
            }
        }
    }

    private void removeOverdueDuplicate664(View rootView){
        TextView label=findText664(rootView,"GECİKMİŞ");if(label==null)return;
        View card=nearestVertical664(label,rootView);if(!(card instanceof ViewGroup))return;
        String ln=norm664(String.valueOf(label.getText()));
        java.util.regex.Matcher m=java.util.regex.Pattern.compile("GECİKMİŞ\\s+(\\d+)\\s+SPORCU").matcher(ln);
        if(!m.find())return;
        String duplicate=m.group(1)+" SPORCU";
        ArrayList<TextView> all=new ArrayList<>();collectText664(card,all);
        for(TextView t:all){
            if(t!=label&&duplicate.equals(norm664(String.valueOf(t.getText()))))collapse664(t);
        }
    }

    private void compactCardByText664(LinearLayout box,String... needles){
        TextView t=findText664(box,needles);if(t==null)return;
        View card=nearestVertical664(t,box);if(card==null)return;
        card.setMinimumHeight(0);
        card.setPadding(dp(10),dp(7),dp(10),dp(8));
        ViewGroup.LayoutParams p=card.getLayoutParams();
        if(p!=null){p.height=ViewGroup.LayoutParams.WRAP_CONTENT;card.setLayoutParams(p);}
        compactTextTree664(card,false);
    }

    private void compactFollowup664(LinearLayout box,String... needles){
        TextView t=findText664(box,needles);if(t==null)return;
        View card=nearestVertical664(t,box);if(card==null)return;
        card.setMinimumHeight(0);
        card.setPadding(dp(9),dp(7),dp(9),dp(8));
        ViewGroup.LayoutParams p=card.getLayoutParams();
        if(p!=null){p.height=ViewGroup.LayoutParams.WRAP_CONTENT;card.setLayoutParams(p);}
        compactTextTree664(card,true);
    }

    private void compactTextTree664(View v,boolean followup){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();String n=norm664(s);
            t.setIncludeFontPadding(false);
            if(n.startsWith("TÜMÜNÜ GÖR")||n.startsWith("DARALT")){
                t.setTextSize(9.6f);t.setPadding(dp(2),dp(5),dp(2),dp(2));
            }else if(s.startsWith("• ")){
                t.setTextSize(followup?10.2f:10.5f);t.setPadding(0,dp(2),0,dp(2));
            }else if(n.matches("[0-9]+")){
                t.setTextSize(23f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(0,dp(1),0,dp(1));
            }else if(n.contains("NET SPORCU")){
                t.setTextSize(22f);t.setPadding(0,dp(2),0,dp(2));
            }else if(n.contains("BAŞLAYANLAR")||n.contains("BIRAKANLAR")||n.contains("EKSİK")||n.contains("ARANACAK")||n.contains("DEVAMSIZ")||n.contains("TİŞÖRT")){
                t.setTextSize(followup?10.5f:10.7f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setMaxLines(2);t.setPadding(dp(2),dp(2),dp(2),dp(3));
            }
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)compactTextTree664(g.getChildAt(i),followup);
        }
    }

    private void tightenSections664(LinearLayout box){
        for(int i=0;i<box.getChildCount();i++){
            View v=box.getChildAt(i);
            if(v instanceof TextView){
                TextView t=(TextView)v;String n=norm664(String.valueOf(t.getText()));
                if(n.startsWith("SPORCU HAREKETLERİ")||n.startsWith("TAKİP GEREKTİRENLER")){
                    t.setPadding(dp(4),dp(13),dp(4),dp(6));
                    t.setTextSize(13.5f);
                }
            }
            ViewGroup.LayoutParams gp=v.getLayoutParams();
            if(gp instanceof LinearLayout.LayoutParams){
                LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)gp;
                if(lp.topMargin>dp(6))lp.topMargin=dp(5);
                if(lp.bottomMargin>dp(6))lp.bottomMargin=dp(5);
                v.setLayoutParams(lp);
            }
        }
    }

    private void collapse664(TextView t){
        t.setVisibility(View.GONE);t.setText("");
        ViewGroup.LayoutParams p=t.getLayoutParams();if(p!=null){p.height=0;t.setLayoutParams(p);}t.setPadding(0,0,0,0);
    }

    private View nearestVertical664(View v,View stop){
        View cur=v,best=null;
        while(cur!=null&&cur!=stop&&cur.getParent() instanceof View){
            View p=(View)cur.getParent();
            if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.VERTICAL)best=p;
            if(p==stop)break;
            if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.HORIZONTAL&&best!=null)break;
            cur=p;
        }
        return best;
    }

    private void collectText664(View v,List<TextView> out){if(v instanceof TextView)out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText664(g.getChildAt(i),out);}}
    private TextView findText664(View v,String... needles){if(v instanceof TextView){String u=norm664(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm664(n)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText664(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private ScrollView findScroll664(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll664(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm664(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
