package iop.org.iop_contributors_app.ui.validators;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.iop.WalletConstants;
import org.iop.WalletModule;

import java.util.List;

import iop_sdk.governance.propose.Beneficiary;
import iop_sdk.governance.propose.Proposal;

/**
 * Created by mati on 03/12/16.
 */

public class CreateProposalActivityValidator {

    private WalletModule module;

    public CreateProposalActivityValidator(WalletModule module) {
        this.module = module;
    }

    public String validateTitle(String title) throws ValidationException{
        if (title==null){
            throwValidationException("Title null");
        }
        if (title.length()<15){
            throwValidationException("Title lenght must be 15 characters");
        }
        return title;
    }


    public String validateSubTitle(String subtitle) {
        return subtitle;
    }

    public String validatCategory(String category) {
        return category;
    }

    public String validateBody(String body) throws ValidationException {
        if (body.length()<20) throwValidationException("Body lenght must be more than 20 characters");
        return body;
    }

    public int validateStartBlock(int startBlock) {
        return startBlock;
    }

    public int validateEndBlock(int endBlock) {
        return endBlock;
    }

    public long validateBlockReward(long blockReward) throws ValidationException {
        if (blockReward>Proposal.BLOCK_REWARD_MAX_VALUE) throwValidationException("block reward must be lower than " + Proposal.BLOCK_REWARD_MAX_VALUE);
        return blockReward;
    }

    public boolean validateBeneficiary(String addressBen1, long value) throws ValidationException {
        if (!checkAddress(addressBen1)) throwValidationException("Address not valid");
        if (module.proposalBeneficiaryAddressExist(addressBen1)) throwValidationException("Address "+addressBen1+" is already used in other proposal,\nfor your privacy please use another address");
        return true;
    }

    public void validateBeneficiaries(List<Beneficiary> beneficiaries, long blockReward) throws ValidationException {
        long beneficiariesValue = 0;
        for (Beneficiary beneficiary : beneficiaries) {
            beneficiariesValue+=beneficiary.getAmount();
        }
        if (beneficiariesValue!=blockReward) throwValidationException("sum of all beneficiaries must be equal to blockReward");
    }

    private void throwValidationException(String message) throws ValidationException {
        throw new ValidationException(message);
    }

    private boolean checkAddress(String addressBase58) {
        try {
            Address.fromBase58(WalletConstants.NETWORK_PARAMETERS,addressBase58);
        }catch (AddressFormatException e){
            return false;
        }
        return true;
    }

    public void validateAddress(String text) throws ValidationException {
        checkAddress(text);
    }
}
