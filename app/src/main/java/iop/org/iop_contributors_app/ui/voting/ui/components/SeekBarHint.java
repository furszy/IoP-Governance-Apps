package iop.org.iop_contributors_app.ui.voting.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import iop.org.iop_contributors_app.R;

import static iop.org.iop_contributors_app.furszy_sdk.android.mine.SizeUtils.convertDpToPx;

/**
 * Created by mati on 12/12/16.
 */

public class SeekBarHint extends SeekBar {
    public SeekBarHint(Context context) {
        super(context);
    }

    public SeekBarHint(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarHint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeekBarHint(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int thumb_x = (int) (( (double)this.getProgress()/this.getMax() ) * (double)this.getWidth());
        int middle = this.getHeight()/2;
        // your drawing code here, ie Canvas.drawText();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(convertDpToPx(getResources(),14));

        LayerDrawable ld = (LayerDrawable) getProgressDrawable();

        int mProgress = getProgress();
//        if (mProgress > 0 & mProgress < 26){
//            c.drawText("YES",getWidth()-convertDpToPx(getResources(),48),middle+12,paint);
//            Drawable drawable = ld.findDrawableByLayerId(R.id.progress_red_background);
//            drawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
//
//        } else if(mProgress > 25 & mProgress < 76){
//            // nothing
//            Drawable drawable = ld.findDrawableByLayerId(R.id.progress_red_background);
//            drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
//            Drawable d2 =  ld.findDrawableByLayerId(R.id.progress_green_background);
//            d2.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
//
//        }else {
//            c.drawText("NO",0+convertDpToPx(getResources(),48),middle+12,paint);
//            Drawable d2 = (Drawable) ld.findDrawableByLayerId(R.id.progress_green_background);
//            d2.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
//        }

    }



}
