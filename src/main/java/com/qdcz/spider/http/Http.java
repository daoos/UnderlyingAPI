/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qdcz.spider.http;

import com.qdcz.spider.utils.DownloadFunction;

public class Http extends HttpBase {

  public Http() {
    super();
  }

  public void setConf() {
    super.setConf();
  }

  public static void main(String[] args) throws Exception {
	String url = "http://www.baidu.com/link?url=9HHvnXJFVfmTKh509HopOJWWw3-eD2XdnIjOrlPAya6efCm-JWQYMl9u13Is8lrgv_Nt6KlWqSf0Mysi2wPgoq&wd=&eqid=da3fb18b0000135b0000000457137d80";
    Http http = new Http();
    http.setConf();
    http.logConf();
    MedicalContent mcontent = new MedicalContent();
    //http.getHttpPages(url,mcontent);
    DownloadFunction download = new DownloadFunction();
    download.download(url+" {}", mcontent);
    System.out.println("code = "+ mcontent.code);
 //   System.out.println("content = "+ new String(mcontent.content,"gb2312"));
    System.out.println("statuscode = "+ mcontent.protocolStatusCode);
    System.out.println("localtion = "+ mcontent.location);
  }
  

  public Response getResponse(String url, boolean redirect)
    throws Exception {
	  HttpResponse httpresponse = new HttpResponse(this, url);
    return httpresponse;
  }

}
