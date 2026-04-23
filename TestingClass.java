package furzefield;

	import static org.junit.Assert.assertEquals;

	import java.sql.*;

	import org.junit.After;
	import org.junit.Test;

	import furzefield.BookingManager;
	import furzefield.DatabaseConnection;
		
	public class TestingClass
	{

		@Test
		public void testDuplicateBooking() throws SQLException
		{
		    BookingManager bm = new BookingManager();
		    
		    
		    String first = bm.bookLesson(2, 39);
		    
		    String second = bm.bookLesson(2, 40);
		    
		    String expected = "ALREADY_BOOKED";
		    assertEquals("The capacity limit exceeded", expected, second);
		   
		    
		}
		
		
		
		@Test
		public void testTimeConflict() throws SQLException
		{
		    
		    BookingManager bm = new BookingManager();
		    try(Connection con = DatabaseConnection.connect())
		    {
		    	String actual = bm.timeConflict(con, 2, 45);
		    	  	
		    	String expected = "NO_CONFLICT";
		    	assertEquals("Not Having Any Lesson", expected, actual);
		
		    }
		}
		
		
		
		@Test
		public void maxCapacity() throws SQLException
		{
			try(Connection con = DatabaseConnection.connect())
		    {
		    	
				BookingManager bm = new BookingManager();
				int lsnId = 6;
				
				bm.capacityCheck(con, lsnId);
				bm.capacityCheck(con, lsnId);
				bm.capacityCheck(con, lsnId);
				bm.capacityCheck(con, lsnId);
		
		    
				String expected = "FULL";
				String actual = bm.capacityCheck(con, lsnId);
				
				assertEquals("Should return FULL when 4 members are already booked", expected, actual);
		    }

			
		}
		
		
		
	    @Test
	    public void testIncomeReport() throws SQLException
	    {
	    	BookingManager bm = new BookingManager();
	    	int mb1 = 14;
	    	int mb2 = 15;
	    	int mb3 = 16;
	    	
	    	try (Connection conn = DatabaseConnection.connect();)
	    	{
	    		String ins = "INSERT INTO members (member_id, user_name, password) VALUES (?, ?, ?)";
	    		try (PreparedStatement ps = conn.prepareStatement(ins))
	    		{
	    			ps.setInt(1, mb1); ps.setString(2, "Test User 1"); ps.setString(3, "Test Password 1"); ps.executeUpdate();
	    			ps.setInt(1, mb2); ps.setString(2, "Test User 2"); ps.setString(3, "Test Password 2"); ps.executeUpdate();
	    			ps.setInt(1, mb3); ps.setString(2, "Test User 3"); ps.setString(3, "Test Password 3"); ps.executeUpdate();
	            }
	        
	            String ist = "INSERT INTO bookings (member_id, lesson_id, status) VALUES (?, ?, ?)";
	        
	            try (PreparedStatement ps = conn.prepareStatement(ist))
	            {
	            
		            ps.setInt(1, mb1);
		            ps.setInt(2, 1); 
		            ps.setString(3, "attended");
		            ps.executeUpdate();
	            
	            
		            ps.setInt(1, mb2);
		            ps.setInt(2, 6);
		            ps.setString(3, "attended");
		            ps.executeUpdate();
		            
		            
		            ps.setInt(1, mb3);
		            ps.setInt(2, 3); 
		            ps.setString(3, "attended");
		            ps.executeUpdate();
	           }

	        
	            double actualIncome = bm.getTestIncome(mb1,mb2,mb3);

	            // Yoga - 15.00 x 2
	            // Aquacise - 20.00 x 1

	            double expectedIncome = 50.00;

	            assertEquals("The total income from the database did not match the expected math.", 
	                     expectedIncome, actualIncome, 0.001);
	    	}
	    }

		
	    
	    @Test
	    public void testInvalidInput() throws SQLException
	    {
	        BookingManager bm = new BookingManager();
	        
	        
	        String expected = "INVALID_ID"; 
	        
	        
	        String actual = bm.bookLesson(-5, 7); 
	        
	        assertEquals("Should return INVALID_ID for negative member ID", expected, actual);
	    }
	    
	    
	    
        
}
