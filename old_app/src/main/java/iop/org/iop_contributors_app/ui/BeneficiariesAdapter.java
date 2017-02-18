package iop.org.iop_contributors_app.ui;

import android.content.Context;
import android.view.View;

import java.util.List;

import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.validators.CreateProposalActivityValidator;
import iop_sdk.governance.propose.Beneficiary;

/**
 * Created by mati on 09/01/17.
 */

public class BeneficiariesAdapter extends FermatAdapterImproved<Beneficiary,BeneficiaryHolder> {

    /** field validator */
    private CreateProposalActivityValidator validator;
    private QrListener onQrListener;

    public interface QrListener{

        void onItemQrTouched(Beneficiary data, int position);

    }


    public BeneficiariesAdapter(Context context, List<Beneficiary> extraBeneficiaries, CreateProposalActivityValidator validator,QrListener qrListener) {
        super(context, extraBeneficiaries);
        this.validator = validator;
        this.onQrListener = qrListener;
    }

    @Override
    protected BeneficiaryHolder createHolder(View itemView, int type) {
        return new BeneficiaryHolder(itemView,type,validator);
    }

    @Override
    protected int getCardViewResource(int type) {
        return R.layout.beneficiary_row;
    }

    @Override
    protected void bindHolder(final BeneficiaryHolder holder, final Beneficiary data, final int position) {

        holder.edit_beneficiary_address.setText(data.getAddress());

        if (data.getAmount()!=0) {
            holder.edit_beneficiary_value.setText(String.valueOf(data.getAmount()));
        }

        holder.img_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQrListener.onItemQrTouched(data,position);
            }
        });

//        holder.edit_beneficiary_value.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (!s.toString().equals(""))
//                    data.setAmount(Long.parseLong(s.toString()));
//            }
//        });
    }


//    public static class AddressWatcher implements TextWatcher{
//
//        String address;
//        private CreateProposalActivityValidator validator;
//
//        public AddressWatcher(CreateProposalActivityValidator validator) {
//            this.validator = validator;
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            try {
//                if (!s.toString().equals("")) {
//                    validator.validateAddress(s.toString());
//                    address = s.toString();
//                }
//            } catch (ValidationException e) {
//                // nothing
//            }
//        }
//    }
}
