package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import br.com.lkm.taxone.mapper.dto.ScheduleDTO;
import br.com.lkm.taxone.mapper.dto.PeriodeDTO;
import br.com.lkm.taxone.mapper.entity.Schedule;


@Mapper
public interface ScheduleMapper{
    
    public ScheduleDTO[] map(List<Schedule> schedules);
    
    @Mapping(target = "userName", source = "user.name")
    public ScheduleDTO map(Schedule schedule);
        
    public PeriodeDTO mapPeriode(Schedule Schedule);
    
    public Schedule map(ScheduleDTO scheduleDTO);

}