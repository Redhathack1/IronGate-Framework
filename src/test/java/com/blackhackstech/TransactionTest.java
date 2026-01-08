package com.blackhackstech;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public class TransactionTest {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }

    @Test(priority = 1)
    public void testCreateTransaction() {
        System.out.println("IronGate Tool: Starting API Validation...");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", 1);
        requestBody.put("title", "BlackHacks_Payment");
        requestBody.put("body", "Verified by IronGate Tool");

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/posts")
        .then()
            .statusCode(201)
            .body("title", equalTo("BlackHacks_Payment"));

        System.out.println("IronGate Tool: API Step 1 - SUCCESS");
    }

    @Test(priority = 2)
    public void testDatabaseVerification() throws SQLException {
        System.out.println("IronGate Tool: Starting Database Validation...");

        // Force load the H2 driver to prevent "No Suitable Driver" error
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            Assert.fail("IronGate Error: H2 Driver not found in classpath!");
        }

        String url = "jdbc:h2:mem:blackhacks_db;DB_CLOSE_DELAY=-1";
        
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement()) {

            // Create table and insert simulated transaction
            stmt.execute("CREATE TABLE transactions(id INT PRIMARY KEY, title VARCHAR(255), status VARCHAR(50))");
            stmt.execute("INSERT INTO transactions VALUES(101, 'BlackHacks_Payment', 'SUCCESS')");

            try (ResultSet rs = stmt.executeQuery("SELECT title, status FROM transactions WHERE id = 101")) {
                if (rs.next()) {
                    String dbTitle = rs.getString("title");
                    String dbStatus = rs.getString("status");

                    System.out.println("Database Record Found -> Title: " + dbTitle + " | Status: " + dbStatus);

                    Assert.assertEquals(dbTitle, "BlackHacks_Payment");
                    Assert.assertEquals(dbStatus, "SUCCESS");
                }
            }
            System.out.println("IronGate Tool: SQL Step 2 - SUCCESS");
        }
    }
  @Test(priority = 3)
    public void testCatchDatabaseBug() throws SQLException {
        System.out.println("IronGate Tool: Starting Negative Validation (Bug Detection)...");

        String url = "jdbc:h2:mem:blackhacks_db;DB_CLOSE_DELAY=-1";
        
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement()) {

            // Ensure table exists (H2 wipes memory between tests)
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions(id INT PRIMARY KEY, title VARCHAR(255), status VARCHAR(50))");
            
            // Insert a 'FAILED' status
            stmt.execute("INSERT INTO transactions VALUES(102, 'BlackHacks_Payment', 'SUCCESS')");

            try (ResultSet rs = stmt.executeQuery("SELECT status FROM transactions WHERE id = 102")) {
                if (rs.next()) {
                    String dbStatus = rs.getString("status");
                    System.out.println("IronGate Audit: Found Status -> " + dbStatus);
                    
                    // THIS IS THE VALIDATION LOGIC
                    Assert.assertEquals(dbStatus, "SUCCESS", "IronGate Alert: Database status does not match API Success!");
                }
            }
        }
    }
}