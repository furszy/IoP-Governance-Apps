package iop.org.iop_contributors_app.furszy_sdk.android.nav_view;

/**
 * Created by mati on 18/11/16.
 */
public class NavMenuItem {

    private int id;
    private boolean isClicked;
    private String text;
    private int icRes;

    public NavMenuItem(int id, boolean isClicked, String text, int icRes) {
        this.id = id;
        this.isClicked = isClicked;
        this.text = text;
        this.icRes = icRes;
    }

    public void setClicked(boolean clicked) {
        isClicked = clicked;
    }

    public int getId() {
        return id;
    }

    public boolean isClicked() {
        return isClicked;
    }

    public String getText() {
        return text;
    }

    public int getIcRes() {
        return icRes;
    }
}
