package com.bs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

public class PropertiesUtil implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static String location = "D:\\qqd_new_workspace\\un_company_maven\\zklock\\src\\main\\resources\\appconfig.properties";
	
	public static Properties prop =  new  Properties();    
    static  {    
       //InputStream in = Object.class.getResourceAsStream( location );    
    	
    	
        try  {    
        	InputStream in = new FileInputStream(new File(location));
           prop.load(in); 
       }  catch  (IOException e) {    
           e.printStackTrace();    
       }    
   }    
  
    
    public PropertiesUtil(){
    	 
    }
    public Properties init() throws FileNotFoundException{
    	Properties p = new Properties();
    	InputStream in = new FileInputStream(new File(location));
        try  {    
           p.load(in); 
       }  catch  (IOException e) {    
           e.printStackTrace();    
       }   
       return p;
    }
 
    public static String getKey(String key){
    	Properties p = null;
		try {
			p = new PropertiesUtil().init();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return p.getProperty(key);
    }
    
    public static void main(String args[]){    
       System.out.println(prop);    
   }    
	
}
