import java.util.Vector;
import java.util.Random;

public class Customer implements Runnable
{
	//random number generator
	static Random random1 = new Random(System.currentTimeMillis());
	static Random random = new Random(random1.nextInt());
 	Thread myThread;
	public static long time = System.currentTimeMillis();
	//this Vector will hold all of the objects that waiting customers are blocked on
	private static Vector<BlockingObject> waitingCustomers = new Vector();
	//this Vector holds all of the objects that waiting Clerks are blocked on
	private static Vector<BlockingObject> waitingClerks = Clerk.getWaitingClerks();
	//an object for a customer to block on if necessary
	public BlockingObject blockingObject = new BlockingObject(this);
	//an object that will hold the object that the clerk who should be serving me is blocked on
	private BlockingObject myClerk;
	//this object will be used to implement mutual exclusion
	private static Object blockForNonStaticMethod = new Object();
	//keeps track of how many customers are in or waiting for a group
	private static int group = 0;
	//gets the initial number of customers
	private static int totalNumCustomers = CS344Project1.getNumInitCustomers();
	//object to implement mutual exclusion while looking for group
	private static Object blockForGroup = new Object();
	//object to implement mutual exclusion wile looking for a table
	private static Object blockForTable = new Object();
	
	//Constructor
	public Customer(String threadName)
	{
		myThread = new Thread(this, threadName);
		myThread.start();
	}
	
	public void run()
	{
		//sleep for random time before entering
		try {
			myThread.sleep(random.nextInt(getRandomNumber()));
		} catch (InterruptedException e1) {}
		this.msg("looking for an item");
		//sleep random time while looking for an item
		try {
			myThread.sleep(random.nextInt(getRandomNumber()));
		} catch (InterruptedException e1) {}
		this.msg("I found an item");
		//object to be synchronized over
		synchronized (blockingObject) {
			//notify a clerk
			this.notifyClerk(blockingObject);
			//block until a clerk tell you you can move on (gives you a ticket)
			while(true)
			{
				try {blockingObject.wait(); break;}
				catch(InterruptedException e) { continue; }
			}
		}
		this.msg("I have recieved my ticket. Now I want to eat");
		//join a group and get a table
		this.getTable();
		this.msg("I have left");
		//decrement the amount of customers to show that you have left
		CS344Project1.decNumCustomers();
		//the last customer notifies the clerks that it's closing time
		if(CS344Project1.getNumCustomers() == 0)
		{
			this.msg("it's closing time");
			for(int i = 0; i < waitingClerks.size(); i++)
			{
				synchronized(waitingClerks.elementAt(i))
				{
					waitingClerks.elementAt(i).notify();
				}
			}
		}
	}
	
	//takes a table for a group to eat at
	private void getTable()
	{
		//mutual exclusion
		synchronized(blockForTable)
		{
			//if there are no free tables, block
			while(CS344Project1.getNumTables() == 0)
			{
				while(true)
				{
					try {blockForTable.wait(); break;}
					catch(InterruptedException e) { continue; }
				}
			}
		}
		
		//mutual exclusion
		synchronized(blockForGroup)
		{
			group++;
			//the last customer always forms a group, even if his group is less than the specified group size
			if(group == totalNumCustomers)
			{
				CS344Project1.decNumTables();
				this.msg("I am in a group and we are eating");
			}
			//if there are not yet 3 customers in the group, block
			else if(group % 3 != 0 && CS344Project1.getNumCustomers() >= 3)
			{
				this.msg("I am waiting for a group to eat");
				while(true)
				{
					try {blockForGroup.wait(); break;}
					catch(InterruptedException e) { continue; }
				}
				//once this customer is notified, it simply returns. Everything that needs to be done will be handled by the thread that
				//formed the group and notified the other 2.
				return;
			}
			//if the group now has three customers, take a table and eat
			else if ((group % 3 == 0 && CS344Project1.getNumCustomers() >= 3))
			{
				CS344Project1.decNumTables();
				this.msg("I am in a group and we are eating");
			}
			//this statement catches the situation where there are less than 3 customers left, but a group should not be formed yet because
			//this customer is not the last customer.
			else
			{
				this.msg("I am waiting for a group to eat");
				while(true)
				{
					try {blockForGroup.wait(); break;}
					catch(InterruptedException e) { continue; }
				}
				return;
			}
		}
		
		//sleep for random time to simulate eating
		try {
			myThread.sleep(random.nextInt(getRandomNumber()));
		} catch (InterruptedException e1) {}
		
		//mutual exclusion
		synchronized(blockForGroup)
		{
			this.msg("we are done eating and are leaving");
			//notify the other two members of your group
			blockForGroup.notify();
			blockForGroup.notify();
			//return your table to the pool of available tables
			CS344Project1.incNumTables();
		}
		
		//mutual exclusion
		synchronized(blockForTable)
		{
			//notify everyone who is waiting for a table. Let them all fight out who gets it.
			blockForTable.notifyAll();
		}
	}
	
	private synchronized void notifyClerk(BlockingObject checkClerk) 
	{
		//if there are Clerks in the waitingClerks Vector, that means that there are Clerks who are free to help
		if(!Clerk.getWaitingClerks().isEmpty())
		{
			//mutual exclusion
			synchronized(blockForNonStaticMethod)
			{
				//take the first clerk in the Vector and assign him to me
				this.myClerk = Clerk.getWaitingClerks().elementAt(0);
				//remove him from the Vector
				Clerk.getWaitingClerks().remove(myClerk);
			}
			this.msg(myClerk.getClerk().getName() + " is available to help me.");
			//notify my clerk and let him know that I am his customer
			synchronized(myClerk){
				myClerk.notify();
				this.msg("I have notified " + myClerk.getClerk().getName());
				myClerk.getClerk().setCustomer(this);
			}
		}
		//there are no free clerks, so add myself to Vector of waiting customers
		else
		{
			this.msg("No clerks are free so I will wait");
			waitingCustomers.addElement(checkClerk);
		}
	}
	
	public static synchronized int getNumWaitingCustomers()
	{
		return waitingCustomers.size();
	}
	
	public  void msg(String m) 
	{
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+ myThread.getName()+": "+m);
	}

	public synchronized static Vector<BlockingObject> getWaitingCustomers() 
	{
		return waitingCustomers;
	}
	
	public String getName()
	{
		return myThread.getName();
	}
	
	public BlockingObject getBlockingObject()
	{
		return blockingObject;
	}
	
	public static synchronized int getRandomNumber()
	{
		return 1 + random.nextInt(500);
	}
	
}
