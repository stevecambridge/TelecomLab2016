package Dns;

public class DnsArgs {
	
	float timeout = 5.0f;
	int retries = 3;
	int port = 53;
	boolean mailServer = false;
	boolean nameServer = false;
	byte[] ip = new byte[4];
	String name;

	static final String timeoutFlag = new String("-t");
	static final String retryFlag = new String("-r");
	static final String portFlag = new String("-p");
	static final String mailServerFlag = new String("-mx");
	static final String nameServerFlag = new String("-ns");

	public DnsArgs(String args[]) {
		int index;

		//parse numerical arguments
		index = findFlag(timeoutFlag, args);
		if(index != -1) {
			try {
				this.timeout = Float.parseFloat(args[index+1]);
			} catch(Exception e) {
				System.out.println("error parsing arguments: abort");
				System.exit(-1);
			}
		}
		index = findFlag(retryFlag, args);
		if(index != -1) {
			try {
				this.retries = Integer.parseInt(args[index+1]);
			} catch(Exception e) {
				System.out.println("error parsing arguments: abort");
				System.exit(-1);
			}
		}
		index = findFlag(portFlag, args);
		if(index != -1) {
			try {
				this.port = Integer.parseInt(args[index+1]);
			} catch(Exception e) {
				System.out.println("error parsing arguments: abort");
				System.exit(-1);
			}
		}

		//parse boolean arguments
		boolean ms = false;
		boolean ns = false;
		index = findFlag(mailServerFlag, args);
		if(index != -1) {
			ms = true;
		}
		index = findFlag(nameServerFlag, args);
		if(index != -1) {
			ns = true;
		}
		//ensure that only 1 such flag is given
		if(ms && ns) {
			System.out.println("cannot send mail server and name server query: abort");
			System.exit(-1);
		} else if(ms) {
			this.mailServer = true;
		} else if(ns) {
			this.nameServer = true;
		}

		//parse ip address argument
		boolean ipFound = false;
		for(int i=0; i<args.length; i++) {
			if (args[i].charAt(0) == '@') {
				index = i;
				ipFound = true;
				break;
			}
		}
		if(!ipFound) {
			System.out.println("enter an ip address with an @ in front of it pls: abort");
			System.exit(-1);
		} else {
			this.ip = parseIp(args[index].substring(1));
		}
		
		//parse name argument
		this.name = args[index+1];
	}

	//search for a given flag in the array of arguments
	//return -1 if not found, index of flag if found
	private int findFlag(String flag, String args[]) {
		for(int i=0; i<args.length; i++) {
			if(args[i].equals(flag)) {
				return i;
			}
		}
		return -1;
	}

	//parse an ip address of the form ###.###.###.### from a string into an array of 4 bytes
	private byte[] parseIp(String addr) {
		byte[] bytes = new byte[4]; 
		int dotIndex = 0, byteIndex = 0;
		int ipSegment;
		for(int i=0; i<addr.length(); i++) {
			if(addr.charAt(i) == '.') {
				ipSegment = Integer.parseInt(addr.substring(dotIndex, i));
				bytes[byteIndex] = (byte)ipSegment;
				byteIndex++;
				dotIndex = i+1;
			}
		}
		ipSegment = Integer.parseInt(addr.substring(dotIndex, addr.length()));
		bytes[byteIndex] = (byte)ipSegment;

		return bytes;
	} 

}