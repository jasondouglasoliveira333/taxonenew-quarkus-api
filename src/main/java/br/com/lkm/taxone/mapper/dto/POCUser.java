package br.com.lkm.taxone.mapper.dto;

import java.util.Collection;

public class POCUser { //extends User {
    private static final long serialVersionUID = -7604858853718333944L;
    
    private Integer id;
    private String username;
    private String password;
    private Collection authorities;

    public POCUser(Integer id, String username, String password, Collection authorities) {
        this(username, password, true, true, true, true, authorities);
        this.id = id;
    }
    
    public POCUser(String username, String password, boolean enabled, boolean accountNonExpired,
            boolean credentialsNonExpired, boolean accountNonLocked,
            Collection authorities) {
        //super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername(){
        return username;
    }
    
    @Override
    public String toString() {
        return "POCUser [id=" + id + "]";
    }

    
}
