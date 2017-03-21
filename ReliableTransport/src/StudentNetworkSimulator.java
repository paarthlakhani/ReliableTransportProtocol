public class StudentNetworkSimulator extends NetworkSimulator
{

	// Things to do:
	// Calculate the checksum at the receiver and estimate RTT 
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
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
	Packet p;
	static byte seqNum;
	static byte ackNum;
	static byte expectedAckNum;
	static boolean isAcknowledged;
	
	static int packetNumA;
	static int ackA;
	static int ackB;
	static int resendPacketA;
	static int numA;
	static int resendA;
	static int numCorrupt;
	static int numPacketAppA; // number of packets sent by application A
	static int numPacketAppB; // number of packets received by application B
	static int numPacketTransportA; // number of packets recevied by transport layer at A
	static int numPacketTransportB;
	static int numLostHost;
	static int receiveCorruptPack; // Number of corrupt ACKs 
	static int expectedSeqB;
	static double estimateRTT; // EstimatedRTT.
	static double startTime;

    // Also add any necessary methods (e.g. checksum of a String)
	int calculateChecksum(int seqNum, int ackNum, String payload)
	{
		int checksum = 0;
		checksum = (byte) (seqNum + ackNum);
		char[] payloadCharacters = payload.toCharArray();
		
		for(int i = 0; i < payloadCharacters.length; i++)
		{
			int charAscii = payloadCharacters[i];
			checksum+=charAscii;
		}
		return checksum;
	}
	
	public void printStatistics()
	{
		System.out.println("Number of packets sent from layer 5 of Host A: " + numPacketAppA);
		System.out.println("Number of packets received and delivered to layer 5 of Host B: " + numPacketAppB);
		System.out.println("Number of packets received and sent by transport layer of Host A: " + (numPacketTransportA));
		System.out.println("Number of packets dropped by transport layer due to timer: " + numLostHost);
		System.out.println("Number of packets received by transport layer of Host B: " + numPacketTransportB);
		System.out.println("Number of ACKS sent by B are:" + ackB);
		System.out.println("Number of ACKS received by A are:" + ackA);
		// subtraction of negative 1 is done because as soon as the last packet is sent to the receiver, the simulator stops.
		System.out.println("Number of lost packets from A to B:" + (( numA + resendA + numCorrupt ) - ackB - 1 ));
		System.out.println("Number of lost ACKS from B to A:" + (( ackB  - ackA) - numCorrupt - receiveCorruptPack)  );
		System.out.println("Number of corrupt packets that is sent from the A to B:" + numCorrupt);
		System.out.println("Number of corrupt ACKS that is sent from the B to A:" + receiveCorruptPack);
		System.out.println("RTT is:" + estimateRTT);
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
    	numPacketAppA++;
    	if(isAcknowledged)
    	{
        	String payload = message.getData();
        	int checksum = calculateChecksum(seqNum, ackNum, payload);
        	p = new Packet(seqNum, ackNum, ~checksum, payload);
	    	this.toLayer3(0, p);
	    	this.startTimer(0, 750);
			startTime = this.getTime();
	    	isAcknowledged = false;
	    	numA++;
	    	numPacketTransportA++;
	    	
	    	// For Lost Packet output
	    	/*System.out.println("Transmitting packet no:" + packetNumA);
	    	System.out.println("Packet information at sender begins:");
	    	System.out.println("Seq. No. is: " + seqNum);
	    	System.out.println("Ack. No. is: " + ackNum);
	    	System.out.println("payload is: " + payload);
	    	System.out.println("checksum is: " + ~checksum);
	    	System.out.println("Packet information at sender ends:");*/
    	}
    	else
    	{
    		numLostHost++;
    	}
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {

		int checksum = this.calculateChecksum(packet.getSeqnum(), packet.getAcknum(), packet.getPayload());
		// First checking if the received packet is corrupt or no
		if(checksum + packet.getChecksum() == -1) // received packet is not corrupt
		{
			this.stopTimer(0);
			if(packet.getAcknum() == expectedAckNum)
			{
				if(seqNum == 0)
					seqNum = 1;
				else
					seqNum = 0;
				
				double sampleRTT = this.getTime() - startTime;
				estimateRTT = 0.875*estimateRTT + 0.125*sampleRTT;
				ackNum = seqNum;
				expectedAckNum = ackNum;
				isAcknowledged = true;
				packetNumA++;
				ackA++; // this was down in the if statement
			}
			else
			{
				// packet that is sent corrupt; send the packet again.
				System.out.println("Packet got corrupted. Retransmitting the packet");
				Packet packetSend = new Packet(p);
				this.toLayer3(0, packetSend);
				this.startTimer(0, 750);
				numCorrupt++;
			}
		}
		else
		{
			// received packet
			receiveCorruptPack++;
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
    		System.out.println("Packet or an ACK has been lost. Retransmitting the packet");
    		System.out.println("Transmitting packet no: " + packetNumA);
    		Packet packetSend = new Packet(p);
    		this.toLayer3(0, packetSend);
    		this.startTimer(0, 1500);
		startTime = this.getTime();
    		resendPacketA++;
    		resendA++;
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
    	packetNumA = 0;
    	ackA = 0;
    	resendPacketA = 0;
    	numA = 0;
    	resendA = 0;
    	numCorrupt = 0;
    	numPacketAppA = 0;
    	numPacketTransportA = 0;
    	numLostHost = 0;
		receiveCorruptPack = 0;
		estimateRTT = 0.0;
		startTime = 0.0;
    }
    
    // This routine will be called whenever a packet sent from the A-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side. "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    	Packet packetSend;
    	packetSend = new Packet(packet.getSeqnum(), packet.getAcknum(), packet.getChecksum(),"ACK");
    	
    	numPacketTransportB++;

    		int checksum = this.calculateChecksum(packet.getSeqnum(), packet.getAcknum(), packet.getPayload());
    		if( (checksum + packet.getChecksum()) == -1 ) // packet not corrupt
    		{
		    /* System.out.println("Packet is not corrupt");	
		    System.out.println("Packet information at receiver begins:");
		    System.out.println("Seq. No. is: " + packet.getSeqnum());
		    System.out.println("Ack. No. is: " + packet.getAcknum());
		    System.out.println("payload is: " + packet.getPayload());
		    System.out.println("checksum is: " + packet.getChecksum());
		    System.out.println("Packet information at receiver ends:");*/
    	    	
    	    	
    			if(expectedSeqB == packet.getSeqnum())
    			{
    				this.toLayer5(1, packet.getPayload());
    				numPacketAppB++; // increasing the count of packets received.
    				if(expectedSeqB == 0)
    					expectedSeqB = 1;
    				else 
    					expectedSeqB = 0;
    			}
    		}
    		else
    		{
    			System.out.println("Packet is corrupt");
    			
			/*System.out.println("Packet information at receiver begins:");
			System.out.println("Seq. No. is: " + packet.getSeqnum());
			System.out.println("Ack. No. is: " + packet.getAcknum());
			System.out.println("payload is: " + packet.getPayload());
			System.out.println("checksum is: " + packet.getChecksum());
			System.out.println("Packet information at receiver ends:");*/
    	    	
            	if(expectedSeqB == 0)
            		packetSend = new Packet(packet.getSeqnum(), 1, packet.getChecksum(),"Corrupt");
            	else 
            		packetSend = new Packet(packet.getSeqnum(), 0, packet.getChecksum(),"Corrupt");
            	
            	
		/*    	    	System.out.println("The opposite of the expected ACK is sent to the receiver to give an indication that packet is corrupted");
    	    	System.out.println("ACK structure begins:");
    	    	System.out.println("Seq. No. is: " + packetSend.getSeqnum());
    	    	System.out.println("Ack. No. is: " + packetSend.getAcknum());
    	    	System.out.println("payload is: " + packetSend.getPayload());
    	    	System.out.println("checksum is: " + packetSend.getChecksum());
    	    	System.out.println("ACK structure ends:");*/
    		}
    	ackB++;
		int receiverCheck = this.calculateChecksum(packetSend.getSeqnum(), packetSend.getAcknum(), packetSend.getPayload());
		packetSend.setChecksum(~receiverCheck);
		this.toLayer3(1, packetSend);
    }
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
    	expectedSeqB = 0;
    	ackB = 0;
    	numPacketAppB = 0;
    	numPacketTransportB = 0;
    }
}
