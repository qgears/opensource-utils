package hu.qgears.remote;

public class Test {
	enum UART_PARAM {
		UART_modeASYNC(0x0000), // !< UART wird asynchron betrieben
		UART_modeSYNC_TO_POSITIVE_FIRST_EDGE(0xC000), // !< UART wird synchron auf die erste positive Flanke (als USART)
														// betrieben
		UART_modeSYNC_TO_POSITIVE_SECOND_EDGE(0xE000), // !< UART wird synchron auf die zweite positive Flanke (als
														// USART) betrieben
		UART_modeSYNC_TO_NEGATIVE_FIRST_EDGE(0x8000), // !< UART wird synchron auf die erste negative Flanke (als USART)
														// betrieben
		UART_modeSYNC_TO_NEGATIVE_SECOND_EDGE(0xA000), // !< UART wird synchron auf die zweite negative Flanke (als
		
		WORD_LENGTH_20(0x20),
		SMART_CARD_C00(0xC00),
		HALF_DUP_200(0x200),
		STOP6(0x6),
		UART_stopbitOne(0x0000), // !< UART benutzt 1 Stopbit
		UART_stopbitHalf(0x0002), // !< UART benutzt 0,5 Stopbits
		UART_stopbitOneAndHalf(0x0003), // !< UART benutzt 1,5 Stopbits
		UART_stopbitTwo(0x0004), // !< UART benutzt 2 Stopbits
		
		HS(0x0),
		UART_handshakeNone(0x0000), // !< UART verwendet kein Handshaking
		UART_handshakeRTSCTS(0x0001), // !< UART verwendet Hardware-Handshaking mit RTS/CTS

		PAR18(0x0018), // !< UART verwendet keine Parität
		UART_parityNone(0x0000), // !< UART verwendet keine Parität
		UART_parityEven(0x0030), // !< UART verwendet gerade Parität
		UART_parityOdd(0x0038); 		// !< UART verwendet ungerade Parität
		
		private int val;


		UART_PARAM(int val) {
			this.val = val;
		}
		
		public int getVal() {
			return val;
		}
	};

	public static void main(String[] args) {
		
		for (UART_PARAM up : UART_PARAM.values()) {
			if (!up.name().startsWith("UART")) {
				System.out.println();
			}
			printBin(up.val,up.name());
		}
		
	}

	private static void printBin(int p, String n) {
		StringBuilder bin = new StringBuilder(Integer.toBinaryString(p));
		
		while (bin.length() != 16) {
			bin.insert(0, "0");
		}
		int s = 12;
		bin.insert(s, "|");
		bin.insert(s-4, "|");
		bin.insert(s-8, "|");
		
		System.out.println(bin+ " = "+n);
	}
}
