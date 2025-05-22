package br.com.lkm.taxone.mapper.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.lkm.taxone.mapper.dto.TaxOneApiDTO;
import br.com.lkm.taxone.mapper.entity.TaxOneApi;


@Mapper
public interface TaxOneApiMapper{
    
    static TaxOneApiMapper INSTANCE = Mappers.getMapper(TaxOneApiMapper.class);
    
    public TaxOneApiDTO map(TaxOneApi taxOneApi);
    public TaxOneApi map(TaxOneApiDTO taxOneApi);
        
}