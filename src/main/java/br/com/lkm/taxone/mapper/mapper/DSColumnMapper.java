package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.lkm.taxone.mapper.dto.DSColumnDTO;
import br.com.lkm.taxone.mapper.entity.DSColumn;


@Mapper
public interface DSColumnMapper{
    
    static DSColumnMapper INSTANCE = Mappers.getMapper(DSColumnMapper.class);
    
    public DSColumnDTO[] map(List<DSColumn> dsColumns);
    
    public DSColumnDTO map(DSColumn DSColumn);
        
    public DSColumn map(DSColumnDTO dsDTO);

}