package de.uni_stuttgart.mci.bluecon.database;

import android.provider.BaseColumns;

/**
 * Created by florian on 03.12.15.
 */
public class BeaconLocationTable {


    public BeaconLocationTable(){

    }

    public static abstract class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";

        public static final String COLUMN_NAME_MACADDRESS = "macaddress";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_SUBCATEGORY = "subcategory";
        public static final String COLUMN_NAME_LABEL = "label";
        public static final String COLUNM_NAME_DESCRIPTION = "description";

        public static final String COLUMN_NAME_NULL = "empty";
    }
}
