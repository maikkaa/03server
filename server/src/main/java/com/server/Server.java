package com.server;

import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import java.io.*;

public class Server implements HttpHandler {

    StringBuilder textDump = new StringBuilder("");
    ArrayList<String> lista = new ArrayList<String>();

    @Override
    public void handle(HttpExchange t) throws IOException {

        String requestParamValue = null;

        if (t.getRequestMethod().equals("POST")) {

            handlePOSTRequest(t);
            handleResponsePOST(t);

        } else if (t.getRequestMethod().equals("GET")) {

            requestParamValue = handleGetRequest(t);
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

    private String handleGetRequest(HttpExchange httpExchange) throws IOException {

        // return httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];

        String response = "";
        for (String j : lista) {
            response += j + "\n";

        }
        return response;

    }

    private void handlePOSTRequest(HttpExchange httpExchange) {

        String text = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));

        // textDump.append(text);
        lista.add(text);

    }

    public static void main(String[] args) throws Exception {
        try {


            // tee http serveri portille 8001

            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = serverSSLContext(args[0], args[1]);
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
