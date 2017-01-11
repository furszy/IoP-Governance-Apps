package iop.org.iop_contributors_app.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.iop.WalletModule;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.services.BlockchainService;
import iop.org.iop_contributors_app.ui.ProposalSummaryActivity;
import iop_sdk.governance.propose.Proposal;

;import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;

public class BroadcastContractDialog extends DialogFragment {

    private WalletModule module;
    private Proposal proposal;
    private CancelLister cancelListener;

    private boolean actionCompleted;

    public static BroadcastContractDialog newinstance(WalletModule module, Proposal proposal) {
        BroadcastContractDialog broadcastContractDialog = new BroadcastContractDialog();
        broadcastContractDialog.setModule(module);
        broadcastContractDialog.setProposal(proposal);
        return broadcastContractDialog;
    }

    public void setModule(WalletModule module) {
        this.module = module;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.broadcast_contract_dialog,null);

        TextView txt_send = (TextView) root.findViewById(R.id.txt_send);
        TextView txt_cancel = (TextView) root.findViewById(R.id.txt_cancel);

        txt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSend();
                actionCompleted = true;
                dismiss();
            }
        });

        txt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCompleted = false;
                dismiss();
            }
        });

        return root;
    }

    private void handleSend(){
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_EXTRA_PROPOSAL, proposal);
        ((ProposalSummaryActivity)getActivity()).sendWorkToBlockchainService(BlockchainService.ACTION_BROADCAST_PROPOSAL_TRANSACTION, bundle);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        cancelListener.cancel(actionCompleted);
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    public DialogFragment setCancelListener(CancelLister cancelListener) {
        this.cancelListener = cancelListener;
        return this;
    }
}
