/*
 * Created on Feb 24, 2005
 * Created by Alon Rohter
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package pluginsimpl.local.messaging;

import plugins.messaging.Message;
import plugins.messaging.MessageManagerListener;
import plugins.messaging.MessageManager;
import plugins.messaging.MessageException;
import plugins.peers.*;

import plugins.download.Download;
import plugins.download.DownloadManagerListener;
import plugins.download.DownloadPeerListener;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;

import torrentlib.Debug;
import stdlib.security.types.SHA1Simple;
import torrentlib.util.platform.win32.PluginInterface;
import plugins.messaging.generic.GenericMessageConnection;
import plugins.messaging.generic.GenericMessageEndpoint;
import plugins.messaging.generic.GenericMessageHandler;
import plugins.messaging.generic.GenericMessageRegistration;

import torrentlib.nat.NATTraversalHandler;
import torrentlib.nat.NATTraverser;
import controller.networkmanager.NetworkManager;
import controller.networkmanager.TransportHelper;
import connect.peer.messaging.MessageStreamDecoder;
import connect.peer.messaging.MessageStreamEncoder;
import connect.peer.messaging.MessageStreamFactory;
import controller.networkmanager.NetworkConnection;
import torrentlib.TorrentEngineCore;



/**
 *
 */
public class MessageManagerImpl implements MessageManager, NATTraversalHandler {
  
  private static MessageManagerImpl instance;
  
  private final HashMap compat_checks = new HashMap();
  
  private final DownloadManagerListener download_manager_listener = new DownloadManagerListener() {
    public void downloadAdded( Download dwnld ) {
      dwnld.addPeerListener( new DownloadPeerListener() {
        public void peerManagerAdded( final Download download, PeerManager peer_manager ) {
          peer_manager.addListener( new PeerManagerListener() {
            public void peerAdded( PeerManager manager, final Peer peer ) {
              peer.addListener( new PeerListener() {
                public void stateChanged( int new_state ) {
                  
                  if( new_state == Peer.TRANSFERING ) {  //the peer handshake has completed
                    if( peer.supportsMessaging() ) {  //if it supports advanced messaging
                      //see if it supports any registered message types
                      Message[] messages = peer.getSupportedMessages();

                      for( int i=0; i < messages.length; i++ ) {
                        Message msg = messages[i];
                        
                        for( Iterator it = compat_checks.entrySet().iterator(); it.hasNext(); ) {
                          Map.Entry entry = (Map.Entry)it.next();
                          Message message = (Message)entry.getKey();
                          
                          if( msg.getID().equals( message.getID() ) ) {  //it does !
                            MessageManagerListener listener = (MessageManagerListener)entry.getValue();
                            
                            listener.compatiblePeerFound( download, peer, message );
                          }
                        }
                      }
                    }
                  }
                }
                
                public void sentBadChunk( int piece_num, int total_bad_chunks ) { /*nothing*/ }
              });
            }

            public void peerRemoved( PeerManager manager, Peer peer ) {
              for( Iterator i = compat_checks.values().iterator(); i.hasNext(); ) {
                MessageManagerListener listener = (MessageManagerListener)i.next();
                
                listener.peerRemoved( download, peer );
              }
            }
          });
        }

        public void peerManagerRemoved( Download download, PeerManager peer_manager ) { /* nothing */ }
      });
    }
    
    public void downloadRemoved( Download download ) { /* nothing */ }
  };
  
  
  
  
  
  
  public static synchronized MessageManagerImpl 
  getSingleton(TorrentEngineCore core) 
  {
	  if ( instance == null ){
		  
		  instance = new MessageManagerImpl( core );
	  }
		  
	  return instance;
  }
  
  private TorrentEngineCore	core;
  
  private Map			message_handlers = new HashMap();
  
  private MessageManagerImpl(TorrentEngineCore _core ) {
  
	  core	= _core;
	  
	  core.getNATTraverser().registerHandler( this );
  }
  
  public NATTraverser
  getNATTraverser()
  {
	  return( core.getNATTraverser());
  }
  
  @Override
  public void registerMessageType( Message message ) throws MessageException {
    try {
            connect.peer.messaging.MessageManager.getSingleton().registerMessageType( new MessageAdapter( message ) );
    }
    catch(  connect.peer.messaging.MessageException me ) {
      throw new MessageException( me.getMessage() );
    }
  }

  public void deregisterMessageType( Message message ) {
         connect.peer.messaging.MessageManager.getSingleton().deregisterMessageType( new MessageAdapter( message ) );
  }
    
  
  
  public void locateCompatiblePeers( PluginInterface plug_interface, Message message, MessageManagerListener listener ) {
    compat_checks.put( message, listener );  //TODO need to copy-on-write?
    
    if( compat_checks.size() == 1 ) {  //only register global peer locator listener once
      plug_interface.getDownloadManager().addListener( download_manager_listener );
    }
  }
  
  
  public void cancelCompatiblePeersLocation( MessageManagerListener orig_listener ) {
    for( Iterator it = compat_checks.values().iterator(); it.hasNext(); ) {
      MessageManagerListener listener = (MessageManagerListener)it.next();
      
      if( listener == orig_listener ) {
        it.remove();
        break;
      }
    }
  }
  
  public GenericMessageRegistration
  registerGenericMessageType(
	 final String					_type,
	 final String					description,
	 final int						stream_crypto,
	 final GenericMessageHandler	handler )
  
  	throws MessageException
  {	  
	final String	type 		= "AEGEN:" + _type;
	final byte[]	type_bytes 	= type.getBytes();
	
	final byte[][]	shared_secrets = new byte[][]{ new SHA1Simple().calculateHash( type_bytes ) };
		
	synchronized( message_handlers ){
		
		message_handlers.put( type, handler );
	}
	
	final NetworkManager.ByteMatcher matcher = 
			new NetworkManager.ByteMatcher()
			{
				public int
				matchThisSizeOrBigger()
				{
					return( maxSize());
				}
				
				public int 
				maxSize() 
				{  
					return type_bytes.length;  
				}
				
				public int 
				minSize()
				{ 
					return maxSize(); 
				}
	
				public Object 
				matches( 
					TransportHelper transport, ByteBuffer to_compare, int port ) 
				{             
					int old_limit = to_compare.limit();
					
					to_compare.limit( to_compare.position() + maxSize() );
					
					boolean matches = to_compare.equals( ByteBuffer.wrap( type_bytes ) );
					
					to_compare.limit( old_limit );  //restore buffer structure
					
					return matches?"":null;
				}
				
				public Object 
				minMatches( 
					TransportHelper transport, ByteBuffer to_compare, int port ) 
				{ 
					return( matches( transport, to_compare, port )); 
				} 
				
				public byte[][] 
				getSharedSecrets()
				{ 
					return( shared_secrets ); 
				}
				
			   	public int 
				getSpecificPort()
				{
					return( -1 );
				}
			};
			
	NetworkManager.getSingleton().requestIncomingConnectionRouting(matcher,
				new NetworkManager.RoutingListener() 
				{
					public void 
					connectionRouted( 
						final NetworkConnection connection, Object routing_data ) 
					{  	
						try{
							ByteBuffer[]	skip_buffer = { ByteBuffer.allocate(type_bytes.length) };
							
							connection.getTransport().read( skip_buffer, 0, 1 );

							if ( skip_buffer[0].remaining() != 0 ){
								
								Debug.out( "incomplete read" );
							}
							
							GenericMessageEndpointImpl endpoint		= new GenericMessageEndpointImpl( connection.getEndpoint());

							GenericMessageConnectionDirect direct_connection = 
								GenericMessageConnectionDirect.receive( 
										endpoint, 
										type, 
										description,
										stream_crypto,
										shared_secrets );
								
							GenericMessageConnectionImpl new_connection = new GenericMessageConnectionImpl( MessageManagerImpl.this, direct_connection );

							direct_connection.connect( connection );
							
							if ( handler.accept( new_connection )){
								
								new_connection.accepted();

							}else{
								
								connection.close( "connection not accepted" );
							}	
							
						}catch( Throwable e ){
							
							Debug.printStackTrace(e);
							
							connection.close( e==null?null:Debug.getNestedExceptionMessage(e));
						}
					}
					
					public boolean
					autoCryptoFallback()
					{
						return( stream_crypto != MessageManager.STREAM_ENCRYPTION_RC4_REQUIRED );
					}
				},
				new MessageStreamFactory() {
					public MessageStreamEncoder createEncoder() {  return new GenericMessageEncoder();}
					public MessageStreamDecoder createDecoder() {  return new GenericMessageDecoder(type, description);}
				});
		
	return( 
		new GenericMessageRegistration()
		{
			public GenericMessageEndpoint
			createEndpoint(
				InetSocketAddress	notional_target )
			{
				return( new GenericMessageEndpointImpl( notional_target ));
			}
			
			public GenericMessageConnection
			createConnection(
				GenericMessageEndpoint	endpoint )
			
				throws MessageException
			{
				return( new GenericMessageConnectionImpl( MessageManagerImpl.this, type, description, (GenericMessageEndpointImpl)endpoint, stream_crypto, shared_secrets ));
			}
			
			public void
			cancel()
			{
				NetworkManager.getSingleton().cancelIncomingConnectionRouting( matcher );
				
				synchronized( message_handlers ){
					
					message_handlers.remove( type );
				}
			}
		});
  }
  
  
  protected GenericMessageHandler
  getHandler(
		 String	type )
  {
		synchronized( message_handlers ){

			return((GenericMessageHandler)message_handlers.get( type ));
		}
  }
  
  	/* NATTraversalHandler methods
  	 */
  
	public int
	getType()
	{
		return( NATTraverser.TRAVERSE_REASON_GENERIC_MESSAGING );
	}
	
	public String
	getName()
	{
		return( "Generic Messaging" );
	}
	
	public Map
	process(
		InetSocketAddress	originator,
		Map					message )
	{
		return( GenericMessageConnectionIndirect.receive( this, originator, message ));
	}
}
