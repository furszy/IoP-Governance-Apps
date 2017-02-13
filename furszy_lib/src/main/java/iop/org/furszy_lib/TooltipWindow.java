package iop.org.furszy_lib;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TooltipWindow {
 
    private static final int MSG_DISMISS_TOOLTIP = 100;
    private Context ctx;
    private PopupWindow tipWindow;
    private View contentView;
    private LayoutInflater inflater;

    private String text = "Tooltip text exaple ;P";
    private TextView txt_text;

    public static TooltipWindow newInstance(Context ctx){
        return new TooltipWindow(ctx);
    }

    private TooltipWindow(Context ctx) {
        this.ctx = ctx;
        tipWindow = new PopupWindow(ctx);
 
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.tooltip_layout, null);

        txt_text = (TextView) contentView.findViewById(R.id.tooltip_text);
    }
 
    public void showToolTip(View anchor) {

        // text
        txt_text.setText(text);
 
        tipWindow.setHeight(LayoutParams.WRAP_CONTENT);
        tipWindow.setWidth(LayoutParams.WRAP_CONTENT);
 
        tipWindow.setOutsideTouchable(true);
        tipWindow.setTouchable(true);
        tipWindow.setFocusable(true);
        tipWindow.setBackgroundDrawable(new BitmapDrawable());
 
        tipWindow.setContentView(contentView);

 
        int screen_pos[] = new int[2];
// Get location of anchor view on screen
        anchor.getLocationOnScreen(screen_pos);
 
// Get rect for anchor view
        Rect anchor_rect = new Rect(screen_pos[0], screen_pos[1], screen_pos[0]
                + anchor.getWidth(), screen_pos[1] + anchor.getHeight());
 
// Call view measure to calculate how big your view should be.
        contentView.measure(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
 
        int contentViewHeight = contentView.getMeasuredHeight();
        int contentViewWidth = contentView.getMeasuredWidth();
// In this case , i dont need much calculation for x and y position of
// tooltip
// For cases if anchor is near screen border, you need to take care of
// direction as well
// to show left, right, above or below of anchor view
        int x = (contentViewWidth / 2);
        int position_x = anchor_rect.centerX() - 562;//-
        int position_y = anchor_rect.bottom - (anchor_rect.height() / 2);
 
        tipWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position_x,position_y);
 
// send message to handler to dismiss tipWindow after X milliseconds
        handler.sendEmptyMessageDelayed(MSG_DISMISS_TOOLTIP, 10000);
    }
 
    public boolean isTooltipShown() {
        if (tipWindow != null && tipWindow.isShowing())
        return true;
        return false;
    }
 
    public void dismissTooltip() {
        if (tipWindow != null && tipWindow.isShowing())
        tipWindow.dismiss();
    }
 
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_TOOLTIP:
                    if (tipWindow != null && tipWindow.isShowing())
                    tipWindow.dismiss();
                    break;
            }
        };
    };

    public void setText(String text){
        this.text = text;
    }


 
}