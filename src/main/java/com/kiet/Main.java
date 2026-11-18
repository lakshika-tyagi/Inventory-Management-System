package com.kiet;


import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL =
            "jdbc:mysql://localhost:3306/retail_db";

    static final String USER = "root";
    static final String PASSWORD = "root";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    // Establish database connection
    static void connect() throws SQLException {

        con = DriverManager.getConnection(
                URL, USER, PASSWORD
        );

        System.out.println("Database Connected Successfully!");
    }

    // (a) Insert at least three product records
    static void insertProducts() {

        String sql = "INSERT INTO Product VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            int[][] data = {
                    {106, 20, 0, 0, 0},
                    {107, 15, 0, 0, 0},
                    {108, 40, 0, 0, 0}
            };

            String[] names = {"Monitor", "USB Cable", "Printer"};
            String[] categories = {
                    "Electronics", "Accessories", "Electronics"
            };

            double[] prices = {12000, 300, 8000};

            for (int i = 0; i < 3; i++) {

                ps.setInt(1, data[i][0]);
                ps.setString(2, names[i]);
                ps.setString(3, categories[i]);
                ps.setInt(4, data[i][1]);
                ps.setDouble(5, prices[i]);

                try {
                    ps.executeUpdate();
                    System.out.println(
                            names[i] + " inserted successfully!"
                    );

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println(
                            "Product ID " + data[i][0]
                                    + " already exists!"
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (b) Retrieve products by category and quantity < 20
    static void retrieveProducts() {

        System.out.print("Enter category: ");
        String category = sc.nextLine();

        String sql = "SELECT * FROM Product " +
                "WHERE category = ? AND quantity < 20 " +
                "ORDER BY price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category);

            ResultSet rs = ps.executeQuery();

            System.out.println("\nProduct Details:");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println(
                        "ID: " + rs.getInt("product_id") +
                                ", Name: " + rs.getString("product_name") +
                                ", Category: " + rs.getString("category") +
                                ", Quantity: " + rs.getInt("quantity") +
                                ", Price: " + rs.getDouble("price")
                );
            }

            if (!found) {
                System.out.println("No matching products found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (c) Update product stock
    static void updateStock() {

        System.out.print("Enter Product ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter quantity to add: ");
        int qty = Integer.parseInt(sc.nextLine());

        if (qty <= 0) {
            System.out.println("Quantity must be positive!");
            return;
        }

        String sql = "UPDATE Product " +
                "SET quantity = quantity + ? " +
                "WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, qty);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Stock updated successfully!");
            } else {
                System.out.println("Product does not exist!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (d) Search and update product price
    static void searchAndUpdatePrice() {

        System.out.print("Enter Product ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter new price: ");
        double newPrice = Double.parseDouble(sc.nextLine());

        if (newPrice <= 0) {
            System.out.println("Price must be greater than zero!");
            return;
        }

        String searchSql =
                "SELECT * FROM Product WHERE product_id = ?";

        try (PreparedStatement ps =
                     con.prepareStatement(searchSql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println("Existing Product Details:");
                System.out.println(
                        "Name: " + rs.getString("product_name")
                );

                System.out.println(
                        "Old Price: " + rs.getDouble("price")
                );

                String updateSql =
                        "UPDATE Product SET price = ? " +
                                "WHERE product_id = ?";

                try (PreparedStatement updatePs =
                             con.prepareStatement(updateSql)) {

                    updatePs.setDouble(1, newPrice);
                    updatePs.setInt(2, id);

                    updatePs.executeUpdate();
                }

                // Display updated product details
                try (PreparedStatement displayPs =
                             con.prepareStatement(searchSql)) {

                    displayPs.setInt(1, id);

                    ResultSet updatedRs =
                            displayPs.executeQuery();

                    if (updatedRs.next()) {
                        System.out.println("Updated Product Details:");
                        System.out.println(
                                "ID: " +
                                        updatedRs.getInt("product_id")
                        );
                        System.out.println(
                                "Name: " +
                                        updatedRs.getString("product_name")
                        );
                        System.out.println(
                                "Category: " +
                                        updatedRs.getString("category")
                        );
                        System.out.println(
                                "Quantity: " +
                                        updatedRs.getInt("quantity")
                        );
                        System.out.println(
                                "New Price: " +
                                        updatedRs.getDouble("price")
                        );
                    }
                }

            } else {
                System.out.println("Product does not exist!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (e) Aggregate query
    static void calculateInventoryValue() {

        String sql = "SELECT COALESCE(SUM(quantity * price), 0) " +
                "AS total_value FROM Product";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println(
                        "Total Inventory Value: " +
                                rs.getDouble("total_value")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (e) Delete product only if quantity = 0
    static void deleteProduct() {

        System.out.print("Enter Product ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String searchSql =
                "SELECT quantity FROM Product WHERE product_id = ?";

        try (PreparedStatement ps =
                     con.prepareStatement(searchSql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                int quantity = rs.getInt("quantity");

                if (quantity == 0) {

                    String deleteSql =
                            "DELETE FROM Product " +
                                    "WHERE product_id = ? AND quantity = 0";

                    try (PreparedStatement deletePs =
                                 con.prepareStatement(deleteSql)) {

                        deletePs.setInt(1, id);

                        int rows = deletePs.executeUpdate();

                        if (rows > 0) {
                            System.out.println(
                                    "Product deleted successfully!"
                            );
                        }
                    }

                } else {
                    System.out.println(
                            "Cannot delete! Product quantity is not zero."
                    );
                }

            } else {
                System.out.println("Product does not exist!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Main menu
    public static void main(String[] args) {

        try {
            connect();

            int choice;

            do {
                System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert Product Records");
                System.out.println("2. Retrieve Products");
                System.out.println("3. Update Product Stock");
                System.out.println("4. Search and Update Product Price");
                System.out.println("5. Calculate Inventory Value");
                System.out.println("6. Delete Product");
                System.out.println("7. Exit");

                System.out.print("Enter your choice: ");

                choice = Integer.parseInt(sc.nextLine());

                switch (choice) {

                    case 1:
                        insertProducts();
                        break;

                    case 2:
                        retrieveProducts();
                        break;

                    case 3:
                        updateStock();
                        break;

                    case 4:
                        searchAndUpdatePrice();
                        break;

                    case 5:
                        calculateInventoryValue();
                        break;

                    case 6:
                        deleteProduct();
                        break;

                    case 7:
                        System.out.println("Exiting...");
                        break;

                    default:
                        System.out.println("Invalid Choice!");
                }

            } while (choice != 7);

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();

        } finally {

            try {
                if (con != null) {
                    con.close();
                    System.out.println("Database Connection Closed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
