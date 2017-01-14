package iop.org.iop_contributors_app.ui.components.switch_seek_bar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.fasterxml.jackson.databind.node.POJONode;

import java.util.ArrayList;
import java.util.List;

import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 20/12/16.
 */

public class SwitchSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener {

    public interface SwitchListener {

        void handleLeft();

        void handleRight();

        void handleCenter();

    }

    public enum Position {

        LEFT(3),CENTER(50),RIGHT(97);

        int pos;

        Position(int pos) {
            this.pos = pos;
        }

        public int getPos() {
            return pos;
        }
    }

    private List<SwitchListener> switchListeners = new ArrayList<>();
    private Position position = Position.CENTER;


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
            movePosition(Position.LEFT);
        } else if(mProgress > 25 & mProgress < 70) {
            movePosition(Position.CENTER);
        } else {
            movePosition(Position.RIGHT);
        }
    }

    private void movePosition(Position position){
        setProgress(position.getPos());
        switch(position){
            case  CENTER:
                setBackgroundResource(R.drawable.img_siwcht_gris);
                for (SwitchListener listener : switchListeners) {
                    listener.handleCenter();
                }
                break;
            case LEFT:
                setBackgroundResource(R.drawable.img_swicth_rojo);
                for (SwitchListener listener : switchListeners) {
                    listener.handleLeft();
                }
                break;
            case RIGHT:
                setBackgroundResource(R.drawable.img_swicht_verde);
                for (SwitchListener listener : switchListeners) {
                    listener.handleRight();
                }
                break;
        }
    }

    public void setPosition(Position position) {
        this.position = position;
        movePosition(position);
    }


}
