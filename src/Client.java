import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Client {

	// declare global variables that will be used over multiple panels,
	// including some panels themselves
	Comms clientComms;
	User loggedInUser;

	JPanel cardPanel;
	ToolbarPanel toolbarPanel;
	LoginPanel loginPanel;

	JPanel toolbarCardPanel;
	GalleryPanel galleryGrid;
	ItemPanel itemPanel;

	DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);
	String[] categories = { "(Select a Category)", "Books",
			"Movies, TV, Music & Games", "Electronics & Computers",
			"Home, Garden, Pets & DIY", "Toys, Children & Baby",
			"Clothes, Shoes & Jewellery", "Sports & Outdoors",
			"Beauty, Health & Grocery", "Car & Motorbike" };
	JFrame frame;

	public static void main(String[] args) throws IOException {
		new Client();
	}

	public Client() {
		clientComms = new Comms();
		// instantiate the frame in which the client application will be
		// displayed
		JFrame clientFrame = new JFrame("Auction System Client");
		//instantiate some of the panels and define their structure
		cardPanel = new JPanel(new CardLayout());
		loginPanel = new LoginPanel();
		toolbarPanel = new ToolbarPanel();
		cardPanel.add(loginPanel, "login");
		cardPanel.add(toolbarPanel, "toolbar");
		clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clientFrame.setMinimumSize(new Dimension(1175, 700));
		clientFrame.setContentPane(cardPanel);
		clientFrame.setVisible(true);
	}

	class LoginPanel extends JPanel {

		JTextField loginIdField;
		JPasswordField passwordLoginField;

		JTextField nameRegisterField;
		JTextField familyNameRegisterField;
		JTextField registerIdField;
		JPasswordField passwordRegisterField;
		JPasswordField passwordConfirmField;

		public LoginPanel() {
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			//components for the log in gui
			JPanel loginForm = new JPanel();
			loginForm.setLayout(new GridLayout(2, 2));
			loginIdField = new JTextField();
			passwordLoginField = new JPasswordField();
			loginForm.setMaximumSize(new Dimension(300, 400));
			loginForm.add(new JLabel("Username (ID):"));
			loginForm.add(loginIdField);
			loginForm.add(new JLabel("Password:"));
			loginForm.add(passwordLoginField);
			JButton loginButton = new JButton("Login");
			loginButton.setAlignmentX(LoginPanel.CENTER_ALIGNMENT);

			//components for the register gui
			JPanel registerForm = new JPanel();
			registerForm.setLayout(new GridLayout(5, 2));
			nameRegisterField = new JTextField();
			familyNameRegisterField = new JTextField();
			registerIdField = new JTextField();
			passwordRegisterField = new JPasswordField();
			passwordConfirmField = new JPasswordField();
			registerForm.setMaximumSize(new Dimension(300, 400));
			registerForm.add(new JLabel("Name:"));
			registerForm.add(nameRegisterField);
			registerForm.add(new JLabel("Family name:"));
			registerForm.add(familyNameRegisterField);
			registerForm.add(new JLabel("Username (ID):"));
			registerForm.add(registerIdField);
			registerForm.add(new JLabel("Password:"));
			registerForm.add(passwordRegisterField);
			registerForm.add(new JLabel("Confirm Password:"));
			registerForm.add(passwordConfirmField);
			JButton registerButton = new JButton("Register");
			registerButton.setAlignmentX(LoginPanel.CENTER_ALIGNMENT);

			loginButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loginPanel.sendAndReceiveMessage(new LoginMessage(loginIdField.getText(), passwordLoginField.getPassword()));
				}
			});

			registerButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loginPanel.sendAndReceiveMessage(new RegisterMessage(nameRegisterField.getText(),familyNameRegisterField.getText(), registerIdField.getText(), passwordRegisterField.getPassword(), passwordConfirmField.getPassword()));
				}
			});

			this.add(Box.createRigidArea(new Dimension(300, 90)));
			this.add(loginForm);
			this.add(loginButton);
			this.add(Box.createGlue());
			this.add(registerForm);
			this.add(registerButton);
			this.add(Box.createRigidArea(new Dimension(300, 90)));
		}
		
		public void sendAndReceiveMessage(Message message){
			clientComms.makeClient("localhost", 4444);
			clientComms.clientSendMessage(message);
			Message m = clientComms.clientReceiveMessage();
			if (!(m instanceof ErrorMessage)) {
				loggedInUser = m.loggedInUser;
				galleryGrid.update(((GalleryViewMessage) m).items);
				//once the user is on the system the NotificationThread is started
				new NotificationThread(loggedInUser, clientComms).start();
				CardLayout cl = (CardLayout) (cardPanel.getLayout());
				cl.show(cardPanel, "toolbar");
			}
		}

		//line to separate the two forms in the gui
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawLine(40, this.getHeight() / 2, this.getWidth() - 40,
					this.getHeight() / 2);
		}
	}

	class ToolbarPanel extends JPanel {

		JTextField userIdField;
		JTextField itemIdField;
		JFormattedTextField endTimeField;
		JComboBox<String> categoriesComboBox;

		public ToolbarPanel() {
			this.setLayout(new BorderLayout());
			// the toolbar allows users to see their own auctions, items they
			// have bid on, items from a certain user, items as defined by their
			// ID, items ending after a given time and date, items from a
			// certain category and the ability to submit a new item
			JPanel toolbar = new JPanel();
			toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
			JButton myAuctionsButton = new JButton("My Auctions");
			JButton myBidsButton = new JButton("My Bids");
			userIdField = new JTextField();
			JButton userIdButton = new JButton("Go");
			itemIdField = new JTextField();
			JButton itemIdButton = new JButton("Go");
			endTimeField = new JFormattedTextField(dateTimeFormat.format(new Date(new Date().getTime())));
			JButton endTimeButton = new JButton("Go");
			categoriesComboBox = new JComboBox<String>(categories);
			JButton submitAuctionButton = new JButton("Submit Auction");

			//define the structure of panels below the toolbar
			toolbarCardPanel = new JPanel(new CardLayout());
			galleryGrid = new GalleryPanel();
			itemPanel = new ItemPanel();
			toolbarCardPanel.add(new JScrollPane(galleryGrid), "gallery");
			toolbarCardPanel.add(itemPanel, "item");
			toolbarCardPanel.add(new SubmitItemPanel(), "submit");

			myAuctionsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//send an update request concerning this particular action
					toolbarPanel.sendAndReceiveGalleryMessage(new GalleryViewRequestMessage(loggedInUser.id, false));
					//reset fields that aren't concerned with this action
					userIdField.setText("");
					itemIdField.setText("");
					endTimeField.setText(dateTimeFormat.format(new Date()));
					categoriesComboBox.setSelectedIndex(0);
				}
			});
			myBidsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//send an update request concerning this particular action
					toolbarPanel.sendAndReceiveGalleryMessage(new GalleryViewRequestMessage(loggedInUser.id, true));
					//reset fields that aren't concerned with this action
					userIdField.setText("");
					itemIdField.setText("");
					endTimeField.setText(dateTimeFormat.format(new Date()));
					categoriesComboBox.setSelectedIndex(0);
				}
			});
			userIdButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//send an update request concerning this particular action
					toolbarPanel.sendAndReceiveGalleryMessage(new GalleryViewRequestMessage(userIdField.getText(), false));
					//reset fields that aren't concerned with this action
					itemIdField.setText("");
					endTimeField.setText(dateTimeFormat.format(new Date()));
					categoriesComboBox.setSelectedIndex(0);
				}
			});
			itemIdButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//send an update request concerning this particular action
					clientComms.makeClient("localhost", 4444);
					clientComms.clientSendMessage(new ItemViewRequestMessage(Integer.parseInt(itemIdField.getText())));
					Message m = clientComms.clientReceiveMessage();
					if (!(m instanceof ErrorMessage)) {
						itemPanel.update(((ItemViewMessage) m).item);
						CardLayout cl = (CardLayout) (toolbarCardPanel.getLayout());
						cl.show(toolbarCardPanel, "gallery");
						cl.show(toolbarCardPanel, "item");
						//reset fields that aren't concerned with this action
						userIdField.setText("");
						endTimeField.setText(dateTimeFormat.format(new Date()));
						categoriesComboBox.setSelectedIndex(0);
					}
				}
			});
			endTimeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						//send an update request concerning this particular action
						toolbarPanel.sendAndReceiveGalleryMessage(new GalleryViewRequestMessage(dateTimeFormat.parse(endTimeField.getText())));
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
					//reset fields that aren't concerned with this action
					userIdField.setText("");
					itemIdField.setText("");
					categoriesComboBox.setSelectedIndex(0);
				}
			});
			categoriesComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!(categoriesComboBox.getSelectedItem().equals("(Select a Category)"))){
						//send an update request concerning this particular action
						toolbarPanel.sendAndReceiveGalleryMessage(new GalleryViewRequestMessage((String) categoriesComboBox.getSelectedItem()));
						//reset fields that aren't concerned with this actionuserIdField.setText("");
						itemIdField.setText("");
						endTimeField.setText(dateTimeFormat.format(new Date()));
					}
				}
			});

			submitAuctionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CardLayout cl = (CardLayout) (toolbarCardPanel.getLayout());
					cl.show(toolbarCardPanel, "submit");
				}
			});

			toolbar.add(myAuctionsButton);
			toolbar.add(myBidsButton);
			toolbar.add(new JLabel("User ID:"));
			toolbar.add(userIdField);
			toolbar.add(userIdButton);
			toolbar.add(new JLabel("Item ID:"));
			toolbar.add(itemIdField);
			toolbar.add(itemIdButton);
			toolbar.add(new JLabel("Finishing after:"));
			toolbar.add(endTimeField);
			toolbar.add(endTimeButton);
			toolbar.add(categoriesComboBox);
			toolbar.add(submitAuctionButton);
			this.add(toolbar, BorderLayout.NORTH);
			this.add(toolbarCardPanel, BorderLayout.CENTER);
		}
		
		public void sendAndReceiveGalleryMessage(Message message){
			//make connection, send and receive a message
			clientComms.makeClient("localhost", 4444);
			clientComms.clientSendMessage(message);
			GalleryViewMessage m = (GalleryViewMessage) clientComms.clientReceiveMessage();
			//update gallery grid
			galleryGrid.update(((GalleryViewMessage) m).items);
			CardLayout cl = (CardLayout) (toolbarCardPanel.getLayout());
			cl.show(toolbarCardPanel, "item");
			cl.show(toolbarCardPanel, "gallery");
		}
		
	}

	class GalleryPanel extends JPanel {

		public GalleryPanel() {
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		}

		public void update(ArrayList<Item> items) {
			this.removeAll();
			// define the structure for the layout of auctions, box layouts
			// within box layouts allow the grid panels to be a defined size and
			// to have the ability to scroll through the items
			for (int i = 0; i < Math.ceil((items.size() / 3) + 1); i++) {
				int row = i * 3;
				JPanel gp = new JPanel();
				gp.setLayout(new BoxLayout(gp, BoxLayout.X_AXIS));
				gp.setSize(this.getWidth(), 500);
				// each panel is a representation of the item it represents as
				// defined by the structure of a GalleryItemPanel
				GalleryItemPanel column1;
				GalleryItemPanel column2;
				GalleryItemPanel column3;
				if (items.size() > row) {
					column1 = new GalleryItemPanel(items.get(row));
					column1.setPreferredSize(new Dimension(this.getWidth() / 3,400));
					gp.add(column1);
				} else {
					gp.add(Box.createRigidArea(new Dimension(this.getWidth() / 3, 0)));
				}
				if (items.size() > row + 1) {
					column2 = new GalleryItemPanel(items.get(row + 1));
					column2.setPreferredSize(new Dimension(this.getWidth() / 3,400));
					gp.add(column2);
				} else {
					gp.add(Box.createRigidArea(new Dimension(this.getWidth() / 3, 0)));
				}
				if (items.size() > row + 2) {
					column3 = new GalleryItemPanel(items.get(row + 2));
					column3.setPreferredSize(new Dimension(this.getWidth() / 3,400));
					gp.add(column3);
				} else {
					gp.add(Box.createRigidArea(new Dimension(this.getWidth() / 3, 0)));
				}
				this.add(gp);
			}
		}
	}

	class GalleryItemPanel extends JPanel {

		Item item;

		public GalleryItemPanel(Item inputItem) {
			this.item = inputItem;
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.setBorder(BorderFactory.createLineBorder(Color.black));

			// displays the relevant details of an item in a concise way
			JPanel detailsPanel = new JPanel();
			detailsPanel.setLayout(new GridLayout(5, 2));
			detailsPanel.add(new JLabel("Category:"));
			detailsPanel.add(new JLabel(item.category));
			detailsPanel.add(new JLabel("Top Bid:"));
			if (item.bids.isEmpty()) {
				detailsPanel.add(new JLabel("<None>"));
			} else {
				detailsPanel.add(new JLabel(NumberFormat.getCurrencyInstance(Locale.UK).format(item.getTopBid().value)));
			}
			detailsPanel.add(new JLabel("Ending On:"));
			detailsPanel.add(new JLabel(dateTimeFormat.format(item.endTime)));
			detailsPanel.add(new JLabel("Sold By:"));
			detailsPanel.add(new JLabel(item.userID));
			detailsPanel.add(new JLabel("Item ID:"));
			detailsPanel.add(new JLabel(Integer.toString(item.id)));

			JLabel titleLabel = new JLabel(item.title);
			titleLabel.setAlignmentX(GalleryItemPanel.CENTER_ALIGNMENT);

			JTextArea descriptionLabel = new JTextArea(item.description);
			descriptionLabel.setEditable(false);
			descriptionLabel.setOpaque(false);
			descriptionLabel.setLineWrap(true);
			descriptionLabel.setAlignmentX(GalleryItemPanel.CENTER_ALIGNMENT);

			JButton viewButton = new JButton("View");
			//the view button displays the individual page for that item when pressed
			viewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clientComms.makeClient("localhost", 4444);
					clientComms.clientSendMessage(new ItemViewRequestMessage(item.id));
					Message m = clientComms.clientReceiveMessage();
					itemPanel.update(((ItemViewMessage) m).item);
					CardLayout cl = (CardLayout) (toolbarCardPanel.getLayout());
					cl.show(toolbarCardPanel, "item");
				}
			});
			
			//borders for a better display
			viewButton.setAlignmentX(GalleryItemPanel.CENTER_ALIGNMENT);
			titleLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
			descriptionLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
			detailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

			this.add(titleLabel);
			this.add(descriptionLabel);
			this.add(detailsPanel);
			this.add(viewButton);
		}

	}

	class ItemPanel extends JPanel {

		JFormattedTextField bidAmount;
		Item item;

		public ItemPanel() {}

		public void update(Item item) {
			this.item = item;
			draw();
		}

		public void draw() {
			this.removeAll();
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.setBorder(new EmptyBorder(50, 100, 50, 100));

			JPanel leftPanel = new JPanel();
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
			JTextArea descriptionLabel = new JTextArea(item.description);
			descriptionLabel.setEditable(false);
			descriptionLabel.setOpaque(false);
			descriptionLabel.setLineWrap(true);
			descriptionLabel.setBorder(new EmptyBorder(20, 0, 0, 20));
			leftPanel.add(descriptionLabel);

			// very similar to that in GalleryItemPanel which displays the
			// details of the item
			JPanel detailsPanel = new JPanel();
			detailsPanel.setLayout(new GridLayout(4, 2));
			detailsPanel.add(new JLabel("Category:"));
			detailsPanel.add(new JLabel(item.category));
			detailsPanel.add(new JLabel("Ending On:"));
			detailsPanel.add(new JLabel(dateTimeFormat.format(item.endTime)));
			detailsPanel.add(new JLabel("Sold By:"));
			detailsPanel.add(new JLabel(item.userID));
			detailsPanel.add(new JLabel("Item ID:"));
			detailsPanel.add(new JLabel(Integer.toString(item.id)));
			leftPanel.add(detailsPanel);

			JPanel bidsPanel = new JPanel();
			bidsPanel.setLayout(new BoxLayout(bidsPanel, BoxLayout.PAGE_AXIS));
			// the bid log shows all the bids that were placed on that
			// particular item
			JTextArea bidLog = new JTextArea();
			bidLog.setEditable(false);
			bidLog.setOpaque(false);
			bidLog.setLineWrap(true);
			JScrollPane scrollBidLog = new JScrollPane(bidLog);
			scrollBidLog.setPreferredSize(new Dimension(200, 200));
			for (int i = item.bids.size() - 1; i >= 0; i--) {
				bidLog.append(item.bids.get(i).bidder.id+ " "+ NumberFormat.getCurrencyInstance(Locale.UK).format(item.bids.get(i).value) + "\n");
			}
			JPanel sumbitBid = new JPanel();
			bidAmount = new JFormattedTextField(NumberFormat.getCurrencyInstance(Locale.UK));
			bidAmount.setValue(00.00);
			JButton bidButton = new JButton("Bid");

			bidButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						clientComms.makeClient("localhost", 4444);
						clientComms.clientSendMessage(new BidMessage(item.id, loggedInUser, NumberFormat.getCurrencyInstance(Locale.UK).parse(bidAmount.getText()).doubleValue()));
						Message m = clientComms.clientReceiveMessage();
						if (!(m instanceof ErrorMessage)) {
							itemPanel.update(((ItemViewMessage) m).item);
							CardLayout cl = (CardLayout) (toolbarCardPanel.getLayout());
							cl.show(toolbarCardPanel, "gallery");
							cl.show(toolbarCardPanel, "item");
						}
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
			});
			sumbitBid.add(bidAmount);
			sumbitBid.add(bidButton);

			bidsPanel.add(new JLabel("Bids:"));
			bidsPanel.add(scrollBidLog);
			bidsPanel.add(sumbitBid);

			JPanel main = new JPanel();
			main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));
			main.add(leftPanel);
			main.add(bidsPanel);

			JLabel titleLabel = new JLabel(item.title);
			this.add(titleLabel);
			this.add(main);
		}
	}

	class SubmitItemPanel extends JPanel {

		JTextField titleField;
		JTextArea descriptionTextArea;
		JComboBox<String> categoryField;
		JFormattedTextField startField;
		JFormattedTextField endField;
		JFormattedTextField reservePriceField;

		public SubmitItemPanel() {
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			// the panel has relevant fields for inputting information for an
			// item to the system
			JPanel detailsPanel = new JPanel();
			detailsPanel.setLayout(new GridLayout(6, 2));
			titleField = new JTextField();
			descriptionTextArea = new JTextArea();
			descriptionTextArea.setLineWrap(true);
			JScrollPane descriptionField = new JScrollPane(descriptionTextArea);
			
			categoryField = new JComboBox<String>(categories);
			startField = new JFormattedTextField(dateTimeFormat.format(new Date(new Date().getTime())));
			endField = new JFormattedTextField(dateTimeFormat.format(new Date(new Date().getTime() + 86400000)));
			reservePriceField = new JFormattedTextField(NumberFormat.getCurrencyInstance(Locale.UK));
			reservePriceField.setValue(00.00);
			detailsPanel.add(new JLabel("Title:"));
			detailsPanel.add(titleField);
			detailsPanel.add(new JLabel("Description:"));
			detailsPanel.add(descriptionField);
			detailsPanel.add(new JLabel("Category:"));
			detailsPanel.add(categoryField);
			detailsPanel.add(new JLabel("Start Time (DD/MM/YY HH:MM):"));
			detailsPanel.add(startField);
			detailsPanel.add(new JLabel("End Time (DD/MM/YY HH:MM):"));
			detailsPanel.add(endField);
			detailsPanel.add(new JLabel("Reserve Price (Include £ sign):"));
			detailsPanel.add(reservePriceField);
			detailsPanel.setMaximumSize(new Dimension(400, 400));
			JButton submitButton = new JButton("Submit");
			submitButton.setAlignmentX(LoginPanel.CENTER_ALIGNMENT);
			// if successful the gui will display the item just submitted to the
			// system
			submitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						clientComms.makeClient("localhost", 4444);
						CreateAuctionMessage cam = new CreateAuctionMessage(titleField.getText(), descriptionTextArea.getText(), (String) categoryField.getSelectedItem(), loggedInUser.id,dateTimeFormat.parse(startField.getText()),dateTimeFormat.parse(endField.getText()),NumberFormat.getCurrencyInstance(Locale.UK).parse(reservePriceField.getText()).doubleValue());
						cam.loggedInUser = loggedInUser;
						clientComms.clientSendMessage(cam);
						Message m = clientComms.clientReceiveMessage();
						if (!(m instanceof ErrorMessage)) {
							itemPanel.update(((ItemViewMessage) m).item);
							CardLayout cl = (CardLayout) (toolbarCardPanel
									.getLayout());
							cl.show(toolbarCardPanel, "item");
						}
					} catch (NumberFormatException e1) {
						e1.printStackTrace();
					} catch (ParseException e1) {
						JOptionPane.showMessageDialog(frame, e1.getMessage());
					}
				}
			});
			this.add(Box.createRigidArea(new Dimension(400, 50)));
			this.add(detailsPanel);
			this.add(submitButton);
		}
	}

}

// the NotificationThread sends a message to the server inquiring if any items
// that have ended have been won by the particular user logged in
class NotificationThread extends Thread{
	
	User loggedInUser;
	Comms clientComms;
	
	NotificationThread(User loggedInUser, Comms clientComms){
		this.loggedInUser = loggedInUser;
		this.clientComms = clientComms;
	}
	
	public void run(){
		while(true){
			clientComms.makeClient("localhost", 4444);
			clientComms.clientSendMessage(new NotificationRequestMessage(loggedInUser));
			clientComms.clientReceiveMessage();
			try {
				//the thread sleeps for a minute before trying again
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
