package de.uni_stuttgart.mci.bluecon.Util;

/**
 * Created by flori_000 on 26.01.2016.
 */
public interface ITtsProvider {
    void queueRead(String read);

    void queueRead(String... read);
}
