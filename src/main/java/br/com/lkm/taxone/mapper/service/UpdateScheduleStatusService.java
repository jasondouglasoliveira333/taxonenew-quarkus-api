package br.com.lkm.taxone.mapper.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.lkm.taxone.mapper.entity.IntegrationStatus;
import br.com.lkm.taxone.mapper.entity.ScheduleLog;
import br.com.lkm.taxone.mapper.entity.ScheduleLogIntergrationError;
import br.com.lkm.taxone.mapper.entity.TaxOneApi;
import br.com.lkm.taxone.mapper.enums.ScheduleLogStatus;
import br.com.lkm.taxone.mapper.integration.OncoClinicasTaxtOneService;
import br.com.lkm.taxone.mapper.integration.OncoClinicasTaxtOneServiceBuilder;
import br.com.lkm.taxone.mapper.integration.dto.ConsultaLoteDTO;
import br.com.lkm.taxone.mapper.integration.dto.ConsultaLoteImportacaoDTO;
import br.com.lkm.taxone.mapper.integration.dto.ImportarDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UpdateScheduleStatusService {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Inject
    private OncoClinicasTaxtOneServiceBuilder oncoIntegrationBuilder; 
    
    @Transactional
    public void process() {
        List<ScheduleLog> ss = null;//scheduleLogRepository.findByStatus(ScheduleLogStatus.SENT);
        if (ss.size() > 0) {
            try {
                OncoClinicasTaxtOneService oncoIntegrationAuthService = oncoIntegrationBuilder.createService(null);
                log.info("Autenticando no api de integracao OncoClinicas");
                TaxOneApi taxOneApi = null;//taxOneApiRepository.getOne(1);
                String token = oncoIntegrationAuthService.authentication(taxOneApi.getUsername(), taxOneApi.getPassword()).execute().body().string();
                
                OncoClinicasTaxtOneService oncoIntegrationService = oncoIntegrationBuilder.createService(token);
                for (ScheduleLog s : ss) {
                    log.info("Iniciando o processamento do agendamento:" + s.getSchedule().getName() + " - created:" + s.getExecutionDate());
                    String numLote = s.getNumLote();
                    if (s.getIntegrationStatus().equals(IntegrationStatus.ENVIADO)) {
                        ConsultaLoteDTO cl = oncoIntegrationService.consultaLote(numLote).execute().body();
                        log.info("cl:" + cl);
                        AtomicBoolean loadedWithError = new AtomicBoolean();
                        cl.getProtocolos().stream().forEach(protocolo -> {
                            if (protocolo.getErros() != null) {
                                protocolo.getErros().stream().forEach(erro -> {
                                    ScheduleLogIntergrationError slie = new ScheduleLogIntergrationError();
                                    slie.setNumeroReg(erro.getNum_reg());
                                    slie.setCodigoErro(erro.getCod_erro());
                                    slie.setDescricaoErro(erro.getDescricao_erro());
                                    slie.setNomeCampo(erro.getNom_campo());
                                    slie.setChaveRegistro(erro.getChave_registro());
                                    slie.setScheduleLog(s);
                                    //scheduleLogIntergrationErrorRepository.save(slie);
                                    loadedWithError.set(true);
                                });
                            }
                        });
                        if (!loadedWithError.get()) {
                            s.setIntegrationStatus(IntegrationStatus.CARREGADO);
                        }else {
                            s.setStatus(ScheduleLogStatus.ERROR_TAXONE);
                            s.setIntegrationStatus(IntegrationStatus.CARREGADO_COM_ERRO);
                        }
                    }else if (s.getIntegrationStatus().equals(IntegrationStatus.CARREGADO)) {
                        ImportarDTO importarResponse = oncoIntegrationService.importar(numLote).execute().body();
                        log.info("importarResponse:" + importarResponse);
                        s.setIntegrationStatus(IntegrationStatus.EXECUTADO_IMPORTAR);
                    }else  if (s.getIntegrationStatus().equals(IntegrationStatus.EXECUTADO_IMPORTAR)) {
                        ConsultaLoteImportacaoDTO consultaLoteImportacao = oncoIntegrationService.consultaLoteImportacao(numLote).execute().body();
                        log.info("consultaLoteImportacao:" + consultaLoteImportacao);
                        AtomicBoolean loadedWithError = new AtomicBoolean();
                        consultaLoteImportacao.getCargas().stream().forEach(carga -> {
                            if (carga.getErros() != null) {
                                carga.getErros().stream().forEach(erro -> {
                                    ScheduleLogIntergrationError slie = new ScheduleLogIntergrationError();
                                    slie.setNumeroReg(erro.getNum_reg());
                                    slie.setCodigoErro(erro.getCod_erro());
                                    slie.setDescricaoErro(erro.getDescricao_erro());
                                    slie.setNomeCampo(erro.getNom_campo());
                                    slie.setChaveRegistro(erro.getChave_registro());
                                    slie.setScheduleLog(s);
                                    //scheduleLogIntergrationErrorRepository.save(slie);
                                    loadedWithError.set(true);
                                });
                            }
                        });
                        if (!loadedWithError.get()) {
                            if (consultaLoteImportacao.getImportacoes() != null) { //just change the state when the load has been done
                                s.setStatus(ScheduleLogStatus.PROCESSED);
                                s.setIntegrationStatus(IntegrationStatus.FINALIZADO);
                            }
                        }else {
                            s.setStatus(ScheduleLogStatus.ERROR_TAXONE);
                            s.setIntegrationStatus(IntegrationStatus.CARREGADO_COM_ERRO);
                        }
                    }
                    //scheduleLogRepository.save(s);
                    log.info("Finalizando o processamento do agendamento:" + s.getSchedule().getName());
                }
            }catch (Exception e) {
                log.error("Erro executando os agendamentos:", e);
            }
        }
    }

}
