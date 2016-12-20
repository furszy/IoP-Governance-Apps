package iop.org.iop_contributors_app.ui.voting.ui.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar;

/**
 * Created by mati on 12/12/16.
 */

public class VoteDialog extends DialogFragment implements SwitchSeekBar.SwitchListener {


    private View root;
    private SwitchSeekBar seek_bar;
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

        seek_bar = (SwitchSeekBar) root.findViewById(R.id.seek_bar);

        seek_bar.addSwitchListener(this);

        return root;

    }




    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    @Override
    public void handleLeft() {
        Toast.makeText(getActivity(),"No touched",Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleRight() {
        Toast.makeText(getActivity(),"Yes touched",Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleCenter() {
        Toast.makeText(getActivity(),"center touched",Toast.LENGTH_LONG).show();
    }
}
