package br.com.lkm.taxone.mapper.dto;

import java.util.Date;

import br.com.lkm.taxone.mapper.enums.ScheduleLogStatus;

public class ScheduleLogDTO {
    private Integer id;
    private String scheduleName;
    private Date executionDate;
    private ScheduleLogStatus status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public ScheduleLogStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleLogStatus status) {
        this.status = status;
    }
    
}
