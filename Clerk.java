import java.util.Random;
import java.util.Vector;
import java.util.Random;

public class Clerk implements Runnable 
{
	//random number generator
	static Random random1 = new Random(System.currentTimeMillis());
	static Random random = new Random(random1.nextInt());
	Thread myThread;
	public static long time = System.currentTimeMillis();
	//object for Clerk to block on if necessary
	public BlockingObject blockingObject = new BlockingObject(this);
	//A vector that contains the objects that the Clerks are blocked on
	private static Vector<BlockingObject> waitingClerks = new Vector();
	//Keeps track of which customer this Clerk is helping
	private Customer myCustomer;
	//For mutual exclusion
	private static Object blockForNonStaticMethod = new Object();
	
	public Clerk(String threadName)
	{
		myThread = new Thread(this, threadName);
		myThread.start();
	}
	
	public void run() 
	{
		this.msg("I am here to help");
		//Keep going until all of the customers have left
		while(CS344Project1.getNumCustomers() > 0)
		{
			synchronized(blockingObject){
				//if no customers are waiting to be helped, block
				if(noWaitingCustomers(blockingObject))
				{
					this.msg("There are no customers waiting to be helped, so I will wait");
					//block if there are no customers waiting to be helped
					while(true)
					{
						try {blockingObject.wait(); break;}
						catch(InterruptedException e) { continue; }
					}
				}
				//if there are still customers left (this is here because the last customer notifies the clerks after he leaves, and in that case 
				//we don't want the clerk to handle a customer.) then take care of a customer
				if(CS344Project1.getNumCustomers() > 0)
					this.handleCustomer();
			}
			
		}
		
	}
	
	private synchronized boolean noWaitingCustomers(BlockingObject blockingObject) 
	{
		//mutual exclusion
		synchronized(blockForNonStaticMethod)
		{
			//if there are no waiting customers, add my blocking object to the Vector
			if(Customer.getWaitingCustomers().isEmpty())
			{
				waitingClerks.addElement(blockingObject);
				return true;
			}
			return false;
		}
	}

	public synchronized void handleCustomer()
	{
		//If the clerk currently has no assigned customer (this means that the clerk was not explicitly notified by a customer this time, 
		//but instead, noticed a customer was blocked and notified him)
		if(myCustomer == null)
		{
			//mutual exclusion
			synchronized(blockForNonStaticMethod)
			{
				//set myCustomer to be the first customer in the Vector
				myCustomer = Customer.getWaitingCustomers().elementAt(0).getCustomer();
				//notify that customer and remove him from the Vector
				synchronized(Customer.getWaitingCustomers().elementAt(0))
				{
					//give the customer a ticket
					this.msg("giving a ticket to " +  myCustomer.getName());
					//sleep for random time to simulate deling with customer
					try {
						myThread.sleep(random.nextInt(getRandomNumber()));
					} catch (InterruptedException e1) {}
					Customer.getWaitingCustomers().elementAt(0).notify();
					Customer.getWaitingCustomers().removeElementAt(0);
				}
			}
		}
		//if the clerk has an assigned customer, because the clerk was blokced and was then explicitly notified by a customer
		else
		{
			//notify the customer
			synchronized(myCustomer.getBlockingObject())
			{
				//give the customer a ticket
				this.msg("giving a ticket to " +  myCustomer.getName());
				//sleep for random time to simulate deling with customer
				try {
					myThread.sleep(random.nextInt(getRandomNumber()));
				} catch (InterruptedException e1) {}
				myCustomer.getBlockingObject().notify();
			}
		}
		//reset myCustomer to null
		myCustomer = null;
	}
	
	public static synchronized Vector<BlockingObject> getWaitingClerks()
	{
		return waitingClerks;
	}
	
	public synchronized void setCustomer(Customer customer)
	{
		synchronized(blockForNonStaticMethod)
		{
			myCustomer = customer;
		}
	}
	
	public void msg(String m) 
	{
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+ myThread.getName()+": "+m);
	}

	public  String getName() 
	{
		return myThread.getName();
	}
	
	public static synchronized int getRandomNumber()
	{
		return 1 + random.nextInt(500);
	}

}
