package com.example.scarlet;

public class Folder {

    private String name;
    private String path;

    Folder(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() { return name; }

    public String getPath() { return path; }
}
