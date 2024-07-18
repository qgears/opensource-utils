package hu.qgears.xtextgrammar.lsp;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.json.JSONArray;
import org.json.JSONObject;

import hu.qgears.xtextgrammar.lsp.ITokenizer.Token5;

/**
 * Handle TCP connection of an LSP server communication session.
 */
public class LspServerSession extends Thread {
	Socket s;
	ITokenizer tokenizer;
	ILspServerModel model;
	File logsFolder;
	public static final int MAX_LINE_LENGTH=1024;

//	private LspServerSession(Socket s, ITokenizer tokenizer) {
//		super("LspServerSession");
//		this.s = s;
//		this.tokenizer = tokenizer;
//	}
	public LspServerSession(Socket s, ILspServerModel model) {
		super("LspServerSession");
		this.s = s;
		this.tokenizer = model.getTokenizer();
		this.model = model;
		long session_id = System.currentTimeMillis();
		this.logsFolder = new File(model.getLogsFolder(), String.valueOf(session_id));
		logsFolder.mkdirs();
	}
	
	private class Logger {
		long id = 1;
		public File getLogFile(String postfix) {
			File result = new File(logsFolder, "%04d%s".formatted(id, postfix == null ? "" : postfix));
			id += 1;
			return result;
		}
	}
	Logger logger = new Logger();

	InputStream is;

	@Override
	public void run() {
		try {
			is = s.getInputStream();
			while (true) {
				processInput();
				// System.out.print((char)ch);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String readLine() throws IOException {
		StringBuilder line = new StringBuilder();
		while (true) {
			int ch = is.read();
			if (ch < 0) {
				throw new EOFException();
			}
			switch (ch) {
			case '\r':
				break;
			case '\n':
				System.out.println("LINE: " + line);
				String l = line.toString();
				return l;
			default:
				line.append((char) ch);
				if(line.length()>MAX_LINE_LENGTH)
				{
					throw new IOException("Too long line");
				}
				break;
			}
		}
	}

	private void processInput() throws IOException {
		JSONObject json; { //read message
			String l=readLine();
			if (!l.startsWith("Content-Length: ")) {
				throw new IOException("Unknown input: "+l);
			}
			
			int length = Integer.parseInt(l.substring("Content-Length: ".length()));
			readLine();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for (int i = 0; i < length; ++i) {
				int b = is.read();
				if (b < 0) {
					throw new IOException();
				}
				bos.write((byte) b);
			}
			
			String json_string = new String(bos.toByteArray(), StandardCharsets.UTF_8); //TODO utf-16, 32
			File logFile = logger.getLogFile("_in");
			FileWriter writer = new FileWriter(logFile);
			writer.write(json_string);
			writer.close();
			json = new JSONObject(json_string);								
		}
		//debug print
		System.out.println("JSON command: " + json.toString(1));
		
		if (json.has("method")) { //respond to request
			JSONObject response; { //basic response
				Object id_jsonNullable = JSONObject.NULL;
				if (json.has("id") && !json.isNull("id")) {
					id_jsonNullable = json.get("id");
				}
				
				response = new JSONObject()
						.put("jsonrpc", "2.0")
						.put("id", id_jsonNullable);
			}
			enum SupportedRequest {
				initialize("initialize"),
				semanticTokens("textDocument/semanticTokens/full"),
				findReferences("textDocument/references");
				public final String method;
				SupportedRequest(String method) {
					this.method = method;
				}
				public static Optional<SupportedRequest> get(String method) {
					return Arrays.stream(SupportedRequest.values())
							.filter(it -> it.method.equals(method))
							.findAny();
				}
			}
			SupportedRequest method; {
				Optional<SupportedRequest> method_optional = SupportedRequest.get(json.getString("method"));
				if (!method_optional.isPresent()) {
					return;
				}
				method = method_optional.get();
			}			
			switch (method) {
			case initialize:
				response.put("result", new JSONObject()
						.put("capabilities", new JSONObject()
								.put("semanticTokensProvider", new JSONObject()
										.put("full", true)
										.put("legend", new JSONObject()
												.put("tokenTypes", new JSONArray(tokenizer.getTokenTypes()))
												.put("tokenModifiers", new JSONArray(tokenizer.getTokenModifiers()))))));					
				break;
			case semanticTokens:
				//TODO is this right
				//TODO safety?
				URI uri = URI.createURI(json.getJSONObject("params").getJSONObject("textDocument").getString("uri"));
				response.put("result", new JSONObject()
						.put("data", tokenize(uri)));
				break;
			case findReferences:
				URI textDocument;
				int line;
				int character;
				{
					JSONObject params = json.getJSONObject("params");
					textDocument = URI.createURI(params.getString("textDocument"));
					JSONObject position = params.getJSONObject("position");
					line = position.getInt("line");
					character = position.getInt("character");
				}
				break;
			default:
				return;
			}
			sendMessage(response);
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private Object tokenize(URI uri) {
		List<Token5> tokens = tokenizer.tokenize(uri);
		if (tokens == null) {
			return JSONObject.NULL;
		}
		return new JSONArray(tokens.stream().flatMap(it -> Stream.of(it.line, it.column, it.length, it.type, it.modifiers)).toList());
	}

	private void sendMessage(JSONObject message) throws IOException {
		/*log the response*/ {
			File logFile = logger.getLogFile("_out");
			FileWriter writer = new FileWriter(logFile);
			writer.write(message.toString());
			writer.close();
		}
		System.out.println(message.toString(1));
		byte[] messageData=message.toString().getBytes(StandardCharsets.UTF_8);
		s.getOutputStream().write(("Content-Length: "+messageData.length+"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
		s.getOutputStream().write(messageData);
		s.getOutputStream().flush();
	}
}
