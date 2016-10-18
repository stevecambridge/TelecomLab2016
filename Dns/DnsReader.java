package Dns;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

//TODO: skip over unrecognized RR types without crashing the whole thing
//format everything good

public class DnsReader {
	
	static final short A_QUERY = 0x0001;
	static final short NS_QUERY = 0x0002;
	static final short MX_QUERY = 0x000f;
	static final short CNAME = 0x0005;

	public DnsReader() {

	}

	public static void readResponse(byte[] pkt, DnsQuery q) throws IOException {
		
		ByteArrayInputStream bytesIn = new ByteArrayInputStream(pkt);
		DataInputStream resp = new DataInputStream(bytesIn);

		/* HEADER SECTION */

		//check ID
		int responseId = resp.readShort();
		if(responseId != q.id) {
			System.out.println("ERROR	Response id does not match request id");
			System.exit(-1);
		}

		//check flags
		short flags = resp.readShort();	
		boolean auth = readFlags(flags);

		//skip qdCount
		resp.readShort();

		//check anCount
		int anCount = resp.readShort();

		//check these other Counts
		int nsCount = resp.readShort();
		int arCount = resp.readShort();


		/* REQUEST SECTION */
		resp.skipBytes(q.reqLength);

		/* ANSWER SECTION */
		System.out.println("------------ANSWER RECORDS (" + anCount +")-------------");
		for(int i=0; i<anCount; i++) {
			// System.out.println("Answer record #" + (i+1) + ":");
			readRR(pkt, resp, auth);
		}

		System.out.println("-----------AUTHORITY RECORDS (" + nsCount +")-----------");
		for(int i=0; i<nsCount; i++) {
			// System.out.println("Authority record #" + (i+1) + ":");
			readRR(pkt, resp, auth);
		}

		System.out.println("----------ADDITIONAL RECORDS (" + arCount +")-----------");
		for(int i=0; i<arCount; i++) {
			// System.out.println("Additional record #" + (i+1) + ":");
			readRR(pkt, resp, auth);
		}
	}

	private static void readRR(byte[] pkt, DataInputStream resp, boolean auth) throws IOException {
		//read the NAME from the start of the name section - 12 accounts for the length of the header
		String name = readName(pkt, resp);

		//read the TYPE
		short qtype = resp.readShort();
		
		// switch(qtype) {
		// 	case A_QUERY: 	typeString = "IP";
		// 				  	break;
		// 	case NS_QUERY: 	typeString = "NS";
		// 					break;
		// 	case MX_QUERY:	typeString = "MX";
		// 					break;
		// 	case CNAME: 	typeString = "CNAME";
		// 				 	break;
		// 	default: 		System.out.println("error: RR type " + qtype + " not recognized");
		// 				   	typeRecognized = false;
		// }

		//check CLASS
		if(resp.readShort() != 0x0001) {
			System.out.println("ERROR	Unexpected value for CLASS");
			System.exit(-1);
		}

		//check TTL
		int ttl = resp.readInt();
		// System.out.println("TTL = " + ttl);

		//check RDLENGTH
		int rdLength = resp.readShort();
		if(rdLength < 0) {
			rdLength += 65536;
		}

		//read RDATA, dependent on the record type
		switch(qtype) {
			case A_QUERY: 	readTypeA(resp, ttl, auth);
						  	break;
			case NS_QUERY: 	readTypeNS(pkt, resp, ttl, auth);
							break;
			case MX_QUERY:	readTypeMX(pkt, resp, ttl, auth);
							break;
			case CNAME: 	readTypeCNAME(pkt, resp, ttl, auth);
						 	break;
			default: 		resp.skipBytes(rdLength);
							System.out.println("ERROR	RR type not recognized");
							break;
		}
	}

	private static void readTypeA(DataInputStream r, int ttl, boolean auth) throws IOException {
		String ip = "";
		int field = 0;
		for(int i=0; i<4; i++) {
			field = r.readByte();
			if(field < 0){
				field += 256;
			}
			ip += field;
			if(i!=3){
				ip += ".";
			}
		}
		System.out.println("IP	" + ip + "	" + "TTL=" + ttl + "	" + (auth ? "auth" : "nonauth"));
	}

	private static void readTypeNS(byte[] pkt, DataInputStream r, int ttl, boolean auth) throws IOException {
		String serverName = readName(pkt, r);
		System.out.println("NS	" + serverName + "	" + "TTL=" + ttl + "	" + (auth ? "auth" : "nonauth"));
	}

	private static void readTypeMX(byte[] pkt, DataInputStream r, int ttl, boolean auth) throws IOException {
		int pref = r.readShort();
		String exchange = readName(pkt, r);
		System.out.println("MX	" + exchange + "	pref=" + pref + "	TTL=" + ttl + "	" + (auth ? "auth" : "nonauth"));
	}

	private static void readTypeCNAME(byte[] pkt, DataInputStream r, int ttl, boolean auth) throws IOException {
		String aliasName = readName(pkt, r);
		System.out.println("CNAME	" + aliasName + "	" + "TTL=" + ttl + "	" + (auth ? "auth" : "nonauth"));
	}

	private static boolean readFlags(short flags) {
		
		//check if response or query
		if((flags & 0x8000) == 32768) {
			// System.out.println("yes it is a response");
		} else {
			System.out.println("ERROR	Query message received");
			System.exit(-1);
		}

		//check if authoritative
		boolean authoritative = false;
		if((flags & 0x0400) > 0) {
			// System.out.println("authoritative request");
			authoritative = true;
		} else {
			// System.out.println("not authoritative");
		}

		if((flags & 0x0200) > 0) {
			System.out.println("ERROR	Truncated message");
			System.exit(-1);
		}

		if((flags & 0x0080) == 0) {
			System.out.println("ERROR	Recursive query unsupported");
		}

		switch((flags & 0x000f)) {
			case 0: 	break;
			case 1: 	System.out.println("ERROR	Name server unable to interpret query");
					 	System.exit(-1);
			case 2: 	System.out.println("ERROR	Server unable to process due to server problems");
					 	System.exit(-1);
			case 3: 	if(authoritative){
					 		System.out.println("ERROR	Domain name not found");
					 		System.exit(-1);
					 	}
			case 4: 	System.out.println("ERROR	Name server does not support the requested query");
					 	System.exit(-1);
			case 5: 	System.out.println("ERROR	Name server refused request");
					 	System.exit(-1);
			default: 	System.out.println("ERROR	Could not read error code (lol)");
					  	System.exit(-1);
		}
		return authoritative;
	}


	//read a name label. will call readNameFromOffset to handle pointers in the names
	//assumes that r is already waiting at the location of the start of the name
	private static String readName(byte[] pkt, DataInputStream r) throws IOException {
		String name = "";

		while(true) {
			int count = r.readByte();

			if(count == 0) {
				return name;
			} else if((count & 0xc0) != 0) {
				int nextByte = r.readByte();
				return name + readNameFromOffset(pkt, (int)(((count & 0x3f) << 8) | nextByte));
			}

			for(int j=0; j<count; j++) {
				name += (char)r.readByte();
			}
			name += ".";
		}
	}

	//reads a series of name labels given the whole packet and the offset where it starts.
	//recursively calls self in the case of packet compression pointers.
	//should only be called by self or by readName()
	private static String readNameFromOffset(byte[] pkt, int offset) throws IOException {
		ByteArrayInputStream b = new ByteArrayInputStream(pkt);
		DataInputStream in = new DataInputStream(b);

		String name = "";

		//skip to offset location
		in.skipBytes(offset);

		//start reading name
		while(true) {

			//read the byte indicating the number of labels
			int count = in.readByte();

			//label termination
			if(count == 0) {
				return name;

			//first two bits are 1 and this byte + the next one are a pointer
			} else if((count & 0xc0) != 0) {
				int nextByte = in.readByte();
				
				//append all tokens gathered thus far to what is found at the pointer
				return name + readNameFromOffset(pkt, (int)(((count & 0x3f) << 8) | nextByte));
			}

			//not a pointer and not terminated; scoop up the label here
			for(int j=0; j<count; j++) {
				name += (char)in.readByte();
			}
			name += ".";
		}
	} 

}
