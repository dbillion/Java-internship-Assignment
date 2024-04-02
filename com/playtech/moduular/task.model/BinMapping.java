package com.playtech.assignment.moduular.task.model;

public class BinMapping {
    private String name;
    private long rangeFrom;
    private long rangeTo;
    private Type type; // Use the enum for type
    private String country;

    // Enum for type
    public enum Type {
        DC, CC;
    }

    // Constructor
    public BinMapping(String name, long rangeFrom, long rangeTo, Type type, String country) {
        this.name = name;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
        this.type = type;
        this.country = country;
    }

    // Getters and Setters

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRangeFrom() {
        return this.rangeFrom;
    }

    public void setRangeFrom(long rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    public long getRangeTo() {
        return this.rangeTo;
    }

    public void setRangeTo(long rangeTo) {
        this.rangeTo = rangeTo;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // ...



    @Override
    public String toString() {
        return "Bin{" +
                "name='" + name + '\'' +
                ", rangeFrom=" + rangeFrom +
                ", rangeTo=" + rangeTo +
                ", type=" + type + // Use the enum's name() method for string representation
                ", country='" + country + '\'' +
                '}';
    }
}
