import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Item implements Serializable {

	String title;
	String description;
	String category;
	String userID;
	Date startTime;
	Date endTime;
	double reservePrice;
	ArrayList<Bid> bids = new ArrayList<Bid>();
	int id;
	boolean closed;
	boolean winnerNotified;
	
	public Item(String title, String description, String category,String userID, Date startTime, Date endTime, double reservePrice,int id) {
		this.title = title;
		this.description = description;
		this.category = category;
		this.userID = userID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.reservePrice = reservePrice;
		this.id = id;
		this.closed = false;
		this.winnerNotified = false;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public String getUserID() {
		return userID;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public double getReservePrive() {
		return reservePrice;
	}

	public Bid getTopBid() {
		return bids.get(bids.size() - 1);
	}

	public ArrayList<Bid> getBids() {
		return bids;
	}

	public int getId() {
		return id;
	}
	
}

//every item has a collection of objects of the bid type
class Bid implements Serializable {

	User bidder;
	double value;
	Date time;
	int itemID;

	Bid(User bidder, double value, Date time, int itemID) {
		this.bidder = bidder;
		this.value = value;
		this.time = time;
		this.itemID = itemID;
	}

}