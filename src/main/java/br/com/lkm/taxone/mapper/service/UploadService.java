package br.com.lkm.taxone.mapper.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import br.com.lkm.taxone.mapper.dto.POCUser;
import br.com.lkm.taxone.mapper.dto.PageResponse;
import br.com.lkm.taxone.mapper.dto.UploadDTO;
import br.com.lkm.taxone.mapper.entity.SAFXColumn;
import br.com.lkm.taxone.mapper.entity.SAFXTable;
import br.com.lkm.taxone.mapper.entity.Upload;
import br.com.lkm.taxone.mapper.entity.User;
import br.com.lkm.taxone.mapper.enums.ColumnType;
import br.com.lkm.taxone.mapper.enums.UploadStatus;
import br.com.lkm.taxone.mapper.util.JExcelHelper;
import br.com.lkm.taxone.mapper.util.XLSField;
import br.com.lkm.taxone.mapper.util.XLSTable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;



@ApplicationScoped
public class UploadService {
	
    @Inject
    private EntityManager entityManager;
    
    @Inject
    private UserTransaction userTransaction;
	
	public static void main(String[] args) throws IOException, Exception {
		String file = "C:\\jason\\generic_workspace_new\\quarkus\\Manual_Layout_MastersafDW_down new.xls";
		new UploadService().parseFileAndStore("Manual_Layout_MastersafDW.xls", "256.1.0", Files.readAllBytes(Paths.get(file)), null);
	}
	
	public void parseFileAndStore(String fileName, String layoutVersion, byte[] data, User user) throws Exception {
		List<XLSTable> xlsTables = JExcelHelper.readSAFXINfo(data);
		
		userTransaction.begin();
		xlsTables.stream().forEach(table -> {
			//Verify if table exists
			System.out.println("tName:" + table.getName());// + " - desc:" + table.getDescription() + " - fields:" + table.getFields());
	
			TypedQuery<SAFXTable> tQuery = entityManager.createQuery("select st from SAFXTable st where st.name = :name", SAFXTable.class);
			tQuery.setParameter("name", table.getName());
			List<SAFXTable> stlist = tQuery.getResultList();
			SAFXTable t = null;
			boolean newTable = false;
			if (stlist.size() > 0) {
				t = stlist.get(0);
			}else{
				newTable = true;
				t = new SAFXTable();
			}
			t.setName(table.getName());
			t.setDescription(table.getDescription());
			t = entityManager.merge(t);
				
			for (XLSField field : table.getFields()) {
				SAFXColumn c = null;
				if (newTable) {
					c = new SAFXColumn();
				}else {
					TypedQuery<SAFXColumn> cQuery = entityManager.createQuery("select sc from SAFXColumn sc where sc.safxTable.id = :safxTableId and sc.name = :name", SAFXColumn.class);
					cQuery.setParameter("safxTableId", t.getId());
					cQuery.setParameter("name", field.getColumnName());
					List<SAFXColumn> sclist = cQuery.getResultList();
					if (sclist.size() > 0){
						c = sclist.get(0);
					}else{
						c = new SAFXColumn();
					}
				}
				c.setName(field.getColumnName());
				c.setColumnType(getColumnType(field));
				c.setRequired(field.getRequired());
				try {
					c.setSize(Integer.parseInt(field.getSize()));
				}catch (Exception e) {
					c.setSize(3);
				}
				c.setPosition(field.getIndex());
				c.setSafxTable(t);
				entityManager.merge(c);
			}
		});

		Query uQuery = entityManager.createQuery("update Upload u set u.status = :status");
		uQuery.setParameter("status", UploadStatus.CANCELED);
		uQuery.executeUpdate();
		
		Upload u = new Upload();
		u.setFileName(fileName);
		u.setLayoutVersion(layoutVersion);
		u.setCreationDate(new Date());
		u.setStatus(UploadStatus.ACTIVE);
		u.setUser(user);
		entityManager.merge(u);
		userTransaction.commit();
	}

	private ColumnType getColumnType(XLSField field) {
		if (field.getType().equals("A")){
			return ColumnType.VARCHAR;
		}else if(field.getColumnName().startsWith("DT_") || field.getColumnName().startsWith("DAT_") 
				|| field.getColumnName().startsWith("DATA_") || field.getDescription().contains("Data")) {
			return ColumnType.DATETIME;
		}else {
			return ColumnType.NUMERIC;
		}
	}
	
}
