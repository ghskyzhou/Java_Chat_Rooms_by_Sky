import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.Optional;

public class Main extends Application {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private TextFlow textFlow;
    private TextField inputField;
    private ScrollPane scrollPane;

    private TextFlow searchTextFlow;
    private TextField searchInputField;

    private String username;
    private String message;
    private String localIP;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        Scene emptyScene = new Scene(new StackPane(), 1, 1);
        primaryStage.setScene(emptyScene);
        primaryStage.getIcons().add(new Image("file:./img/Wife.jpg"));
        primaryStage.show();

        username = inputUsername(primaryStage);
        if(username == null) System.exit(0);
        System.out.println(username);

        primaryStage.setTitle("Java Chat Rooms - " + username);

        textFlow = new TextFlow();
        textFlow.setPrefHeight(390);

        scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        inputField = new TextField();
        inputField.setPromptText("Please Input...");
        inputField.setPrefWidth(420);
        inputField.setPrefHeight(30);
        inputField.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER) sendMessage();
        });

        Button sendButton = new Button("Send");
        sendButton.setPrefHeight(30);
        sendButton.setOnAction(e -> sendMessage());

        Label searchHint = new Label("Press Ctrl + F to search");

        HBox inputLayout = new HBox(10);
        inputLayout.getChildren().addAll(inputField, sendButton);

        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(10));
        layout.getChildren().addAll(scrollPane, inputLayout, searchHint);

        Scene scene = new Scene(layout, 500, 500);
        primaryStage.setScene(scene);

        scene.setOnKeyPressed(e -> {
            if(e.isControlDown() && e.getCode() == KeyCode.F) {
                openSearchWindow(primaryStage);
            }
        });

        primaryStage.show();
        Platform.runLater(() -> inputField.requestFocus());

        connectToServer();
    }

    public String inputUsername(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Username");
        dialog.setHeaderText("Welcome to Java Chat Rooms\nMade by Sky");
        dialog.setContentText("Please enter your username: \n(DO NOT USE ANY SYMBOL PLEASE)");

        Image custumImage = new Image("file:./img/Touhou_Flandre.jpg");
        ImageView imageView = new ImageView(custumImage);

        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        dialog.getDialogPane().setGraphic(imageView);

        dialog.initOwner(primaryStage);

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void sendMessage() {
        String message = inputField.getText();
        if(!message.isEmpty()) {
            out.println(username + ": " + message);
            inputField.clear();
        }
    }

    private void receiveMessages() {
        try {
            while((message = in.readLine()) != null) {
                System.out.println(message);
                message = message + "\n";
                Text text1 = new Text(message.substring(1));
                if(message.charAt(0) == '!') {
                    text1.setFont(Font.font("Arial", 16));
                    text1.setFill(Color.GRAY);
                } else if(message.charAt(0) == '*') {
                    text1.setFont(Font.font("Arial", 20));
                } else if(message.charAt(0) == '#') {
                    text1.setFont(Font.font("Arial", 16));
                    text1.setFill(Color.BLUE);
                } else if(message.charAt(0) == '?') { // Search Part
                    Text text2 = new Text(message.substring(2));
                    if(message.charAt(1) == '!') {
                        text2.setFont(Font.font("Arial", 16));
                        text2.setFill(Color.GRAY);
                    } else if(message.charAt(1) == '*') {
                        text2.setFont(Font.font("Arial", 20));
                    } else if(message.charAt(1) == '#') {
                        text2.setFont(Font.font("Arial", 16));
                        text2.setFill(Color.BLUE);
                    }
                    Platform.runLater(() -> {
                        searchTextFlow.getChildren().add(text2);
                        new Thread(() -> {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                    continue;
                } else continue;

                Platform.runLater(() -> {
                    textFlow.getChildren().add(text1);
                    new Thread(() -> {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Platform.runLater(() -> scrollPane.setVvalue(1.0));
                    }).start();
                });

            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void openSearchWindow(Stage owner) {
        Stage searchStage = new Stage();
        searchStage.setTitle("Search");

        searchTextFlow = new TextFlow();
        searchTextFlow.setPrefHeight(290);

        ScrollPane searchScrollPane = new ScrollPane(searchTextFlow);
        searchScrollPane.setFitToWidth(true);
        searchScrollPane.setPrefHeight(300);

        searchInputField = new TextField();
        searchInputField.setPromptText("Please Search...");
        searchInputField.setPrefWidth(300);
        searchInputField.setPrefHeight(30);
        searchInputField.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER) sendSearch();
        });

        Button sendButton = new Button("Search");
        sendButton.setPrefHeight(30);
        sendButton.setOnAction(e -> sendSearch());

        Label caseHint = new Label("Case-insensitive");

        HBox inputLayout = new HBox(10);
        inputLayout.getChildren().addAll(searchInputField, sendButton);

        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(10));
        layout.getChildren().addAll(inputLayout, searchScrollPane, caseHint);

        Scene scene = new Scene(layout, 400, 400);
        searchStage.setScene(scene);

        searchStage.initOwner(owner);
        searchStage.initModality(Modality.APPLICATION_MODAL);
        searchStage.getIcons().add(new Image("file:./img/Question.png"));
        searchStage.show();
    }

    private void sendSearch() {
        String searchMessage = searchInputField.getText();
        searchTextFlow.getChildren().clear();
        if(!searchMessage.isEmpty()) {
            out.println("?" + searchMessage);
            searchInputField.clear();
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8023);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(username);

            Thread receiveThread = new Thread(this::receiveMessages);
            receiveThread.setDaemon(true);
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            Text text1 = new Text("Unable to connect the server!\n");
            text1.setFont(Font.font("Arial", 20));
            text1.setFill(Color.RED);

            textFlow.getChildren().add(text1);
        }
    }

    public void stop() {
        try {
            if(socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}