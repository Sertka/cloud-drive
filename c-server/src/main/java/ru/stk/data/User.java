package ru.stk.data;

/**
 * Stores user data
 */
public class User {

    private final String login;
    private final String pass;
    private final String firstname;
    private final String lastname;

    public User(String login, String password, String firstname, String lastname) {
        this.login = login;
        this.pass = password;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getLogin() {
        return login;
    }

    public String getFirstName() {
        return firstname;
    }

    public String getLastName() {
        return lastname;
    }

    public boolean isPasswordCorrect(String password) {
        return this.pass.equals(password);
    }
}
