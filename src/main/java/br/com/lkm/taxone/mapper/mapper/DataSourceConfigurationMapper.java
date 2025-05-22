package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.lkm.taxone.mapper.dto.DataSourceDTO;
import br.com.lkm.taxone.mapper.entity.DataSourceConfiguration;


@Mapper
public interface DataSourceConfigurationMapper{
    
    public DataSourceDTO[] map(List<DataSourceConfiguration> dataSources);
    
    public DataSourceDTO map(DataSourceConfiguration dataSource);
        
    public DataSourceConfiguration map(DataSourceDTO dataSource);

}