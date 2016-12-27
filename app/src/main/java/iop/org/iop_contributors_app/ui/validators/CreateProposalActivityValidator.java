package iop.org.iop_contributors_app.ui.validators;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;

import java.util.Map;

import iop_sdk.governance.propose.Proposal;
import org.iop.WalletConstants;

/**
 * Created by mati on 03/12/16.
 */

public class CreateProposalActivityValidator {

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
        return true;
    }

    public void validateBeneficiaries(Map<String, Long> beneficiaries, long blockReward) throws ValidationException {
        long beneficiariesValue = 0;
        for (Long aLong : beneficiaries.values()) {
            beneficiariesValue+=aLong;
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
