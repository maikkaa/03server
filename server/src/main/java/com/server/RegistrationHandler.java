package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.*;

public class RegistrationHandler implements HttpHandler {

    UserAuthenticator authenticator = null;

    public RegistrationHandler(UserAuthenticator authentication) {
        authenticator = authentication;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        String responseBody = "";
        int code = 200;

        if (t.getRequestMethod().equals("GET"))

        {
            code = 400;
            t.sendResponseHeaders(code, -1);
        } else {
            // try {
            if (t.getRequestMethod().equalsIgnoreCase("POST")) {
                Headers headers = t.getRequestHeaders();
                String contentType = "";

                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                } else {
                    code = 400;
                    responseBody = "ei kkontenttia";
                }

                if (contentType.equalsIgnoreCase("text/plain")) {

                    InputStream stream = t.getRequestBody();

                    String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));

                    stream.close();

                    if (text == null || text.length() <= 1) {
                        code = 400;
                        responseBody = "ei käyttäjätietoja";

                    } else {

                        String[] items = text.split(":");

                        if (items[0].trim().length() > 0 && items[1].trim().length() > 0) {
                            // create account
                            if (authenticator.addUser(items[0], items[1])) {
                                t.sendResponseHeaders(code, -1);
                            } else {
                                code = 400;
                                responseBody = "väärät käyttäjätiedot";
                            }

                        } else {
                            code = 400;
                            responseBody = "väärät käyttäjätiedot";
                        }
                    }
                } else {
                    code = 400;
                    responseBody = "ei kontenttia";

                }
            }
            if (code < 200 || code > 299) {
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
