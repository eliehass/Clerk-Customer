
public class CS344Project1 
{
	private static int numInitCustomers;
	private static int numInitClerks;
	private static int numCustomers;
	private static int numTables;
	
	public static void main(String args[])
	{
		//try to read in command line arguments
		try{
			numInitCustomers = new Integer(args[0]).intValue();
			numInitClerks = new Integer(args[1]).intValue();
			numTables = new Integer(args[2]).intValue();
			numCustomers = numInitCustomers;
			//if there are no command line arguments, or not enough, just use default values.
		}catch(Exception e){
			numInitCustomers = 14;
			numInitClerks = 2;
			numCustomers = numInitCustomers;
			numTables = 5;
		}
		
		//start all of the customers
		for(int i = 0; i < numInitCustomers; i++)
		{
			new Customer("Customer " + i);
		}
		
		//start all of the vlerks
		for(int i = 0; i < numInitClerks; i++)
		{
			new Clerk("Clerk " + i);
		}
	}
	
	//return the amount of customers
	public synchronized static int getNumCustomers()
	{
		return numCustomers;
	}
	
	//decrement customers by 1
	public synchronized static void decNumCustomers()
	{
		numCustomers--;
	}
	
	//return the amount of tables
	public synchronized static int getNumTables()
	{
		return numTables;
	}
	
	//decrement tables by 1
	public synchronized static void decNumTables()
	{
		numTables--;
	}
	
	//increment tables by 1
	public synchronized static void incNumTables()
	{
		numTables++;
	}
	
	//return the initial number of customers
	public synchronized static int getNumInitCustomers()
	{
		return numInitCustomers;
	}
}
