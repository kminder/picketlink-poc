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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.picketlink.identity.federation.web.filters.SPFilter;
import org.picketlink.identity.federation.web.filters.ServiceProviderContextInitializer;

import java.awt.*;
import java.io.File;
import java.net.URI;

public class PocServer {

  public static void main( String[] args ) throws Exception {

    Server jetty = new Server( 8888 );
    HandlerCollection handlerCollection = new HandlerCollection ();
    ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();

    handlerCollection.setHandlers( new Handler[]{ contextHandlerCollection } );
    jetty.setHandler( handlerCollection );

    jetty.setStopAtShutdown( true );
    jetty.start();

    System.out.println( "Deploying war at http://localhost:8888/ui" );
    File file1 = createWar( "ui" );
    WebAppContext war = new WebAppContext();
    war.setDefaultsDescriptor( null );
    war.setContextPath( "/ui" );
    war.setWar( file1.getAbsolutePath() );
    contextHandlerCollection.addHandler( war );
    war.start();

    Desktop.getDesktop().browse( new URI( "http://localhost:8888/ui" ) );

    System.out.println( "Press Control-C to exit." );
    System.in.read();
  }

  public static File createWar( String name ) {
    final WebArchive war = ShrinkWrap.create( WebArchive.class, name );

    WebAppDescriptor webXmlDesc = Descriptors.create( WebAppDescriptor.class )
        .createListener().listenerClass( ServiceProviderContextInitializer.class.getName() ).up()
        .createFilter().filterName( "picketlink" ).filterClass( SPFilter.class.getName() ).up()
        .createFilterMapping().filterName( "picketlink" ).urlPattern( "/*" ).up()
        .createServlet().servletName( "test" ).servletClass( PocServlet.class.getName() )
        .createInitParam()
        .paramName( "message" )
        .paramValue( name )
        .up().up()
        .createServletMapping()
        .servletName( "test" )
        .urlPattern( "/*" )
        .up();

    Asset webXmlAsset = new StringAsset( webXmlDesc.exportAsString() );
    war.setWebXML( webXmlAsset );
    war.addAsWebInfResource( ClassLoader.getSystemResource( "picketlink.xml" ), "picketlink.xml" );

    File dir = new File( System.getProperty( "user.dir" ), "target" );
    File file = war.as( ExplodedExporter.class ).exportExploded( dir, name );
    System.out.println( "Created WAR in dir " + file.getAbsolutePath() );

    return file;
  }
}
