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

    public String executeStatement(String query, String err){
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
        String query1 = "CREATE TABLE TRUCK(TRUCK_ID INT PRIMARY KEY NOT NULL, WHID INT NOT NULL, STATUS VARCHAR(20) NOT NULL,X INT NOT NULL, Y INT NOT NULL);";
        executeStatement(query1, "failure");

        //-1 Truck id means package has been delivered
        //-1 user id means no owner
        String query2 = "CREATE TABLE PACKAGE(PACKAGE_ID BIGINT PRIMARY KEY NOT NULL, X INT NOT NULL, Y INT NOT NULL, TRUCK_ID INT NOT NULL, USER_NAME VARCHAR(100) NOT NULL, STATUS VARCHAR(25) NOT NULL);";// status can be pickup, loading, delivering, delivered
        executeStatement(query2, "failure");

        String query3 = "CREATE TABLE USERS(USER_ID INT PRIMARY KEY NOT NULL, USERNAME VARCHAR(100) NOT NULL, PASSWORD VARCHAR(128) NOT NULL, SALT VARCHAR(50) NOT NULL);"; 
        executeStatement(query3, "failure");

        String query4 = "CREATE TABLE PRODUCT(PRODUCT_ID SERIAL PRIMARY KEY NOT NULL, PACKAGE_ID BIGINT NOT NULL, DESCRIPTION VARCHAR(200), COUNT INT NOT NULL, CONSTRAINT PACK FOREIGN KEY(PACKAGE_ID) REFERENCES PACKAGE(PACKAGE_ID));";
        executeStatement(query4, "failure");
    }

    public void deleteDB(){
        String query1 = "DROP TABLE IF EXISTS TRUCK;";
        executeStatement(query1, "failure");
        String query2 = "DROP TABLE IF EXISTS PACKAGE;";
        executeStatement(query2, "failure");
        String query3 = "DROP TABLE IF EXISTS USERS;";
        executeStatement(query3, "failure");
        String query4 = "DROP TABLE IF EXISTS PRODUCT;";
        executeStatement(query4, "failure");
    }  
}
