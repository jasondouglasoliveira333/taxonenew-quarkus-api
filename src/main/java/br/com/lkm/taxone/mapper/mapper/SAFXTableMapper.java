package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import br.com.lkm.taxone.mapper.dto.SAFXTableDTO;
import br.com.lkm.taxone.mapper.entity.SAFXTable;


@Mapper
public interface SAFXTableMapper{
    
    public SAFXTableDTO[] map(List<SAFXTable> safxTables);
    
    @Mapping(target = "dsTableId", source = "dsTable.id")
    @Mapping(target = "dsTableName", source = "dsTable.name")
    public SAFXTableDTO map(SAFXTable safxTable);
        
}