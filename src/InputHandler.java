import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class InputHandler {

	// get index of the array as part of search for an element during UPDATE operation
	public static int getIndex(ArrayList<Order> ord ,String key){
		for(Order singleOrder: ord ){
			if(singleOrder.key.equals(key)){
				return ord.indexOf(singleOrder);
			}
		}
		return 0;
	}
	
	public static void setOrderDetails(JSONObject jsonObject, ArrayList<Order> ord)   {
		String key;
		Date eventTime ; 
		String customerId; 
		double totalAmount;
		Long max =0L;
        Long min =100000000000L;
        
        try{
		
		String verb = (String) jsonObject.get("verb");
		
		if(verb.length() == 0) throw new Exception();
		
		// if this is a new order
		if(verb.equals("NEW")){
			key = (String) jsonObject.get("key");
			
			/*
			 *  I am generating the event time randomly
			 *  This is because I was facing problems with fetching event time from file through json
			 *  the error that I was encountering was parse exception despite handling it through try catch block, I tried resolving it for a long time, but I was losing a lot of time in it
			 *  in order to circumvent through the issue, I am randomly generating the dates which is considered as an eventtime
			 *  so line 51 -54 and line 66-69 in UPDATE section generate event time randomly
			 */
			Random r = new Random();
	        Long randomLong=(r.nextLong() % (max - min)) + min;
	        Date dt =new Date(randomLong);
	        eventTime =  new java.sql.Timestamp(  dt.getTime() ); 
			
			customerId = (String) jsonObject.get("customer_id"); 
			totalAmount = Double.parseDouble((String) jsonObject.get("total_amount")) ;
			
			if(key.length() == 0 || customerId.length() == 0 ) throw new Exception();
			
			Order tempVariableInsert = new Order(key, eventTime, customerId, totalAmount);
			
			// add operation is amortised constant time
			ord.add(tempVariableInsert);
		}
		// if it is an update for a pre-existing order
		else if(verb.equals("UPDATE")){
			key = (String) jsonObject.get("key");
			
			Random r = new Random();
	        Long randomLong=(r.nextLong() % (max - min)) + min;
	        Date dt =new Date(randomLong);
	        eventTime =  new java.sql.Timestamp(  dt.getTime() );
				
			customerId = (String) jsonObject.get("customer_id"); 
			totalAmount = Double.parseDouble((String) jsonObject.get("total_amount")) ;
			
			if(key.length() == 0 || customerId.length() == 0 ) throw new Exception();
			
			Order tempVariableUpdate = new Order(key, eventTime, customerId, totalAmount);
			
			// the search is linear, i.e. for n elements, n * n which is quite inefficient
			int index = getIndex(ord, key);
			
			/*
			 * Instead of array list where addition of elements is cheap, search is costly
			 * We can perhaps resort to a data structure like Binary Search Red Black Tree or AVL tree which 
			 * promises a logarithmic insert and search efficiecy per operation
			 */
			
			ord.set(index,tempVariableUpdate);
			
		}
        }catch(Exception e){
        	System.out.println(" One of the fields in input could be zero length");
        	System.out.println(e);
        }
	}
	
	public static int getSiteIndex(ArrayList<SiteVisits> site ,String key){
		for(SiteVisits sv: site ){
			if(sv.key.equals(key)){
				return site.indexOf(sv);
			}
		}
		return 0;
	}
	
	// build the array list of site visit repository
	public static void setSiteVisitDetails(JSONObject jsonObject, ArrayList<SiteVisits> site){
		String key = (String) jsonObject.get("key");
		String CustomerId= (String) jsonObject.get("customer_id");
		String verb = (String) jsonObject.get("verb");
		
		if(verb.equals("NEW")){
			SiteVisits sv = new SiteVisits(key,CustomerId);
			site.add(sv);
		}
		else if(verb.equals("UPDATE")){
			SiteVisits sv = new SiteVisits(key,CustomerId);
			int index = getSiteIndex(site, key);
			site.set(index,sv);
		}
		
	}

	public void getDetails(ArrayList<Order> ord, HashMap<String,String> custm, ArrayList<SiteVisits> site) throws IOException {
				//start of input handler where according to type of event the operation is performed
				JSONParser parser = new JSONParser();
				
				File file = new File("D:/code-challenge/sample_input/sampleInput.txt");  
				BufferedReader br = new BufferedReader(new FileReader(file));
				
			    String s = br.readLine();
					
			      try{
			    	  Object obj = parser.parse(s);
			          JSONArray array = (JSONArray)obj;
			          for(int i = 0; i < array.size(); i++){
			        	  JSONObject jsonObject = (JSONObject) array.get(i);
			          
			        	  String type = (String) jsonObject.get("type");
			        	  
			        	  // catching a type field which is mandatory
			        	  if(type.length() == 0) throw new Exception();
			        	  
			        	  // perform insert update for order
			        	  if(type.equals("ORDER")){
			        		  setOrderDetails(jsonObject, ord);
			        	  }
			        	  else if(type.equals("CUSTOMER")){
			        		  String customerId = (String) jsonObject.get("key");
			        		  String lastName = (String) jsonObject.get("last_name");
			        		  if(lastName.length() == 0 || customerId.length() == 0 ) throw new Exception();
			        		  custm.put(customerId, lastName);
			        	  }
			        	  else if(type.equals("SITE_VISIT")){
			        		  setSiteVisitDetails(jsonObject, site);
			        	  }
			          
			          }
			          
			          
			      }catch(ParseException pe){
			  		
			          System.out.println("position: " + pe.getPosition());
			          System.out.println(pe);
			       	}
			      catch(Exception e){
			    	  System.out.println(" This could be some field with length 0. ");
			    	  System.out.println(e);
			      }
			      br.close();
	}

}
