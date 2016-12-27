package iop.org.furszy_lib.nav_view;

/**
 * Created by mati on 18/11/16.
 */
public class NavMenuItem {

    private int id;
    private boolean isClicked;
    private String text;
    private int icResOff;
    private int icResOn;

    public NavMenuItem(int id, boolean isClicked, String text, int icResOff) {
        this.id = id;
        this.isClicked = isClicked;
        this.text = text;
        this.icResOff = icResOff;
    }

    public NavMenuItem(int id, boolean isClicked, String text, int icResOff, int icResOn) {
        this.id = id;
        this.isClicked = isClicked;
        this.text = text;
        this.icResOff = icResOff;
        this.icResOn = icResOn;
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

    public int getIcResOff() {
        return icResOff;
    }

    public int getIcResOn() {
        return icResOn;
    }
}
