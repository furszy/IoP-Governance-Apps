package iop.org.iop_contributors_app.ui.transactions;

import org.bitcoinj.core.Transaction;

import java.util.ArrayList;
import java.util.List;

import iop_sdk.governance.propose.Proposal;

/**
 * Created by mati on 17/01/17.
 */
public class ProposalTransactionsWrapper {

    private final Proposal proposal;
    private List<Transaction> transactions;

    public ProposalTransactionsWrapper(Proposal proposal) {
        this.transactions = new ArrayList<>();
        this.proposal = proposal;
    }

    public void addTx(Transaction transaction){
        transactions.add(transaction);
    }

    public Proposal getProposal() {
        return proposal;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
