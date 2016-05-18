package com.tribe.app.domain.entity;

import java.util.Date;

public class MarvelCharacter {

    private int id;
    private String name;
    private String description;

    public MarvelCharacter(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public MarvelCharacter(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
