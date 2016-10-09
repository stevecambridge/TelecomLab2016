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
		System.out.println("Server: " + params.ip[0] + "." + params.ip[1] + "." + params.ip[2] + "." + params.ip[3]);
		System.out.println("Request type: " + (params.nameServer ? "NS" : "") + (params.mailServer ? "MX" : "") + (params.mailServer || params.nameServer ? "" : "A"));

		//get the address of the DNS server
		InetAddress targetAddr = InetAddress.getByAddress(params.ip);

		//initialize send buffer
		byte[] sendData = new byte[1024];
		byte[] recvData = new byte[1024];

		//initialize socket and datagram packet
		DatagramSocket dSock = new DatagramSocket();
		DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, targetAddr, params.port);
		DatagramPacket recvPkt = new DatagramPacket(recvData, recvData.length);

		int tries = 0;
		long startTime = 0, endTime = 0;
		boolean received = false;
		while(tries < params.retries) {

			try {
				dSock.send(sendPkt);
				dSock.setSoTimeout((int)(params.timeout*1000));
				startTime = System.currentTimeMillis();

				dSock.receive(recvPkt);

				received = true;
				endTime = System.currentTimeMillis();
				break;

			} catch (SocketTimeoutException e) {
				System.out.println("request timed out");
				tries++;
			}
		}

		if(!received) {
			System.out.println("failed to receive response in " + tries + " attempts: aborting");
			System.exit(-1);
		}

		System.out.println("Response received after " + (endTime - startTime)/1000.0 + "seconds (" + (tries+1) + " retries)");



		

		return;

	}
}