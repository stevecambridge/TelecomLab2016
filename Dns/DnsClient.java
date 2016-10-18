package Dns;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

public class DnsClient {

	public static void main(String[] args) throws Exception {

		DnsArgs params = new DnsArgs(args);

		//Print sending information
		System.out.println("DnsClient sending request for " + params.name);
		System.out.println("Server: " + btu(params.ip[0]) + "." + btu(params.ip[1]) + "." + btu(params.ip[2]) + "." + btu(params.ip[3]));
		System.out.println("Request type: " + (params.nameServer ? "NS" : "") + (params.mailServer ? "MX" : "") + (params.mailServer || params.nameServer ? "" : "A"));

		//get the address of the DNS server
		InetAddress targetAddr = InetAddress.getByAddress(params.ip);

		//initialize receive buffer
		byte[] recvData = new byte[2048];

		//generate a dns query
		DnsQuery dq = new DnsQuery(params.name, params.nameServer, params.mailServer);

		// System.out.println("length = " + dq.query.length);
		// for(int i=0; i<dq.query.length; i++){
		// 	if(i%2 == 0) {
		// 		System.out.println("");
		// 	}
		// 	System.out.print((byte)dq.query[i] + " ");			
		// }

		//initialize socket and datagram packet
		DatagramSocket dSock = new DatagramSocket();
		DatagramPacket sendPkt = new DatagramPacket(dq.query, dq.query.length, targetAddr, params.port);
		DatagramPacket recvPkt = new DatagramPacket(recvData, recvData.length);

		int tries = 0;
		long startTime = 0, endTime = 0;
		while(tries < params.retries) {

			try {
				dSock.send(sendPkt);
				dSock.setSoTimeout((int)(params.timeout*1000));
				startTime = System.currentTimeMillis();

				dSock.receive(recvPkt);

				endTime = System.currentTimeMillis();
				break;

			} catch (SocketTimeoutException e) {
				System.out.println("Timeout");
				tries++;
			}
		}

		if(tries == params.retries) {
			System.out.println("ERROR	Failed to receive response in " + tries + " attempts: aborting");
			System.exit(-1);
		}

		//o dang we did it!!!
		System.out.println("Response received after " + (endTime - startTime)/1000.0 + " seconds (" + (tries+1) + " retries)");

		DnsReader.readResponse(recvPkt.getData(), dq);

		return;

	}

	//make a byte into an unsigned int
	private static int btu(byte b) {
		int i = b;
		if(i<0) {
			i += 256;
		}
		return i;
	}
}