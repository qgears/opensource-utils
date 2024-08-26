package hu.qgears.xtextgrammar.lsp;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
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
	ILspServerModel model;
	public static final int MAX_LINE_LENGTH=1024;
	Logger logger;

	public LspServerSession(Socket s, ILspServerModel model) {
		super("LspServerSession");
		this.s = s;
		this.model = model;
		long session_id = System.currentTimeMillis();
		if (model.getLogsFolder() == null) {
			this.logger = new Logger(null);
		} else {			
			this.logger = new Logger(new File(model.getLogsFolder(), String.valueOf(session_id)));
		}
	}
	
	private static class Logger {
		private long id = 1;
		private File logsFolder;
		public Logger(File logsFolder) {
			if (this.logsFolder != null) {
				logsFolder.mkdirs();
			}
			this.logsFolder = logsFolder;
		}
		private File getLogFile(String postfix) {
			File result = new File(logsFolder, "%04d%s".formatted(id, postfix == null ? "" : postfix));
			id += 1;
			return result;
		}
		//If the logs folder is null aka not given, then no logs are made.
		public void log(String file_name_postfix, String content) throws IOException {
			if (logsFolder == null) return;
			File logfile = getLogFile(file_name_postfix);
			FileWriter writer = new FileWriter(logfile);
			writer.write(content);
			writer.close();
		}
	}

	InputStream is;

	@Override
	public void run() {
		try {
			is = s.getInputStream();
			while (true) {
				processInput();
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
	
	private static enum MessageType {
		Request, Response, Notification;
		public static MessageType get(JSONObject message) {
			Set<String> keyset = message.keySet();
			Set<String> requestKeys = Set.of("jsonrpc", "id", "method"); //optional: params
			Set<String> responseKeys = Set.of("jsonrpc", "id"); //optional: result, error
			Set<String> notificationKeys = Set.of("jsonrpc", "method"); //optional: params
			if (keyset.containsAll(requestKeys)) return Request;
			if (keyset.containsAll(responseKeys)) return Response;
			if (keyset.containsAll(notificationKeys)) return Notification;
			return null;
		}
	}

	//listen for requests and respond.
	private void processInput() throws IOException {
		JSONObject message = readMessage();
		System.out.println("JSON command: " + message.toString(1)); //debug print
		
		/* validate message. take only requests for now. */ {
			MessageType messageType = MessageType.get(message);
			if (!MessageType.Request.equals(messageType)) {
				return;
			}
		}

		JSONObject response = processRequest(message);
		addRequiredFields(message, response);
		sendMessage(response);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private JSONObject readMessage() throws IOException {
		JSONObject message;
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
		String message_string = new String(bos.toByteArray(), StandardCharsets.UTF_8); //TODO utf-16, 32
		message = new JSONObject(message_string);
		logger.log("_in", message.toString(1));
		return message;
	}
	
	private JSONObject addRequiredFields(JSONObject request, JSONObject response) {
		return response
				.put("jsonrpc", "2.0")
				.put("id", request.get("id"));
	}
	
	/**
	 * 
	 * @param request
	 * @return A JSONObject that may contain the result or the error.
	 */
	private JSONObject processRequest(JSONObject request) {
		switch (request.getString("method")) {
		case "initialize":
			return processRequest_initialize(request);
		case "textDocument/semanticTokens/full":
			return processRequest_semanticTokens(request);
		case "textDocument/definition":
			return processRequest_findDefinition(request);
		default:
			return new JSONObject();
		}
	}
	
	private JSONObject processRequest_findDefinition(JSONObject request) {
		String textDocumentUri_string; int line, character; { //get the parameters
			JSONObject params = request.getJSONObject("params");
			textDocumentUri_string = params.getJSONObject("textDocument").getString("uri");
			JSONObject position = params.getJSONObject("position");
			line = position.getInt("line");
			character = position.getInt("character");
		}
		
		IDefinitionProvider.Location location; {
			IDefinitionProvider linkProvider = model.getLinkProvider();
			if (linkProvider == null) {
				return new JSONObject();
			}
			location = linkProvider.findDefinition(textDocumentUri_string, line, character);
			if (location == null) {
				return new JSONObject();
			}
		}
		
		return new JSONObject()
				.put("result", new JSONObject()
						.put("uri", location.uri)
						.put("range", new JSONObject()
								.put("start", new JSONObject()
										.put("line", location.start_line)
										.put("character", location.start_column))
								.put("end", new JSONObject()
										.put("line", location.start_line)
										.put("character", location.start_column))));
	}

	private JSONObject processRequest_semanticTokens(JSONObject request) {
		URI uri = URI.createURI(request.getJSONObject("params").getJSONObject("textDocument").getString("uri"));
		return new JSONObject()
				.put("result", new JSONObject()
						.put("data", tokenize(uri)));
	}

	private JSONObject processRequest_initialize(JSONObject request) {
		return new JSONObject()
				.put("result", new JSONObject()
						.put("capabilities", new JSONObject()
								.put("semanticTokensProvider", new JSONObject()
										.put("full", true)
										.put("legend", new JSONObject()
												.put("tokenTypes", new JSONArray(model.getTokenizer().getTokenTypes()))
												.put("tokenModifiers", new JSONArray(model.getTokenizer().getTokenModifiers()))))
								.put("definitionProvider", true)));
	}

	private Object tokenize(URI uri) {
		List<Token5> tokens = model.getTokenizer().tokenize(uri);
		if (tokens == null) {
			return JSONObject.NULL;
		}
		return new JSONArray(tokens.stream().flatMap(it -> Stream.of(it.line, it.column, it.length, it.type, it.modifiers)).toList());
	}

	private void sendMessage(JSONObject message) throws IOException {
		logger.log("_out", message.toString(1));
		System.out.println(message.toString(1));
		byte[] messageData=message.toString().getBytes(StandardCharsets.UTF_8);
		s.getOutputStream().write(("Content-Length: "+messageData.length+"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
		s.getOutputStream().write(messageData);
		s.getOutputStream().flush();
	}
}
