package iop.org.iop_contributors_app.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.WalletTransaction;
import org.iop.WalletConstants;

import java.util.ArrayList;
import java.util.List;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;

/**
 * Created by mati on 17/01/17.
 */

public class TransactionsActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private View root;
//    private RecyclerView recycler_transactions;
//    private ViewGroup container_empty_screen;
//    private SwipeRefreshLayout swipe_refresh;
//    private LinearLayoutManager layoutManager;
//    private TransactionAdapter adapter;
    private List<Transaction> proposalTransactionsWrapperList;

    private View container_tx;


    private Runnable loadTransactions = new Runnable() {
        @Override
        public void run() {
            try {
                Context.propagate(WalletConstants.CONTEXT);
                for (Transaction transaction : module.getWalletManager().getWallet().getTransactionPool(WalletTransaction.Pool.UNSPENT).values()) {
                    if (!transaction.isMature())
                        proposalTransactionsWrapperList.add(transaction);
                }

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        adapter.changeDataSet(proposalTransactionsWrapperList);
//                        adapter.notifyDataSetChanged();
//                        swipe_refresh.setRefreshing(false);
//                    }
//                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        proposalTransactionsWrapperList = new ArrayList<>();

        setTitle("Unconfirmed Transactions");

        root = getLayoutInflater().inflate(R.layout.transactions_main,container);




//        recycler_transactions = (RecyclerView) root.findViewById(R.id.recycler_transactions);
//        container_empty_screen = (ViewGroup) root.findViewById(R.id.container_empty_screen);
//        swipe_refresh = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);

        //((TextView)root.findViewById(R.id.txt_block_height)).setText("Block count: "+module.getBlockchainManager().getChainHeadHeight());

//        // use this setting to improve performance if you know that changes
//        // in content do not change the layout size of the RecyclerView
//        recycler_transactions.setHasFixedSize(true);
//
//        // use a linear layout manager
//        layoutManager = new LinearLayoutManager(this);
//        recycler_transactions.setLayoutManager(layoutManager);
//
//        adapter = new TransactionAdapter(this,module);
//        recycler_transactions.setAdapter(adapter);
//
//
//        swipe_refresh.setOnRefreshListener(this);

//        onRefresh();



        container_tx = root.findViewById(R.id.container_tx);


        UnconfirmedTransactionFragment unconfirmedTransactionFragment = UnconfirmedTransactionFragment.newInstance(module);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(container_tx.getId(), unconfirmedTransactionFragment)
                .commit();


    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected boolean hasDrawer() {
        return false;
    }

    @Override
    public void onRefresh() {
//        swipe_refresh.setRefreshing(true);
//        executor.submit(loadTransactions);
    }
}
