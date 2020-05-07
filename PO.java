import java.util.LinkedList;
import java.util.concurrent.Semaphore;


public class PO extends Thread {
	// Max customers and workers
	private static final int MAX_CUSTOMERS = 50;
	private static final int MAX_WORKERS = 3;
	
	// customer count and worker count
	private static int custnr;
	private static int workernr;
	
	// Semaphores
	private Semaphore max_capacity;
	private Semaphore cust_ready;
	private Semaphore[] finished;
	private Semaphore leave;
	private Semaphore scale;
	private Semaphore mutex1;
	private Semaphore mutex2;
	
	// Threads and queue
	private Thread[] customers;
	private Thread[] workers;
	private LinkedList<Customer> queue;
		
	// Post office constructor
	public PO() {
		custnr = 0;
		workernr = 0;
		max_capacity = new Semaphore(10);
		cust_ready = new Semaphore(0);
		finished = new Semaphore[50];
		//leave = new Semaphore(0);
		scale = new Semaphore(1);
		mutex1 = new Semaphore(1);
		mutex2 = new Semaphore(1);
		
		customers = new Thread[MAX_CUSTOMERS];
		workers = new Thread[MAX_WORKERS];
		queue = new LinkedList<Customer>();
		
		// Initialize finish semaphores
		for(int i = 0; i < MAX_CUSTOMERS; i++) {
			finished[i] = new Semaphore(0);
		}	
	}
	
	@Override
	public void run() {
		// Create customers and workers
        System.out.println("Simulating Post Office with 50 customers and 3 postal workers\n");

        for (int i = 0; i < MAX_WORKERS; i++) {
            workers[i] = new Thread(new Worker());
            workers[i].start();
        }

        for (int i = 0; i < MAX_CUSTOMERS; i++) {
            customers[i] = new Thread(new Customer());
            customers[i].start();
        }

        try {
            for (int i = 0; i < MAX_WORKERS; i++) {
                workers[i].join();
            }

            for (int i = 0; i < MAX_CUSTOMERS; i++) {
                customers[i].join();
                System.out.println("Joined Customer " + i);
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        }
		
	}
	
	// Customer class
	private class Customer implements Runnable{
		private int number;
		private int task;
		
		@Override
		public void run() {
			try {
				max_capacity.acquire();
				mutex1.acquire();
				initialize();
				mutex1.release();
				enterOffice();
				mutex2.acquire();
				queue.add(this);
				cust_ready.release();
				mutex2.release();
				finished[this.getNumber()].acquire();
				leaveOffice();
				max_capacity.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		// Get number
		public int getNumber() {
			return number;
		}
		
		// Get task
		public int getTask() {
			return task;
		}
		
		// Assign customer number and task
		public void initialize() {
			number = custnr++;
			task = (int)(Math.random() * 3) + 1;
			System.out.println("Customer " + number + " created");
		}
		
		// Enter office
        private void enterOffice() {
            System.out.println("Customer " + number + " enters post office");
        }
		
		// Leave office
        private void leaveOffice() {
            System.out.println("Customer " + number + " leaves post office");
        }
	}
	
	// Worker class
	private class Worker implements Runnable{
		private int number;
		private String[] tasks = {"buy stamps", "mail a letter", "mail a package"};
		
		@Override 
		public void run() {
			while(true) {
				try {
					cust_ready.acquire();
					mutex2.acquire();
					Customer customer= queue.remove();
					mutex2.release();
					doTask(customer);
                    finished[customer.getNumber()].release();
                    if (queue.isEmpty()) break;
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Constructor
		public Worker() {
			number = workernr++;
			System.out.println("Postal worker " + number + " created");
		}
		
		private void doTask(Customer customer) {
			// Serving customer
			System.out.println("Postal Worker " + number + " Serving Customer " + customer.getNumber());
			System.out.println("Custemer " + customer.getNumber() + " asks postal worker " + number + " to " + tasks[customer.getTask() - 1]);
			
			// Customer do one of three tasks
			try {
				switch(customer.getTask()) {
					case 1:
						Thread.sleep(1000);
						break;
					case 2:
						Thread.sleep(1500);
						break;
					case 3:
						scale.acquire();
						System.out.println("Scales in use by postal worker " + number);
						Thread.sleep(2000);
						System.out.println("Scales released by postal worker " + number);
						scale.release();
						break;
				}
			} catch (InterruptedException e) {
				System.out.println(e);
			}
			System.out.println("Poster worker " + number + " finished serving customer " + customer.getNumber());
		}		
	}
	
	// Main class
    public static void main(String[] args) {
        try {
            PO postOffice = new PO();
            postOffice.start();
            postOffice.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

}
