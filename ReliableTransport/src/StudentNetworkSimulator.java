import java.util.ArrayList;

public class StudentNetworkSimulator extends NetworkSimulator
{
	
	/*
	 * Buffer of 50 elements. Buffer is going to be an ArrayList.
	 * Window size = 8 (given)
	 * Sequence number range = Window size * 2 = 16
	 * variable base,
	 * variable nextseqNum
	 * We have to just one Array List of 50 elements. 
	 */
	
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
	
	// A side variables
	Packet p;
	static byte seqNum;
	static byte ackNum;
	static byte expectedAckNum;
	static boolean isAcknowledged;
	
	// A side - PartB variables
	static int base;
	static int nextSeqNum;
	static ArrayList<Packet> buffer;
	static int windowSize;
	static int currentSequenceNum;
	static int sequenceSpace;
	static int currentAddition;
	static int bufferSize;
	
	
	// B side variables
	static int expectedSeqB;
	static int mostRecentlySeqB;

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
	
	/*public void printStatistics()
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
	}*/
	
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
    	
    	// Part B initialization variables
    	bufferSize = 50;
    	currentSequenceNum = 0; // sequence number in the packet
    	base = 0;
    	nextSeqNum = 0; // position in the arrayList.
    	buffer = new ArrayList<Packet>(bufferSize);
    	windowSize = 8;
    	sequenceSpace = windowSize * 2;
    	currentAddition = 0;
    	
    }

    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	System.out.println("Current Sequence number is: " + currentSequenceNum);
    	if(currentAddition < bufferSize)
    	{
    		String payload = message.getData();
    		int checksum = calculateChecksum(seqNum, ackNum, payload);
    		Packet p = new Packet(currentSequenceNum, ackNum, ~checksum, payload);
    		buffer.add(p);
    		currentAddition++;
    		currentSequenceNum = (currentSequenceNum + 1) % sequenceSpace;
    	}
    	else
    	{
    		// buffer is filled up. Drop the packet
    	}
		
    	if(nextSeqNum < (base + windowSize))
    	{
    		Packet sendPacket = buffer.get(nextSeqNum);
    		this.toLayer3(0, sendPacket);
    		if(nextSeqNum == base)
    			this.startTimer(0, 750);
    		nextSeqNum++;
    	}
    	else
    	{
    		// Window is filled up with unknowledged packets.
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
			int numberPacketsRemove = 0;
			
			if(buffer.get(0).getSeqnum() <= packet.getAcknum())
			{
				numberPacketsRemove = packet.getAcknum() - buffer.get(0).getSeqnum();
			}
			else
				numberPacketsRemove = (sequenceSpace - buffer.get(0).getSeqnum()) + (packet.getAcknum() - 0);
			
			for(int i = 0; i < numberPacketsRemove; i++)
			{
				buffer.remove(0);
			}
			nextSeqNum = nextSeqNum - numberPacketsRemove - 1;
			currentAddition = currentAddition - numberPacketsRemove;
			
			if(base == nextSeqNum)
			{
				this.stopTimer(0);
			}
			else
			{
				// this.startTimer(0, 750); // not sure about this. I don't I need this.
			}
				
		}
		else
		{
			// received packet is corrupt
		}
    }
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    	this.startTimer(0, 750);
    	for(int i = base; i <= nextSeqNum - 1; i++)
    	{
    		Packet packetSend = buffer.get(base);
    		this.toLayer3(0, packetSend);
    	}
    }
    

    
    // This routine will be called whenever a packet sent from the A-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side. "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    	Packet packetSend;
    	packetSend = new Packet(packet.getSeqnum(), packet.getAcknum(), packet.getChecksum(),"ACK");
		int checksum = this.calculateChecksum(packet.getSeqnum(), packet.getAcknum(), packet.getPayload());
		
		if( (checksum + packet.getChecksum()) == -1 ) // packet not corrupt
		{
			if(expectedSeqB == packet.getSeqnum())
			{
				this.toLayer5(1, packet.getPayload());
				mostRecentlySeqB = expectedSeqB;
				expectedSeqB = (expectedSeqB + 1) % sequenceSpace;
			}
		}
		else
		{
        	packetSend = new Packet(packet.getSeqnum(), mostRecentlySeqB, packet.getChecksum(),"Corrupt");
		}
		
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
    	mostRecentlySeqB = -1;
    }
}
