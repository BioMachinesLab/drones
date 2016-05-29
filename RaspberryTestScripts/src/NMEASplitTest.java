public class NMEASplitTest {
	public static void main(String[] args) {
		new NMEASplitTest();
	}

	public NMEASplitTest() {
		String str;

		// $GPGGA
		/* no fix */
		// str = "$GPGGA,013733.087,,,,,0,0,,,M,,M,,*42";
		/* test */
		// str =
		// "$GPGGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh";
		/* with fix */
		// str =
		// "$GPGGA,035814.000,3845.1152,N,00916.7390,W,2,9,0.98,155.5,M,50.7,M,0000,0000*46";

		// $GPGSA
		/* no fix */
		// str = "$GPGSA,A,1,,,,,,,,,,,,,,,*1E";
		/* with fix */
		// str = "$GPGSA,A,3,19,28,14,18,27,22,31,39,,,,,1.7,1.0,1.3*35";

		// $GPGSV
		/* no fix */
		// str = "$GPGSV,1,1,00*79";
		/* with fix */
		// str =
		// "$GPGSV,3,1,11,23,70,086,37,13,57,179,46,17,52,248,33,20,48,058,22*76";
		// str =
		// "$GPGSV,3,2,11,33,44,189,39,04,37,310,24,01,33,120,45,10,21,266,37*71";
		// str = "$GPGSV,3,3,11,11,12,137,42,32,11,067,16,31,07,039,*41";

		// $GPRMC
		/* no fix */
		// str = "$GPRMC,175433.088,V,,,,,0.00,0.00,290614,,,N*42";
		/* with fix */
		str = "$GPRMC,035937.000,A,3845.1147,N,00916.7387,W,0.01,199.58,120514,,,D*70";

		// $GPVTG
		/* no fix */
		// str = "$GPVTG,0.00,T,,M,0.00,N,0.00,K,N*32";
		/* with fix */
		// str = "$GPVTG,199.58,T,,M,0.01,N,0.02,K,D*37";

		System.out.println(str + "\n");
		String[] params = str.split(",");
		for (int i = 0; i < params.length; i++) {
			if (params[i].length() == 0)
				System.out.print("É null!");
			System.out.println("[" + i + "] " + params[i]);
		}

		params[1] = params[1].replace(".", "");
		String[] t = params[1].split("(?<=\\G.{2})");
		for (String s : t) {
			System.out.println(s);
		}
	}

	public String stringToHex(String string) {
		StringBuilder buf = new StringBuilder(200);
		for (char ch : string.toCharArray()) {
			if (buf.length() > 0)
				buf.append(' ');
			buf.append(String.format("%02x", (int) ch));
		}
		return buf.toString();
	}

}
