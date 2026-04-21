package furzefield;

import java.sql.*;

public class BookingManager
{
	public String bookLesson(int mbId, int lsnId)
	{
	    if (mbId <= 0 || lsnId <= 0)
	    {
	        return "INVALID_ID";
	    }

	    try (Connection con = DatabaseConnection.connect())
	    {
	        if (con == null)
	        {
	            return "DB_ERROR";
	        }

	        con.setAutoCommit(false);

	       
	        String sql = "SELECT COUNT(*) FROM bookings " +
	                          "WHERE member_id = ? AND lesson_id = ? AND status != 'cancelled'";

	        try (PreparedStatement ps = con.prepareStatement(sql))
	        {
	            ps.setInt(1, mbId);
	            ps.setInt(2, lsnId);

	            try (ResultSet rs = ps.executeQuery())
	            {
	                if (rs.next() && rs.getInt(1) > 0)
	                {
	                    con.rollback();
	                    return "ALREADY_BOOKED";
	                }
	            }
	        }
	        if (capacityCheck(con, lsnId).equals("FULL"))
	        {
	              con.rollback();
	              return "FULL";
	        }
	                	
	        if (timeConflict(con, mbId, lsnId).equals("TIME_CONFLICT"))
	        {
	        	 con.rollback();
	        	  return "TIME_CONFLICT";
	        }
	        
	        String result = insertLesson(con, mbId, lsnId);

	        con.commit();

	        return result;
	                	
	     }
	     catch (SQLException e)
	     {
	    	 System.out.println("Booking Error: " + e.getMessage());
	         return "DB_ERROR";
		 }
	     
	}
	
	
	
	
	
	public String insertLesson(Connection con, int mbId, int lsnId)
	{
	    if (con == null)
	    {
	        return "DB_ERROR";
	    }

	    String ins = "INSERT INTO bookings (member_id, lesson_id, status) VALUES (?, ?, 'booked')";

	    try (PreparedStatement ps = con.prepareStatement(ins))
	    {
	        ps.setInt(1, mbId);
	        ps.setInt(2, lsnId);

	        int rows = ps.executeUpdate();

	        if (rows > 0)
	        {
	            return "SUCCESS";
	        }
	        else
	        {
	            return "INSERT_FAILED";
	        }

	    }
	    catch (SQLException e)
	    {
	        System.out.println("Insert Error: " + e.getMessage());
	        return "DB_ERROR";
	    }
	}

	
	
	
	
	
	public String capacityCheck(Connection con, int lsnId) throws SQLException
	{
		    
		  
	       String sql = "SELECT COUNT(*) FROM bookings WHERE lesson_id = ? AND status != 'cancelled'";
	       try (PreparedStatement ps = con.prepareStatement(sql))
	       {
	            ps.setInt(1, lsnId);
	            try (ResultSet rs = ps.executeQuery())
	            {
	                if (rs.next())
	                {
	                    int count = rs.getInt(1);
	                    
	                    if (count >= 4)
	                    {
	                        return "FULL";
	                    }
	                }
	           }
	       }
	       return "NOT_FULL";
		  
	}
	
	
	
	
	
	public String timeConflict(Connection con, int mbId, int lsnId)
	{
	    String sql =
	        "SELECT COUNT(*) " +
	        "FROM bookings b " +
	        "JOIN lessons l1 ON b.lesson_id = l1.lesson_id " +
	        "JOIN lessons l2 ON l2.lesson_id = ? " +
	        "WHERE b.member_id = ? " +
	        "AND b.status != 'cancelled' " +
	        "AND l1.lesson_day = l2.lesson_day " +
	        "AND l1.time_slot = l2.time_slot " +
	        "AND l1.weekend_number = l2.weekend_number";

	        try (PreparedStatement ps = con.prepareStatement(sql))
	        {
	            ps.setInt(1, lsnId);
	            ps.setInt(2, mbId);

	            try (ResultSet rs = ps.executeQuery())
	            {
	                if (rs.next() && rs.getInt(1) > 0)
	                {
	                    return "TIME_CONFLICT";
	                }
	            }
	            return "NO_CONFLICT";
	        }
	        catch (Exception e)
	        {
	        	System.out.println("Time Conflict Error: " + e.getMessage());
	        	return "DB_ERROR";
	        }
	}




    public boolean cancelBooking(int mbId, int bkgId)
	{
	    String sql = "UPDATE bookings SET status = 'cancelled' " +
	                 "WHERE booking_id = ? AND member_id = ? " +
	                 "AND status IN ('booked','changed')";

	    try (Connection con = DatabaseConnection.connect();
	    	 PreparedStatement ps = con.prepareStatement(sql))
	    {
	        if (con == null)
	        {
	            return false;
	        }

	        
            ps.setInt(1, bkgId);
            ps.setInt(2, mbId);

            int rows = ps.executeUpdate();

            if (rows > 0)
            {
                return true;
            }
            else
            {
                return false;
            }

	    }
	    catch (Exception e)
	    {
	        System.out.println("Cancellation Error: " + e.getMessage());
	        return false;
	    }
	}
	
	
	
	
	public boolean changeBooking(int mbId, int bkgId, int newLsnId)
	{
	    System.out.println("Processing change request for Booking #" + bkgId + "...");

	    try (Connection con = DatabaseConnection.connect())
	    {
	        if (con == null)
	        {
	            return false;
	        }

	        if (capacityCheck(con, newLsnId).equals("FULL"))
	        {
	            System.out.println("Change failed: New lesson is full (Max capacity 4 Only).");
	            return false;
	        }

	        
	        String sql =
	            "SELECT COUNT(*) FROM bookings b " +
	            "JOIN lessons l1 ON b.lesson_id = l1.lesson_id " +
	            "JOIN lessons l2 ON l2.lesson_id = ? " +
	            "WHERE b.member_id = ? " +
	            "AND b.status != 'cancelled' " +
	            "AND b.booking_id != ? " +
	            "AND l1.lesson_day = l2.lesson_day " +
	            "AND l1.time_slot = l2.time_slot " +
	            "AND l1.weekend_number = l2.weekend_number";

	        try (PreparedStatement ps = con.prepareStatement(sql))
	        {
	            ps.setInt(1, newLsnId);
	            ps.setInt(2, mbId);
	            ps.setInt(3, bkgId);

	            try (ResultSet rs = ps.executeQuery())
	            {
	                if (rs.next() && rs.getInt(1) > 0)
	                {
	                    System.out.println("Change failed: You already have another lesson scheduled at this time.");
	                    return false;
	                }
	            }
	        }

	        
	        String update =
	            "UPDATE bookings SET lesson_id = ?, status = 'changed' " +
	            "WHERE booking_id = ? AND member_id = ?";

	        try (PreparedStatement ps = con.prepareStatement(update))
	        {
	            ps.setInt(1, newLsnId);
	            ps.setInt(2, bkgId);
	            ps.setInt(3, mbId);

	            int rows = ps.executeUpdate();

	            if (rows > 0)
	            {
	                System.out.println("Success: Booking #" + bkgId +
	                                   " changed to Lesson ID " + newLsnId);
	                return true;
	            }
	            else
	            {
	                System.out.println("Error: Booking not found or unauthorized.");
	                return false;
	            }
	        }

	    }
	    catch (Exception e)
	    {
	        System.out.println("Change Booking Error: " + e.getMessage());
	        return false;
	    }
	}

}
