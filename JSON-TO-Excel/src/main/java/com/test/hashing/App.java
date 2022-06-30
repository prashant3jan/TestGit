package com.test.hashing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;




public class App {

	public static void call_api() throws Exception {
			try{
		     String url = "https://comtrade.un.org/api//refs/da/view?type=C&freq=A&px=HS&ps=2019&r=All&p=all&rg=all&cc=TOTAL&fmt=json";
		     URL obj = new URL(url);
		     HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		     // optional default is GET
		     con.setRequestMethod("GET");
		     //add request header
		     //con.setRequestProperty("User-Agent", "Chrome/80.0.3987.132");
		     int responseCode = con.getResponseCode();
		     System.out.println("\nSending 'GET' request to URL : " + url);
		     System.out.println("Response Code : " + responseCode);
		     String readLine = null;
		     StringBuffer response =null;
		     if (responseCode == HttpURLConnection.HTTP_OK) {
		         BufferedReader in = new BufferedReader(
		             new InputStreamReader(con.getInputStream()));
		         	 response = new StringBuffer();
		         while ((readLine = in.readLine()) != null) {
		             	response.append(readLine);
		         } in .close();
		         // print result
		         System.out.println("JSON String Result " + response.toString());
		         //GetAndPost.POSTRequest(response.toString());
		         in.close();
		        
		     }
		     JSONParser parse = new JSONParser(); 
		     JSONArray jsonArray = (JSONArray)parse.parse(response.toString());
		     int length = jsonArray.size();
		     System.out.println("length"+length);
		     String excelPath = "D:\\UNComtrade.xls";
		     FileOutputStream fileOutputStream = new FileOutputStream(new File(excelPath));
		     HSSFWorkbook workbook = new HSSFWorkbook();
		     HSSFSheet sheet = workbook.createSheet("year1");
		     int rownum = 0;
		     int colnum = 0;
		     Row row1 = sheet.createRow(rownum);
		     Cell  cell1 = row1.createCell(0);
		     cell1.setCellValue("Type");
		     Cell  cell2 = row1.createCell(1);
		     cell2.setCellValue("Freq");	 
		     Cell  cell3 = row1.createCell(2);
		     cell3.setCellValue("PX");	 
		     Cell  cell4 = row1.createCell(3);
		     cell4.setCellValue("R");	 
		     Cell  cell5 = row1.createCell(4);
		     cell5.setCellValue("rDesc");	 
		     Cell  cell6 = row1.createCell(5);
		     cell6.setCellValue("ps");
		     Cell  cell7 = row1.createCell(6);
		     cell7.setCellValue("TotalRecords");
		     Cell  cell8 = row1.createCell(7);
		     cell8.setCellValue("isOriginal");
		     Cell  cell9 = row1.createCell(8);
		     cell9.setCellValue("PublicationDate");
		     Cell  cell10 = row1.createCell(8);
		     cell10.setCellValue("IsPartnerDetail");
		  
		     
		    for(int i=0; i< length; i++){	
		    	Row row2 = sheet.createRow(++rownum);
		    	System.out.println("rownum"+rownum);
		    	JSONObject jsonObject = (JSONObject)jsonArray.get(i);
		    	 Cell  cell_1 = row2.createCell(0);
		    	 String type = (String)jsonObject.get("type");
		    	 System.out.println("type"+type);
			     cell_1.setCellValue(type);
			     Cell  cell_2 = row2.createCell(1);
			     String freq = (String)jsonObject.get("freq");
		    	 System.out.println("freq"+freq);
			     cell_2.setCellValue(freq);	 
			     Cell  cell_3 = row2.createCell(2);
			     cell_3.setCellValue((String)jsonObject.get("px"));	 
			     Cell  cell_4 = row2.createCell(3);
			     cell_4.setCellValue((String)jsonObject.get("r"));	 
			     Cell  cell_5 = row2.createCell(4);
			     cell_5.setCellValue((String)jsonObject.get("rDesc"));	 
			     Cell  cell_6 = row2.createCell(5);
			     cell_6.setCellValue((String)jsonObject.get("ps"));
			     Cell  cell_7 = row2.createCell(6);
			     cell_7.setCellValue((Long)jsonObject.get("TotalRecords"));
			     Cell  cell_8 = row2.createCell(7);
			     cell_8.setCellValue((Long)jsonObject.get("isOriginal"));
			     Cell  cell_9 = row2.createCell(8);
			     cell_9.setCellValue((String)jsonObject.get("publicationDate"));
			     Cell  cell_10 = row2.createCell(9);
			     cell_10.setCellValue((Long)jsonObject.get("isPartnerDetail"));
		     }
		     
		    //Write workbook into the excel
            workbook.write(fileOutputStream);
            //Close the workbook
            workbook.close();

		     }catch(Exception e){
		    	 e.printStackTrace();
		     }
			}
		    
		public static void main(String[] args){
			try{
				App.call_api();
			}catch(Exception e){
				e.printStackTrace();
			}

		}
	}

