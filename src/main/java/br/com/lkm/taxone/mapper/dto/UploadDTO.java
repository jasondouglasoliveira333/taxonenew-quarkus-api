package br.com.lkm.taxone.mapper.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.lkm.taxone.mapper.enums.UploadStatus;

public class UploadDTO {

    private Long id;
    
    private String fileName;
    
    private String layoutVersion;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date creationDate;
    
    private UploadStatus status;
    
    private String userName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLayoutVersion() {
        return layoutVersion;
    }

    public void setLayoutVersion(String layoutVersion) {
        this.layoutVersion = layoutVersion;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
}
