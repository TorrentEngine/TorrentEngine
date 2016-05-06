/*
 * Created on 1 Nov 2006
 * Created by Paul Gardner
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


package controller.networkmanager.admin;


import java.net.InetAddress;

import torrentlib.util.platform.win32.PluginInterface;

import controller.networkmanager.admin.NetworkAdminException;
import controller.networkmanager.admin.NetworkAdminNetworkInterfaceAddress;
import controller.networkmanager.admin.NetworkAdminProgressListener;
import controller.networkmanager.admin.NetworkAdminProtocol;
import plugins.upnp.UPnPMapping;
import plugins.upnp.UPnPPlugin;
import torrentlib.TorrentEngineCore;

public class 
NetworkAdminProtocolImpl 
	implements NetworkAdminProtocol
{
	private TorrentEngineCore		core;
	private int				type;
	private int				port;
	
	protected 
	NetworkAdminProtocolImpl(
		TorrentEngineCore	_core,
		int			_type )
	{
		core		= _core;
		type		= _type;
		port		= -1;
	}
	
	protected 
	NetworkAdminProtocolImpl(
		TorrentEngineCore	_core,
		int			_type,
		int			_port )
	{
		core		= _core;
		type		= _type;
		port		= _port;
	}
	
	public int
	getType()
	{
		return( type );
	}
	
	public int 
	getPort()
	{
		return( port );
	}
	
	public InetAddress
	test(
		NetworkAdminNetworkInterfaceAddress	address )
	
		throws NetworkAdminException
	{
		return( test( address, null ));
	}
	
	public InetAddress
	test(
		NetworkAdminNetworkInterfaceAddress		address,
		boolean									upnp_map,
		NetworkAdminProgressListener			listener )
	
		throws NetworkAdminException
	{
		InetAddress bind_ip = address==null?null:address.getAddress();
		
		NetworkAdminProtocolTester	tester;
		
		if ( type == PT_HTTP ){
			
			tester = new NetworkAdminHTTPTester( core, listener );
			
		}else if ( type == PT_TCP ){
			
			tester = new NetworkAdminTCPTester( core, listener );

		}else{
			
			tester = new NetworkAdminUDPTester( core, listener );
		}
		
		InetAddress	res;
		
		if ( port <= 0 ){
			
			res = tester.testOutbound( bind_ip, 0 );
			
		}else{

			UPnPMapping new_mapping = null;

			if ( upnp_map ){
				
				PluginInterface pi_upnp = core.getPluginManager().getPluginInterfaceByClass( UPnPPlugin.class );

				if( pi_upnp != null ) {

					UPnPPlugin upnp = (UPnPPlugin)pi_upnp.getPlugin();

					UPnPMapping mapping = upnp.getMapping( type != PT_UDP , port );

					if ( mapping == null ) {

						new_mapping = mapping = upnp.addMapping( "NAT Tester", type != PT_UDP, port, true );

							// give UPnP a chance to work

						try{
							Thread.sleep( 500 );

						}catch( Throwable e ){

						}
					}
				}
			}
			
			try{
				res = tester.testInbound( bind_ip, port );
				
			}finally{
				
				if ( new_mapping != null ){
					
					new_mapping.destroy();
				}
			}
		}
		
		return( res );
	}
	
	public InetAddress
	test(
		NetworkAdminNetworkInterfaceAddress	address,
		NetworkAdminProgressListener		listener )
	
		throws NetworkAdminException
	{
		return( test( address, false, listener ));
	}
	
	public String
	getTypeString()
	{
		String	res;
		
		if ( type == PT_HTTP ){
			
			res = "HTTP";
			
		}else if ( type == PT_TCP ){
			
			res = "TCP";

		}else{
			
			res = "UDP";
		}
		
		return( res );
	}
	
	public String
	getName()
	{
		String	res = getTypeString();
		
		if ( port == -1 ){
			
			return( res + " outbound" );
			
		}else{
			
			return( res + " port " + port + " inbound" );
		}
	}
}
