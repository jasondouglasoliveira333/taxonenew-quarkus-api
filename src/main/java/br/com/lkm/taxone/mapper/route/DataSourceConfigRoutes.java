package br.com.lkm.taxone.mapper.route;

import static org.apache.camel.LoggingLevel.DEBUG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import br.com.lkm.taxone.mapper.dto.DSColumnDTO;
import br.com.lkm.taxone.mapper.dto.DSTableDTO;
import br.com.lkm.taxone.mapper.dto.DataSourceDTO;
import br.com.lkm.taxone.mapper.dto.POCUser;
import br.com.lkm.taxone.mapper.dto.PageResponse;
import br.com.lkm.taxone.mapper.entity.DSColumn;
import br.com.lkm.taxone.mapper.entity.DSTable;
import br.com.lkm.taxone.mapper.entity.DataSourceConfiguration;
import br.com.lkm.taxone.mapper.enums.DataSourceType;
import br.com.lkm.taxone.mapper.mapper.DSColumnMapper;
import br.com.lkm.taxone.mapper.mapper.DSTableMapper;
import br.com.lkm.taxone.mapper.util.DatabaseHelper;
import br.com.lkm.taxone.mapper.util.FTPHelper;
import br.com.lkm.taxone.mapper.util.FileHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.UserTransaction;

@ApplicationScoped
public class DataSourceConfigRoutes extends EndpointRouteBuilder {
    
    public static final POCUser user = new POCUser(1, "WE", "WE", null);
    
    public static final Map<Integer, List<DSTableDTO>> dsTableTemporary = new HashMap<>();
    public static final Map<Integer, List<DSColumnDTO>> dsColumnsTemporary = new HashMap<>();

    @Inject
    private EntityManager entityManager;
    
    @Inject
    private UserTransaction userTransaction;

    
    @Override
    public void configure() throws Exception {

        interceptFrom()
            .to("direct:validateAutentication")
            .to("direct:cors");

        from(platformHttp("/dataSourceConfigs")
            .httpMethodRestrict("GET"))
            .toD("jpa:?query=select d from DataSourceConfiguration d")
            .convertBodyTo(DataSourceDTO[].class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/dataSourceConfigs")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");
            
        from(platformHttp("/dataSourceConfigs/{dataSourceType}")
            .httpMethodRestrict("GET"))
            .toD("jpa:?query=select d from DataSourceConfiguration d where d.dataSourceType = '${headers.dataSourceType}'")
            .setBody(simple("${body[0]}"))
            .convertBodyTo(DataSourceDTO.class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/dataSourceConfigs/{dataSourceType}/dsTables")
            .httpMethodRestrict("GET"))
            .process(e -> {
                POCUser user = DataSourceConfigRoutes.user;
                String dataSourceType = e.getIn().getHeader("dataSourceType", String.class);
                List<DSTableDTO> dsTs = DataSourceConfigRoutes.dsTableTemporary.get(user.getId());
                if (dsTs == null) {
                    userTransaction.begin();
                    TypedQuery<DSTable> query = entityManager.createQuery("select ds from DSTable ds where ds.dataSourceConfiguration.dataSourceType = :dataSourceType", DSTable.class);
                    query.setParameter("dataSourceType", DataSourceType.valueOf(dataSourceType));
                    List<DSTable> dsTables = query.getResultList();
                    dsTs = Arrays.asList(DSTableMapper.INSTANCE.map(dsTables));
                    userTransaction.commit();
                }
                e.getIn().setBody(dsTs);
            })
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> dsTables:${body}");

        from(platformHttp("/dataSourceConfigs/{dataSourceType}/dsTables")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

        from(platformHttp("/dataSourceConfigs/{dataSourceType}/dsTables/{dsTableId}/dsColumns")
            .httpMethodRestrict("GET"))
            .process(e -> {
                String dataSourceType = e.getIn().getHeader("dataSourceType", String.class);
                Integer dsTableId = e.getIn().getHeader("dsTableId", Integer.class);
                Integer page = e.getIn().getHeader("page", 0, Integer.class);
                Integer size = e.getIn().getHeader("size", 10, Integer.class);
                PageResponse<DSColumnDTO> dsCPage = null; 
                List<DSColumnDTO> dscList = DataSourceConfigRoutes.dsColumnsTemporary.get(dsTableId);
                if (dscList != null) {
                    dsCPage = new PageResponse<>();
                    int lastIdx = page * size + size;
                    if (lastIdx > dscList.size()) {
                        lastIdx = dscList.size();  
                    }
                    dsCPage.setContent(dscList.subList(page * size, lastIdx));
                    int totalPages = dscList.size() / size + (dscList.size() % size == 0 ? 0 : 1);
                    System.out.println("totalPages:" + totalPages);
                    dsCPage.setTotalPages(totalPages);
                }else {
                    userTransaction.begin();
                    TypedQuery<Long> countQuery = entityManager.createQuery("select count(dc) from DSColumn dc where dc.dsTable.id = :id", Long.class);
                    countQuery.setParameter("id", dsTableId);
                    Integer totalRegistro = countQuery.getFirstResult();
                    TypedQuery<DSColumn> query = entityManager.createQuery("select dc from DSColumn dc where dc.dsTable.id = :id", DSColumn.class);
                    query.setParameter("id", dsTableId);
                    query.setFirstResult(page * size);
                    query.setMaxResults(size);
                    List<DSColumn> dcEList = query.getResultList();
                    dsCPage = new PageResponse<>();
                    DSColumnDTO[] dsArray = DSColumnMapper.INSTANCE.map(dcEList);
                    dsCPage.setContent(Arrays.asList(dsArray));
                    int totalPages = totalRegistro / size + (totalRegistro % size == 0 ? 0 : 1);
                    dsCPage.setTotalPages(totalPages);
                    userTransaction.commit();
                }
                e.getMessage().setBody(dsCPage);
            })
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> dsTables:${body}");
            
        from(platformHttp("/dataSourceConfigs/{dataSourceType}/dsTables/{dsTableId}/dsColumns")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");


        from(platformHttp("/dataSourceConfigs/{dataSourceType}/metadata")
            .httpMethodRestrict("POST"))
            .convertBodyTo(String.class)
            .unmarshal().json(DataSourceDTO.class)
            .process(e -> {
                String dataSourceType = e.getIn().getHeader("dataSourceType", String.class);
                DataSourceDTO dataSourceDTO = (DataSourceDTO)e.getIn().getBody();
                dataSourceDTO.setDataSourceType(DataSourceType.valueOf(dataSourceType));
                List<DSColumnDTO> dsList = null;
                if (dataSourceDTO.getDataSourceType().equals(DataSourceType.Database)) {
                    dsList = DatabaseHelper.getTableMetadata(dataSourceDTO);
                }else if (dataSourceDTO.getDataSourceType().equals(DataSourceType.TXT)) {
                    dsList = FileHelper.getFileMetadata(dataSourceDTO);
                }else {
                    dsList = FTPHelper.getFileMetadata(dataSourceDTO);
                }
                DataSourceConfigRoutes.clearUserTableAndColumns();
                DataSourceConfigRoutes.loadTableAndColumns(dataSourceDTO.getResourceNames(), dsList);
                e.getIn().setBody("OK");
            })
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> dsTables:${body}");

        from(platformHttp("/dataSourceConfigs/{dataSourceType}/metadata")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

        from(platformHttp("/dataSourceConfigs/{dataSourceType}")
            .httpMethodRestrict("POST"))
            .convertBodyTo(String.class)
            .unmarshal().json(DataSourceConfiguration.class)
            .process(e -> {
                DataSourceConfiguration ds = e.getIn().getBody(DataSourceConfiguration.class);
                var dataSourceType = e.getIn().getHeader("dataSourceType", String.class);
                ds.setDataSourceType(DataSourceType.valueOf(dataSourceType));
            })
            .to("jpa:DataSourceConfiguration?useExecuteUpdate=true")
            .process(e -> {
                int dsId = e.getIn().getBody(DataSourceConfiguration.class).getId();
                if (DataSourceConfigRoutes.dsTableTemporary.get(user.getId()) != null) {
                    userTransaction.begin();
                    DataSourceConfigRoutes.dsTableTemporary.get(user.getId()).forEach(dsTable -> {
                        saveTablesAndColumns(dsId, dsTable, DataSourceConfigRoutes.dsColumnsTemporary.get(dsTable.getId()));
                    });
                    userTransaction.commit();
                }
                DataSourceConfigRoutes.clearUserTableAndColumns();
            })
            .convertBodyTo(DataSourceDTO.class)
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .log(DEBUG, ">>> ${body}");

        from(platformHttp("/dataSourceConfigs/{dataSourceType}")
            .httpMethodRestrict("OPTIONS"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .log(DEBUG, ">>> in Options");

    }
    
    public static void loadTableAndColumns(String tableNames, List<DSColumnDTO> dsList) {
        POCUser user = DataSourceConfigRoutes.user;
        List<String> tables = Arrays.asList(tableNames.split(","));
        tables.stream().forEach(tableName -> {
            List<DSColumnDTO> dsCList = dsList.stream().filter(dsc -> dsc.getDsTable().getName().equals(tableName)).collect(Collectors.toList());
            int pseudoId = (int)(Math.random() * 100000);
            DSTableDTO dstDTO = new DSTableDTO();
            dstDTO.setId(pseudoId);
            dstDTO.setName(tableName);
            List<DSTableDTO> dstList = DataSourceConfigRoutes.dsTableTemporary.get(user.getId());
            if (dstList == null) {
                dstList = new ArrayList<>();
                DataSourceConfigRoutes.dsTableTemporary.put(user.getId(), dstList);
            }
            dstList.add(dstDTO);
            DataSourceConfigRoutes.dsColumnsTemporary.put(pseudoId, dsCList);
        });

    }

    public static void clearUserTableAndColumns() {
        POCUser user = DataSourceConfigRoutes.user;
        if (DataSourceConfigRoutes.dsTableTemporary.get(user.getId()) != null) {
            DataSourceConfigRoutes.dsTableTemporary.get(user.getId()).forEach(dsTable -> {
                DataSourceConfigRoutes.dsColumnsTemporary.remove(dsTable.getId());
            });
            DataSourceConfigRoutes.dsTableTemporary.remove(user.getId());
        }
    }
    
    public void saveTablesAndColumns(Integer dataSourceConfigId, DSTableDTO dsTable, List<DSColumnDTO> dsColumnsList) {
        TypedQuery<DSTable> dtQuery = entityManager.createQuery("select dt from DSTable dt where dt.dataSourceConfiguration.id = :dataSourceConfigId and dt.name = :name", DSTable.class);
        dtQuery.setParameter("dataSourceConfigId", dataSourceConfigId);
        dtQuery.setParameter("name", dsTable.getName());
        List<DSTable> dstList =  dtQuery.getResultList();
        DSTable dst = null;
        //DSTable dst = dsTableRepository.findFirstBydataSourceConfigurationIdAndName(dataSourceConfigId, dsTable.getName());
        if (dstList == null || dstList.size() == 0) {
            dst = new DSTable();
            TypedQuery<DataSourceConfiguration> dscQuery = entityManager.createQuery("select dsc from DataSourceConfiguration dsc where dsc.id = :dataSourceConfigId", DataSourceConfiguration.class);
            dscQuery.setParameter("dataSourceConfigId", dataSourceConfigId);
            DataSourceConfiguration dsc =  dscQuery.getSingleResult();
            dst.setDataSourceConfiguration(dsc);
            dst.setName(dsTable.getName());
            dst = entityManager.merge(dst);
        }else{
            dst = dstList.get(0);
        }
        
        for (DSColumnDTO dsColumnDTO : dsColumnsList)  {
            TypedQuery<DSColumn> dcQuery = entityManager.createQuery("select dc from DSColumn dc where dc.dsTable.id = :dsTableId and dc.name = :name", DSColumn.class);
            dcQuery.setParameter("dsTableId", dst.getId());
            dcQuery.setParameter("name", dsColumnDTO.getName());
            List<DSColumn> dscs =  dcQuery.getResultList();
            //List<DSColumn> dscs = dsColumnRepository.findFirstBydsTableIdAndName(dst.getId(), dsColumnDTO.getName());

            DSColumn dsc = null;
            if (dscs == null || dscs.size() == 0) {
                dsc = DSColumnMapper.INSTANCE.map(dsColumnDTO);
                dsc.setDsTable(dst);
            }else {
                dsc = dscs.get(0);
                dsc.setColumnType(dsColumnDTO.getColumnType());
                dsc.setSize(dsColumnDTO.getSize());
            }
            entityManager.merge(dsc);
        }
    }


}    
