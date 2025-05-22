package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import br.com.lkm.taxone.mapper.dto.ScheduleLogDTO;
import br.com.lkm.taxone.mapper.dto.ScheduleLogIntergrationErrorDTO;
import br.com.lkm.taxone.mapper.entity.ScheduleLog;
import br.com.lkm.taxone.mapper.entity.ScheduleLogIntergrationError;


@Mapper
public interface ScheduleLogMapper{
    
    public ScheduleLogDTO[] map(List<ScheduleLog> ScheduleLogs);
    
    @Mapping(target = "scheduleName", source = "schedule.name")
    public ScheduleLogDTO map(ScheduleLog ScheduleLog);
        
    public ScheduleLog map(ScheduleLogDTO ScheduleLogDTO);

    public ScheduleLogIntergrationErrorDTO[] mapTaxError(List<ScheduleLogIntergrationError> slie);
}