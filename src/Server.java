import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Server {

	DataPersistence dp;
	Comms serverComms;
	ArrayList<User> users;
	ArrayList<Item> items;
	ArrayList<String> activity;
	HashSet<Integer> itemsBidOn;
	ServerPanel serverPanel;
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yy-HH:mm");

	public static void main(String[] args) throws IOException {
		new Server();
	}

	public Server() {
		// instantiate the data persistence class and read in the data stored on
		// file
		dp = new DataPersistence();
		users = dp.returnUsers();
		items = dp.returnItems();
		activity = dp.returnLog();

		//create the frame in which the server gui will be displayed
		JFrame serverFrame = new JFrame("Auction Server");
		serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		serverFrame.setSize(900, 600);
		serverPanel = new ServerPanel();
		serverFrame.setContentPane(serverPanel);
		serverFrame.setVisible(true);

		//create a thread to check if actions have closed
		new CloseAuctionsThread().start();
		serverComms = new Comms(this);
		try {
			serverComms.makeServer(4444);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Message processInput(Message message) {

		if (message instanceof RegisterMessage) {
			//cast the message to a more specific definition
			RegisterMessage registerMessage = (RegisterMessage) message;
			//check if the inputs aren't empty
			if (registerMessage.name.equals("")) {
				return new ErrorMessage("Please enter a name.");
			}
			if (registerMessage.familyName.equals("")) {
				return new ErrorMessage("Please enter a family name.");
			}
			if (registerMessage.id.equals("")) {
				return new ErrorMessage("Please enter a username (ID).");
			}
			if (Arrays.equals(registerMessage.password, new char[] {})) {
				return new ErrorMessage("Please enter a password.");
			}
			//check that passwords match
			if (Arrays.equals(registerMessage.password,(registerMessage.confirmPassword))) {
				//add the user and save users to file
				User user = new User(registerMessage.name,registerMessage.familyName, registerMessage.id,registerMessage.password);
				users.add(user);
				dp.writeToFile("users", users);
				//create a log entry and update the log
				String log = dateTimeFormat.format(new Date()) + " " + user.getId() + " was registered on the system.\n";
				serverPanel.update(log);
				//return items that have started
				ArrayList<Item> resultItems = new ArrayList<Item>();
				for (Item item : items) {
					if(item.getStartTime().before(new Date()) && item.getEndTime().after(new Date())){
						resultItems.add(item);
					}
				}
				GalleryViewMessage galleryViewMessage = new GalleryViewMessage(new ArrayList<Item>(resultItems));
				galleryViewMessage.loggedInUser = user;
				return galleryViewMessage;
			} else {
				return new ErrorMessage("Passwords don't match.");
			}
		}

		if (message instanceof LoginMessage) {
			//cast the message to a more specific definition
			LoginMessage loginMessage = (LoginMessage) message;
			//check if the inputs aren't empty
			if (loginMessage.id.equals("")) {
				return new ErrorMessage("Please enter a username (ID).");
			}
			if (Arrays.equals(loginMessage.password, new char[] {})) {
				return new ErrorMessage("Please enter a password.");
			}
			for (User user : users) {
				if (user.getId().equals(loginMessage.id) && Arrays.equals(loginMessage.password, user.getPassword())) {
					//create a log entry and update the log
					String log = dateTimeFormat.format(new Date()) + " " + user.getId() + " logged in to the system.\n";
					serverPanel.update(log);
					//return items that have started
					ArrayList<Item> resultItems = new ArrayList<Item>();
					for (Item item : items) {
						if(item.getStartTime().before(new Date()) && item.getEndTime().after(new Date())){
							resultItems.add(item);
						}
					}
					GalleryViewMessage galleryViewMessage = new GalleryViewMessage(new ArrayList<Item>(resultItems));
					galleryViewMessage.loggedInUser = user;
					return galleryViewMessage;
				}
			}
			return new ErrorMessage("Incorrect details, user not found.");
		}

		if (message instanceof GalleryViewRequestMessage) {
			synchronized(items){
				//cast the message to a more specific definition
				GalleryViewRequestMessage galleryViewRequestMessage = (GalleryViewRequestMessage) message;
				ArrayList<Item> resultItems = new ArrayList<Item>();
				//evaluates true if the request is to see the items the user has bid on
				if (galleryViewRequestMessage.bid == true) {
					//a HashSet is used as a user may have bid on one item more than once
					itemsBidOn = new HashSet<Integer>();
					for (Item item : items) {
						for(Bid bid : item.getBids()){
							//check that items end after the given time
							if (item.getEndTime().after(new Date())) {
								if (bid.bidder.getId().equals(galleryViewRequestMessage.userID)) {
									itemsBidOn.add(bid.itemID);
								}
							}
						}
					}
					//fill the arraylist with the contents of the HashSet
					for (int id : itemsBidOn) {
						resultItems.add(items.get(id));
					}
				}else{
					for (Item item : items) {
						//check that items have started
						if(item.getStartTime().before(new Date())){
							//check that items end after the given time
							if (item.getEndTime().after(galleryViewRequestMessage.endTime)) {
								if(galleryViewRequestMessage.userID!=null){
									//check if the userID matches given it is not null
									if (item.getUserID().equals(galleryViewRequestMessage.userID)) {
										resultItems.add(item);
									}
								}else{
									//check that the category matches, or the request has no category
									if (item.getCategory().equals(galleryViewRequestMessage.category) || galleryViewRequestMessage.category.equals("(Select a Category)")) {
										resultItems.add(item);
									}								
								}
							}
						}
					}
				}
				return new GalleryViewMessage(resultItems);
			}
		}

		if (message instanceof ItemViewRequestMessage) {
			//cast the message to a more specific definition
			ItemViewRequestMessage itemViewRequestMessage = (ItemViewRequestMessage) message;
			synchronized(items){
				//check to see if item should exist
				if (itemViewRequestMessage.id < items.size() && itemViewRequestMessage.id >= 0) {
					Item item = items.get(itemViewRequestMessage.id);
					return new ItemViewMessage(item);
				} else {
					return new ErrorMessage("Item not on system.");
				}
			}
		}

		if (message instanceof CreateAuctionMessage) {

			synchronized(items){
				//cast the message to a more specific definition
				CreateAuctionMessage createAuctionMessage = (CreateAuctionMessage) message;
				//check if the inputs aren't empty
				if (createAuctionMessage.title.equals("")) {
					return new ErrorMessage("Please enter a title for your item.");
				}
				if (createAuctionMessage.description.equals("")) {
					return new ErrorMessage("Please enter a description for your item.");
				}
				if (createAuctionMessage.category.equals("(Select a Category)")) {
					return new ErrorMessage("Please enter a category for your item.");
				}
				if (createAuctionMessage.reservePrive <= 0) {
					return new ErrorMessage("Please enter a valid value for reserve price, you must include the £ symbol.");
				}
				//check if the auction starts in the future
				if (createAuctionMessage.startTime.before(new Date())) {
					return new ErrorMessage("Please enter a start time in the future.");
				}
				//check if the auction ends after it starts
				if (createAuctionMessage.startTime.after(createAuctionMessage.endTime)) {
					return new ErrorMessage("Please enter an end time after the start time.");
				}
				//add item to the system and save to file
				Item newItem = new Item(createAuctionMessage.title,createAuctionMessage.description,createAuctionMessage.category,createAuctionMessage.loggedInUser.getId(),createAuctionMessage.startTime,createAuctionMessage.endTime,createAuctionMessage.reservePrive, items.size());
				items.add(newItem);
				dp.writeToFile("items", items);
				//create a log entry and update the log
				String log = dateTimeFormat.format(new Date()) + " " + newItem.getUserID() + " created the auction: " + newItem.getTitle() + ".\n";
				serverPanel.update(log);
				return new ItemViewMessage(newItem);
			}
		}

		if (message instanceof BidMessage) {
			//cast the message to a more specific definition
			BidMessage bidMessage = (BidMessage) message;
			//read in the item that will be bid on
			Item item = items.get(bidMessage.itemID);
			//check if the item is sold by the bidder
			if (bidMessage.user.getId().equals(item.getUserID())) {
				return new ErrorMessage("Cannot bid on your own item.");
			}
			//check if the value of the bid is valid
			if (bidMessage.bidAmount <= 0) {
				return new ErrorMessage("Please enter a valid value for your bid, you must include the £ symbol.");
			}
			//check that the value of the bid exceeds the current top value
			if (!(item.getBids().isEmpty())) {
				if (bidMessage.bidAmount <= item.getTopBid().value) {
					return new ErrorMessage("Please enter a valid value for your bid, it must be higher than the current highest bid.");
				}
			}
			//check the item isn't open at this particular time
			if (item.startTime.after(new Date())) {
				return new ErrorMessage("Cannot bid on an auction that hasn't begun.");
			}
			if (item.endTime.before(new Date())) {
				return new ErrorMessage("Cannot bid on an auction that has ended.");
			}
			synchronized(items){
				//add the bid to the item and save items to file
				Bid bid = new Bid(bidMessage.user, bidMessage.bidAmount,new Date(), item.getId());
				item.getBids().add(bid);
				dp.writeToFile("items", items);
				//create a log entry and update the log
				String log = dateTimeFormat.format(new Date()) + " " + bidMessage.user.getId() + " placed a bid of " + NumberFormat.getCurrencyInstance(Locale.UK).format(bidMessage.bidAmount) + " on " + item.getTitle() + ".\n";
				serverPanel.update(log);
				return new ItemViewMessage(item);
			}
		}
		
		if (message instanceof NotificationRequestMessage) {
			//cast the message to a more specific definition
			NotificationRequestMessage notificationRequestMessage = (NotificationRequestMessage) message;
			synchronized(items){
				for (Item item : items) {
					//check if the item has ended and whether the winner was notified of the win
					if (item.endTime.before(new Date()) && item.winnerNotified == false) {
						if (!(item.getBids().isEmpty())) {
							if(item.getTopBid().bidder.getId().equals(notificationRequestMessage.loggedInUser.getId())){
								//confirm that the reserve price was met for there to be a winner of the auction
								if (item.getTopBid().value >= item.reservePrice) {
									//update the item and write items to file
									item.winnerNotified = true;
									dp.writeToFile("items", items);
									//create a log entry and update the log
									String log = dateTimeFormat.format(new Date()) + " " + item.getTopBid().bidder.getId() + " was notified that they won " + item.getTitle() + " for " + NumberFormat.getCurrencyInstance(Locale.UK).format(item.getTopBid().value) + ".\n";
									serverPanel.update(log);
									return new NotificationMessage("Congratulations you have won the item: "+item.getTitle());
								}
							}
						}
					}
				}
			return null;
			}
		}
		return new ErrorMessage("Message can't be processed");
	}

	public void createReport() {
		String report = "";
		//loop through items looking for ended auctions
		for (Item item : items) {
			if (item.getEndTime().before(new Date())) {
				if(!(item.getBids().isEmpty())){
					//check that the item has a winner
					if (item.getTopBid().value >= item.reservePrice) {
						//add the winner name, the item name and the price sold for to the report
						report = report + item.getTopBid().bidder.getFullName()+ " won " + item.getTitle() + " for " + NumberFormat.getCurrencyInstance(Locale.UK).format(item.getTopBid().value)+"." + System.lineSeparator();					
					}
				}
			}
		}
		//write the report to file
		try {
			FileWriter fw = new FileWriter(new File("./report.txt"), false);
			fw.write(report);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
	}

	class ServerPanel extends JPanel {

		JTextArea textArea;

		public ServerPanel() {
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			//text area to display the log
			textArea = new JTextArea(30, 60);
			JScrollPane scrollPane = new JScrollPane(textArea);
			JButton reportButton = new JButton("Create Report");
			reportButton.setAlignmentX(ServerPanel.CENTER_ALIGNMENT);
			if (!(activity.isEmpty())) {
				for (String log : activity) {
					textArea.append(log);
				}
			}
			//produces a report when clicked
			reportButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					createReport();
				}
			});
			this.add(scrollPane);
			this.add(reportButton);
		}

		public void update(String log) {
			//update the text area with the log and write the log to file
			textArea.append(log);
			activity.add(log);
			dp.writeToFile("activity", activity);
		}
	}
	
	class CloseAuctionsThread extends Thread{
		
		public void run(){
			while(true){
				synchronized(items){
					for (Item item : items) {
						if (item.getEndTime().before(new Date()) && item.closed == false) {
							//write a log to show the auction has closed and update the log
							String log = dateTimeFormat.format(new Date()) + " The auction: " +item.getTitle() + " was closed.\n";
							serverPanel.update(log);
							item.closed = true;
							dp.writeToFile("items", items);
						}
					}
				}
				//the thread sleeps for a minute
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}


