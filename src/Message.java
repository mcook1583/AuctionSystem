import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Message implements Serializable {
	
	// every message has a variable for a string message and a user to aid with
	// processing information
	String message;
	User loggedInUser;
	
}

class RegisterMessage extends Message {

	public String name;
	public String familyName;
	String id;
	public char[] password;
	public char[] confirmPassword;

	RegisterMessage(String name, String familyName, String id, char[] password,char[] confirmPassword) {
		this.name = name;
		this.familyName = familyName;
		this.id = id;
		this.password = password;
		this.confirmPassword = confirmPassword;
	}
}

class LoginMessage extends Message {

	String id;
	char[] password;

	LoginMessage(String id, char[] password) {
		this.id = id;
		this.password = password;
	}

}

class GalleryViewRequestMessage extends Message {

	String category;
	Date endTime;
	String userID;
	boolean bid;
	
	// GalleryViewRequestMessage has multiple constructors depending on the
	// information being asked for, it allows the server to return items
	// dependent on a users auctions, a users bids, auctions ending after a
	// certain time and auctions of a certain category

	GalleryViewRequestMessage(String category) {
		this.category = category;
		this.endTime = new Date();
		this.bid = false;
	}

	GalleryViewRequestMessage(String userID, boolean bid) {
		this.endTime = new Date();
		this.userID = userID;
		this.bid = bid;
	}
	
	GalleryViewRequestMessage(Date endTime) {
		this.category = "(Select a Category)";
		this.endTime = endTime;
		this.bid = false;
	}

}

class GalleryViewMessage extends Message {

	ArrayList<Item> items;

	GalleryViewMessage(ArrayList<Item> items) {
		this.items = items;
	}

}

class ItemViewRequestMessage extends Message {

	int id;

	ItemViewRequestMessage(int id) {
		this.id = id;
	}
}

class ItemViewMessage extends Message {

	Item item;

	ItemViewMessage(Item item) {
		this.item = item;
	}
}

class CreateAuctionMessage extends Message {

	String title;
	String description;
	String category;
	String userID;
	Date startTime;
	Date endTime;
	double reservePrive;

	CreateAuctionMessage(String title, String description, String category,String userID, Date startTime, Date endTime, double reservePrive) {
		this.title = title;
		this.description = description;
		this.category = category;
		this.userID = userID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.reservePrive = reservePrive;
	}

}

class BidMessage extends Message {

	User user;
	double bidAmount;
	int itemID;

	BidMessage(int itemID, User user, double bidAmount) {
		this.itemID = itemID;
		this.user = user;
		this.bidAmount = bidAmount;
	}
}

class NotificationRequestMessage extends Message {

	NotificationRequestMessage(User user) {
		this.loggedInUser = user;
	}
}

class NotificationMessage extends Message {

	NotificationMessage(String message) {
		this.message = message;
	}
}

class ErrorMessage extends Message {

	ErrorMessage(String message) {
		this.message = message;
	}
}
