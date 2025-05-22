package br.com.lkm.taxone.mapper.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.lkm.taxone.mapper.dto.EmailDTO;
import br.com.lkm.taxone.mapper.entity.Email;


@Mapper
public interface EmailMapper{
    
    public EmailDTO[] map(List<Email> emails);
    
    public EmailDTO map(Email email);
        
}