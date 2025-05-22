package br.com.lkm.taxone.mapper.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

public class JExcelHelper {
	
	private static List<String> tableNames = new ArrayList<>();
	
	private static final String LAYOUT_ARQUIVOS = "LAYOUT DOS ARQUIVOS";

	public static void main(String[] args) throws Exception {
		String file = "C:\\jason\\generic_workspace_new\\quarkus\\Manual_Layout_MastersafDW_down new.xls";
		List<XLSTable> xfs =  readSAFXINfo(Files.readAllBytes(Paths.get(file)));
		xfs.stream().forEach(xf -> {
			System.out.println(xf);
		});
	}

    public static  List<XLSTable> readSAFXINfo(byte[] data) throws IOException, BiffException {
    	List<XLSTable> tables = new ArrayList<>();

    	WorkbookSettings ws = new WorkbookSettings();
    	ws.setEncoding("Cp1252");
        Workbook workbook = Workbook.getWorkbook(new ByteArrayInputStream(data), ws);

        //
        Sheet sheet = workbook.getSheet(4);
        System.out.println("sheeet.getName():" + sheet.getName());
        int rows = sheet.getRows();
        System.out.println("rows:" + rows);
        int firstRow = 0;
        for (int i = 0; i < rows; i++) {
        	System.out.println("value:" + sheet.getCell(0, i).getContents());
        	if (sheet.getCell(0, i).getContents().startsWith("SAFX")) {
        		firstRow = i+1;
        		break;
        	}
        }
        for (int i = firstRow; i < rows; i++) {
        	try {
        		String tableName = sheet.getCell(0, i).getContents();
        		if (tableNames.contains(tableName)) {
        			continue;
        		}
        		tableNames.add(tableName);
	        	XLSTable xt = new XLSTable();
	        	xt .setName(tableName);
	        	xt .setDescription(sheet.getCell(4, i).getContents());
	        	xt.setFields(new ArrayList<>());
	        	tables.add(xt);
        	}catch (Exception e) {
        		//Tem algumas linhas apos as linhas de dados
//        		System.out.println("erro linha:" + i);
//        		e.printStackTrace();
        	}
        }

        
        //Get the columns
        sheet = workbook.getSheet(4);
        rows = sheet.getRows();
        firstRow = 0;
        for (int i = 0; i < rows; i++) {
        	if (sheet.getCell(0, i).getContents().equals(LAYOUT_ARQUIVOS)) {
        		firstRow = i+1;
        		break;
        	}
        }

        for (int i = firstRow; i < rows; i++) {
        	try {
	        	XLSField xf = new XLSField();
	        	xf.setTableName(sheet.getCell(0, i).getContents());
	        	xf.setRequired(sheet.getCell(1, i).getContents().equals("(*)"));
	        	xf.setIndex(Integer.parseInt(sheet.getCell(2, i).getContents()));
	        	xf.setDescription(sheet.getCell(3, i).getContents());
	        	xf.setColumnName(sheet.getCell(4, i).getContents());
	        	xf.setSize(sheet.getCell(6, i).getContents());
	        	xf.setType(sheet.getCell(7, i).getContents());
	        	List<XLSField> fields = tables.stream().filter(t -> t.getName().equals(xf.getTableName())).collect(Collectors.toList()).get(0).getFields();
	        	fields.add(xf);
        	}catch (Exception e) {
        		//Tem algumas linhas apos as linhas de dados
//        		System.out.println("erro linha:" + i);
//        		e.printStackTrace();
        	}
        }
        return tables;
    }
}
