package com.egov.socialservice;

import java.util.UUID;

public class SocialEvent1 {

    String service_name;
    String type;
    UUID userid;

    public void setType(String type) {
        this.type = type;
    }

    public void setUsername(UUID username) {
        this.userid = username;
    }

    public String getType() {
        return type;
    }

    public UUID getUsername() {
        return userid;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }
}
