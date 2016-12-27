package iop.org.iop_contributors_app.ui.voting.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.services.BlockchainService;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.module.WalletModule;
import iop_sdk.governance.vote.Vote;

public class BroadcastVoteDialog extends DialogFragment {

    private WalletModule module;
    private Vote vote;
    private CancelLister cancelListener;
    private boolean isActionOkey;


    public static BroadcastVoteDialog newinstance(WalletModule module, Vote vote) {
        BroadcastVoteDialog broadcastVoteDialog = new BroadcastVoteDialog();
        broadcastVoteDialog.setModule(module);
        broadcastVoteDialog.setVote(vote);
        return broadcastVoteDialog;
    }

    public void setModule(WalletModule module) {
        this.module = module;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.broadcast_vote_dialog,null);

        TextView txt_send = (TextView) root.findViewById(R.id.txt_send);
        TextView txt_cancel = (TextView) root.findViewById(R.id.txt_cancel);

        txt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSend();
                isActionOkey = true;
                dismiss();
            }
        });

        txt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelListener.cancel();
                dismiss();
            }
        });



        return root;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
//        cancelListener.cancel();
        cancelListener.onDismiss(isActionOkey);
    }

    private void handleSend(){
        Bundle bundle = new Bundle();
        // todo: acá tengo que mandar el voto..
        bundle.putSerializable(BlockchainService.INTENT_EXTRA_PROPOSAL_VOTE, vote);
        ((BaseActivity)getActivity()).sendWorkToBlockchainService(BlockchainService.ACTION_BROADCAST_VOTE_PROPOSAL_TRANSACTION, bundle);
    }


    public void setVote(Vote vote) {
        this.vote = vote;
    }

    public DialogFragment setCancelListener(CancelLister cancelListener) {
        this.cancelListener = cancelListener;
        return this;
    }
}
