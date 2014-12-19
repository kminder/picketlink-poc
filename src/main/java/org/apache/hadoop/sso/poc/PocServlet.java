/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.sso.poc;

import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

public class PocServlet extends HttpServlet {

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    System.out.println( "servlet" );
    resp.setStatus( 200 );
    resp.setContentType( "text/html" );
    org.picketlink.identity.federation.core.SerializablePrincipal principal
        = (org.picketlink.identity.federation.core.SerializablePrincipal)req.getUserPrincipal();
    Object assertion = req.getSession().getAttribute( "org.picketlink.sp.assertion" );
    try {
      assertion = domToStr( (Document)assertion );
    } catch( TransformerException e ) {
      throw new ServletException( e );
    }
    System.out.println( assertion );
    resp.getWriter().write( "<html>" + getInitParameter( "message" ) + ", principal=" + principal.getName() + "</html>" );
  }

  public static String domToStr( Document doc ) throws TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
    StringWriter writer = new StringWriter();
    transformer.transform( new DOMSource( doc ), new StreamResult( writer ) );
    String str = writer.getBuffer().toString().replaceAll( "\n|\r", "" );
    return str;
  }

}
