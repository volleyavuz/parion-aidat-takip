package com.parion.aidat;

import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/** v4.0.37 - attendance photos use the app's existing Supabase/cloud photo loader. */
public class MainActivityV637 extends MainActivityV636 {
    @Override void base(String title, boolean back){
        super.base(title, back);
        if(root!=null){
            root.postDelayed(()->patchAttendancePhotos637(root),120);
            root.postDelayed(()->patchAttendancePhotos637(root),650);
        }
    }

    private void patchAttendancePhotos637(View v){
        if(page==null || !page.startsWith("ATTENDANCE_MONTH_635:")) return;
        String group=page.substring("ATTENDANCE_MONTH_635:".length());
        if(v instanceof ImageView){
            ImageView iv=(ImageView)v;
            CharSequence cd=iv.getContentDescription();
            if(cd!=null){
                String s=cd.toString();
                String suffix=" fotoğrafı";
                if(s.endsWith(suffix)){
                    String name=s.substring(0,s.length()-suffix.length());
                    String photo=photoFor637(group,name);
                    if(photo!=null && !photo.trim().isEmpty() && !"NONE".equalsIgnoreCase(photo.trim())){
                        String ref=photo.trim();
                        if(!ref.startsWith("CLOUD:") && (ref.startsWith("athletes/") || ref.startsWith("user/"))) ref="CLOUD:"+ref;
                        setAthletePhoto(iv,ref);
                    }
                }
            }
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++) patchAttendancePhotos637(g.getChildAt(i));
        }
    }

    private String photoFor637(String group,String name){
        Cursor c=null;
        try{
            c=db.getReadableDatabase().rawQuery(
                "SELECT photo FROM athletes WHERE category=? COLLATE NOCASE AND name=? COLLATE NOCASE AND TRIM(COALESCE(deletedAt,''))='' LIMIT 1",
                new String[]{group,name});
            return c.moveToFirst()?c.getString(0):null;
        }finally{if(c!=null)c.close();}
    }
}
