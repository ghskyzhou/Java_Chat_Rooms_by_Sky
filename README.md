# Java Chat Room
#### Made by Skyzhou
---
**IntelliJ IDEA Project**

Based on JavaFX, MySQL.

*An CUHKSZ CSC1004 Assignment ([Link](https://guiliang.github.io/courses/cuhk-csc-1004/project-topics/java_chat_room.html))*

## Introduction

A simple Java chatroom with GUI based on JavaFX. Two codes included.

Server code running on local port 8023, with JDBC's MySQL "jdbc:mysql://localhost:3306/JavaChat". Thread and socket are applied.

Client code can be run for serval times, which are not conflicted with each other. Socket still using local port 8023. After entered the username you will be able to send messages in the chatroom, and ID will be assigned automatically.

You can use "Ctrl + F" to start a search window in the client, where you can search for any messages or users sent before.

The MySQL database is built on local MySQL. So you need to create a database *JavaChat* before starting the server code, and tables *log* and *users* are required. You can initialize the database like this (if you have MySQL):

```sql
CREATE DATABASE JavaChat;
USE JavaChat;
CREATE TABLE users(id INT, name VARCHAR(100));
CREATE TABLE log(IND INT, text VARCHAR(1000));
```

After initialize the database, you can launch IDEA and open the project. Set your own JavaFX library. Run the server.java using IDEA's run "Server". Then run Main.java using IDEA's run "Run JavaFX".

## Functions

- **Basic Features**
  - Multithreading Implementation
  - Chat Room Functions
  - Message Display
- **Advanced Features**
  - Chat Record Storage
  - Search Function

## Test images

<img src="https://github.com/ghskyzhou/Java-Chat-Rooms-by-Sky/blob/main/img/ClientTest.png?raw=true" alt="ClientTest" width="300">
<img src="https://github.com/ghskyzhou/Java-Chat-Rooms-by-Sky/blob/main/img/SearchTest.png?raw=true" alt="SearchTest" width="250">

---
  ### Thank [Jake](https://github.com/littlestarjake) and  [SRQ](https://github.com/SRQhuajiGabaxi) for testing the code!