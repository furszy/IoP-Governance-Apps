package iop.org.voting_app.ui.components.my_votes;

import android.view.View;
import android.widget.TextView;

import iop.org.furszy_lib.adapter.FermatViewHolder;
import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 23/12/16.
 */

public class MyVotesHolder extends FermatViewHolder {

    public View card_view;
    TextView txt_title;
    TextView txt_forum_id;
    TextView txt_sub_title;
    TextView txt_categories;
    TextView txt_body;
    TextView txt_forum;
    TextView txt_read_more;
    View view_proposal_state;
    TextView txt_state;


    public MyVotesHolder(View itemView, int holderType) {
        super(itemView, holderType);

        card_view = itemView.findViewById(R.id.card_view);
        txt_title = (TextView) itemView.findViewById(R.id.txt_title);
        txt_forum_id = (TextView) itemView.findViewById(R.id.txt_forum_id);
        txt_sub_title = (TextView) itemView.findViewById(R.id.txt_sub_title);
        txt_categories = (TextView) itemView.findViewById(R.id.txt_categories);
        txt_body = (TextView) itemView.findViewById(R.id.txt_body);
        txt_forum = (TextView) itemView.findViewById(R.id.txt_forum);
        txt_read_more = (TextView) itemView.findViewById(R.id.txt_read_more);
        view_proposal_state = itemView.findViewById(R.id.view_proposal_state);
        txt_state = (TextView) itemView.findViewById(R.id.txt_state);

    }
}