package iop.org.iop_contributors_app.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import java.util.List;

import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.validators.CreateProposalActivityValidator;
import iop.org.iop_contributors_app.ui.validators.ValidationException;
import iop_sdk.governance.propose.Beneficiary;

/**
 * Created by mati on 09/01/17.
 */

public class BeneficiariesAdapter extends FermatAdapterImproved<Beneficiary,BeneficiaryHolder> {

    /** field validator */
    private CreateProposalActivityValidator validator;

    protected BeneficiariesAdapter(Context context,CreateProposalActivityValidator validator) {
        super(context);

    }

    public BeneficiariesAdapter(Context context, List<Beneficiary> extraBeneficiaries, CreateProposalActivityValidator validator) {
        super(context, extraBeneficiaries);
        this.validator = validator;
    }

    @Override
    protected BeneficiaryHolder createHolder(View itemView, int type) {
        return new BeneficiaryHolder(itemView,type);
    }

    @Override
    protected int getCardViewResource(int type) {
        return R.layout.beneficiary_row;
    }

    @Override
    protected void bindHolder(BeneficiaryHolder holder, final Beneficiary data, int position) {

        holder.edit_beneficiary_address.setText(data.getAddress());
        holder.edit_beneficiary_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().equals("")) {
                        validator.validateAddress(s.toString());
                        data.setAddress(s.toString());
                    }
                } catch (ValidationException e) {
                    // nothing
                }
            }
        });

        if (data.getAmount()!=0) {
            holder.edit_beneficiary_value.setText(String.valueOf(data.getAmount()));
        }

        holder.edit_beneficiary_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    data.setAmount(Long.parseLong(s.toString()));
            }
        });
    }
}
