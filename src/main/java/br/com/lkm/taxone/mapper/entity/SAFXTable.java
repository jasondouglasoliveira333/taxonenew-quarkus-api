package br.com.lkm.taxone.mapper.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class SAFXTable {
    
    @Id
    @GeneratedValue
    private Integer id;
    
    private String name;
    
    private String description;
    
    @OneToMany(mappedBy = "safxTable")
    private List<SAFXColumn> safxColumns;
    
    @ManyToOne
    @JoinColumn(insertable=false, updatable=false)
    private DSTable dsTable;

    @Column(name="DSTABLE_ID", insertable=true, updatable=true)
    private Integer dsTableId;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SAFXColumn> getSafxColumns() {
        return safxColumns;
    }

    public void setSafxColumns(List<SAFXColumn> safxColumns) {
        this.safxColumns = safxColumns;
    }

    public DSTable getDsTable() {
        return dsTable;
    }

    public void setDsTable(DSTable dsTable) {
        this.dsTable = dsTable;
    }
    
    public Integer getDsTableId() {
        return dsTableId;
    }

    public void setDsTableId(Integer dsTableId) {
        this.dsTableId = dsTableId;
    }

    
}
