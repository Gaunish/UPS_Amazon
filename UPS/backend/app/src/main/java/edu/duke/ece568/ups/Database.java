package edu.duke.ece568.ups;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.ResultSet;

public class Database {
    private Connection c;

    public Database(){
        c = null;
    }

    public void connectDB(){
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://db:5432/postgres?sslmode=disable","postgres", "1234");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            return;
            //System.exit(0);
        }
        System.out.println("Opened database successfully");
        deleteDB();
        initDB();
    }

    public ResultSet SelectStatement(String query){
        try{
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery(query);
            return rs;
        }
        catch (Exception e) {
            //System.err.println(e);
            return null;
        }
    }

    public String executeQuery(String query, String err){
        String out = "Success";
        try{
            Statement s = c.createStatement();
            String sql = query;
            s.executeUpdate(sql);
            s.close();
        }
        catch ( Exception e ) {
            out = err;
        }
        return out;
    } 

    public void initDB(){
        String query1 = "CREATE TABLE TRUCK(TRUCK_ID INT PRIMARY KEY NOT NULL, X INT NOT NULL, Y INT NOT NULL, STATUS VARCHAR(20) NOT NULL);";
        executeQuery(query1, "failure");

        //-1 Truck id means package has been delivered
        //-1 user id means no owner
        String query2 = "CREATE TABLE PACKAGE(PACKAGE_ID INT PRIMARY KEY NOT NULL, X INT NOT NULL, Y INT NOT NULL, TRUCK_ID INT NOT NULL, USER_ID INT NOT NULL, ITEMS VARCHAR(200));";
        executeQuery(query2, "failure");

        String query3 = "CREATE TABLE USERS(USER_ID INT PRIMARY KEY NOT NULL, USERNAME VARCHAR(100) NOT NULL, PASSWORD VARCHAR(50) NOT NULL, SALT VARCHAR(50) NOT NULL);"; 
        executeQuery(query3, "failure");
    }

    public void deleteDB(){
        String query1 = "DROP TABLE IF EXISTS TRUCK;";
        executeQuery(query1, "failure");
        String query2 = "DROP TABLE IF EXISTS PACKAGE;";
        executeQuery(query2, "failure");
        String query3 = "DROP TABLE IF EXISTS USERS;";
        executeQuery(query3, "failure");


    }
    
}
