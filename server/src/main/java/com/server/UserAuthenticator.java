package com.server;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {

    private MessageDatabase db = null;

    public UserAuthenticator() {
        super("warning");
        db = MessageDatabase.getInstance();
    }

    @Override
    public boolean checkCredentials(String username, String password) {

        boolean isValidUser;
        try {
            isValidUser = db.authenticateUser(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return isValidUser;
    }

    public boolean addUser(String username, String password, String email) throws JSONException, SQLException {

        boolean result = db
                .setUser(new JSONObject().put("username", username).put("password", password).put("email", email));

        if (!result) {
            return false;
        }
        return true;
    }
}
