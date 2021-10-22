package tw.firemaples.onscreenocr.utils;

/**
 * Created by firemaples on 04/10/2016.
 */

public interface Callback<T> {
    boolean onCallback(T result);
}
