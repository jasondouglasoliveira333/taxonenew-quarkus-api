package br.com.lkm.taxone.mapper.entity;

import br.com.lkm.taxone.mapper.enums.ColumnType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class SAFXColumn {

    @Id
    @GeneratedValue
    private Integer id;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private ColumnType columnType;
    
    private Boolean required;
    
    private Integer position; 
    
    private Integer size;
    
    @ManyToOne
    private SAFXTable safxTable;
    
    @ManyToOne
    @JoinColumn(insertable=false, updatable=false)
    private DSColumn dsColumn;

    @Column(name="DSCOLUMN_ID", insertable=true, updatable=true)
    private Integer dsColumnId;

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

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public SAFXTable getSafxTable() {
        return safxTable;
    }

    public void setSafxTable(SAFXTable safxTable) {
        this.safxTable = safxTable;
    }

    public DSColumn getDsColumn() {
        return dsColumn;
    }

    public void setDsColumn(DSColumn dsColumn) {
        this.dsColumn = dsColumn;
    }

    public Integer getDsColumnId() {
        return dsColumnId;
    }

    public void setDsColumnId(Integer dsColumnId) {
        this.dsColumnId = dsColumnId;
    }

}
