package com.example.majorprojectticketbookingsystem;

import java.io.Serializable;

public class Theatre implements Serializable {
    private String id;
    private String name;
    private String location;
    private String totalHalls;

    public Theatre(String id, String name, String location, String totalHalls) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.totalHalls = totalHalls;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getTotalHalls() {
        return totalHalls;
    }
}
