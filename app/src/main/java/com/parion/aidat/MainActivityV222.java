package com.parion.aidat;

public class MainActivityV222 extends MainActivity {
    @Override
    String tl(double x) {
        return String.format(TR, "%,d ₺", java.lang.Math.round(x));
    }
}
