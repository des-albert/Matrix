package com.db;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Summary {
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty value;

    Summary(String name, int value) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleIntegerProperty(value);
    }

    public String getName() {
        return name.get();
    }
    public int getValue() {
        return value.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public void setValue(int value)
    {
        this.value.set(value);
    }
}