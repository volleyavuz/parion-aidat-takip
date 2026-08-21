package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.widget.*;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivityV386 extends MainActivityV384 {
    private final ExecutorService photoPool = Executors.newFixedThreadPool(2);
    private final android.util.LruCache<String, Bitmap> thumbCache = new android.util.LruCache<String, Bitmap>(48) {
        @Override protected int sizeOf(String key, Bitmap value) { return 1; }
    };

    @Override void row(LinearLayout b, A x, String detail, int amount) {
        LinearLayout r=new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(8),dp(7),dp(8),dp(7));
        r.setBackground(round(Color.WHITE,10));

        ImageView av=new ImageView(this);
        av.setScaleType(ImageView.ScaleType.CENTER_CROP);
        av.setImageDrawable(new ColorDrawable(Color.rgb(230,230,230)));
        String photoKey=x.photo==null?"":x.photo;
        av.setTag(photoKey);
        loadThumbAsync(av, photoKey);
        r.addView(av,new LinearLayout.LayoutParams(dp(58),dp(58)));

        LinearLayout t=new LinearLayout(this); t.setOrientation(LinearLayout.VERTICAL);
        t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));
        t.addView(tv(x.cat+(x.status==null||x.status.isEmpty()?"":" • "+x.status)+" • "+money(x.fee),12,Color.DKGRAY,false));
        if(detail!=null)t.addView(tv(detail,12,Color.DKGRAY,false));
        r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        if(amount>0)r.addView(tv(money(amount),14,BLACK,true),new LinearLayout.LayoutParams(dp(115),-2));
        final long id=x.id; r.setOnClickListener(v->showProfile(id));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(7)); b.addView(r,lp);
    }

    private void loadThumbAsync(ImageView view, String photo) {
        final String key = photo==null?"":photo;
        Bitmap cached = thumbCache.get(key);
        if(cached!=null){ view.setImageBitmap(cached); return; }
        photoPool.execute(() -> {
            Bitmap bm = decodeThumb(key);
            if(bm==null) return;
            thumbCache.put(key,bm);
            runOnUiThread(() -> {
                Object tag=view.getTag();
                if(tag!=null && key.equals(String.valueOf(tag))) view.setImageBitmap(bm);
            });
        });
    }

    private Bitmap decodeThumb(String photo) {
        try {
            Bitmap bm=null;
            if(photo.startsWith("USER:")) {
                File f=new File(new File(getFilesDir(),"athlete_photos"),photo.substring(5));
                if(f.isFile()) bm=BitmapFactory.decodeFile(f.getAbsolutePath());
            } else if("NONE".equals(photo) || photo.trim().isEmpty()) {
                try(InputStream in=getAssets().open("photos/0000 BOS.jpg")){bm=BitmapFactory.decodeStream(in);} 
            } else {
                try(InputStream in=getAssets().open("photos/"+photo.trim())){bm=BitmapFactory.decodeStream(in);} 
            }
            if(bm==null) return null;
            int target=128;
            int w=bm.getWidth(), h=bm.getHeight();
            if(w<=target && h<=target) return bm;
            float s=Math.min((float)target/w,(float)target/h);
            int nw=Math.max(1,Math.round(w*s)), nh=Math.max(1,Math.round(h*s));
            Bitmap small=Bitmap.createScaledBitmap(bm,nw,nh,true);
            if(small!=bm) bm.recycle();
            return small;
        } catch(Exception e) { return null; }
    }

    @Override protected void onDestroy(){
        photoPool.shutdownNow();
        super.onDestroy();
    }
}
