package iop.org.iop_contributors_app.ui.components;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import iop.org.furszy_lib.adapter.FermatViewHolder;
import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 17/11/16.
 */

public class ProposalsHolder extends FermatViewHolder {

    TextView txt_title;
    TextView txt_forum_id;
    TextView txt_sub_title;
    TextView txt_categories;
    TextView txt_body;
    Button btn_read_more;
    TextView txt_start_block;
    TextView txt_end_block;
    TextView txt_total_amount;
    TextView txt_go_forum;
    TextView txt_state;



    public ProposalsHolder(View itemView, int holderType) {
        super(itemView,holderType);

        txt_title = (TextView) itemView.findViewById(R.id.txt_title);
        txt_forum_id = (TextView) itemView.findViewById(R.id.txt_forum_id);
        txt_sub_title = (TextView) itemView.findViewById(R.id.txt_sub_title);
        txt_categories = (TextView) itemView.findViewById(R.id.txt_categories);
        txt_body = (TextView) itemView.findViewById(R.id.txt_body);
        btn_read_more = (Button) itemView.findViewById(R.id.btn_read_more);
        txt_start_block = (TextView) itemView.findViewById(R.id.txt_start_block);
        txt_end_block = (TextView) itemView.findViewById(R.id.txt_end_block);
        txt_total_amount = (TextView) itemView.findViewById(R.id.txt_total_amount);
        txt_go_forum = (TextView) itemView.findViewById(R.id.txt_go_forum);
        txt_state = (TextView) itemView.findViewById(R.id.txt_state);


    }
}
