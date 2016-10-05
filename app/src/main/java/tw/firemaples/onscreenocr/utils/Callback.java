package tw.firemaples.onscreenocr.utils;

/**
 * Created by louis1chen on 04/10/2016.
 */

public interface Callback<T> {
    boolean onCallback(T result);
}
