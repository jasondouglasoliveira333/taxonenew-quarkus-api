package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.lkm.taxone.mapper.dto.SAFXColumnDTO;
import br.com.lkm.taxone.mapper.entity.SAFXColumn;


@Mapper
public interface SAFXColumnMapper{
    
    public SAFXColumnDTO[] map(List<SAFXColumn> safxColumns);
    
    @Mapping(target = "dsColumnId", source = "dsColumn.id")
    @Mapping(target = "dsColumnName", source = "dsColumn.name")
    public SAFXColumnDTO map(SAFXColumn safxColumn);
        
}