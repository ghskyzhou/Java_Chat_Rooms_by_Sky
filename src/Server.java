import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Server
{
    private static final int PORT = 8023;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static Connection connection;

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/JavaChat";
        String username = "root";
        String password = "";

        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to the database successfully");

            System.out.println("Chat server started...");
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) new ClientHandler(serverSocket.accept(), connection).start();
            } catch(IOException e) {
                e.printStackTrace();
            }

            connection.close();
            System.out.println("Connection successfully closed");
        } catch(SQLException e) {
            System.out.println("SQL connection Error!");
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private Connection connection;

        public ClientHandler(Socket socket, Connection connection) {
            this.socket = socket;
            this.connection = connection;
        }

        public static String toID(int id) {
            String re = "";
            for(int x = 0; x < 5; x++) {
                re = Integer.toString(id%10) + re;
                id /= 10;
            }
            return re;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Users log in
                username = in.readLine();
                synchronized (clientHandlers) {
                    clientHandlers.add(this);

                    String sql2 = "SELECT * FROM log";
                    try(Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(sql2)) {
                        while(resultSet.next()){
                            this.out.println(resultSet.getString("text"));
                        }
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }

                    int dbID = 0;
                    String sql = "SELECT * FROM users where name='" + username + "'";
                    try(Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(sql)) {
                        int cnt = 0;
                        while(resultSet.next()) {
                            cnt++;
                            dbID = resultSet.getInt("id");
                            System.out.println(dbID + " " + username);
                        }
                        if(cnt == 0) {
                            System.out.println("No user found, creating a new id");
                            String sql1 = "SELECT COUNT(*) FROM users";
                            int rowCount = 0;
                            try(Statement statement1 = connection.createStatement();
                                ResultSet resultSet1 = statement1.executeQuery(sql1)) {
                                if(resultSet1.next()) rowCount = resultSet1.getInt(1);
                            } catch(SQLException e) {
                                e.printStackTrace();
                            }
                            rowCount++;
                            sql1 = "INSERT INTO users (id, name) VALUES (?, ?)";
                            try(PreparedStatement preparedStatement = connection.prepareStatement(sql1)) {
                                preparedStatement.setInt(1, rowCount);
                                preparedStatement.setString(2, username);
                                preparedStatement.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            System.out.println((rowCount) + " " + username);
                            dbID = rowCount;
                        }
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                    for (ClientHandler clientHandler : clientHandlers)
                        clientHandler.out.println("#" + username + "(ID: " + toID(dbID) + ")" + " joined to the chat room!");
                }

                // Receive and send message to all
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    if(message.charAt(0) == '?') {
                        String sql = "SELECT * FROM log WHERE text LIKE '%" + message.substring(1) + "%'";
                        int cnt = 0;
                        try(Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery(sql)) {
                            while(resultSet.next()) {
                                String getRe = resultSet.getString("text"), getTime = "";
                                int getIND = resultSet.getInt("IND") - 1;
                                if(getRe.charAt(0) != '*') continue;

                                cnt++;
                                String sql1 = "SELECT * FROM log WHERE IND = " + getIND;
                                try(Statement statement1 = connection.createStatement();
                                    ResultSet resultSet1 = statement1.executeQuery(sql1)) {
                                    while(resultSet1.next()) {
                                        getTime = resultSet1.getString("text");
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                synchronized (clientHandlers) {
                                    for (ClientHandler clientHandler : clientHandlers) {
                                        System.out.println(getTime);
                                        clientHandler.out.println("?" + getTime);
                                        clientHandler.out.println("?" + getRe);
                                    }

                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if(cnt == 0) {
                            synchronized (clientHandlers) {
                                for (ClientHandler clientHandler : clientHandlers)
                                    clientHandler.out.println("?#Can't search anything!");
                            }
                        }
                        continue;
                    }

                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("!yyyy-MM-dd HH:mm:ss");
                    String msgTime = now.format(formatter);
                    message = "*" + message;
                    synchronized (clientHandlers) {
                        for (ClientHandler clientHandler : clientHandlers)
                            clientHandler.out.println(msgTime + "\n" + message);
                    }

                    String sql1 = "SELECT COUNT(*) FROM log";
                    int rowCount = 0;
                    try(Statement statement1 = connection.createStatement();
                        ResultSet resultSet1 = statement1.executeQuery(sql1)) {
                        if(resultSet1.next()) rowCount = resultSet1.getInt(1);
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                    rowCount++;

                    String sql = "INSERT INTO log VALUES (?, ?)";
                    try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                        preparedStatement.setInt(1, rowCount);
                        preparedStatement.setString(2, msgTime);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    rowCount++;

                    sql = "INSERT INTO log VALUES (?, ?)";
                    try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                        preparedStatement.setInt(1, rowCount);
                        preparedStatement.setString(2, message);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    synchronized (clientHandlers) {
                        if(username != null) {
                            int dbID = 0;
                            String sql = "SELECT * FROM users where name='" + username + "'";
                            try(Statement statement = connection.createStatement();
                                ResultSet resultSet = statement.executeQuery(sql)) {
                                while(resultSet.next())
                                    dbID = resultSet.getInt("id");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            for (ClientHandler clientHandler : clientHandlers)
                                clientHandler.out.println("#" + username + "(ID: " + toID(dbID) + ")" + " has left the chat room.");
                            clientHandlers.remove(this);
                        }
                    }
                    if (out != null) out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}