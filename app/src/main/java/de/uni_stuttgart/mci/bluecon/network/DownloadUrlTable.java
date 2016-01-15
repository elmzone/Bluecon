package de.uni_stuttgart.mci.bluecon.network;

import android.provider.BaseColumns;

/**
 * Created by florian on 03.12.15.
 */
public class DownloadUrlTable {

    public DownloadUrlTable() {

    }

    public static abstract class URLEntry implements BaseColumns {
        public static final String TABLE_NAME = "url";

        public static final String COLUNM_NAME_URL = "urlname";

        public static final String COLUMN_NAME_NULL = "empty";

    }
}
