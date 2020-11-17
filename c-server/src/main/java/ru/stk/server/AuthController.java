package ru.stk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.data.User;

import java.util.ArrayList;
import java.util.HashMap;

public class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    HashMap<String, User> users = new HashMap<>();

    public void init() {
        for (User user : receiveUsers()) {
            users.put(user.getLogin(), user);
        }
    }

    public boolean checkUser(String login, String password) {
        User user = users.get(login);
        if (user != null && user.isPasswordCorrect(password)) {
            logger.info("User " + login + " authorised");
            return true;
        }
        logger.debug("Authorisation for user " + login + " failed");
        return false;
    }

    private ArrayList<User> receiveUsers() {
        logger.info("Users received from DB");
        return DBService.getUsers();
    }
    
}
