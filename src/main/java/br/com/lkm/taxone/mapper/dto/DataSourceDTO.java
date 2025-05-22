package br.com.lkm.taxone.mapper.dto;

import br.com.lkm.taxone.mapper.enums.DataSourceType;

public class DataSourceDTO {
    private Integer id;
    private String url;
    private String username;
    private String password;
    private String resourceNames;
    private DataSourceType dataSourceType;
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getResourceNames() {
        return resourceNames;
    }
    public void setResourceNames(String resourceNames) {
        this.resourceNames = resourceNames;
    }
    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }
    public void setDataSourceType(DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    } 

}
