package com.example.shoeapp.model;

public class Color {
    private int id;
    private String name;
    private String hexcode;
    private String colorId;

    public Color(int id, String name, String hexcode, String colorId) {
        this.id = id;
        this.name = name;
        this.hexcode = hexcode;
        this.colorId = colorId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getHexcode() { return hexcode; }
    public String getColorId() { return colorId; }
}
