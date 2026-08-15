package androidx.appcompat.widget;

public class AppCompatImageView extends android.widget.ImageView {
    public AppCompatImageView(android.content.Context context) { super(context); }
    @Override public com.parion.aidat.Drawable getDrawable(){
        android.graphics.drawable.Drawable d=super.getDrawable();
        return d==null?null:new com.parion.aidat.Drawable(d);
    }
}
