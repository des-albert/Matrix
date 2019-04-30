package com.db;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class AssemblyInfo {
    private final SimpleStringProperty AssemblyNameColumn;
    private final SimpleIntegerProperty AssemblyMaxColumn;
    private final SimpleStringProperty AssemblyTypeColumn;
    private final SimpleIntegerProperty AssemblyQtyColumn;

    AssemblyInfo(String name, int max, String type, int qty) {
        this.AssemblyNameColumn = new SimpleStringProperty(name);
        this.AssemblyMaxColumn = new SimpleIntegerProperty(max);
        this.AssemblyTypeColumn = new SimpleStringProperty(type);
        this.AssemblyQtyColumn =  new SimpleIntegerProperty(qty);
    }

    public String getAssemblyNameColumn () {
        return AssemblyNameColumn.get();
    }
    public int getAssemblyMaxColumn() {
        return AssemblyMaxColumn.get();
    }
    public String getAssemblyTypeColumn () { return AssemblyTypeColumn.get(); }
    public int getAssemblyQtyColumn () {
        return AssemblyQtyColumn.get();
    }
}
