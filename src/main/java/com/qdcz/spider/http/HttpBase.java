package com.qdcz.spider.http;

// JDK imports
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;


public abstract class HttpBase{
  
  public static final int BUFFER_SIZE = 8 * 1024;
  
  private static final byte[] EMPTY_CONTENT = new byte[0];

  public RobotRulesParser robots = null;
 
  /** The proxy hostname. */ 
  public String proxyHost = null;

  /** The proxy port. */
  public int proxyPort = 8080; 

  /** Indicates if a proxy is used */
  public boolean useProxy = false;

  /** The network timeout in millisecond */
  public int timeout = 10000;

  /** The length limit for downloaded content, in bytes. */
  public int maxContent = 64 * 1024; 

  /** The number of times a thread will delay when trying to fetch a page. */
  public int maxDelays = 3;

  /**
   * The maximum number of threads that should be allowed
   * to access a host at one time.
   */
  public int maxThreadsPerHost = 1; 

  /**
   * The number of seconds the fetcher will delay between
   * successive requests to the same server.
   */
  public long serverDelay = 1000;

  /** The Nutch 'User-Agent' request header */
  public String userAgent = getAgentString(
                        "NutchCVS", null, "Nutch",
                        "http://lucene.apache.org/nutch/bot.html",
                        "nutch-agent@lucene.apache.org");

  /** The "Accept-Language" request header value. */
  public String acceptLanguage = "en-us,en-gb,en;q=0.7,*;q=0.3";
    
  /**
   * Maps from host to a Long naming the time it should be unblocked.
   * The Long is zero while the host is in use, then set to now+wait when
   * a request finishes.  This way only one thread at a time accesses a
   * host.
   */
  public static HashMap BLOCKED_ADDR_TO_TIME = new HashMap();
  
  /**
   * Maps a host to the number of threads accessing that host.
   */
  public static HashMap THREADS_PER_HOST_COUNT = new HashMap();
  
  /**
   * Queue of blocked hosts.  This contains all of the non-zero entries
   * from BLOCKED_ADDR_TO_TIME, ordered by increasing time.
   */
  public static LinkedList BLOCKED_ADDR_QUEUE = new LinkedList();
  
  /** Do we block by IP addresses or by hostnames? */
  public boolean byIP = true;
 
  /** Do we use HTTP/1.1? */
  public boolean useHttp11 = false;
  
  /** Skip page if Crawl-Delay longer than this value. */
  public long maxCrawlDelay = -1L;

  /** Plugin should handle host blocking internally. */
  public boolean checkBlocking = true;
  
  /** Plugin should handle robot rules checking internally. */
  public boolean checkRobots = false;

  
  /** Creates a new instance of HttpBase */
  public HttpBase() {
    robots = new RobotRulesParser();
  }
  
   // Inherited Javadoc
    public void setConf() {
    	/**
    	 * 
         http.proxy.host
         The proxy hostname.  If empty, no proxy is used.
         Default is ""
    	 */
        this.proxyHost = null;
        this.proxyPort = 8080;
        this.useProxy = (proxyHost != null && proxyHost.length() > 0);
        
        
//        this.timeout =1000*30*1;
        this.timeout =1000*15;
        this.maxContent = 64 * 1024 * 1024; //64M
        
        /**
         * 
         http.max.delays

         The number of times a thread will delay when trying to
         fetch a page.  Each time it finds that a host is busy, it will wait
         fetcher.server.delay.  After http.max.delays attepts, it will give
         up on the page for now.
         */
        this.maxDelays = 10;
        this.maxThreadsPerHost =  1;
        this.userAgent = getAgentString("google spider", "1.1", "Google Spider", "http://www.google.com", "zhstc278@gmail.com");
        this.acceptLanguage =  acceptLanguage;
        
        /**
         * fetcher.server.delay
         * The number of seconds the fetcher will delay between 
           successive requests to the same server.
         */
        this.serverDelay = (long)  1.0f * 1000;
        this.maxCrawlDelay = (long) -1* 1000;
        // backward-compatible default setting
        /**
         * If true, then fetcher will count threads by IP address,
        to which the URL's host name resolves. If false, only host name will be
        used. NOTE: this should be set to the same value as
        "generate.max.per.host.by.ip" - default settings are different only for
        reasons of backward-compatibility.
         */
        this.byIP = true;
        this.useHttp11 =  false;

        this.checkBlocking =  false;
        this.checkRobots = false;
    }
    
    
	public void getHttpPages(String url,MedicalContent mcontent) throws Exception {
		try {
			URL u = new URL(url.split("\\s+")[0]);
			long delay = serverDelay;

			if (checkRobots) {
				try {
					if (!robots.isAllowed(this, u)) {
						System.out.println("robots denied");
					}
				} catch (Throwable e) {
					System.out.println(e.getMessage());
				}

				long crawlDelay = robots.getCrawlDelay(this, u);
				delay = crawlDelay > 0 ? crawlDelay : serverDelay;
			}

			if (checkBlocking && maxCrawlDelay >= 0 && delay > maxCrawlDelay) {
				// skip this page, otherwise the thread would block for too
				// long.
				System.out.println("Skipping: " + u
						+ " exceeds fetcher.max.crawl.delay, max="
						+ (maxCrawlDelay / 1000) + ", Crawl-Delay="
						+ (delay / 1000));
			}

			String host = null;
			if (checkBlocking) {
				try {
					host = blockAddr(u, delay);
				} catch (Exception be) {
				}
			}
			
			Response response;
		    try {
		        response = getResponse(url,false); // make a request
		    } finally {
		        if (checkBlocking) unblockAddr(host, delay);
		    }
		    
		    int code = response.getCode();
		    
		    mcontent.setCode(code);
		   
		    
		    if (code == 200 || code==521) { // got a good response
		    	mcontent.protocolStatusCode = ProtocolStatus.SUCCESS;
		    	//System.out.println("Success");
		    	mcontent.setContent(response.getContent());
		    	response = null;
		    	u = null;
		      } else if (code == 410) { // page is gone
		    	mcontent.protocolStatusCode = ProtocolStatus.GONE;
		    	System.out.println("Page is gone");
		    	response = null;
		    	u = null;
		      } else if (code >= 300 && code < 400) { // handle redirect
		        String location = response.getHeader("Location");
		        // some broken servers, such as MS IIS, use lowercase header name...
		        if (location == null) location = response.getHeader("location");
		        if (location == null) location = "";
		        /**
		         * add by hzy on 2015-06-02
		         * 修复部分情况下location不包含主机头和端口信息导致重定向后得到的URL不完整而报错的bug
		         */
		        if(!location.contains("://")){
		        	String protocol = u.getProtocol();
			        String urlHost = u.getHost();
			        int port = u.getPort()==-1?80:u.getPort();
			        location = protocol+"://"+urlHost+":"+port+"/"+location;
		        }
		        //System.out.println("redirect url:"+location);
		        /**
		         * add end
		         */
		        u = new URL(u, location);
		        int protocolStatusCode;
		        switch (code) {
		          case 300:   // multiple choices, preferred value in Location
		            protocolStatusCode = ProtocolStatus.MOVED;
		            break;
		          case 301:   // moved permanently
		          case 305:   // use proxy (Location is URL of proxy)
		            protocolStatusCode = ProtocolStatus.MOVED;
		            break;
		          case 302:   // found (temporarily moved)
		          case 303:   // see other (redirect after POST)
		          case 307:   // temporary redirect
		            protocolStatusCode = ProtocolStatus.TEMP_MOVED;
		            break;
		          case 304:   // not modified
		            protocolStatusCode = ProtocolStatus.NOTMODIFIED;
		            break;
		          default:
		            protocolStatusCode = ProtocolStatus.MOVED;
		        }
		        
		        // handle this in the higher layer.
		        mcontent.setDirectUrl(location, protocolStatusCode);
		        location = null;
		        response = null;
		        u = null;
		      } else if (code == 400) { // bad request, mark as GONE
		    	mcontent.protocolStatusCode = ProtocolStatus.GONE;
		        System.out.println("400 Bad request: " + u);
		        response = null;
		        u = null;
		      } else if (code == 401) { // requires authorization, but no valid auth provided.
		    	mcontent.protocolStatusCode = ProtocolStatus.ACCESS_DENIED;
		        System.out.println("401 Authentication Required");
		        response = null;
		        u = null;
		      } else if (code == 404) {
		    	mcontent.protocolStatusCode = ProtocolStatus.NOTFOUND;
		    	System.out.println("404 Not Found");
		    	response = null;
		    	u = null;
		      } else if (code == 410) { // permanently GONE
		    	mcontent.protocolStatusCode = ProtocolStatus.GONE;
		    	System.out.println("410 permanently gone");
		    	response = null;
		    	u = null;
		      } else {
		    	mcontent.protocolStatusCode = ProtocolStatus.UNKNOWN;
		    	System.out.println("Http code=" + code + ", url="+ u);
		    	response = null;
		    	u = null;
		      }
		    
		} catch (Exception ex) {
			mcontent.protocolStatusCode = ProtocolStatus.ERROR;
			throw new Exception("error "  + ex.getMessage());
		}
	}
  
  /* -------------------------- *
   * </implementation:Protocol> *
   * -------------------------- */

  public void setTimeout(int Timeout){
	  this.timeout = Timeout;
  }
	


  public String getProxyHost() {
    return proxyHost;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public boolean useProxy() {
    return useProxy;
  }

  public int getTimeout() {
    return timeout;
  }

  public int getMaxContent() {
    return maxContent;
  }

  public int getMaxDelays() {
    return maxDelays;
  }

  public int getMaxThreadsPerHost() {
    return maxThreadsPerHost;
  }

  public long getServerDelay() {
    return serverDelay;
  }

  public String getUserAgent() {
    return userAgent;
  }
  
  /** Value of "Accept-Language" request header sent by Nutch.
   * @return The value of the header "Accept-Language" header.
   */
  public String getAcceptLanguage() {
         return acceptLanguage;
  }

  public boolean getUseHttp11() {
    return useHttp11;
  }
  
  private String blockAddr(URL url, long crawlDelay) throws Exception {
    
    String host;
    if (byIP) {
      try {
        InetAddress addr = InetAddress.getByName(url.getHost());
        host = addr.getHostAddress();
      } catch (UnknownHostException e) {
        // unable to resolve it, so don't fall back to host name
        throw new Exception(e);
      }
    } else {
      host = url.getHost();
      if (host == null)
        throw new Exception("Unknown host for url: " + url);
      host = host.toLowerCase();
    }
    
    int delays = 0;
    while (true) {
      cleanExpiredServerBlocks();                 // free held addresses
      
      Long time;
      synchronized (BLOCKED_ADDR_TO_TIME) {
        time = (Long) BLOCKED_ADDR_TO_TIME.get(host);
        if (time == null) {                       // address is free
          
          // get # of threads already accessing this addr
          Integer counter = (Integer)THREADS_PER_HOST_COUNT.get(host);
          int count = (counter == null) ? 0 : counter.intValue();
          
          count++;                              // increment & store
          THREADS_PER_HOST_COUNT.put(host, new Integer(count));
          
          if (count >= maxThreadsPerHost) {
            BLOCKED_ADDR_TO_TIME.put(host, new Long(0)); // block it
          }
          return host;
        }
      }
      
      if (delays == maxDelays)
        throw new Exception("Exceeded http.max.delays: retry later.");
      
      long done = time.longValue();
      long now = System.currentTimeMillis();
      long sleep = 0;
      if (done == 0) {                            // address is still in use
        sleep = crawlDelay;                      // wait at least delay
        
      } else if (now < done) {                    // address is on hold
        sleep = done - now;                       // wait until its free
      }
      
      try {
        Thread.sleep(sleep);
      } catch (InterruptedException e) {}
      delays++;
    }
  }
  
  private void unblockAddr(String host, long crawlDelay) {
    synchronized (BLOCKED_ADDR_TO_TIME) {
      int addrCount = ((Integer)THREADS_PER_HOST_COUNT.get(host)).intValue();
      if (addrCount == 1) {
        THREADS_PER_HOST_COUNT.remove(host);
        BLOCKED_ADDR_QUEUE.addFirst(host);
        BLOCKED_ADDR_TO_TIME.put
                (host, new Long(System.currentTimeMillis() + crawlDelay));
      } else {
        THREADS_PER_HOST_COUNT.put(host, new Integer(addrCount - 1));
      }
    }
  }
  
  private static void cleanExpiredServerBlocks() {
    synchronized (BLOCKED_ADDR_TO_TIME) {
      for (int i = BLOCKED_ADDR_QUEUE.size() - 1; i >= 0; i--) {
        String host = (String) BLOCKED_ADDR_QUEUE.get(i);
        long time = ((Long) BLOCKED_ADDR_TO_TIME.get(host)).longValue();
        if (time <= System.currentTimeMillis()) {
          BLOCKED_ADDR_TO_TIME.remove(host);
          BLOCKED_ADDR_QUEUE.remove(i);
        }
      }
    }
  }
  
  private static String getAgentString(String agentName,
                                       String agentVersion,
                                       String agentDesc,
                                       String agentURL,
                                       String agentEmail) {
    
    if ( (agentName == null) || (agentName.trim().length() == 0) ) {
      // TODO : NUTCH-258
      
        System.out.println("No User-Agent string set (http.agent.name)!");

    }
    
    StringBuffer buf= new StringBuffer();
    
    buf.append(agentName);
    if (agentVersion != null) {
      buf.append("/");
      buf.append(agentVersion);
    }
    if ( ((agentDesc != null) && (agentDesc.length() != 0))
    || ((agentEmail != null) && (agentEmail.length() != 0))
    || ((agentURL != null) && (agentURL.length() != 0)) ) {
      buf.append(" (");
      
      if ((agentDesc != null) && (agentDesc.length() != 0)) {
        buf.append(agentDesc);
        if ( (agentURL != null) || (agentEmail != null) )
          buf.append("; ");
      }
      
      if ((agentURL != null) && (agentURL.length() != 0)) {
        buf.append(agentURL);
        if (agentEmail != null)
          buf.append("; ");
      }
      
      if ((agentEmail != null) && (agentEmail.length() != 0))
        buf.append(agentEmail);
      
      buf.append(")");
    }
    return buf.toString();
  }

  protected void logConf() {

      System.out.println("http.proxy.host = " + proxyHost);
      System.out.println("http.proxy.port = " + proxyPort);
      System.out.println("http.timeout = " + timeout);
      System.out.println("http.content.limit = " + maxContent);
      System.out.println("http.agent = " + userAgent);
      System.out.println("http.accept.language = " + acceptLanguage);
      System.out.println("checkBlocking =" + checkBlocking);
      System.out.println("checkRobots =" + checkRobots);
      if (checkBlocking) {
    	  System.out.println("fetcher.server.delay = " + serverDelay);
    	  System.out.println("http.max.delays = " + maxDelays);
      }

  }
  
  public byte[] processGzipEncoded(byte[] compressed, URL url) throws IOException {

    //System.out.println("uncompressing....");

    byte[] content;
    if (getMaxContent() >= 0) {
        content = GZIPUtils.unzipBestEffort(compressed, getMaxContent());
    } else {
        content = GZIPUtils.unzipBestEffort(compressed);
    } 

    if (content == null)
      throw new IOException("unzipBestEffort returned null");

    
/*    System.out.println("fetched " + compressed.length
                 + " bytes of compressed content (expanded to "
                 + content.length + " bytes) from " + url);*/
    return content;
  }

  public byte[] processDeflateEncoded(byte[] compressed, URL url) throws IOException {

    System.out.println("inflating....");

    byte[] content = DeflateUtils.inflateBestEffort(compressed, getMaxContent());

    if (content == null)
      throw new IOException("inflateBestEffort returned null");

   
      System.out.println("fetched " + compressed.length
                 + " bytes of compressed content (expanded to "
                 + content.length + " bytes) from " + url);
    return content;
  }
  
  
  protected abstract Response getResponse(String url,boolean followRedirects)throws Exception;

  public RobotRules getRobotRules(String url) {
    return robots.getRobotRulesSet(this, url);
  }
 
}
