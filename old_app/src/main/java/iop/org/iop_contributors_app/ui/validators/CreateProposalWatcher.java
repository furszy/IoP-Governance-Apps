package iop.org.iop_contributors_app.ui.validators;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import static iop_sdk.governance.ProposalForum.FIELD_ADDRESS;
import static iop_sdk.governance.ProposalForum.FIELD_BLOCK_REWARD;
import static iop_sdk.governance.ProposalForum.FIELD_BODY;
import static iop_sdk.governance.ProposalForum.FIELD_CATEGORY;
import static iop_sdk.governance.ProposalForum.FIELD_END_BLOCK;
import static iop_sdk.governance.ProposalForum.FIELD_START_BLOCK;
import static iop_sdk.governance.ProposalForum.FIELD_SUBTITLE;
import static iop_sdk.governance.ProposalForum.FIELD_TITLE;
import static iop_sdk.governance.ProposalForum.FIELD_VALUE;


/**
 * Created by mati on 07/12/16.
 */

public class CreateProposalWatcher implements TextWatcher {

    /** field identifier */
    private String idWatcher;
    /** field validator */
    private CreateProposalActivityValidator validator;
    /** view to show if something happen */
    private View errorView;
    /** watcher state */
    private boolean isValid;
    /** text to show in case of an error */
    private String errorToShow;

    public CreateProposalWatcher(String idWatcher, CreateProposalActivityValidator validator, View errorView) {
        this.idWatcher = idWatcher;
        this.validator = validator;
        this.errorView = errorView;
        this.errorToShow = idWatcher + "empty";
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        if (text!=null && !text.equals("")) {
            try {
                switch (idWatcher) {
                    case FIELD_TITLE:
                        validator.validateTitle(text);
                        break;
                    case FIELD_SUBTITLE:
                        validator.validateSubTitle(text);
                        break;
                    case FIELD_BODY:
                        validator.validateBody(text);
                        break;
                    case FIELD_CATEGORY:
                        validator.validatCategory(text);
                        break;
                    case FIELD_START_BLOCK:
                        validator.validateStartBlock(Integer.parseInt(text));
                        break;
                    case FIELD_END_BLOCK:
                        validator.validateEndBlock(Integer.parseInt(text));
                        break;
                    case FIELD_BLOCK_REWARD:
                        validator.validateBlockReward(Long.parseLong(text));
                        break;
                    case FIELD_ADDRESS:
                        validator.validateAddress(text);
                        break;
                    case FIELD_VALUE:
                        // nothing por ahora
                        break;
                }
                isValid = true;
            } catch (ValidationException e) {
                errorToShow = e.getMessage();
                isValid = false;
            } catch (Exception e) {
                isValid = false;
            }
        }else {
            isValid = false;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public String getErrorToShow() {
        return errorToShow;
    }
}
