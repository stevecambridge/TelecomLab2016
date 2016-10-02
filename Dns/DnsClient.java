package Dns;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

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
		while(tries <= params.retries) {
			dSock.send(sendPkt);
			dSock.setSoTimeout((int)(params.timeout*1000));

			dsock.receive()


		}



		

		return;

	}
}