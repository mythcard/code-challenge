
public class SiteVisits {
	// Here for now I am only capturing the site visit, I am not capturing the event time as a randomly
	// Without the event I see how badly I am at a disadvantage
	String key;
	String customerId;
	// eventTime
	public SiteVisits(String key, String customerId) {
		this.key = key;
		this.customerId = customerId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
}
