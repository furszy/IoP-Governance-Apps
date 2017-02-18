package iop.org.iop_contributors_app.ui;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.iop.WalletModule;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.transactions.ProposalTransactionsWrapper;
import iop_sdk.governance.propose.Proposal;

public class UnconfirmedTransactionFragment extends Fragment {

    private static final String TAG = "UnconfirmedTxsFrag";
    private SectionedRecyclerViewAdapter sectionAdapter;
    private WalletModule module;


    public static UnconfirmedTransactionFragment newInstance(WalletModule module) {

        UnconfirmedTransactionFragment fragment = new UnconfirmedTransactionFragment();
        fragment.setModule(module);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unconfirmed_transactions_main, container, false);

        sectionAdapter = new SectionedRecyclerViewAdapter();



//        for(char alphabet = 'A'; alphabet <= 'Z';alphabet++) {
//            ProposalTransactionsWrapper contacts = getWrappers(alphabet).get(0);
//
//            if (contacts.getTransactions().size() > 0) {
//
//            }
//        }

        sectionAdapter.addSection(new ContactsSection("Unnavailable transactions",getWrappers('A').get(0) ));

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(sectionAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof AppCompatActivity) {
//            AppCompatActivity activity = ((AppCompatActivity) getActivity());
//            if (activity.getSupportActionBar() != null)
//                activity.getSupportActionBar().setTitle(R.string.nav_example1);
        }
    }

    private List<ProposalTransactionsWrapper> getWrappers(char letter) {
        List<ProposalTransactionsWrapper> wrappers = new ArrayList<>();

        ProposalTransactionsWrapper proposalTransactionsWrapper = new ProposalTransactionsWrapper(Proposal.buildRandomProposal());
        for (Transaction transaction : module.getWalletManager().getWallet().getTransactions(false)) {
            if (!transaction.isMature())
                proposalTransactionsWrapper.addTx(transaction);
            else
                if (transaction.getConfidence().getConfidenceType()== TransactionConfidence.ConfidenceType.PENDING){
                    proposalTransactionsWrapper.addTx(transaction);
                }
        }
        wrappers.add(proposalTransactionsWrapper);

        return wrappers;
    }

    public void setModule(WalletModule module) {
        this.module = module;
    }

    class ContactsSection extends StatelessSection {

        String title;
        ProposalTransactionsWrapper wrapper;

        public ContactsSection(String title, ProposalTransactionsWrapper wrapper) {
            super(R.layout.section_unnconfirmed_transactions_header, R.layout.unconfirmed_transactions_row);

            this.title = title;
            this.wrapper = wrapper;
        }

        @Override
        public int getContentItemsTotal() {
            return wrapper.getTransactions().size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            Transaction wrapper = this.wrapper.getTransactions().get(position);
            itemHolder.txt_tx_hash.setText("Transaction hash: "+wrapper.getHash().toString());
            itemHolder.txt_value.setText("Value: "+wrapper.getValueSentToMe(module.getWalletManager().getWallet()));
            if(wrapper.isCoinBase()){
                itemHolder.txt_blocks_left.setText("Blocks left until it be available: "+(100-wrapper.getConfidence().getDepthInBlocks()));
            }else {
                itemHolder.txt_blocks_left.setText("Blocks left until it be available: next block");
            }


            //itemHolder.imgItem.setImageResource(name.hashCode() % 2 == 0 ? R.drawable.ic_face_black_48dp : R.drawable.ic_tag_faces_black_48dp);

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getActivity(), String.format("Clicked on position #%s of Section %s", sectionAdapter.getSectionPosition(itemHolder.getAdapterPosition()), title), Toast.LENGTH_SHORT).show();

                    ContactsSection contactsSection = (ContactsSection) sectionAdapter.getSectionForPosition(sectionAdapter.getSectionPosition(itemHolder.getAdapterPosition()));
                    ProposalTransactionsWrapper proposalTransactionsWrapper = contactsSection.getSectionWrapper();
                    Log.i(TAG,"wrapper: "+proposalTransactionsWrapper.toString());
                    //Log.i("Tx: "+proposalTransactionsWrapper.getTransactions().get());
                }
            });

            itemHolder.itemView.setBackgroundColor((position%2==0)? Color.parseColor("#CCCCCC"):Color.WHITE);
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);
        }

        public ProposalTransactionsWrapper getSectionWrapper(){
            return wrapper;
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        public HeaderViewHolder(View view) {
            super(view);

            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        TextView txt_tx_hash;
        TextView txt_value;
        TextView txt_blocks_left;

        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            txt_tx_hash = (TextView) itemView.findViewById(R.id.txt_tx_hash);
            txt_value = (TextView) itemView.findViewById(R.id.txt_value);
            txt_blocks_left = (TextView) itemView.findViewById(R.id.txt_blocks_left);
        }
    }
}