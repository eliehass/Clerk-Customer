
//This class is used as a blocking object for convenience sake. This way a Clerk or Customer can get information on who they are dealing with
//just by having access to the object that they are blocked on.
public class BlockingObject 
{
	private Customer myCustomer;
	private Clerk myClerk;
	
	public BlockingObject(Customer theCustomer)
	{
		myCustomer = theCustomer;
	}
	
	public BlockingObject(Clerk theClerk)
	{
		myClerk = theClerk;
	}
	
	public Customer getCustomer()
	{
		return myCustomer;
	}
	
	public Clerk getClerk()
	{
		return myClerk;
	}
	
	public BlockingObject getBlockingObject()
	{
		return this;
	}
}
