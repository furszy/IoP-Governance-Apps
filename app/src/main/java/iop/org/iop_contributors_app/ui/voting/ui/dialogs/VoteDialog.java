package iop.org.iop_contributors_app.ui.voting.ui.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;

/**
 * Created by mati on 12/12/16.
 */

public class VoteDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener {


    private View root;
    private SeekBar seek_bar;
    private Proposal proposal;

    public static VoteDialog newInstance(Proposal proposal) {
        VoteDialog fragment = new VoteDialog();
        fragment.setProposal(proposal);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.vote_dialog,null);

        seek_bar = (SeekBar) root.findViewById(R.id.seek_bar);

        seek_bar.setOnSeekBarChangeListener(this);

        return root;

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // we don't need it
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // we don't need it
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int mProgress = seekBar.getProgress();
//        if (mProgress==0){
//            seekBar.setProgress(0);
//        }else if(mProgress==1){
//            seekBar.setProgress(1);
//        }else if (mProgress==2){
//            seekBar.setProgress(2);
//        }
        if(mProgress > 0 & mProgress < 26) {

        } else if(mProgress > 25 & mProgress < 76) {
            seekBar.setProgress(50);
        } else seekBar.setProgress(100);
    }



    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }
}