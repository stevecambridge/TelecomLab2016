package Dns;

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

public class DnsQuery {

	final short A_QUERY = 0x0001;
	final short NS_QUERY = 0x0002;
	final short MX_QUERY = 0x000f;
	final short QCLASS = 0x0001;
	
	int id;
	int qdCount;
	int anCount;
	int nsCount;
	int arCount;

	short qType;

	byte[] query;

	public DnsQuery(String host, boolean ns, boolean mx) throws IOException {

		/*HEADER SECTION*/
		//a byte array output stream that we can just write to sequentially instead of keeping track of hella indices
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		DataOutputStream req = new DataOutputStream(bytesOut);

		//generate random id
		Random r = new Random();
		id = r.nextInt();

		//set other stuff
		qdCount = 1;
		anCount = 0;
		nsCount = 0;
		arCount = 0;

		//write stuff
		req.writeShort(id);
		req.writeShort(1 << 8);
		req.writeShort(qdCount);
		req.writeShort(anCount);
		req.writeShort(nsCount);
		req.writeShort(arCount);


		/*REQUEST SECTION*/

		//tokenize hostname and write into QNAME section
		int tokenIndex = 0;
		String token = "";
		for(int i=0; i<host.length(); i++) {
			if(host.charAt(i) == '.') {
				req.writeByte(token.length());
				for(int j=0; j<token.length(); j++){
					req.writeByte(token.charAt(j));
				}
				token = "";
			} else {
				token = token + host.charAt(i);
			}
		}
		req.writeByte(token.length());
		for(int j=0; j<token.length(); j++){
			req.writeByte(token.charAt(j));
		}
		req.writeByte(0x00);

		//write QTYPE
		if(ns) {
			qType = NS_QUERY;
		} else if(mx) {
			qType = MX_QUERY;
		} else {
			qType = A_QUERY;
		}
		req.writeShort(qType);

		//write QCLASS
		req.writeShort(QCLASS);

		query = bytesOut.toByteArray();
	}
}