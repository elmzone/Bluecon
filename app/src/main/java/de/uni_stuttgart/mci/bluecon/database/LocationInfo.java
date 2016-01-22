package de.uni_stuttgart.mci.bluecon.database;

public class LocationInfo {
    public String macAddress = "macaddress";
    public String category = "category";
    public String subcategory = "subcategory";
    public String label = "label";
    public String description = "description";

    public LocationInfo (String macaddress, String category, String subcategory, String label, String description) {
        this.macAddress = macaddress;
        this.category = category;
        this.subcategory = subcategory;
        this.label = label;
        this.description = description;
    }

    public String toString() {
        return "category: " + category + "; subcategory: " + subcategory + "; label: " + label + "; description: " + description;
    }
}
