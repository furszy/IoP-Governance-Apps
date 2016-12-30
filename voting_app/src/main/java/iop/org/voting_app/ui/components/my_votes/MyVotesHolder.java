package iop.org.voting_app.ui.components.my_votes;

import android.view.View;
import android.widget.TextView;

import iop.org.furszy_lib.adapter.FermatViewHolder;
import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 23/12/16.
 */

public class MyVotesHolder extends FermatViewHolder {

    TextView txt_title;
    TextView txt_forum_id;
    TextView txt_sub_title;
    TextView txt_categories;
    TextView txt_body;


    public MyVotesHolder(View itemView, int holderType) {
        super(itemView, holderType);

        txt_title = (TextView) itemView.findViewById(R.id.txt_title);
        txt_forum_id = (TextView) itemView.findViewById(R.id.txt_forum_id);
        txt_sub_title = (TextView) itemView.findViewById(R.id.txt_sub_title);
        txt_categories = (TextView) itemView.findViewById(R.id.txt_categories);
        txt_body = (TextView) itemView.findViewById(R.id.txt_body);

    }
}