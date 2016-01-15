// IBeacon.aidl
package de.uni_stuttgart.mci.bluecon;

// Declare any non-default types here with import statements

interface IBeacon {
    int getCount();
    String getName();
    List getList();
    void setPeriod();
}
