package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;

/** v3.9.2 - recovery UI + visible password check. Cloud data writes remain blocked by V500. */
public class MainActivityV502 extends MainActivityV501 {
    @Override public void onCreate(Bundle b){ super.onCreate(b); }

    @Override void showLogin(){
        page="LOGIN"; currentAthlete=-1; base("PARION • ONLINE GİRİŞ",false);
        ScrollView sv=scroll(); LinearLayout box=box(sv); box.setPadding(dp(18),dp(24),dp(18),dp(18));
        ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.parion_logo); logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE); box.addView(logo,new LinearLayout.LayoutParams(-1,dp(120)));
        TextView title=tv("PARION SPOR OKULU",22,BLACK,true); title.setGravity(Gravity.CENTER); box.addView(title);
        TextView info=tv("Merkezi veritabanına bağlanmak için hesabınızla giriş yapın.",13,Color.DKGRAY,false); info.setGravity(Gravity.CENTER); info.setPadding(0,dp(4),0,dp(14)); box.addView(info);

        EditText email=new EditText(this); email.setHint("E-posta"); email.setSingleLine(true); email.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); email.setText("volleyavuz@gmail.com"); box.addView(email,new LinearLayout.LayoutParams(-1,dp(56)));
        EditText pass=new EditText(this); pass.setHint("Şifre"); pass.setSingleLine(true); pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); box.addView(pass,new LinearLayout.LayoutParams(-1,dp(56)));

        CheckBox show=new CheckBox(this); show.setText("ŞİFREYİ GÖSTER"); show.setOnCheckedChangeListener((b,checked)->{
            int pos=pass.getSelectionStart();
            pass.setInputType(InputType.TYPE_CLASS_TEXT | (checked?InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:InputType.TYPE_TEXT_VARIATION_PASSWORD));
            if(pos>=0 && pos<=pass.length()) pass.setSelection(pos);
        }); box.addView(show);

        Button login=btn("GİRİŞ YAP"); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58)); lp.setMargins(0,dp(8),0,dp(6)); box.addView(login,lp);
        Button forgot=btn("ŞİFREMİ UNUTTUM"); box.addView(forgot,new LinearLayout.LayoutParams(-1,dp(54)));

        TextView note=tv("Parola kurtarma yalnızca Supabase Auth e-postası gönderir. Sporcu, ödeme, aidat ve medya verilerine yazma yapılmaz.",12,Color.DKGRAY,false); note.setPadding(dp(4),dp(12),dp(4),0); box.addView(note);

        login.setOnClickListener(v->auth(email.getText().toString().trim(),pass.getText().toString(),false));
        forgot.setOnClickListener(v->sendRecovery502(email.getText().toString().trim()));
    }

    private void sendRecovery502(String email){
        if(email==null||email.trim().isEmpty()){toast("E-POSTA ADRESİNİ GİRİN.");return;}
        final AlertDialog wait=new AlertDialog.Builder(this).setMessage("Şifre sıfırlama e-postası gönderiliyor...").setCancelable(false).create(); wait.show();
        new Thread(()->{
            try{
                JSONObject body=new JSONObject().put("email",email.trim());
                HttpResult r=super.request("POST",SUPABASE_URL+"/auth/v1/recover",body.toString(),null);
                if(r.code>=200&&r.code<300){
                    runOnUiThread(()->{wait.dismiss();new AlertDialog.Builder(this).setTitle("E-POSTA GÖNDERİLDİ").setMessage("Şifre sıfırlama bağlantısı "+email.trim()+" adresine gönderildi. Gelen kutusu ve spam klasörünü kontrol edin.").setPositiveButton("TAMAM",null).show();});
                }else{
                    String msg=r.body==null?"":r.body; if(msg.length()>180)msg=msg.substring(0,180); final String m=msg;
                    runOnUiThread(()->{wait.dismiss();toast("ŞİFRE SIFIRLAMA HATASI • HTTP "+r.code+" • "+m);});
                }
            }catch(Exception e){runOnUiThread(()->{wait.dismiss();toast("ŞİFRE SIFIRLAMA HATASI • "+shortMsg(e));});}
        },"parion-password-recovery-502").start();
    }
}
