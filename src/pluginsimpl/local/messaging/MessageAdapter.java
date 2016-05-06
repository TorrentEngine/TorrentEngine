/*
 * Created on Feb 10, 2005
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
import plugins.messaging.MessageException;
import java.nio.ByteBuffer;

import torrentlib.DirectByteBuffer;

/**
 *
 */
public class MessageAdapter implements Message, connect.peer.messaging.Message {
  private Message plug_msg = null;
  private connect.peer.messaging.Message core_msg = null;
  
  
  public MessageAdapter( Message plug_msg ) {
    this.plug_msg = plug_msg;
  }
  
  
  public MessageAdapter( connect.peer.messaging.Message core_msg ) {
    this.core_msg = core_msg;
  }
  
  
  public Message getPluginMessage() {  return plug_msg;  }

  public connect.peer.messaging.Message getCoreMessage() {  return core_msg;  }
  
  
  
  //plugin Message implementation
  public ByteBuffer[] getPayload() {
    if( core_msg == null ) {
      return plug_msg.getPayload();
    }
    
    DirectByteBuffer[] dbbs = core_msg.getData();  
    ByteBuffer[] bbs = new ByteBuffer[ dbbs.length ];  //TODO cache it???
    for( int i=0; i < dbbs.length; i++ ) {
      bbs[i] = dbbs[i].getBuffer( DirectByteBuffer.SS_MSG );
    }
    return bbs;
  }
  
  public Message create( ByteBuffer data ) throws MessageException  {
    if( core_msg == null ) {
      return plug_msg.create( data );
    }
    
    try{
      return new MessageAdapter( core_msg.deserialize( new DirectByteBuffer( data ), (byte)1 ) );
    }
    catch( connect.peer.messaging.MessageException e ) {
      throw new MessageException( e.getMessage() );
    }
  }
  

  
  //shared Message implementation
  public String getID() {
    return core_msg == null ? plug_msg.getID() : core_msg.getID();
  }
  
  public byte[] getIDBytes() {
	    return core_msg == null ? plug_msg.getID().getBytes() : core_msg.getIDBytes();
	  }
  
  public int getType() {
    return core_msg == null ? plug_msg.getType() : core_msg.getType();
  }
  
  public byte getVersion() {
	    return core_msg == null ? (byte)1 : core_msg.getVersion();
  }
	  
  
  public String getDescription() {
    return core_msg == null ? plug_msg.getDescription() : core_msg.getDescription();
  }
  
  public void destroy() {
    if( core_msg == null ) plug_msg.destroy();
    else core_msg.destroy();
  }
  
  
  
  //core Message implementation
  
  public String getFeatureID() {  return "AZPLUGMSG";  }
  
  public int getFeatureSubID() {  return -1;  }  
  
  
  public DirectByteBuffer[] getData() {
    if( plug_msg == null ) {
      return core_msg.getData();
    }
    
    ByteBuffer[] bbs = plug_msg.getPayload();
    DirectByteBuffer[] dbbs = new DirectByteBuffer[ bbs.length ];  //TODO cache it???
    for( int i=0; i < bbs.length; i++ ) {
      dbbs[i] = new DirectByteBuffer( bbs[i] );
    }
    return dbbs;
  }
  
  public connect.peer.messaging.Message deserialize( DirectByteBuffer data, byte version ) throws connect.peer.messaging.MessageException {
    if( plug_msg == null ) {
      return core_msg.deserialize( data, version );
    }
    
    try{
    	Message message = plug_msg.create( data.getBuffer( DirectByteBuffer.SS_MSG ) );
    	
    	if ( message == null ){
    		
    		throw( new connect.peer.messaging.MessageException( "Plugin message deserialisation failed" ));
    	}
    	
    	return new MessageAdapter( message );
    }
    catch( MessageException e ) {
      throw new connect.peer.messaging.MessageException( e.getMessage() );
    }
    finally {
      data.returnToPool();
    }
  }
  
}
