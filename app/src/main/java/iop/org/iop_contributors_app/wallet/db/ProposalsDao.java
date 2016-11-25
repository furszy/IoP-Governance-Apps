package iop.org.iop_contributors_app.wallet.db;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

import iop.org.iop_contributors_app.core.Proposal;
import iop.org.iop_contributors_app.core.iop_sdk.blockchain.contribution_contract.ProposalTransactionBuilder;

/**
 * Created by mati on 16/11/16.
 */
//todo: más que esto, lo que deberia tener es una base de datos de proposals con las transacciones lockeadas correspondientes a cada uno.
public class ProposalsDao {

    private ProposalsDatabaseHandler proposalsDatabaseHandler;



    public ProposalsDao(Context context) {
        this.proposalsDatabaseHandler = new ProposalsDatabaseHandler(context);
    }

    /**
     * returns true if the transaction is already used in other proposal and is locked.
     *
     * @param parentTransactionHash
     * @return
     */
    public boolean isLockedOutput(byte[] parentTransactionHash,long position){
        return proposalsDatabaseHandler.isOutputLocked(parentTransactionHash,position);
    }

    /**
     * Save proposal
     *
     * @param proposal
     * @throws CantSaveProposalException
     */
    public boolean saveProposal(Proposal proposal) throws CantSaveProposalException {
        try {
            return proposalsDatabaseHandler.addProposal(proposal);
        } catch (JsonProcessingException e) {
            throw new CantSaveProposalException(e);
        }
    }

    public List<Proposal> listProposals() {
        return proposalsDatabaseHandler.getAllProposals();
    }

    public boolean exist(long ioPIP) {
        return proposalsDatabaseHandler.exist(ioPIP);
    }

    public void lockOutput(long contractId,byte[] hash, int index) {
        proposalsDatabaseHandler.lockOutput(contractId,hash,index);
    }

    public long getTotalLockedBalance() {
        return proposalsDatabaseHandler.getProposalsCount()*ProposalTransactionBuilder.FREEZE_VALUE.getValue();
    }
}
