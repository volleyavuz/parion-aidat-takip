package com.parion.aidat;

/**
 * v4.2.30 - legacy HOME cover disabled.
 *
 * V660 historically placed a full-screen white PARION/SPORCU TAKIP UYGULAMASI
 * cover over HOME for 1500 ms on every showHome() call. That produced a second
 * white startup screen after SplashActivity and also reappeared whenever HOME
 * was rebuilt from another page.
 *
 * Keep this compatibility class in the inheritance chain, but never add the
 * legacy cover. Dashboard rendering/navigation behavior remains owned by the
 * later HOME layers.
 */
public class MainActivityV660 extends MainActivityV659 {
    @Override void showHome(){
        super.showHome();
    }
}
