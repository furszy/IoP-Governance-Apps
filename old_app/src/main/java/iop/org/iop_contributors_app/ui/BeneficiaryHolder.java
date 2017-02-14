package iop.org.iop_contributors_app.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import iop.org.furszy_lib.adapter.FermatViewHolder;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.validators.CreateProposalActivityValidator;

/**
 * Created by mati on 09/01/17.
 */

public class BeneficiaryHolder extends FermatViewHolder {

    public EditText edit_beneficiary_address;
    public EditText edit_beneficiary_value;
    public ImageView img_qr;

    protected BeneficiaryHolder(View itemView, int holderType, CreateProposalActivityValidator validator) {
        super(itemView, holderType);
        edit_beneficiary_address = (EditText) itemView.findViewById(R.id.edit_beneficiary_address);

        edit_beneficiary_value = (EditText) itemView.findViewById(R.id.edit_beneficiary_value);

        img_qr = (ImageView) itemView.findViewById(R.id.img_qr);

//        edit_beneficiary_address.addTextChangedListener(new BeneficiariesAdapter.AddressWatcher(validator));
    }


}
