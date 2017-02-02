import java.io.Serializable;

public class User implements Serializable {

	String name;
	String familyName;
	String id;
	char[] password;

	public User(String name, String familyName, String id, char[] password) {
		this.name = name;
		this.familyName = familyName;
		this.id = id;
		this.password = password;
	}

	// returns both name and family name separated by a space to save repeating
	// code
	public String getFullName() {
		return name + " " + familyName;
	}

	public String getName() {
		return name;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getId() {
		return id;
	}

	public char[] getPassword() {
		return password;
	}
}
