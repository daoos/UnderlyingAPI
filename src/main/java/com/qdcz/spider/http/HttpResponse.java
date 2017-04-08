package com.qdcz.spider.http;

// JDK imports
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


/** An HTTP response. */
public class HttpResponse implements Response {
 
  private HttpBase http; 
  private URL url;
  private String orig;
  private String base;
  private byte[] content;
  private int code;
  private Metadata headers = new SpellCheckedMetadata();


  public HttpResponse(HttpBase http, String str)
    throws Exception {

    this.http = http;
    this.url = new URL(str.split("\\s+")[0]);
    this.orig = url.toString();
    this.base = url.toString();

    if (!"http".equals(url.getProtocol()))
      throw new Exception("Not an HTTP url:" + url);

    //构建HTTP请求报文
    HttpRequestPackageBuilder builder = HttpRequestPackageBuilder.getInstance(str);
    if(builder==null){
    	throw new Exception("Error occured when building a http request package");
    }
    Socket socket = null;

    try {
      socket = new Socket();                    // create the socket
      socket.setSoTimeout(http.getTimeout());

      //设置socket连接，判断是否使用代理
      String sockHost = builder.useProxy() ? builder.getProxyAddr() : builder.getHost();
      int sockPort = builder.useProxy() ? builder.getProxyPort() : builder.getPort();
      InetSocketAddress sockAddr= new InetSocketAddress(sockHost, sockPort);
      //使用全局超时，如有必要可修改HttpRequestPackageBuilder定制每个URL的请求超时时间
      socket.connect(sockAddr, http.getTimeout());

      // make request
      OutputStream req = socket.getOutputStream();

      byte[] reqBytes= builder.getRequestPackage().toString().getBytes();
      
      req.write(reqBytes);
      req.flush();
//      byte[] buffer = new byte[1024*1024];
//      socket.getInputStream().read(buffer);
//      System.out.println(new String(buffer));
      PushbackInputStream in =                  // process response
        new PushbackInputStream(
          new BufferedInputStream(socket.getInputStream(), Http.BUFFER_SIZE), Http.BUFFER_SIZE) ;
      
      StringBuffer line = new StringBuffer();

      boolean haveSeenNonContinueStatus= false;
      int continueStatatusNum=0;
      while (!haveSeenNonContinueStatus&&continueStatatusNum<2000) {
    	continueStatatusNum++;
        // parse status code line
        this.code = parseStatusLine(in, line); 
        // parse headers
        parseHeaders(in, line);
        haveSeenNonContinueStatus= code != 100; // 100 is "Continue"
        
      }

      /**
       * modified on 2015-05-18
       * add start
       * 增加响应头逻辑判断
       * 修复爬虫不能识别部分站点的响应头,解压数据失败导致下载失败的bug
       */
      //readPlainContent(in);
      String transferEncoding = getHeader(Response.TRANSFER_ENCODING); 
      if(transferEncoding != null && "chunked".equalsIgnoreCase(transferEncoding.trim())){    	  
        readChunkedContent(in, line);
      }else{
        readPlainContent(in);
      }
      /**
       * add end
       */

      String contentEncoding = getHeader(Response.CONTENT_ENCODING);
      if ("gzip".equals(contentEncoding) || "x-gzip".equals(contentEncoding)) {
        content = http.processGzipEncoded(content, url);
      } else if ("deflate".equals(contentEncoding)) {
       content = http.processDeflateEncoded(content, url);
      } else {
         // System.out.println("downloaded " + content.length + " bytes from " + url);
      }

    } finally {
      if (socket != null)
        socket.close();
    }

  }

  
  /* ------------------------- *
   * <implementation:Response> *
   * ------------------------- */
  
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
    if (http.getMaxContent() >= 0
      && contentLength > http.getMaxContent())   // limit download size
      contentLength  = http.getMaxContent();

    ByteArrayOutputStream out = new ByteArrayOutputStream(Http.BUFFER_SIZE);
    byte[] bytes = new byte[Http.BUFFER_SIZE];
    int length = 0; 
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

      if ( (contentBytesRead + chunkLen) > http.getMaxContent() )
        chunkLen= http.getMaxContent() - contentBytesRead;

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

    if (!doneChunks) {
      if (contentBytesRead != http.getMaxContent()) 
        throw new Exception("chunk eof: !doneChunk && didn't max out");
      return;
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
      throw new Exception("bad status line '" + line  + "': " + e.getMessage(), e);
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
    throws Exception {
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

}
