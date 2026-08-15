package com.parion.aidat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.io.InputStream;

public class MainActivityV383 extends MainActivityV381 {
    @Override void showHome() {
        super.showHome();
        replaceFirstImageWithAsset(root, "parion_logo.png");
    }

    @Override void showLogin() {
        super.showLogin();
        replaceFirstImageWithAsset(root, "parion_logo.png");
    }

    @Override void setAthletePhoto(ImageView v, String photo) {
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (photo != null && !photo.trim().isEmpty() && loadAsset(v, "photos/" + photo.trim())) return;
        if (loadAsset(v, "photos/0000 BOS.jpg")) return;
        loadAsset(v, "parion_logo.png");
    }

    private boolean loadAsset(ImageView v, String path) {
        try (InputStream in = getAssets().open(path)) {
            Bitmap bm = BitmapFactory.decodeStream(in);
            if (bm != null) {
                v.setImageBitmap(bm);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean replaceFirstImageWithAsset(View v, String path) {
        if (v instanceof ImageView) return loadAsset((ImageView) v, path);
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                if (replaceFirstImageWithAsset(g.getChildAt(i), path)) return true;
            }
        }
        return false;
    }
}
