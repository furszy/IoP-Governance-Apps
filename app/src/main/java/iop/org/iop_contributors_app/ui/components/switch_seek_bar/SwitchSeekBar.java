package iop.org.iop_contributors_app.ui.components.switch_seek_bar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.voting.ui.dialogs.VoteDialog;

/**
 * Created by mati on 20/12/16.
 */

public class SwitchSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener {

    public interface SwitchListener {

        void handleLeft();

        void handleRight();

        void handleCenter();

    }

    private List<SwitchListener> switchListeners = new ArrayList<>();

    public SwitchSeekBar(Context context) {
        super(context);
        init();
    }

    public SwitchSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SwitchSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        setOnSeekBarChangeListener(this);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public void addSwitchListener(SwitchListener switchListener) {
        switchListeners.add(switchListener);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int mProgress = seekBar.getProgress();
        if(mProgress >= 0 & mProgress < 31) {
            seekBar.setProgress(15);
            seekBar.setBackgroundResource(R.drawable.img_swicth_rojo);
            for (SwitchListener listener : switchListeners) {
                listener.handleLeft();
            }
        } else if(mProgress > 25 & mProgress < 70) {
            seekBar.setBackgroundResource(R.drawable.img_siwcht_gris);
            seekBar.setProgress(50);
            for (SwitchListener listener : switchListeners) {
                listener.handleCenter();
            }
        } else {
            seekBar.setProgress(85);
            seekBar.setBackgroundResource(R.drawable.img_swicht_verde);
            for (SwitchListener listener : switchListeners) {
                listener.handleRight();
            }
        }
    }

}
