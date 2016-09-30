package Dns;

import java.net.InetAddress;

public class DnsClient {

	public static void main(String[] args) throws Exception {

		DnsArgs params = new DnsArgs(args);
		System.out.println(params.timeout);
		System.out.println(params.retries);
		System.out.println(params.port);
		System.out.println(params.mailServer);
		System.out.println(params.nameServer);
		System.out.println(params.ip);
		System.out.println(params.name);

		InetAddress testaddr = InetAddress.getByAddress(params.ip);

		return;

	}
}