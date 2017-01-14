package iop.org.iop_contributors_app.ui;

import android.view.View;
import android.widget.EditText;

import iop.org.furszy_lib.adapter.FermatViewHolder;
import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 09/01/17.
 */

public class BeneficiaryHolder extends FermatViewHolder {

    public EditText edit_beneficiary_address;
    public EditText edit_beneficiary_value;

    protected BeneficiaryHolder(View itemView, int holderType) {
        super(itemView, holderType);
        edit_beneficiary_address = (EditText) itemView.findViewById(R.id.edit_beneficiary_address);
        edit_beneficiary_value = (EditText) itemView.findViewById(R.id.edit_beneficiary_value);
    }


}