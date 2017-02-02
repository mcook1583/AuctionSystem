import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DataPersistence {

	ArrayList<User> registeredUsers;
	ArrayList<Item> items;
	ArrayList<String> activity;

	public DataPersistence() {
		//instantiate arraylists for each collection of data stored
		registeredUsers = new ArrayList<User>();
		items = new ArrayList<Item>();
		activity = new ArrayList<String>();
		// read in the data from file into the arraylists, if the files don't
		// exist the arraylists remain empty
		try {
			registeredUsers = readUsersFromFile();
		} catch (Exception e) {}
		try {
			items = readItemsFromFile();
		} catch (Exception e) {}
		try {
			activity = readActivityFromFile();
		} catch (Exception e) {}
	}

	public ArrayList<User> returnUsers() {
		return registeredUsers;
	}

	public ArrayList<Item> returnItems() {
		return items;
	}

	public ArrayList<String> returnLog() {
		return activity;
	}

	//writes the arraylist passed in to a text file of the name passed in
	public void writeToFile(String name, ArrayList<?> list) {
		try {
			FileOutputStream fout = new FileOutputStream("./" + name + ".txt");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(list);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<User> readUsersFromFile() throws Exception {
		//reads in the arraylist of users from the users.txt file
		try {
			FileInputStream fis = new FileInputStream("./users.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			ArrayList<User> users = (ArrayList<User>) ois.readObject();
			ois.close();
			return users;
		} catch (Exception e) {
			throw e;
		}
	}

	private ArrayList<Item> readItemsFromFile() throws Exception {
		//reads in the arraylist of items from the items.txt file
		try {
			FileInputStream fis = new FileInputStream("./items.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			ArrayList<Item> items = (ArrayList<Item>) ois.readObject();
			ois.close();
			return items;
		} catch (Exception e) {
			throw e;
		}
	}

	private ArrayList<String> readActivityFromFile() throws Exception {
		//reads in the arraylist of log activity from the activity.txt file
		try {
			FileInputStream fis = new FileInputStream("./activity.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			ArrayList<String> activity = (ArrayList<String>) ois.readObject();
			ois.close();
			return activity;
		} catch (Exception e) {
			throw e;
		}
	}

}