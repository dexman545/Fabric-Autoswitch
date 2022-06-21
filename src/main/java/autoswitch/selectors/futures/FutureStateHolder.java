package autoswitch.selectors.futures;

public abstract class FutureStateHolder {
    protected FutureState state = FutureState.AWAITING_VALIDATION;

    public boolean isValid() {
        return state == FutureState.VALID;
    }

    public FutureState getState() {
        return state;
    }

    public void setState(FutureState state) {
        this.state = state;
    }

    public void resetState() {
        state = FutureState.AWAITING_VALIDATION;
    }

    public abstract void validateEntry(boolean force);

    public void validateEntry() {
        validateEntry(false);
    }

    public enum FutureState {
        AWAITING_VALIDATION,
        VALID,
        INVALID;
    }
}
