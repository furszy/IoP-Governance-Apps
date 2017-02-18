package iop.org.iop_contributors_app.ui.settings.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bitcoinj.core.Peer;

import java.util.List;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;

/**
 * Created by mati on 16/02/17.
 */

public class NetworkInfoActivity extends BaseActivity {


    private View root;
    private TextView txt_block_number;
    private TextView txt_node;

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {
        super.onCreateView(container, savedInstance);

        root = getLayoutInflater().inflate(R.layout.dev_settings_main,container);
        txt_block_number = (TextView) root.findViewById(R.id.txt_block_number);
        txt_node = (TextView) root.findViewById(R.id.txt_node);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateInfo();

    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected boolean hasDrawer() {
        return false;
    }

    private void updateInfo(){
        txt_block_number.setText("Blockchain height: "+module.getBlockchainManager().getChainHeadHeight());
        List<Peer> list = module.getBlockchainManager().getConnectedPeers();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Nodes connected: ");
        if (list.isEmpty()){
            stringBuilder.append("null");
        }else {
            for (Peer peer : list) {
                stringBuilder.append(peer.getAddress().toString()).append(" , ");
            }
        }
        txt_node.setText(stringBuilder.toString());

    }

}
