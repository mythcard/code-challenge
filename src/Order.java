import java.util.Date;

public class Order{
	
	String key;
	Date eventTime;
	String customerId;
	double totalAmount;
	
	public Order(String key, Date eventTime, String customerId, double totalAmount ){
		this.key = key;
		this.eventTime = eventTime;
		this.customerId = customerId;
		this.totalAmount = totalAmount;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	
}
