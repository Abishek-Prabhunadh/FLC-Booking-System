package furzefield;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class HomePage
{

    public static void main(String[] args)
    {
    	System.out.println("Current Working Directory: " + System.getProperty("user.dir"));
        CreateTables.createAllTables();
        CreateTables.seedMembers();
        Lessons.seedLessons();
        CreateTables.seedBookings();
       

        Scanner input = new Scanner(System.in);
        System.out.println("\n--- Welcome to Furzefield Leisure Centre (FLC) ---");

        home(input);
    }

    public static void home(Scanner input)
    {
        boolean running = true;

        while (running)
        {
            System.out.println("\nChoose an Option:");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");

            int choice = input.nextInt();

            if (choice == 1)
            {
                loginOperation(input);
                running = false;
            }
            else if (choice == 2)
            {
                registrationOperation(input);
                running = false;
            }
            else if (choice == 3)
            {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            else
            {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void registrationOperation(Scanner input)
    {
        System.out.println("\n*** User Registration ***");
        System.out.print("Enter Your Username: ");
        String username = input.next();
        System.out.print("Enter Your Password: ");
        String password = input.next();

        MainApp  mn = new MainApp();
        int id = addMember(username, password);
        if (id != -1)
        {
            mn.start(id);
        }
        else
        {
            System.out.println("Registration failed. Returning to menu...");
            home(input);
        }
    }

    public static void loginOperation(Scanner input)
    {
        System.out.println("\n*** User Login ***");
        System.out.print("Enter Your Username: ");
        String username = input.next();
        System.out.print("Enter Your Password: ");
        String password = input.next();

        String sql = "SELECT * FROM members WHERE user_name=? AND password=?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql))
        {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    int userId = rs.getInt("member_id");
                    System.out.println("\nLogin Success! Welcome, " + username);
                    new MainApp().start(userId);
                } else {
                    System.out.println("Invalid Credentials. Please try again.\n");
                    home(input);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    public static int addMember(String userName, String password)
    {
        String sql = "INSERT INTO members (user_name, password) VALUES(?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {

            ps.setString(1, userName);
            ps.setString(2, password);

            int rowsUpdate = ps.executeUpdate();

            if (rowsUpdate > 0)
            {
                try (ResultSet rs = ps.getGeneratedKeys()) 
                {
                    if (rs.next()) 
                    {
                        int userId = rs.getInt(1);
                        System.out.println("Registration Success!");
                        return userId;
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error during registration: " + e.getMessage());
        }
        return -1;
    }
}
