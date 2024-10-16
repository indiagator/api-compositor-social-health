package com.egov.compositorsh;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;


@Getter @Setter
public class CitizenBtypeSocialView implements Serializable
{

    UUID citizenid;
    String btype;
    Socialevent socialevent;

    @Override
    public String toString()
    {
        return "CitizenBtypeSocialView{" +
                "citizenid=" + citizenid +
                ", btype='" + btype + '\'' +
                ", socialevent=" + socialevent +
                '}';
    }

}
