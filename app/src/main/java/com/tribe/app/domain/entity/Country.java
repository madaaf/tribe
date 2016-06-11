package com.tribe.app.domain.entity;

import java.text.Normalizer;

public class Country implements Comparable<Country> {

    public String code;
    public String name;

    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public int compareTo(Country other) {
        if (other == null) return 1;

        //remove accent
        String name1 = Normalizer.normalize(this.name, Normalizer.Form.NFD);
        name1 = name1.replaceAll("[^\\p{ASCII}]", "");
        String name2 = Normalizer.normalize(other.name, Normalizer.Form.NFD);
        name2 = name2.replaceAll("[^\\p{ASCII}]", "");

        return name1.compareTo(name2);
    }
}