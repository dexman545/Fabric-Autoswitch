package autoswitch;

//Class to store data for use of switchback feature
@SuppressWarnings("WeakerAccess")
public class SwitchDataStorage {

    private int prevSlot;
    private boolean hasSwitched;

    public SwitchDataStorage() {
        prevSlot = -1;
        hasSwitched = false;
    }

    public boolean getHasSwitched() {
        return hasSwitched;
    }

    public void setHasSwitched(boolean hasSwitched) {
        this.hasSwitched = hasSwitched;
    }

    public int getPrevSlot() {
        return prevSlot;
    }

    public void setPrevSlot(int prevSlot) {
        this.prevSlot = prevSlot;
    }
}
