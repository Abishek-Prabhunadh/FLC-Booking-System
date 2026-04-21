package furzefield;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.List;

public class MainApp
{

	    public void start(int userId)
	    {

	        BookingManager bookingManager = new BookingManager();
	        Lessons les = new Lessons();

	        Scanner sc = new Scanner(System.in);

	        while (true)
	        {
	            System.out.println("Your Member ID is: " + userId+"\n");
	            System.out.println("1. Book lesson");
	            System.out.println("2. Change/Cancel booking");
	            System.out.println("3. Attend lesson");
	            System.out.println("4. Show lesson report");
	            System.out.println("5. Show income report");
	            System.out.println("6. Exit");

	            System.out.print("Choose option: ");
	            int choice = sc.nextInt();
	            sc.nextLine();

	            if (choice == 1)
	            {

	                System.out.println("1. View Timetable by Day (Saturday/Sunday)");
	                System.out.println("2. View Timetable by Exercise Type");

	                int option = sc.nextInt();
	                sc.nextLine();

	                if (option == 1)
	                {
	                    System.out.print("Enter Day: ");
	                    String day = sc.nextLine();
	                    les.showLessons("SELECT * FROM lessons WHERE lesson_day LIKE ?", day);
	                    handleBooking(sc, userId, bookingManager);
	                }
	                else if (option == 2)
	                {
	                    System.out.print("Enter Exercise (Yoga, Zumba, etc.): ");
	                    String type = sc.nextLine();
	                    les.showLessons("SELECT * FROM lessons WHERE exercise_type LIKE ?", type);
	                    handleBooking(sc, userId, bookingManager);
	                }

	            }
	        }
	    }
	    
	    
	    
	    
	    public void handleBooking(Scanner sc, int mbId, BookingManager bm)
	    {
	    	System.out.print("\nEnter Lesson ID to book\n");
	        String input = sc.next();
	        
	        if (input.startsWith("L") || input.startsWith("l"))
	        {
	            input = input.substring(1);
	        }

	        int lsnId = Integer.parseInt(input);

			String result = bm.bookLesson(mbId, lsnId);

			switch (result)
			{
			    case "SUCCESS": System.out.println("\nSuccess: Booking confirmed!");
			    break;
			        
			    case "ALREADY_BOOKED": System.out.println("\nError: You have already booked this specific lesson.");
			    break;
			        
			    case "FULL": System.out.println("\nError: This lesson is already full (Max 4 students only).");
			    break;
			        
			    case "TIME_CONFLICT": System.out.println("\nError: Time conflict! You already have a lesson at this time.");
			    break;
			        
			    case "INSERT_FAILED": System.out.println("\nError: The booking could not be saved to the database.");
			        break;
			        
			    case "DB_ERROR":  System.out.println("\nError: Database connection issue. Please try again later.");
			    break;
			        
			    default: System.out.println("\nError: An unexpected issue occurred.");
			    break;
			}
	    }
}

