package com.server;

import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.sql.SQLException;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class Server implements HttpHandler {

    StringBuilder textDump = new StringBuilder("");
    private MessageDatabase db = MessageDatabase.getInstance();

    @Override
    public void handle(HttpExchange t) throws IOException {
        String requestParamValue = null;

        if (t.getRequestMethod().equals("POST")) {

            try {
                handlePOSTRequest(t);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            handleResponsePOST(t);

        } else if (t.getRequestMethod().equals("GET")) {

            try {
                requestParamValue = handleGetRequest(t);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            handleResponseGET(t, requestParamValue);
        } else {
            handleResponse400(t, "not supported");
        }
    }

    private void handleResponseGET(HttpExchange httpExchange, String requestParamValue) throws IOException {

        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();
        // htmlBuilder.append("<html>")
        // .append("<body>")
        // .append("<h1>")
        // .append("Returning payload ")
        htmlBuilder.append(requestParamValue);
        // .append("</h1>")
        // .append("</body>")
        // .append("</html>");

        String htmlResponse = htmlBuilder.toString();

        byte[] bytes = htmlResponse.getBytes("UTF-8");
        httpExchange.sendResponseHeaders(200, bytes.length);

        outputStream.write(htmlResponse.getBytes("UTF-8"));

        outputStream.flush();

        outputStream.close();
    }

    private void handleResponsePOST(HttpExchange httpExchange) throws IOException {

        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();

        // htmlBuilder.append("<html>")
        // .append("<body>")
        htmlBuilder.append(textDump);
        // .append("</h1>")
        // .append("</body>")
        // .append("</html>");

        String htmlResponse = htmlBuilder.toString();

        httpExchange.sendResponseHeaders(200, htmlResponse.length());

        outputStream.write(htmlResponse.getBytes());

        outputStream.flush();

        outputStream.close();
    }

    private void handleResponse400(HttpExchange httpExchange, String requestParamValue) throws IOException {

        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();
        // htmlBuilder.append("<html>")
        // .append("<body>")
        // .append("<h1>")
        // .append("Returning payload ")
        htmlBuilder.append(requestParamValue);
        // .append("</h1>")
        // .append("</body>")
        // .append("</html>");

        String htmlResponse = htmlBuilder.toString();

        httpExchange.sendResponseHeaders(400, htmlResponse.length());

        outputStream.write(htmlResponse.getBytes());

        outputStream.flush();

        outputStream.close();

    }

    private String handleGetRequest(HttpExchange httpExchange) throws IOException, SQLException {

        return db.getMessages().toString();

    }

    private void handlePOSTRequest(HttpExchange httpExchange) throws IOException, SQLException {

        String text = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        try {
            // tekstistä json objecti
            JSONObject teksti = new JSONObject(text);
            // tarkista että onhan tekstin latitude ja longitude doubleja & onko "sent" oikeassa muodossa
            if (!Double.isNaN(teksti.optDouble("latitude")) && !Double.isNaN(teksti.optDouble("longitude"))  && teksti.optString("sent") != null && teksti.optString("sent").matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z")) {
                // luo tekstistä warningmessage ja laita se databaseen jsonobjectina
                WarningMessage msg = new WarningMessage(teksti);
                db.setMessage(msg.json());
                httpExchange.sendResponseHeaders(200, 0);
            } else {
                httpExchange.sendResponseHeaders(469, 0);
                System.out.println("ei oo double");
            }
        } catch (JSONException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws Exception {
        try {

            // tee https serveri portille 8001

            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = serverSSLContext("C:/users/MIKA/keystore.jks", "lol123");
            // SSLContext sslContext = serverSSLContext(args[0], args[1]);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });

            // luo konteksti
            HttpContext context = server.createContext("/warning", new Server());

            // tee auth instanssi
            UserAuthenticator authentication = new UserAuthenticator();
            context.setAuthenticator(authentication);

            // tee path rekisteröinnille

            server.createContext("/registration", new RegistrationHandler(authentication));

            // defaultti juttu
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static SSLContext serverSSLContext(String file, String password) throws Exception {
        char[] passphrase = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(file), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }
}
