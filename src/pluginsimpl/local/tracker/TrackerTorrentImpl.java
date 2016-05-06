/*
 * File    : TrackerTorrentImpl.java
 * Created : 08-Dec-2003
 * By      : parg
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package pluginsimpl.local.tracker;

/**
 * @author parg
 *
 */

import plugins.torrent.Torrent;
import plugins.tracker.TrackerTorrent;
import plugins.tracker.TrackerTorrentWillBeRemovedListener;
import plugins.tracker.TrackerTorrentListener;
import plugins.tracker.TrackerTorrentRemovalVetoException;
import plugins.tracker.TrackerException;
import plugins.tracker.TrackerPeer;
import connect.tracker.host.TRHostTorrentRequest;
import connect.tracker.host.TRHostTorrentWillBeRemovedListener;
import connect.tracker.host.TRHostPeer;
import connect.tracker.host.TRHostTorrentRemovalVetoException;
import connect.tracker.host.TRHostTorrentListener;
import connect.tracker.host.TRHostException;
import connect.tracker.host.TRHostTorrent;
import pluginsimpl.local.torrent.TorrentImpl;
import java.util.*;

import torrentlib.AEMonitor;

public class 
TrackerTorrentImpl
	implements TrackerTorrent, TRHostTorrentListener, TRHostTorrentWillBeRemovedListener
{
	protected TRHostTorrent		host_torrent;

	protected List	listeners_cow		= new ArrayList();
	protected List	removal_listeners	= new ArrayList();
	
	protected AEMonitor this_mon 	= new AEMonitor( "TrackerTorrent" );

	public
	TrackerTorrentImpl(
		TRHostTorrent	_host_torrent )
	{
		host_torrent	= _host_torrent;
	}
	
	// not visible to plugins
	public TRHostTorrent
	getHostTorrent()
	{
		return( host_torrent );
	}
	
	public void
	start()
	
		throws TrackerException
	{
		try{
			host_torrent.start();
			
		}catch( Throwable e ){
			
			throw( new TrackerException("Start failed", e ));
		}
	}
	
	public void
	stop()
	
		throws TrackerException
	{
		try{
			host_torrent.stop();
			
		}catch( Throwable e ){
			
			throw( new TrackerException("Stop failed", e ));
		}
	}
	
	public void
	remove()
	
		throws TrackerTorrentRemovalVetoException
	{
		try{
			host_torrent.remove();
			
		}catch( TRHostTorrentRemovalVetoException e ){
			
			throw( new TrackerTorrentRemovalVetoException(e.getMessage()));
		}
	}
	
	public boolean
	canBeRemoved()
	
		throws TrackerTorrentRemovalVetoException
	{
		try{
			host_torrent.canBeRemoved();
			
		}catch( TRHostTorrentRemovalVetoException e ){
			
			throw( new TrackerTorrentRemovalVetoException(e.getMessage()));
		}
		
		return( true );
	}
	
	public Torrent
	getTorrent()
	{
		return( new TorrentImpl( host_torrent.getTorrent()));
	}
	
	public TrackerPeer[]
	getPeers()
	{
		TRHostPeer[]	peers = host_torrent.getPeers();
		
		TrackerPeer[]	res = new TrackerPeer[peers.length];
		
		for (int i=0;i<peers.length;i++){
			
			res[i] = new TrackerPeerImpl( peers[i]);
		}
		
		return( res );
	}
	
	public int
	getStatus()
	{
		int	status = host_torrent.getStatus();
		
		switch(status){
			case TRHostTorrent.TS_STARTED:
				return( TS_STARTED );
			case TRHostTorrent.TS_STOPPED:
				return( TS_STOPPED );
			case TRHostTorrent.TS_PUBLISHED:
				return( TS_PUBLISHED );
			default:
				throw( new RuntimeException( "TrackerTorrent: status invalid"));
		}
	}
	
	public long
	getTotalUploaded()
	{
		return( host_torrent.getTotalUploaded());
	}
	
	public long
	getTotalDownloaded()
	{
		return( host_torrent.getTotalDownloaded());
	}
	
	public long
	getAverageUploaded()
	{
		return( host_torrent.getAverageUploaded());
	}
	
	public long
	getAverageDownloaded()
	{
		return( host_torrent.getAverageDownloaded());
	}
	
	public long
	getTotalLeft()
	{
		return( host_torrent.getTotalLeft());
	}	
	
	public long
	getCompletedCount()
	{
		return( host_torrent.getCompletedCount());
	}
	
	public long
	getTotalBytesIn()
	{
		return( host_torrent.getTotalBytesIn());
	}	
	
	public long
	getAverageBytesIn()
	{
		return( host_torrent.getAverageBytesIn());
	}
	
	public long
	getTotalBytesOut()
	{
		return( host_torrent.getTotalBytesOut());
	}
	
	public long
	getAverageBytesOut()
	{
		return( host_torrent.getAverageBytesOut());
	}
	
	public long
	getAverageScrapeCount()
	{
		return( host_torrent.getAverageScrapeCount());
	}
	
	public long
	getScrapeCount()
	{
		return( host_torrent.getScrapeCount());
	}
	
	public long
	getAverageAnnounceCount()
	{
		return( host_torrent.getAverageAnnounceCount());
	}

	public long
	getAnnounceCount()
	{
		return( host_torrent.getAnnounceCount());
	}
	
	public int
	getSeedCount()
	{
		return( host_torrent.getSeedCount());
	}	
	
	public int
	getLeecherCount()
	{
		return( host_torrent.getLeecherCount());
	}
	
	public int
	getBadNATCount()
	{
		return( host_torrent.getBadNATCount());
	}
	
	public void
	disableReplyCaching()
	{
		host_torrent.disableReplyCaching();
	}
	
	public boolean
	isPassive()
	{
		return( host_torrent.isPassive());
	}
	
	public long
	getDateAdded()
	{
		return( host_torrent.getDateAdded());
	}
	
	public void
	preProcess(
		TRHostTorrentRequest	request )
	
		throws TRHostException
	{
		List	listeners_ref = listeners_cow;
		
		for (int i=0;i<listeners_ref.size();i++){
			
			try{
				((TrackerTorrentListener)listeners_ref.get(i)).preProcess(new TrackerTorrentRequestImpl(request));
				
			}catch( TrackerException e ){
				
				throw( new TRHostException( e.getMessage(), e ));
				
			}catch( Throwable e ){
			
				throw( new TRHostException( "Pre-process fails", e ));
			}
		}
	}
	
	public void
	postProcess(
		TRHostTorrentRequest	request )
	
		throws TRHostException
	{
		List	listeners_ref = listeners_cow;
		
		for (int i=0;i<listeners_ref.size();i++){
			
			try{
				((TrackerTorrentListener)listeners_ref.get(i)).postProcess(new TrackerTorrentRequestImpl(request));
				
			}catch( TrackerException e ){
				
				throw( new TRHostException( e.getMessage(), e ));
				
			}catch( Throwable e ){
			
				throw( new TRHostException( "Post-process fails", e ));
			}
		}
	}
	
	public void
	addListener(
		TrackerTorrentListener	listener )
	{
		try{
			this_mon.enter();
		
			List	new_listeners = new ArrayList( listeners_cow );
			
			new_listeners.add( listener );
			
			if ( new_listeners.size() == 1 ){
				
				host_torrent.addListener( this );
			}
			
			listeners_cow = new_listeners;
			
		}finally{
			
			this_mon.exit();
		}
	}
	
	public void
	removeListener(
		TrackerTorrentListener	listener )
	{
		try{
			this_mon.enter();
		
			List	new_listeners = new ArrayList( listeners_cow );
			
			new_listeners.remove( listener );
				
			if ( new_listeners.size() == 0 ){
				
				host_torrent.removeListener(this);
			}
			
			listeners_cow = new_listeners;

		}finally{
			
			this_mon.exit();
		}
	}
	
	public void
	torrentWillBeRemoved(
		TRHostTorrent	t )
	
		throws TRHostTorrentRemovalVetoException
	{
		for (int i=0;i<removal_listeners.size();i++){
			
			try{
				((TrackerTorrentWillBeRemovedListener)removal_listeners.get(i)).torrentWillBeRemoved( this );
				
			}catch( TrackerTorrentRemovalVetoException e ){
				
				throw( new TRHostTorrentRemovalVetoException( e.getMessage()));
			}
		}
	}
	
	public void
	addRemovalListener(
		TrackerTorrentWillBeRemovedListener	listener )
	{
		try{
			this_mon.enter();
		
			removal_listeners.add( listener );
			
			if ( removal_listeners.size() == 1 ){
				
				host_torrent.addRemovalListener( this );
			}
		}finally{
			
			this_mon.exit();
		}
	}
	
	public void
	removeRemovalListener(
		TrackerTorrentWillBeRemovedListener	listener )
	{
		try{
			this_mon.enter();
		
			removal_listeners.remove( listener );
			
			if ( removal_listeners.size() == 0 ){
				
				host_torrent.removeRemovalListener(this);
			}	
		}finally{
			
			this_mon.exit();
		}
	}
	
	public boolean
	equals(
		Object	other )
	{
			// as we're lazy and create new instances of this on demand we need to
			// do something sensible about equivalence
		
		if ( other instanceof TrackerTorrentImpl ){
			
			return( host_torrent == ((TrackerTorrentImpl)other).host_torrent );
		}
		
		return( false );
	}
	
	public int
	hashCode()
	{
		return( host_torrent.hashCode());
	}
}