package tw.firemaples.onscreenocr.captureview;

/**
 * Created by firem_000 on 2016/3/6.
 */
public abstract class CaptureView {
    public final static int MODE_SELECTION = 1;

    public final static int MODE_RESULT = 2;

    public abstract void showView();

    public abstract void hideView();

    public abstract void setProgressMode(boolean progress, String message);

    /**
     * use {@link CaptureView#MODE_SELECTION}, {@link CaptureView#MODE_RESULT}
     *
     * @param mode
     */
    public abstract void onModeChange(int mode);
}
