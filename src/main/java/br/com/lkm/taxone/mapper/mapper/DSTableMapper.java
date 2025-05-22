package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.lkm.taxone.mapper.dto.DSTableDTO;
import br.com.lkm.taxone.mapper.entity.DSTable;


@Mapper
public interface DSTableMapper{
    
    DSTableMapper INSTANCE = Mappers.getMapper(DSTableMapper.class);
    
    public DSTableDTO[] map(List<DSTable> dsTables);
    
    public DSTableDTO map(DSTable DSTable);
        
}