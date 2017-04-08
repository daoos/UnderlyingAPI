package com.qdcz.spider.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

public class IndependenceSocket {

	private Socket socket = null;
	private int code;
	 private Metadata headers = new SpellCheckedMetadata();
	  private byte[] content;
	  private URL url;
	
	public IndependenceSocket(){
		
	}
	
	public static void main(String[] args){
		
		IndependenceSocket instance = new IndependenceSocket();

		
					try {
						instance.exe();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			

		
	}
	
	public void exe() throws Exception{
		
		socket = new Socket();                    // create the socket
	    socket.setSoTimeout(60000);
	    String sockHost = "61.135.158.90";
	    int sockPort = 80;
	    InetSocketAddress sockAddr= new InetSocketAddress(sockHost, sockPort);
	    socket.connect(sockAddr, 60000);
	    
	 // make request
	    OutputStream req = socket.getOutputStream();

	    StringBuffer reqStr = new StringBuffer("GET /webmail/readmail/context.action?folderName=INBOX&uid=2&location=0&coremail= HTTP/1.1\r\n"+
	                                           "Accept: text/html, application/xhtml+xml, */*\r\n" +
	    		                               "Referer: http://61.135.158.90/webmail/main/index.action\r\n" + 
	                                           "Accept-Language: zh-CN\r\n" + 
	    		                               "User-Agent: Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0; BOIE9;ZHCN)\r\n" +
	                                           "Accept-Encoding: gzip, deflate\r\n" +
	    		                               "Host: 61.135.158.90\r\n" +
	                                           "DNT: 1\r\n" +
	    		                               "Connection: Keep-Alive\r\n" +
                                               "Cookie: JSESSIONID=1ojvbq7hiv0r71kzkglw160w4b; Hm_lvt_76487c578ac2d0d5b2ccc78fc1b875c7=1383824881,1383825316,1383825392,1383829459; user_token=EB3FA7C9825D9503797B696A8F0A2511CEEEF285050DD228BE934CE186E671A8; SERVERID=CNC_173217_B; BIGipServermail-80=1806375084.20480.0000; Hm_lpvt_76487c578ac2d0d5b2ccc78fc1b875c7=1383829714\r\n\r\n");
	      
	      byte[] reqBytes= reqStr.toString().getBytes();

	      req.write(reqBytes);
	      req.flush();
	        
	      PushbackInputStream in =                  // process response
	        new PushbackInputStream(
	          new BufferedInputStream(socket.getInputStream(), Http.BUFFER_SIZE),
	          Http.BUFFER_SIZE) ;

	      StringBuffer line = new StringBuffer();

	      boolean haveSeenNonContinueStatus= false;
	      while (!haveSeenNonContinueStatus) {
	        // parse status code line
	        this.code = parseStatusLine(in, line); 
	        // parse headers
	        parseHeaders(in, line);
	        haveSeenNonContinueStatus= code != 100; // 100 is "Continue"
	      }

	      readPlainContent(in);

	      String contentEncoding = getHeader(Response.CONTENT_ENCODING);
	      if ("gzip".equals(contentEncoding) || "x-gzip".equals(contentEncoding)) {
	        content = processGzipEncoded(content, url);
	      } else if ("deflate".equals(contentEncoding)) {
	       content = processDeflateEncoded(content, url);
	      } else {
	         // System.out.println("downloaded " + content.length + " bytes from " + url);
	      }
	      
	      System.out.println(new String(content));

	      if (socket != null)
	        socket.close();
	}


public URL getUrl() {
    return url;
  }
  
  public int getCode() {
    return code;
  }

  public String getHeader(String name) {
    return headers.get(name);
  }
  
  public Metadata getHeaders() {
    return headers;
  }

  public byte[] getContent() {
    return content;
  }

/* ------------------------- *
 * <implementation:Response> *
 * ------------------------- */


private void readPlainContent(InputStream in) 
  throws Exception {

  int contentLength = Integer.MAX_VALUE;    // get content length
  String contentLengthString = headers.get(Response.CONTENT_LENGTH);
  if (contentLengthString != null) {
    contentLengthString = contentLengthString.trim();
    try {
      contentLength = Integer.parseInt(contentLengthString);
    } catch (NumberFormatException e) {
      throw new Exception("bad content length: "+contentLengthString);
    }
  }

  ByteArrayOutputStream out = new ByteArrayOutputStream(Http.BUFFER_SIZE);
  byte[] bytes = new byte[Http.BUFFER_SIZE];
  int length = 0;                           // read content
  for (int i = in.read(bytes); i != -1; i = in.read(bytes)) {

    out.write(bytes, 0, i);
    length += i;
    if (length >= contentLength)
      break;
  }
  content = out.toByteArray();
}

private void readChunkedContent(PushbackInputStream in,  
                                StringBuffer line) 
  throws Exception {
  boolean doneChunks= false;
  int contentBytesRead= 0;
  byte[] bytes = new byte[Http.BUFFER_SIZE];
  ByteArrayOutputStream out = new ByteArrayOutputStream(Http.BUFFER_SIZE);

  while (!doneChunks) {
    
    System.out.println("Http: starting chunk");
   

    readLine(in, line, false);

    String chunkLenStr;
    // if (LOG.isTraceEnabled()) { LOG.trace("chunk-header: '" + line + "'"); }

    int pos= line.indexOf(";");
    if (pos < 0) {
      chunkLenStr= line.toString();
    } else {
      chunkLenStr= line.substring(0, pos);
      // if (LOG.isTraceEnabled()) { LOG.trace("got chunk-ext: " + line.substring(pos+1)); }
    }
    chunkLenStr= chunkLenStr.trim();
    int chunkLen;
    try {
      chunkLen= Integer.parseInt(chunkLenStr, 16);
    } catch (NumberFormatException e){ 
      throw new Exception("bad chunk length: "+line.toString());
    }

    if (chunkLen == 0) {
      doneChunks= true;
      break;
    }

    // read one chunk
    int chunkBytesRead= 0;
    while (chunkBytesRead < chunkLen) {

      int toRead= (chunkLen - chunkBytesRead) < Http.BUFFER_SIZE ?
                  (chunkLen - chunkBytesRead) : Http.BUFFER_SIZE;
      int len= in.read(bytes, 0, toRead);

      if (len == -1) 
        throw new Exception("chunk eof after " + contentBytesRead
                                    + " bytes in successful chunks"
                                    + " and " + chunkBytesRead 
                                    + " in current chunk");

      // DANGER!!! Will printed GZIPed stuff right to your
      // terminal!
      // if (LOG.isTraceEnabled()) { LOG.trace("read: " +  new String(bytes, 0, len)); }

      out.write(bytes, 0, len);
      chunkBytesRead+= len;  
    }

    readLine(in, line, false);

  }

  content = out.toByteArray();
  parseHeaders(in, line);

}

private int parseStatusLine(PushbackInputStream in, StringBuffer line)
  throws Exception {
  readLine(in, line, false);

  int codeStart = line.indexOf(" ");
  int codeEnd = line.indexOf(" ", codeStart+1);

  // handle lines with no plaintext result code, ie:
  // "HTTP/1.1 200" vs "HTTP/1.1 200 OK"
  if (codeEnd == -1) 
    codeEnd= line.length();

  int code;
  try {
    code= Integer.parseInt(line.substring(codeStart+1, codeEnd));
  } catch (NumberFormatException e) {
    throw new Exception("bad status line '" + line 
                            + "': " + e.getMessage(), e);
  }

  return code;
}


private void processHeaderLine(StringBuffer line)
  throws Exception {

  int colonIndex = line.indexOf(":");       // key is up to colon
  if (colonIndex == -1) {
    int i;
    for (i= 0; i < line.length(); i++)
      if (!Character.isWhitespace(line.charAt(i)))
        break;
    if (i == line.length())
      return;
    throw new Exception("No colon in header:" + line);
  }
  String key = line.substring(0, colonIndex);

  int valueStart = colonIndex+1;            // skip whitespace
  while (valueStart < line.length()) {
    int c = line.charAt(valueStart);
    if (c != ' ' && c != '\t')
      break;
    valueStart++;
  }
  String value = line.substring(valueStart);
  headers.set(key, value);
}


// Adds headers to our headers Metadata
private void parseHeaders(PushbackInputStream in, StringBuffer line)
  throws Exception {

  while (readLine(in, line, true) != 0) {

    // handle HTTP responses with missing blank line after headers
    int pos;
    if ( ((pos= line.indexOf("<!DOCTYPE")) != -1) 
         || ((pos= line.indexOf("<HTML")) != -1) 
         || ((pos= line.indexOf("<html")) != -1) ) {

      in.unread(line.substring(pos).getBytes("UTF-8"));
      line.setLength(pos);

      try {
          //TODO: (CM) We don't know the header names here
          //since we're just handling them generically. It would
          //be nice to provide some sort of mapping function here
          //for the returned header names to the standard metadata
          //names in the ParseData class
        processHeaderLine(line);
      } catch (Exception e) {
        // fixme:
        e.printStackTrace();
      }
      return;
    }

    processHeaderLine(line);
  }
}

private static int readLine(PushbackInputStream in, StringBuffer line,
                    boolean allowContinuedLine)
  throws IOException {
  line.setLength(0);
  for (int c = in.read(); c != -1; c = in.read()) {
    switch (c) {
      case '\r':
        if (peek(in) == '\n') {
          in.read();
        }
      case '\n': 
        if (line.length() > 0) {
          // at EOL -- check for continued line if the current
          // (possibly continued) line wasn't blank
          if (allowContinuedLine) 
            switch (peek(in)) {
              case ' ' : case '\t':                   // line is continued
                in.read();
                continue;
            }
        }
        return line.length();      // else complete
      default :
        line.append((char)c);
    }
  }
  throw new EOFException();
}

private static int peek(PushbackInputStream in) throws IOException {
  int value = in.read();
  in.unread(value);
  return value;
}


public byte[] processGzipEncoded(byte[] compressed, URL url) throws IOException {

    System.out.println("uncompressing....");

    byte[] content;
 
        content = GZIPUtils.unzipBestEffort(compressed);


    if (content == null)
      throw new IOException("unzipBestEffort returned null");

    
    System.out.println("fetched " + compressed.length
                 + " bytes of compressed content (expanded to "
                 + content.length + " bytes) from " + url);
    return content;
  }

  public byte[] processDeflateEncoded(byte[] compressed, URL url) throws IOException {

    System.out.println("inflating....");

    byte[] content = DeflateUtils.inflateBestEffort(compressed, 64*1024*1024);

    if (content == null)
      throw new IOException("inflateBestEffort returned null");

   
      System.out.println("fetched " + compressed.length
                 + " bytes of compressed content (expanded to "
                 + content.length + " bytes) from " + url);
    return content;
  }
}
