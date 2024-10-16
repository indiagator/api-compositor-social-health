package com.egov.compositorsh;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.UUID;


public class Btypedetail implements Serializable
{

    private UUID id;

    private String btype;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBtype() {
        return btype;
    }

    public void setBtype(String btype) {
        this.btype = btype;
    }

    @Override
    public String toString() {
        return "Btypedetail{" +
                "id=" + id +
                ", btype='" + btype + '\'' +
                '}';
    }
}