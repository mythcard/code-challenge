import java.security.Timestamp;
import java.util.Date;

public class TimeChronon {
	long weekNo;
	Date weekStart;
	Date weekEnd;
	
	public TimeChronon(long weekNo, Date weekStart, Date weekEnd) {
		this.weekNo = weekNo;
		this.weekStart = weekStart;
		this.weekEnd = weekEnd;
	}

	public long getWeekNo() {
		return weekNo;
	}

	public void setWeekNo(long weekNo) {
		this.weekNo = weekNo;
	}

	public Date getWeekStart() {
		return weekStart;
	}

	public void setWeekStart(Date weekStart) {
		this.weekStart = weekStart;
	}

	public Date getWeekEnd() {
		return weekEnd;
	}

	public void setWeekEnd(Date weekEnd) {
		this.weekEnd = weekEnd;
	}
	
	
}
