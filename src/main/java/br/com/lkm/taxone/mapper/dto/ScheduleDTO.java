package br.com.lkm.taxone.mapper.dto;

import java.util.List;

import br.com.lkm.taxone.mapper.enums.ScheduleStatus;

public class ScheduleDTO {
    private Integer id;
    private String name;
    private String days;
    private String hours;
    private List<SAFXTableDTO> safxTables;
    private List<CriteriaDTO> criterias;
    private String userName;
    private ScheduleStatus status;
    
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
    public String getDays() {
        return days;
    }
    public void setDays(String days) {
        this.days = days;
    }
    public String getHours() {
        return hours;
    }
    public void setHours(String hours) {
        this.hours = hours;
    }
    public List<SAFXTableDTO> getSafxTables() {
        return safxTables;
    }
    public void setSafxTables(List<SAFXTableDTO> safxTables) {
        this.safxTables = safxTables;
    }
    public List<CriteriaDTO> getCriterias() {
        return criterias;
    }
    public void setCriterias(List<CriteriaDTO> criterias) {
        this.criterias = criterias;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public ScheduleStatus getStatus() {
        return status;
    }
    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

}
