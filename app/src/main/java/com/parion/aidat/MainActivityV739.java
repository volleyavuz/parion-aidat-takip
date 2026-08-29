package com.parion.aidat;

import android.database.Cursor;
import android.view.*;
import android.widget.ImageView;

/** v4.2.16 - exact 92dp profile portrait + same local/alias/cloud resolver used by athlete lists. */
public class MainActivityV739 extends MainActivityV738 {
    @Override void showProfile(long id){
        super.showProfile(id);
        if(root!=null){
            root.postDelayed(()->patchExactProfilePhoto739(id),40L);
            root.postDelayed(()->patchExactProfilePhoto739(id),220L);
            root.postDelayed(()->patchExactProfilePhoto739(id),900L);
            root.postDelayed(()->patchExactProfilePhoto739(id),1800L);
        }
    }

    private void patchExactProfilePhoto739(long id){
        if(currentAthlete!=id||root==null||!"PROFILE".equals(page))return;
        String ref="";
        try{
            Cursor c=db.athlete(id);
            if(c.moveToFirst()){
                int i=c.getColumnIndex("photo");
                ref=i>=0&&!c.isNull(i)?c.getString(i):"";
            }
            c.close();
        }catch(Exception ignored){}

        // Important: setAthletePhoto understands USER:, CLOUD:, and the legacy photoAlias map.
        // Athlete lists already use this resolver successfully, so profile portraits use the exact same path now.
        if(ref==null)ref="";
        if(ref.trim().isEmpty()||"NONE".equalsIgnoreCase(ref.trim())){
            String cloud=photoMap413().get(id);
            if(cloud!=null&&!cloud.trim().isEmpty())ref="CLOUD:"+cloud;
        }
        if(ref.trim().isEmpty()||"NONE".equalsIgnoreCase(ref.trim()))return;

        ImageView portrait=findExactPortrait739(root);
        if(portrait!=null)setAthletePhoto(portrait,ref);
    }

    private ImageView findExactPortrait739(View v){
        final int target=dp(92),tol=dp(8);
        if(v instanceof ImageView){
            ImageView iv=(ImageView)v;
            int w=iv.getWidth(),h=iv.getHeight();
            if(Math.abs(w-target)<=tol&&Math.abs(h-target)<=tol)return iv;
            ViewGroup.LayoutParams lp=iv.getLayoutParams();
            if(lp!=null&&Math.abs(lp.width-target)<=tol&&Math.abs(lp.height-target)<=tol)return iv;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                ImageView x=findExactPortrait739(g.getChildAt(i));
                if(x!=null)return x;
            }
        }
        return null;
    }
}
