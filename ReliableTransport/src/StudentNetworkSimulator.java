public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B
     *
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(int entity, String dataSent)
     *       Passes "dataSent" up to layer 5 from "entity" [A or B]
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */
	
	 /*
     * List of global variables I would be needing:
     * Packet, seq num, ack num, checksum number
     * 
     * List of things I should know about:
     * What's seqNum, ackNum and checkSum values initially
     * For the ackNum, and the seqNum, do I need to switch between 0 and 1 while sending the packets? Yes, I am switching between the two
     * 
     * What's the acknowledgement packet from B look like?
     * Does ACK number changes the same way as SEQ number?
     * 
     * Part 2:
     * You are in part2 now. Do loss of packets first. In-depth understanding
     * 
     * 
     */
	Packet p;
	static byte seqNum;
	static byte ackNum;
	static byte expectedAckNum;
	static boolean isAcknowledged;
	static int packetNum; // For debugging purposes.
	
	// expected sequence number at B.
	static byte expectSeqB;
	static byte expectAckB;
    
	// Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)
	int calculateChecksum(int seqNum, int ackNum, String payload)
	{
		int checksum = 0;
		checksum = seqNum + ackNum;
		char[] payloadCharacters = payload.toCharArray();
		
		for(int i = 0; i < payloadCharacters.length; i++)
		{
			byte charAscii = (byte)payloadCharacters[i];
			checksum+=charAscii;
		}
		
		return checksum;
	}
	
	
    // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   long seed)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
    }

    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	// Only do the below statements if there is no unacknowledged packets in the medium.
    	// what's RTT?
    	if(isAcknowledged)
    	{
        	String payload = message.getData();
        	// Dealing with corruption later.
        	int checkSum = 0;
        	p = new Packet(seqNum, ackNum, checkSum, payload);
        	System.out.println("Packet number is:" + packetNum);
	    	this.startTimer(0, 15);
	    	this.toLayer3(0, p);
	    	isAcknowledged = false;
    	}
    	else
    	{
    		System.out.println("Hello I am here.");
    	}
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {
    	
    	if(packet.getAcknum() == expectedAckNum)
    	{
    		this.stopTimer(0);
    		if(seqNum == 0)
    			seqNum = 1;
    		else
    			seqNum = 0;
	    	ackNum = seqNum;
	    	expectedAckNum = ackNum;
	    	isAcknowledged = true;
        	packetNum++;
    	}
    }
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    	if(!isAcknowledged)
    	{
    		/*System.out.println("Packet has been lost");
        	System.out.println("I am resending the packet");
        	System.out.println("Packet number is:" + packetNum);*/
    		this.startTimer(0, 15);
    		Packet packetSend = new Packet(p);
    		this.toLayer3(0, packetSend);
    		isAcknowledged = false;
    	}
    }
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
    	seqNum = 0;
    	ackNum = 0;
    	expectedAckNum = 0;
    	isAcknowledged = true;
    	packetNum = 1;
    }
    
    // This routine will be called whenever a packet sent from the A-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    	this.toLayer5(1, packet.getPayload());
    	Packet packetSend;
    	
    	/*
    	System.out.println("Packet Statistics at receiver");
    	System.out.println(p.getAcknum());
    	System.out.println(p.getChecksum());
    	System.out.println(p.getPayload());
    	System.out.println(p.getSeqnum());
    	System.out.print("Ending of the packet receiver");
    	*/
    	
    	packetSend = new Packet(packet.getSeqnum(), packet.getAcknum(), packet.getChecksum(),"Successful Receive");
    	
    	
    	/*if(expectSeqB == packet.getSeqnum() &&  expectAckB == packet.getAcknum())
    	{
    		// Send an acknowledgement packet; you have to check about checksum later.
    		packetSend = new Packet(expectSeqB, expectAckB, packet.getChecksum(),"Successful Receive");
    		
    		if(expectSeqB == 0)
    			expectSeqB = 1;	
    		else expectSeqB = 0;
    		expectAckB = expectSeqB;
    	}
    	else
    	{
    		// Send previous acknowledgement
    		packetSend = new Packet(expectSeqB, expectAckB, packet.getChecksum(),"unsuccessful Receive");
    	}*/
		this.toLayer3(1, packetSend);
    }
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
    	expectSeqB = 0;
    	expectAckB = 0;
    }
}
