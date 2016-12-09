package iop.org.iop_contributors_app.wallet.db;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.logging.Logger;

import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.core.iop_sdk.governance.ProposalTransactionBuilder;
import iop.org.iop_contributors_app.core.iop_sdk.governance.ProposalsContractDao;

/**
 * Created by mati on 16/11/16.
 */
//todo: más que esto, lo que deberia tener es una base de datos de proposals con las transacciones lockeadas correspondientes a cada uno.
public class ProposalsDao implements ProposalsContractDao {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProposalsDao.class);

    private ProposalsDatabaseHandler proposalsDatabaseHandler;

    public ProposalsDao(Context context) {
        this.proposalsDatabaseHandler = new ProposalsDatabaseHandler(context);
    }

    /**
     * returns true if the transaction is already used in other proposal and is locked.
     *
     * @param parentTransactionHashHex
     * @return
     */
    public boolean isLockedOutput(String parentTransactionHashHex,long position){
        boolean ret = proposalsDatabaseHandler.isOutputLocked(parentTransactionHashHex,position);
        LOG.info("isLockedOtput ret: "+ret);
        return ret;
    }

    /**
     * Save proposal
     *
     * @param proposal
     * @throws CantSaveProposalException
     */
    public boolean saveProposal(Proposal proposal) throws CantSaveProposalException, CantSaveProposalExistException {
        try {
            if (proposalsDatabaseHandler.exist(proposal.getTitle())) throw new CantSaveProposalExistException("Proposal title already exist");
                return proposalsDatabaseHandler.addProposal(proposal);
        } catch (JsonProcessingException e) {
            throw new CantSaveProposalException(e);
        }
    }

    public List<Proposal> listProposals() {
        return proposalsDatabaseHandler.getAllProposals();
    }

    public boolean exist(String title) {
        return proposalsDatabaseHandler.exist(title);
    }

    public boolean lockOutput(int forumId,String hash, int index) {
        return proposalsDatabaseHandler.lockOutput(forumId,hash,index)==1;
    }

    public long getTotalLockedBalance() {
        return proposalsDatabaseHandler.getSentProposalsCount()*ProposalTransactionBuilder.FREEZE_VALUE.getValue();
    }

    public Proposal findProposal(String forumTitle) throws CantGetProposalException {
        return proposalsDatabaseHandler.getProposal(forumTitle);
    }

    public Proposal findProposal(int forumId) throws CantGetProposalException{
        return proposalsDatabaseHandler.getProposal(forumId);
    }

    public boolean updateProposal(Proposal proposal) throws CantUpdateProposalException {
        try {
            return proposalsDatabaseHandler.updateProposal(proposal) == 1;
        }catch (Exception e){
            throw new CantUpdateProposalException(e);
        }
    }

    public void saveIfChange(Proposal proposal) throws CantGetProposalException, CantUpdateProposalException {

        Proposal proposalDb = findProposal(proposal.getTitle());
        boolean change = false;

        if (!proposal.getSubTitle().equals(proposalDb.getSubTitle()))
            change = true;
        if (!proposal.getCategory().equals(proposalDb.getCategory()))
            change = true;
        if (!proposal.getBody().equals(proposalDb.getBody()))
            change = true;
        if (proposal.getStartBlock()!=proposalDb.getStartBlock())
            change = true;
        if (proposal.getEndBlock()!=proposalDb.getEndBlock())
            change = true;
        if (proposal.getBlockReward()!=proposalDb.getBlockReward())
            change = true;
        if (proposal.getForumId()!=proposalDb.getForumId())
            change = true;

        if (change){
            updateProposal(proposal);
        }


    }

    public boolean isProposalMine(String title) {
        return proposalsDatabaseHandler.isProposalMine(title);
    }
    public boolean isProposalMine(int forumId) {
        return proposalsDatabaseHandler.isProposalMine(forumId);
    }

    public boolean markSentProposal(int forumId){
        return proposalsDatabaseHandler.markSentProposal(forumId)==1;
    }
}
