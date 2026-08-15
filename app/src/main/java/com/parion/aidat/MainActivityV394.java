package com.parion.aidat;

import android.graphics.Color;

public class MainActivityV394 extends MainActivityV393 {
    private static final int PAID_LIGHT_GREEN = Color.rgb(198, 239, 206);

    @Override int paymentColor(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        int c=super.paymentColor(m,fee,sibling,start,end,restart,marker,amount);
        return c==GREEN ? PAID_LIGHT_GREEN : c;
    }
}
