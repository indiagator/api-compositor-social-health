package com.egov.socialservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "socials")
public class Social {
    @Id
    @Size(max = 50)
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Size(max = 50)
    @Column(name = "type", length = 50)
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}