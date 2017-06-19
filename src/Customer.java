import java.security.Timestamp;

public class Customer {
	String key;
	String last_name;
	double simpleLTV;
	public Customer(String key, String last_name, double simpleLTV) {
		this.key = key;
		this.last_name = last_name;
		this.simpleLTV = simpleLTV;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getLast_name() {
		return last_name;
	}
	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}
	public double getSimpleLTV() {
		return simpleLTV;
	}
	public void setSimpleLTV(double simpleLTV) {
		this.simpleLTV = simpleLTV;
	}
	
	
}


