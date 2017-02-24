package com.qdcz.spider.utils;

import com.qdcz.spider.http.*;

public class DownloadFunction {

	public Http http = new Http();
	public Https https = new Https();

	public DownloadFunction() {
		http.setConf();
	}
	
	public void setTimeOut(int TimeOut){
		http.setTimeout(TimeOut);
	}
	public Response gethttpResponse(String url){
		Response respone = null;
		try {
			respone =   http.getResponse(url,false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respone;
	}
	 

	public void download(String url, MedicalContent mcontent) {
		if (url.startsWith("http://"))
			down_http(url, mcontent);
		else if(url.startsWith("https://")){
			 //https.set_proxy(true, "119.188.94.145", 80);
			 
	    	 try {
	    		 https.download(url,mcontent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public String down_http_get_redirect(String url, MedicalContent mcontent) throws Exception {
		int maxRedirect = 2;
		int maxRetry = 2;

		
			if (url == null) {
				return "";
			}

				boolean redirecting;
				boolean retrying;
				int redirectCount = 0;
				int retryCount = 0;
				do {

					redirecting = true;
					retrying = true;

					http.getHttpPages(url, mcontent);
					switch (mcontent.protocolStatusCode) {

					case ProtocolStatus.SUCCESS: {
						return "";
					}
					case ProtocolStatus.MOVED: // redirect
					case ProtocolStatus.TEMP_MOVED:
						int code;
						String newUrl = mcontent.location;
						if (newUrl != null && !newUrl.equalsIgnoreCase("")
								&& !newUrl.equals(url.toString())) {
							url = newUrl;
							if (maxRedirect > 0) {
								redirecting = true;
								redirectCount++;
//								System.out.println(" - protocol redirect to "
//										+ url + " (fetching now)");

							} else {
//								System.out
//										.println(" - protocol redirect to "
//												+ url
//												+ " (fetching later) because maxRedirect <= 0");
							}
							
							return url;
						} else
							System.out.println(" - protocol redirect skipped: "
									+ (newUrl != null ? "to same url"
											: "filtered"));
						newUrl = null;
						break;

					// failures - increase the retry counter
					case ProtocolStatus.ERROR:
					case ProtocolStatus.EXCEPTION:
						/* FALLTHROUGH */
					case ProtocolStatus.RETRY: // retry
						/* FALLTHROUGH */
						// intermittent blocking - retry without increasing
						// the counter
					case ProtocolStatus.WOULDBLOCK:
					case ProtocolStatus.BLOCKED:
					case ProtocolStatus.UNKNOWN:
						retrying = true;
						retryCount++;
						// System.out.println("retryCount "+retryCount);
						break;

					// permanent failures
					case ProtocolStatus.GONE: // gone
					case ProtocolStatus.NOTFOUND:
					case ProtocolStatus.ACCESS_DENIED:
					case ProtocolStatus.ROBOTS_DENIED:
					case ProtocolStatus.NOTMODIFIED:
						return "";

					}

				} while ((redirecting && (redirectCount < maxRedirect))
						|| (retrying && (retryCount < maxRetry)));
		
		return "";
	}
	

	private void down_http(String url, MedicalContent mcontent) {
		
		int maxRedirect = 2;
		int maxRetry = 1;
		int sleepTime = 300;

		try {
			if (url == null) {
				return;
			}
			try {
				// System.out.println("downOnePage " + url);

				// download the page
				boolean redirecting;
				boolean retrying;
				int redirectCount = 0;
				int retryCount = 0;
				do {

					// System.out.println("redirectCount= " + redirectCount);
					// System.out.println("retryingCount= "+retryCount);
					redirecting = false;
					retrying = false;
					http.getHttpPages(url, mcontent);
					switch (mcontent.protocolStatusCode) {

					case ProtocolStatus.SUCCESS: {
						return;
					}
					case ProtocolStatus.MOVED: // redirect
					case ProtocolStatus.TEMP_MOVED:
						int code;
						String newUrl = mcontent.location;
						if (newUrl != null && !newUrl.equalsIgnoreCase("")
								&& !newUrl.equals(url.toString())) {
							url = newUrl;
							if (maxRedirect > 0) {
								redirecting = true;
								redirectCount++;
								System.out.println(" - protocol redirect to "
										+ url + " (fetching now)");

							} else {
								System.out
										.println(" - protocol redirect to "
												+ url
												+ " (fetching later) because maxRedirect <= 0");
							}
						} else
							System.out.println(" - protocol redirect skipped: "
									+ (newUrl != null ? "to same url"
											: "filtered"));
						newUrl = null;
						break;

					// failures - increase the retry counter
					case ProtocolStatus.ERROR:
					case ProtocolStatus.EXCEPTION:
						/* FALLTHROUGH */
					case ProtocolStatus.RETRY: // retry
						/* FALLTHROUGH */
						// intermittent blocking - retry without increasing
						// the counter
					case ProtocolStatus.WOULDBLOCK:
					case ProtocolStatus.BLOCKED:
					case ProtocolStatus.UNKNOWN:
						retrying = true;
						retryCount++;
						// System.out.println("retryCount "+retryCount);
						break;

					// permanent failures
					case ProtocolStatus.GONE: // gone
					case ProtocolStatus.NOTFOUND:
					case ProtocolStatus.ACCESS_DENIED:
					case ProtocolStatus.ROBOTS_DENIED:
					case ProtocolStatus.NOTMODIFIED:
						return;

					default:
						System.out.println("Unknown ProtocolStatus: "
								+ mcontent.protocolStatusCode);
						retrying = true;
						retryCount++;
						System.out.println("retryCount " + retryCount);
					}

					if (redirecting && redirectCount >= maxRedirect)
						System.out.println(" - redirect count exceeded " + url);

					if (retryCount >= maxRetry)
						System.out.println("retry count exceeded " + url);

					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} while ((redirecting && (redirectCount < maxRedirect))
						|| (retrying && (retryCount < maxRetry)));

			} catch (Throwable t) { // unexpected exception
				System.out.println(t.getMessage());
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}
