package main;

import javafx.util.Pair;
import main.graph.Graph;
import org.glassfish.jersey.internal.util.Base64;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";


    public static String getTokenFromHeader(ContainerRequestContext requestContext) {
        List<String> authHeader = requestContext.getHeaders().get(AUTHORIZATION_HEADER_KEY);
        if (authHeader == null || authHeader.size() <= 0) {
            Response unauthorizedStatus = Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Don't have rights for this resource")
                    .build();
            requestContext.abortWith(unauthorizedStatus);
          return "";
        }
        String authToken = authHeader.get(0);
        return authToken;

    }

    public static String getEmailFromToken(String authToken) {
        authToken = authToken.replaceFirst(AUTHORIZATION_HEADER_PREFIX, "");

        String decodedString = Base64.decodeAsString(authToken);
        StringTokenizer tokenizer = new StringTokenizer(decodedString, ":");
        String email = tokenizer.nextToken();
        return email;
    }

    public static void importSQL(Connection conn, InputStream in) throws SQLException {
        Scanner s = new Scanner(in);
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        Statement st = null;
        try {
            st = conn.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }

                if (line.trim().length() > 0) {
                    st.execute(line);
                }
            }
        } finally {
            if (st != null) st.close();
        }
    }

    public static Connection initializeDatabase(Connection connection, @Context ServletContext servletContext) {

        String url = "jdbc:mysql://localhost:3306/javabase?" + "allowPublicKeyRetrieval=true&useSSL=false";
        String username = RailwayApplication.properties.getProperty("USERNAME");
        String password = RailwayApplication.properties.getProperty("PASSWORD");


        System.out.println("Connecting database...");
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(url, username, password);

            String path = servletContext.getRealPath("/");
            String pathToSql = new File(path).getParentFile().getParentFile().getParent() + "/project.sql";
            File initialFile = new File(pathToSql);
            InputStream targetStream = new FileInputStream(initialFile);
            importSQL(connection, targetStream);
            System.out.println("Database connected!\n");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cannot connect the database!", e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static Pair<DataInputStream, DataOutputStream> initializeSocket (@Context ServletContext servletContext, DataOutputStream dout, DataInputStream din) {
        System.out.println("Connecting socket...");
        Pair<DataInputStream, DataOutputStream> pair = new Pair<>(din, dout);
        try {

            // starting new Thread
            String path = servletContext.getRealPath("/");
            String pathToRoot = new File(path).getParentFile().getParentFile().getParent();
            ProcessBuilder builder = new ProcessBuilder(RailwayApplication.properties.getProperty("PYTHON"), pathToRoot + "/mapgenerator.py");
            builder.directory(new File(pathToRoot));
            builder.start();

            // Wait until the python script will run
            TimeUnit.SECONDS.sleep(5);

            // Initialize socket
            int port = Integer.parseInt(RailwayApplication.properties.getProperty("SOCKET_PORT"));
            Socket socket = new Socket("localhost", port);
            pair = new Pair<>(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
            System.out.println("Socket connected!\n");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return pair;
    }


    public static Graph initializeGraph() {

        Graph graph = new Graph();
        graph.addVertex("Nur-Sultan");
        graph.addVertex("Kokshetau");
        graph.addVertex("Petropavl");
        graph.addVertex("Kostanay");
        graph.addVertex("Pavlodar");
        graph.addVertex("Uralsk");
        graph.addVertex("Aktobe");
        graph.addVertex("Atyrau");
        graph.addVertex("Aktobe");
        graph.addVertex("Aktau");
        graph.addVertex("Kyzylorda");
        graph.addVertex("Shymkent");


        graph.addVertex("Taraz");
        graph.addVertex("Almaty");
        graph.addVertex("Karaganda");
        graph.addVertex("Semipalatinsk");
        graph.addVertex("Ust-kamenogorsk");

        graph.addEdge("Nur-Sultan", "Kokshetau");
        graph.addEdge("Nur-Sultan", "Karaganda");
        graph.addEdge("Nur-Sultan", "Pavlodar");
        graph.addEdge("Nur-Sultan", "Kyzylorda");
        graph.addEdge("Kokshetau", "Petropavl");
        graph.addEdge("Kokshetau", "Kostanay");
        graph.addEdge("Petropavl", "Kostanay");
        graph.addEdge("Kostanay", "Uralsk");
        graph.addEdge("Kostanay", "Aktobe");
        graph.addEdge("Uralsk", "Aktobe");
        graph.addEdge("Uralsk", "Atyrau");
        graph.addEdge("Aktobe", "Atyrau");
        graph.addEdge("Atyrau", "Aktau");
        graph.addEdge("Aktau", "Kyzylorda");
        graph.addEdge("Kyzylorda", "Shymkent");
        graph.addEdge("Shymkent", "Taraz");
        graph.addEdge("Taraz", "Almaty");
        graph.addEdge("Almaty", "Karaganda");
        graph.addEdge("Almaty", "Ust-kamenogorsk");
        graph.addEdge("Ust-kamenogorsk", "Semipalatinsk");
        graph.addEdge("Semipalatinsk", "Pavlodar");

        return graph;
    }
    public static void makeLog(HttpHeaders headers, String message, String requestType, @Context ServletContext servletContext,String url){
        String contentType = "";
        if (headers.getRequestHeader(HttpHeaders.CONTENT_TYPE)==null){
            contentType = "does not accept any content";
        }else{
            contentType = headers.getRequestHeader(HttpHeaders.CONTENT_TYPE).get(0);
        }

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        String currentDate = formatter.format(date);



        String path = servletContext.getRealPath("/");
        String pathToRoot = new File(path).getParentFile().getParentFile().getParent();





        try
        {
            String filename= pathToRoot + "/logger.txt";
            FileWriter fw = new FileWriter(filename,true); //the true will append the new data
            fw.write(message + "\n" +
                    "Request type: " + requestType + "\n" +
                    "Content type: " + contentType + "\n" +
                    "Date: " + currentDate+"\n" +
                    "URL: "+ url +"\n\n");//appends the string to the file
            fw.close();
        }
        catch(Exception ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }

    }
}
