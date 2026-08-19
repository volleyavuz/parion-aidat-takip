package com.parion.aidat;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;

public class Drawable extends android.graphics.drawable.Drawable {
    private final android.graphics.drawable.Drawable base;
    public Drawable(android.graphics.drawable.Drawable base){this.base=base;}
    @Override public void draw(Canvas canvas){if(base!=null)base.draw(canvas);}
    @Override public void setAlpha(int alpha){if(base!=null)base.setAlpha(alpha);}
    @Override public void setColorFilter(ColorFilter cf){if(base!=null)base.setColorFilter(cf);}
    @Override public int getOpacity(){return base==null?PixelFormat.TRANSPARENT:base.getOpacity();}
    @Override public int getIntrinsicWidth(){return base==null?0:base.getIntrinsicWidth();}
    @Override public int getIntrinsicHeight(){return base==null?0:base.getIntrinsicHeight();}
}
