package ru.stk.data;

public class User {

    private String login;
    private String pass;
    private String firstname;
    private String lastname;

    public User(String login, String password, String firstname, String lasttname) {
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
