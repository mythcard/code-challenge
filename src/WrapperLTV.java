import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;

import java.util.ArrayList;
import java.util.Calendar;




public class WrapperLTV {
	
	// get the minimum time from orders array list data structure
	// linear time performance in terms of number of orders
	private static Date getMinTime(ArrayList<Order> ord){
		Date minTime = ord.get(0).eventTime;
		
		for(Order item: ord){
			if(item.eventTime.before(minTime)){
				minTime = item.eventTime;
			}
		}
		
		return minTime;
	}
	
	// get the maximum time from orders array list data structure
	// linear time performance in terms of number of orders
	private static Date getMaxTime(ArrayList<Order> ord){
		Date maxTime = ord.get(0).eventTime;
		
		for(Order item: ord){
			if(item.eventTime.after(maxTime)){
				maxTime = item.eventTime;
			}
		}
		
		return maxTime;
	}
	
	/*
	 * the number of operations are liner as a function of number of weeks W
	 * where w = w1 + w2
	 * w1 is the number of weeks from current date to min time (this is part of operation 1)
	 * w2 is total number of weeks between minTime and maxTime (this is part of operation 2)
	 */
	private static void buildTimeDimesion(ArrayList<TimeChronon> tme, Date minTime, Date maxTime){
		// first lets calcluate the sunday just previous to min time  (operation 1 for w1 part)
		Calendar c = Calendar.getInstance();
		
		c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		while(minTime.before(c.getTime())){
			c.add(Calendar.DATE, -7);
		}
		
		
		/*  
		 * this is the first Sunday which happens to be the week 1
		 * now I will add the weeks to time dimension array as week 1, 2,3 and as follows until the max date is covered
		 * (operation 2 for w2 part)
		 */
		Date currentSunday = c.getTime();
		int counter = 1;
		while(maxTime.after(currentSunday)){
			c.add(Calendar.DATE, 7);
			Date nextSunday = c.getTime();
			TimeChronon tm = new TimeChronon(counter, currentSunday, nextSunday);
			tme.add(tm);
			currentSunday = nextSunday;
			counter++;
		}
		
		/*
		 * piece of code below to test all weeks are properly added to time dimension
		 */
//		for(TimeChronon item:tme){
//			System.out.println("The week "+item.weekNo+" and week start "+ item.weekStart + " and week End is "+item.weekEnd);
//		}
		
	}
	
	private static int getWeekNo(Order ordItem, ArrayList<TimeChronon> tme){
		int weekNo = 0;
		
		for(TimeChronon item: tme){
			// orders event time should be between time dimensions start and end time
			// corner case is week start date is same as order event time  like Sun Jul 12 00:00:00, considered as part of new week 
			if((ordItem.getEventTime().after(item.getWeekStart()) || ordItem.getEventTime().equals(item.getWeekStart())) && ordItem.getEventTime().before(item.getWeekEnd())){
				return weekNo;
			}
			weekNo++;
		}
		
		return -1;
	}
	
	private static void buildWeeklyOrderSummary(ArrayList<Order> ord, ArrayList<TimeChronon> tme, HashMap<String, ArrayList<Double>> weeklyOrderSummary){
		
		/*  
		 * there are 2 parts here, 
		 * 1st part if there is no customer entry, 
		 * add the key and initialize the array list with amount for respective week 
		 * and rest of the week to 0
		 * 2nd part: if the key already exists just add the amount for the respective week
		 * 1st and second part is an either or relationship  
		 */
		
		for(Order ordItem: ord){
			
			// check if customerid is not present in the hashmap then initialise all week's values with 0.0
			if(!weeklyOrderSummary.containsKey(ordItem.getCustomerId())){
				String key = ordItem.getCustomerId();
				ArrayList<Double> totalAmount = new ArrayList<Double>();
				for(int i = 0 ; i <tme.size(); i++){
					totalAmount.add(0.0);
				}
				weeklyOrderSummary.put(key,totalAmount);
			}
			
			// based on time dimension get the index or week no
			int index = getWeekNo(ordItem,tme);
			
			// now based on week no please update the array list for respective key
			ArrayList<Double> amount = weeklyOrderSummary.get(ordItem.getCustomerId());
			// first get the previous value and add it to order event's total value
			double actualAmount = amount.get(index)+ ordItem.getTotalAmount();
			amount.set(index,actualAmount);
			weeklyOrderSummary.put(ordItem.getCustomerId(),amount);
			
		}
		
	}
	
	private static void buildordSimpleLTV(int size, HashMap<String, ArrayList<Double>> weeklyOrderSummary, HashMap<String,Double> ordSimpleLTV){
		// the way below Hash map is built is inefficient, a better approach through iterators should be possible
		// I am finally resorting to this approach as I was unable to resolve the error I was obtaining through iterators where we can fetch both keys and values together
		for (String key : weeklyOrderSummary.keySet()) {
			ArrayList<Double> amt = weeklyOrderSummary.get(key);
			double sum = 0.0;
			for(double item: amt) sum += item; 
			
			// (sum of revenue spanning over all weeks)/ (number of weeks) gives a the average, thus finally simpleLTV = 52(a)* t where t here is 10
			ordSimpleLTV.put(key, 52 * (sum/size)* 10);   
		}
	}
	
	private static void TopXSimpleLTVCustomers(int x, HashMap<String,Double> ordSimpleLTV, HashMap<String,String> custm) throws IOException{
		
		/*
		 *  Heap data structure is used to retrieve the top N elements, max heap to be precise
		 *  while building the heap, each insert is logarithmic in nature, hence if there are n elements, total time is O(nlogn) 
		 *  the above approach is William's method which is sub optimal 
		 *  This building of heap can be improved by using Floyd's approach where elements are inserted arbitrarily first and then heapify operations are performed from leaves to root
		 *  In the above method, the most of the work is done at higher levels with less nodes and nodes at lower levels which are already heapified, less work needs to be done
		 *  by Floyd's approach building heap becomes linear in nature to the number of elements i.e O(n)
		 *  to be noted, the current implementation below is sub optimal in nature
		 */
		
		// building heap
		MaxHeap pq = new MaxHeap(ordSimpleLTV.size());
		for(String key:ordSimpleLTV.keySet()){
			
			//first get the customer name
			String lastName = custm.get(key);
			if(lastName == null) lastName = "Unknown";
			
			pq.insert(key, lastName , ordSimpleLTV.get(key));
			//System.out.println("The root is "+pq.peek().getSimpleLTV());
		}
		
		 File outFile = new File("D:/code-challenge/output/output.txt");
		 BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		 
		 
		/*
		 * since TopXSimpleLTVCustomers(x,D) could be repeatedly queried for, or meaning high impact reads, we can check if we can avoid building heaps repeatedly
		 *  There are 2 perspectives here
		 *  1st perspective:
		 *  below approach of building the heap is n * log n
		 *  if top x elements are asked, delMax will have a cost of x* log n, where log n work is required in heapifying again or in our case the demote procedure 
		 *  as long as x < n, when we perform delMax, we can store the elements in a stack and rebuild the heap with x* logn work
		 *  
		 *  2nd perspective:
		 *  if building of heap is O(n) as per Floyd's approach
		 *  if top x elements are asked for, delMax will have a cost of  x * log n, we can still follow above approach of using a stack as buffer (to bypass rebuilding heap all over again) and rebuilding heap in linear time
		 *  as long as x * log n < n i.e, x < (n / log n)
		 *  
		 */
		// perform del max x number of times to get top N elements
		int count = x;
		while(count > 0){
			Customer cust = pq.delMax();
			
			/*
			 *  declare stack before this loop Stack<Customer> st = new Stack<Customer>();
			 *  st.push(cust);
			 */
			
			bw.write("Simple LTV of top " + x +" th customer with customer id "+cust.getKey()+" and last name "+cust.getLast_name()+" is  "+cust.getSimpleLTV()+"\n");
			count--;
		}
		
		/*
		 *  re build the heap from consecutive pops from the stack
		 *  Customer custTemp = st.pop() 
		 *  pq.insert(custTemp.key, custTemp.lastName , custTemp.simpleLTV);
		 */
		bw.close();
	}
	
	public static void main(String[] args) throws IOException  {
			// creating array list of orders
			ArrayList<Order> ord = new ArrayList<Order>();
			
			// creating an HashMap of customers
			HashMap<String,String> custm =  new HashMap<String,String>();
			
			// creating an array list of site visits
			ArrayList<SiteVisits> site = new ArrayList<SiteVisits>();
			
			// this is a call to input handler to process the events through getDetails
			InputHandler ip = new InputHandler();
			ip.getDetails(ord, custm, site);
			
			// get both max and min time which is necessary to build the time dimension TimeChronon
			// we are trying to get the time frame from the data or orders in this case, specifically the orders
			// please note that event times are randomly generated and not taken from files, why this happened is explained in InputHandler class as also in the report
			Date minTime = getMinTime(ord);
			Date maxTime = getMaxTime(ord);
			System.out.println("Min time is: "+ minTime);
			System.out.println("Max time is: "+ maxTime);
			
			for(Order ord1: ord){
				System.out.println("The amount for Customer "+ord1.getCustomerId()+" is "+ord1.getTotalAmount());
				System.out.println("The event time is "+ord1.getEventTime());
			}
			
			/* 
			 * We are building a separate time dimension becuase this can help in not just current scenario but in lot of other scenarios
			 * now building the time dimension which is again an array list of TimeChronon
			 * format shall be week no, start time and end time based on
			 * min and max time from ArrayList<Order> ord data structure built earlier through getDetails
			 */
			ArrayList<TimeChronon> tme = new ArrayList<TimeChronon>();
			buildTimeDimesion(tme, minTime, maxTime );
			
			/*
			 *  now building a hash map for orders with customerId as key 
			 *  and an arraylist as value to hold total revenue at a weekly aggregation level, 
			 *  the index 0 of the arraylist indicates week1 and index 1 as week2 and so on
			 */
			HashMap<String, ArrayList<Double>> weeklyOrderSummary = new HashMap<String, ArrayList<Double>>();
			buildWeeklyOrderSummary(ord, tme, weeklyOrderSummary);
			
			/*
			 * piece of code to check if the summary is proper, quite difficult to verify by eye
			 * best way to handle is using object viewer during debug mode
			 */
			
//			for (ArrayList<Double> amnt : weeklyOrderSummary.values()) {
//			    for(double item: amnt){
//			    	System.out.println(item);
//			    }
//			}
			
			/*
			 *  A similar approach can be used for site visits 
			 *  to capture the number of site visits per week
			 */
			// here shall go the code
			
			
			System.out.println("The total number of weeks are: "+tme.size());
			
			/*
			 * hashmap to build the customer and simple LTV
			 * we shall build a hashmap again before building the heap or priority queue 
			 * which contains customer id as the key and the simpleLTV as the result
			 * We will do this calculation once as a pre-computation
			 * We could have directly calculated the simpleLTV from weeklyOrderSummary hashmap
			 * but its better to do pre-computation once and proceed with insertion of elements into max heap
			 * Max heap acts as a data structure D for querying top N elements
			 * input parameters: total number of weeks from time dimension, weeklyOrderSummary which is 
			 * hash map for orders with customerId as key 
			 * and an arraylist as value to hold total revenue at a weekly aggregation level, 
			 * the index 0 of the arraylist indicates week1 and index 1 as week2 and so on
			 * ordSimpleLTV is the HashMap of customerId and SimpleLTV that is being built
			 */
			HashMap<String,Double> ordSimpleLTV = new HashMap<String,Double>();
			buildordSimpleLTV(tme.size(), weeklyOrderSummary, ordSimpleLTV);
			
			// piece of code for validation
			for(String key: ordSimpleLTV.keySet()){
				System.out.println("The simpleLTV's for customer "+key+" are: "+ ordSimpleLTV.get(key));
			}
			
			/*
			 * input parameters: 
			 * prints top x customers with highest LTV
			 */
			//the call for TopXSimpleLTVCustomers(x, D)
			TopXSimpleLTVCustomers(2, ordSimpleLTV, custm);
			
	    }
	}


