package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import br.com.lkm.taxone.mapper.dto.UploadDTO;
import br.com.lkm.taxone.mapper.entity.Upload;


@Mapper
public interface UploadMapper{
    
    public UploadDTO[] map(List<Upload> uploads);
    
    @Mapping(target = "userName", source = "user.name")
    public UploadDTO map(Upload upload);
        
}