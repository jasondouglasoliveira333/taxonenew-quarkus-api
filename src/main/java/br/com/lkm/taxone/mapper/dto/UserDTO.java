package br.com.lkm.taxone.mapper.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserDTO {
    private Integer id;
    private String name;
    private String password;
    //@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date creationDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
}
