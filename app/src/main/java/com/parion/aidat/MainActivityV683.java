package com.parion.aidat;

import android.view.*;
import android.widget.*;

/** v4.0.83 - remove the obsolete Winter call card as a whole container (frame + phone icon + label). */
public class MainActivityV683 extends MainActivityV682 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::cleanupLegacyWinter683);
            root.postDelayed(this::cleanupLegacyWinter683,220);
        }
    }

    private void cleanupLegacyWinter683(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        View row=findTag683(root,"v663-call-row");
        if(!(row instanceof ViewGroup))return;
        ViewGroup calls=(ViewGroup)row;
        // The obsolete Winter card is the second child of the old Summer/Winter call row.
        // Remove the entire card view, not just its TextView, so border and phone icon disappear too.
        if(calls.getChildCount()>1)calls.removeViewAt(1);
        // If the row is now empty, remove its wrapper as well.
        if(calls.getChildCount()==0){
            ViewParent p=calls.getParent();
            if(p instanceof ViewGroup)((ViewGroup)p).removeView(calls);
        }
    }

    private View findTag683(View v,String tag){
        if(v!=null&&tag.equals(v.getTag()))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View r=findTag683(g.getChildAt(i),tag);
                if(r!=null)return r;
            }
        }
        return null;
    }
}
