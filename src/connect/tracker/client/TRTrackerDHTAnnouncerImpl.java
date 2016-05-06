/*
 * Created on 14-Feb-2005
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

package connect.tracker.client;

import torrentlib.util.logging.LogEvent;
import torrentlib.util.logging.Logger;
import torrentlib.util.logging.LogIDs;
import java.net.URL;
import java.util.*;


import torrentlib.internat.MessageText;
import torrentlib.data.torrent.TOTorrent;
import torrentlib.data.torrent.TOTorrentAnnounceURLSet;
import torrentlib.data.torrent.TOTorrentException;
import connect.tracker.client.TRTrackerAnnouncer;
import connect.tracker.client.TRTrackerAnnouncerDataProvider;
import connect.tracker.client.TRTrackerAnnouncerException;
import connect.tracker.client.TRTrackerAnnouncerListener;
import connect.tracker.client.TRTrackerAnnouncerResponse;
import connect.tracker.client.TRTrackerAnnouncerResponsePeer;
import connect.tracker.client.TRTrackerAnnouncerHelper;
import connect.tracker.client.TRTrackerAnnouncerImpl;
import connect.tracker.client.TRTrackerAnnouncerResponseImpl;
import connect.tracker.client.TRTrackerAnnouncerResponsePeerImpl;
import torrentlib.Debug;
import torrentlib.HashWrapper;
import torrentlib.IndentWriter;
import torrentlib.SystemTime;
import torrentlib.util.TorrentUtils;
import plugins.clientid.ClientIDException;
import plugins.download.DownloadAnnounceResult;
import plugins.download.DownloadAnnounceResultPeer;
import pluginsimpl.local.clientid.ClientIDManagerImpl;

import connect.tracker.peer.TrackerPeerSource;

/**
 * @author parg
 *
 */

public class 
TRTrackerDHTAnnouncerImpl
	implements TRTrackerAnnouncerHelper
{
	public final static LogIDs LOGID = LogIDs.TRACKER;

	private TOTorrent		torrent;
	private HashWrapper		torrent_hash;
	
	private TRTrackerAnnouncerImpl.Helper		helper;
	
	private byte[]			data_peer_id;
	
	private String						tracker_status_str;
	private long						last_update_time;
	
	private int							state = TS_INITIALISED;
	
	private TRTrackerAnnouncerResponseImpl	last_response;
	
	private boolean			manual;
	
	public
	TRTrackerDHTAnnouncerImpl(
		TOTorrent						_torrent,
		String[]						_networks,
		boolean							_manual,
		TRTrackerAnnouncerImpl.Helper	_helper )
	
		throws TRTrackerAnnouncerException
	{		
		torrent		= _torrent;
		manual		= _manual;
		helper		= _helper;
		
		try{
			torrent_hash	= torrent.getHashWrapper();
			
		}catch( TOTorrentException e ){
			
			Debug.printStackTrace(e);
		}
		try{
			data_peer_id = ClientIDManagerImpl.getSingleton().generatePeerID( torrent_hash.getBytes(), false );
			
		}catch( ClientIDException e ){

			 throw( new TRTrackerAnnouncerException( "TRTrackerAnnouncer: Peer ID generation fails", e ));
		}
		
		last_response = 
			new TRTrackerAnnouncerResponseImpl( 
				torrent.getAnnounceURL(),
				torrent_hash,
				TRTrackerAnnouncerResponse.ST_OFFLINE, 0, "Initialising" );
		
		tracker_status_str = MessageText.getString("PeerManager.status.checking") + "...";
	}
	
	public void
	setAnnounceDataProvider(
		TRTrackerAnnouncerDataProvider		provider )
	{	
	}
	
	public boolean
	isManual()
	{
		return( manual );
	}
	
	public TOTorrent
	getTorrent()
	{
		return( torrent );
	}
	
	public URL
	getTrackerURL()
	{
		return( TorrentUtils.getDecentralisedURL( torrent ));
	}
	
	public void
	setTrackerURL(
		URL		url )
	{
		Debug.out( "Not implemented" );
	}
	
	public TOTorrentAnnounceURLSet[]
	getAnnounceSets()
	{
		return( new TOTorrentAnnounceURLSet[]{
					torrent.getAnnounceURLGroup().createAnnounceURLSet( 
							new URL[]{ TorrentUtils.getDecentralisedURL( torrent )})} );
	}
	
	public void
	resetTrackerUrl(
		boolean	shuffle )
	{
	}
		
	public void
	setIPOverride(
		String		override )
	{
	}
	
	public void
	clearIPOverride()
	{
	}
			
	public int
	getPort()
	{
		return(0);
	}
	
	public byte[]
	getPeerId()
	{
		return( data_peer_id );
	}
	
	public void
	setRefreshDelayOverrides(
		int		percentage )
	{
	}
	
	public int
	getTimeUntilNextUpdate()
	{
		long elapsed = (SystemTime.getCurrentTime() - last_update_time)/1000;
		
		return( (int)(last_response.getTimeToWait()-elapsed));
	}
	
	public int
	getLastUpdateTime()
	{
		return( (int)(last_update_time/1000));
	}
			
	public void
	update(
		boolean	force )
	{
		state = TS_DOWNLOADING;
	}	
	
	public void
	complete(
		boolean	already_reported )
	{
		state	= TS_COMPLETED;
	}
	
	public void
	stop(
		boolean	for_queue )
	{
		state	= TS_STOPPED;
	}
	
	public void
	destroy()
	{
	}
	
	public int
	getStatus()
	{
		return( state );
	}
	
	public String
	getStatusString()
	{
		return( tracker_status_str );
	}
	
	public TRTrackerAnnouncer
	getBestAnnouncer()
	{
		return( this );
	}
	
	public TRTrackerAnnouncerResponse
	getLastResponse()
	{
		return( last_response );
	}
	
	public boolean 
	isUpdating() 
	{
		return( false );
	}
	
	public long
	getInterval()
	{
		return( -1 );
	}
	
	public long
	getMinInterval()
	{
		return( -1 );
	}
	
	public void
	refreshListeners()
	{	
	}
	
	public void
	setAnnounceResult(
		DownloadAnnounceResult	result )
	{
		last_update_time	= SystemTime.getCurrentTime();
		
		TRTrackerAnnouncerResponseImpl response;
		
		if ( result.getResponseType() == DownloadAnnounceResult.RT_ERROR ){
			
			tracker_status_str = MessageText.getString("PeerManager.status.error"); 
		      
			String	reason = result.getError();
	
			if ( reason != null ){
		
				tracker_status_str += " (" + reason + ")";		
			}
			
	  		response = new TRTrackerAnnouncerResponseImpl(
				  				result.getURL(),
				  				torrent_hash,
				  				TRTrackerAnnouncerResponse.ST_OFFLINE, 
								result.getTimeToWait(), 
								reason );
		}else{
			DownloadAnnounceResultPeer[]	ext_peers = result.getPeers();
			
			List<TRTrackerAnnouncerResponsePeerImpl> peers_list = new ArrayList<TRTrackerAnnouncerResponsePeerImpl>( ext_peers.length );
				
			for (int i=0;i<ext_peers.length;i++){
				
				DownloadAnnounceResultPeer	ext_peer	= ext_peers[i];
				
				if ( ext_peer == null){
					
					continue;
				}
				
				if (Logger.isEnabled()){
					Logger.log(new LogEvent(torrent, LOGID, "EXTERNAL PEER DHT: ip="
							+ ext_peer.getAddress() + ",port=" + ext_peer.getPort() +",prot=" + ext_peer.getProtocol()));
				}
				
				int		http_port	= 0;
				byte	az_version 	= TRTrackerAnnouncer.AZ_TRACKER_VERSION_1;
				
				peers_list.add( new TRTrackerAnnouncerResponsePeerImpl( 
									ext_peer.getSource(),
									ext_peer.getPeerID(),
									ext_peer.getAddress(), 
									ext_peer.getPort(),
									ext_peer.getUDPPort(),
									http_port,
									ext_peer.getProtocol(),
									az_version,
									(short)0 ));
			}
			
			TRTrackerAnnouncerResponsePeerImpl[]	peers = peers_list.toArray( new TRTrackerAnnouncerResponsePeerImpl[peers_list.size()] );
			
			helper.addToTrackerCache( peers);
		
			tracker_status_str = MessageText.getString("PeerManager.status.ok");

			response = new TRTrackerAnnouncerResponseImpl( result.getURL(), torrent_hash, TRTrackerAnnouncerResponse.ST_ONLINE, result.getTimeToWait(), peers );
		}
		
		last_response = response;
			
		TRTrackerAnnouncerResponsePeer[] peers = response.getPeers();
		
		if ( peers == null || peers.length < 5 ){
			
		     TRTrackerAnnouncerResponsePeer[]	cached_peers = helper.getPeersFromCache(100);

		     if ( cached_peers.length > 0 ){
		     	
		    	 Set<TRTrackerAnnouncerResponsePeer>	new_peers = 
		    		 new TreeSet<TRTrackerAnnouncerResponsePeer>(
			    		new Comparator<TRTrackerAnnouncerResponsePeer>()
			    		{
			    			public int 
			    			compare(
			    				TRTrackerAnnouncerResponsePeer o1,
			    				TRTrackerAnnouncerResponsePeer o2 ) 
			    			{
			    				return( o1.compareTo( o2 ));
			    			}		    			
			    		});
		    	 
		    	 if ( peers != null ){
		    		 
		    		 new_peers.addAll( Arrays.asList( peers ));
		    	 }
		    	 
	    		 new_peers.addAll( Arrays.asList( cached_peers ));

		    	 response.setPeers( new_peers.toArray( new TRTrackerAnnouncerResponsePeer[new_peers.size()]) );
		     }
		}
		
		helper.informResponse( this, response );
	}
	
	public void 
	addListener(
		TRTrackerAnnouncerListener l )
	{
		helper.addListener( l );
	}
	
	public void 
	removeListener(
		TRTrackerAnnouncerListener l )
	{
		helper.removeListener( l );
	}
	
	public void 
	setTrackerResponseCache(
		Map map	)
	{
		helper.setTrackerResponseCache( map );
	}
	
	public void 
	removeFromTrackerResponseCache(
		String ip, int tcpPort) 
	{
		helper.removeFromTrackerResponseCache( ip, tcpPort );
	}
	
	public Map 
	getTrackerResponseCache() 
	{
		return( helper.getTrackerResponseCache());
	}
	
	public TrackerPeerSource 
	getTrackerPeerSource(
		TOTorrentAnnounceURLSet set) 
	{
		Debug.out( "not implemented" );
		
		return null;
	}
	
	public TrackerPeerSource 
	getCacheTrackerPeerSource()
	{
		Debug.out( "not implemented" );
		
		return null;
	}
	
	public void 
	generateEvidence(
		IndentWriter writer )
	{
		writer.println( "DHT announce: " + (last_response==null?"null":last_response.getString()));
	}
}
