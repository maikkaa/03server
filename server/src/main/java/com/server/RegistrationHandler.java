package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.*;

public class RegistrationHandler implements HttpHandler {

    UserAuthenticator authenticator = null;
    MessageDatabase db = null;

    public RegistrationHandler(UserAuthenticator authentication) {
        authenticator = authentication;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        String responseBody = "";
        int code = 200;
        JSONObject obj = null;

        if (t.getRequestMethod().equals("GET"))

        {
            code = 402;
            t.sendResponseHeaders(code, -1);
        } else {
            try {
                if (t.getRequestMethod().equalsIgnoreCase("POST")) {
                    Headers headers = t.getRequestHeaders();
                    String contentType = "";

                    if (headers.containsKey("Content-Type")) {
                        contentType = headers.get("Content-Type").get(0);
                    } else {
                        code = 401;
                        responseBody = "ei kkontenttia";
                    }

                    if (contentType.equalsIgnoreCase("application/json")) {

                        InputStream stream = t.getRequestBody();

                        String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));

                        stream.close();

                        if (text == null || text.length() <= 1) {
                            code = 412;
                            responseBody = "ei käyttäjätietoja";

                        } else {

                            try {
                                obj = new JSONObject(text);
                            } catch (JSONException e) {
                                System.out.println("json parse error");
                            }
                            // tarkista eihän tiedot ole tyhjiä
                            if (obj.getString("username").length() == 0 || obj.getString("password").length() == 0
                                    || obj.getString("email").length() == 0) {
                                code = 413;
                                responseBody = "ei kunnollisia käyttäjätietoja";
                            } else {
                                // luo account
                                Boolean result = (authenticator.addUser(obj.getString("username"),
                                        obj.getString("password"), obj.getString("email")));

                                if (result == false) {
                                    code = 415;
                                    responseBody = "käyttäjä on jo olemassa";
                                } else {
                                    code = 200;
                                    responseBody = "rekisteröity";
                                }
                            }       
                            
                        }
                        byte[] bytes = responseBody.getBytes("UTF-8");
                        t.sendResponseHeaders(code, bytes.length);
                        OutputStream os = t.getResponseBody();
                        os.write(bytes);
                        os.flush();
                        os.close();
                    } else {
                        code = 407;
                        responseBody = "ei oo application/json";
                    }
                } else {
                    code = 401;
                    responseBody = "only post";
                }


            } catch (SQLException e) {
                System.out.println(e.getStackTrace());
                code = 500;
                System.out.println("VIRHE!!!!!!!!!!!");
            }

            if (code >= 400) {
                byte[] bytes = responseBody.getBytes("UTF-8");
                t.sendResponseHeaders(code, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.flush();
                os.close();

            }
        }
    }
}
