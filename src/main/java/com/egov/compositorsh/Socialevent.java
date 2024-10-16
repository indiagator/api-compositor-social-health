package com.egov.compositorsh;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.UUID;

public class Socialevent implements Serializable
{

    private UUID id;

    private UUID citizenid;

    private String socialeventtype;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCitizenid() {
        return citizenid;
    }

    public void setCitizenid(UUID citizenid) {
        this.citizenid = citizenid;
    }

    public String getSocialeventtype() {
        return socialeventtype;
    }

    public void setSocialeventtype(String socialeventtype) {
        this.socialeventtype = socialeventtype;
    }

    @Override
    public String toString() {
        return "Socialevent{" +
                "id=" + id +
                ", citizenid=" + citizenid +
                ", socialeventtype='" + socialeventtype + '\'' +
                '}';
    }
}