package com.server;

import java.util.ArrayList;
import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {

    private ArrayList<User> users = null;

    public UserAuthenticator() {
        super("warning");
        users = new ArrayList<User>();
        
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(username) && users.get(i).getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean addUser(String username, String password, String email) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(username)) {
                return false;
            }
        }
        User user = new User(username, password, email);
        users.add(user);
        return true;
    }
}
