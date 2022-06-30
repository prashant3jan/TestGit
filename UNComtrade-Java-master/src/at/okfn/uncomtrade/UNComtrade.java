package at.okfn.uncomtrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
class MyException extends Exception{
	   String str1;
	   /* Constructor of custom exception class
	    * here I am copying the message that we are passing while
	    * throwing the exception to a string and then displaying 
	    * that string along with the message.
	    */
	   MyException(String str2) {
		str1=str2;
	   }
	   public String toString(){ 
		return ("MyException Occurred: "+str1) ;
	   }
	}

public class UNComtrade {
	public static void call_api() throws Exception {
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
	     if (responseCode == HttpURLConnection.HTTP_OK) {
	         BufferedReader in = new BufferedReader(
	             new InputStreamReader(con.getInputStream()));
	         StringBuffer response = new StringBuffer();
	         while ((readLine = in.readLine()) != null) {
	             response.append(readLine);
	         } in .close();
	         // print result
	         System.out.println("JSON String Result " + response.toString());
	         //GetAndPost.POSTRequest(response.toString());
	         in.close();
	        
	     }
	    
	}

	
	public static void main(String[] args){
		try{
			UNComtrade.call_api();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}

