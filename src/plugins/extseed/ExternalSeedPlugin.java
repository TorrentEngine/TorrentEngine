/*
 * Created on 15-Dec-2005
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

package plugins.extseed;

import plugins.utils.DelayedTask;
import plugins.utils.Utilities;
import plugins.utils.UTTimerEventPerformer;
import plugins.utils.UTTimerEvent;
import plugins.utils.UTTimer;
import plugins.utils.Monitor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import torrentlib.data.torrent.TOTorrent;
import torrentlib.AEThread2;
import torrentlib.SystemTime;
import torrentlib.util.platform.win32.Plugin;
import torrentlib.util.platform.win32.PluginInterface;
import plugins.download.Download;
import plugins.download.DownloadManagerListener;
import plugins.download.DownloadManagerStats;
import plugins.download.DownloadPeerListener;
import plugins.logging.LoggerChannel;
import plugins.logging.LoggerChannelListener;
import plugins.peers.PeerManager;
import plugins.torrent.Torrent;
import plugins.torrent.TorrentAttribute;

import connect.tracker.peer.TrackerPeerSource;
import connect.tracker.peer.TrackerPeerSourceAdapter;
import plugins.extseed.impl.getright.ExternalSeedReaderFactoryGetRight;
import plugins.extseed.impl.webseed.ExternalSeedReaderFactoryWebSeed;

public class
ExternalSeedPlugin
	implements Plugin, DownloadManagerListener
{
	private static ExternalSeedReaderFactory[]	factories = {
		new ExternalSeedReaderFactoryGetRight(),
		new ExternalSeedReaderFactoryWebSeed(),
	};

	private PluginInterface			plugin_interface;
	private DownloadManagerStats	dm_stats;

	private LoggerChannel			log;

	private 		Random	random = new Random();

	private Map		download_map	= new HashMap();
	private Monitor	download_mon;

	public static void
	load(
		PluginInterface		plugin_interface )
	{
		plugin_interface.getPluginProperties().setProperty( "plugin.version", 	"1.0" );
		plugin_interface.getPluginProperties().setProperty( "plugin.name", 		"External Seed" );
	}

	public void
	initialize(
		PluginInterface	_plugin_interface )
	{
		plugin_interface	= _plugin_interface;
		dm_stats = plugin_interface.getDownloadManager().getStats();
		log	= plugin_interface.getLogger().getTimeStampedChannel( "External Seeds" );
		download_mon	= plugin_interface.getUtilities().getMonitor();
		Utilities utilities = plugin_interface.getUtilities();

		final DelayedTask dt = plugin_interface.getUtilities().createDelayedTask(new Runnable()
			{
				public void
				run()
				{
					AEThread2 t =
						new AEThread2( "ExternalSeedInitialise", true )
						{
							public void
							run()
							{
								plugin_interface.getDownloadManager().addListener( ExternalSeedPlugin.this);
							}
						};

					t.setPriority( Thread.MIN_PRIORITY );

					t.start();

				}
			});

		dt.queue();

		UTTimer timer = utilities.createTimer("ExternalPeerScheduler", true);

		timer.addPeriodicEvent(
				5000,
				new UTTimerEventPerformer()
				{
					public void
					perform(
						UTTimerEvent		event )
					{
						try{
							Iterator	it = download_map.values().iterator();

							while( it.hasNext()){

								List	peers = randomiseList((List)it.next());

								for (int i=0;i<peers.size();i++){

										// bail out early if the state changed for this peer
										// so one peer at a time gets a chance to activate

									if (((ExternalSeedPeer)peers.get(i)).checkConnection()){

										break;
									}
								}
							}

						}catch( Throwable e ){
							// we do this without holding the monitor as doing so causes potential
							// deadlock between download_mon and the connection's connection_mon

							// so ignore possible errors here that may be caused by concurrent
							// modification to the download_map ans associated lists. We are only
							// reading the data so errors will only be transient
						}
					}
				});

	}

	public void
	downloadAdded(
		Download	download )
	{
		Torrent	torrent = download.getTorrent();

		if ( torrent == null ){

			return;
		}

		List	peers = new ArrayList();

		for (int i=0;i<factories.length;i++){


			String attributeID = "no-ext-seeds-" + factories[i].getClass().getSimpleName();
			TorrentAttribute attribute = plugin_interface.getTorrentManager().getPluginAttribute( attributeID );

			boolean noExternalSeeds = download.getBooleanAttribute(attribute);
			if (noExternalSeeds) {
				continue;
			}

			ExternalSeedReader[]	x = factories[i].getSeedReaders( this, download );

			if (x.length == 0) {
				download.setBooleanAttribute(attribute, true);
			} else {

  			for (int j=0;j<x.length;j++){

  				ExternalSeedReader	reader = x[j];

  				ExternalSeedPeer	peer = new ExternalSeedPeer( this, download, reader );

  				peers.add( peer );
  			}
			}
		}

		addPeers( download, peers );
	}

	public void
	downloadChanged(
		Download	download )
	{
		downloadRemoved( download );

		downloadAdded( download );
	}

	public List<ExternalSeedPeer>
	addSeed(
		Download	download,
		Map			config )
	{
		Torrent	torrent = download.getTorrent();

		List<ExternalSeedPeer>	peers = new ArrayList<ExternalSeedPeer>();

		if ( torrent != null ){

			for (int i=0;i<factories.length;i++){

				String attributeID = "no-ext-seeds-" + factories[i].getClass().getSimpleName();
				TorrentAttribute attribute = plugin_interface.getTorrentManager().getPluginAttribute( attributeID );

				ExternalSeedReader[]	x = factories[i].getSeedReaders( this, download, config );

				download.setBooleanAttribute(attribute, x.length == 0);

				for (int j=0;j<x.length;j++){

					ExternalSeedReader	reader = x[j];

					ExternalSeedPeer	peer = new ExternalSeedPeer( this, download, reader );

					peers.add( peer );
				}
			}

			addPeers( download, peers );
		}

		return( peers );
	}

	protected void
	addPeers(
		final Download	download,
		List			_peers )
	{
		final List peers = new ArrayList();

		peers.addAll( _peers );

		if ( peers.size() > 0 ){

			boolean	add_listener = false;

			try{
				download_mon.enter();

				List	existing_peers = (List)download_map.get( download );

				if ( existing_peers == null ){

					add_listener	= true;

					existing_peers = new ArrayList();

					download_map.put( download, existing_peers );
				}

				Iterator	it = peers.iterator();

				while( it.hasNext()){

					ExternalSeedPeer	peer = (ExternalSeedPeer)it.next();

					boolean	skip = false;

					for (int j=0;j<existing_peers.size();j++){

						ExternalSeedPeer	existing_peer = (ExternalSeedPeer)existing_peers.get(j);

						if ( existing_peer.sameAs( peer )){

							skip	= true;

							break;
						}
					}

					if ( skip ){

						it.remove();

					}else{

						log( download.getName() + " found seed " + peer.getName());


						existing_peers.add( peer );
					}
				}

			}finally{

				download_mon.exit();
			}

			if ( add_listener ){

				download.addPeerListener(
					new DownloadPeerListener()
					{
						public void
						peerManagerAdded(
							Download		download,
							PeerManager		peer_manager )
						{
							List	existing_peers = getPeers();

							if ( existing_peers== null ){

								return;
							}

							for (int i=0;i<existing_peers.size();i++){

								ExternalSeedPeer	peer = (ExternalSeedPeer)existing_peers.get(i);

								peer.setManager( peer_manager );
							}
						}

						public void
						peerManagerRemoved(
							Download		download,
							PeerManager		peer_manager )
						{
							List	existing_peers = getPeers();

							if ( existing_peers== null ){

								return;
							}

							for (int i=0;i<existing_peers.size();i++){

								ExternalSeedPeer	peer = (ExternalSeedPeer)existing_peers.get(i);

								peer.setManager( null );
							}
						}

						protected List
						getPeers()
						{
							List	existing_peers = null;

							try{
								download_mon.enter();

								List	temp = (List)download_map.get( download );

								if ( temp != null ){

									existing_peers = new ArrayList( temp.size());

									existing_peers.addAll( temp );
								}
							}finally{

								download_mon.exit();
							}

							return( existing_peers );
						}
					});
			}else{

					// fix up newly added peers to current peer manager

				PeerManager	existing_pm = download.getPeerManager();

				if ( existing_pm != null ){

					for (int i=0;i<peers.size();i++){

						ExternalSeedPeer	peer = (ExternalSeedPeer)peers.get(i);

						if ( peer.getManager() == null ){

							peer.setManager( existing_pm );
						}
					}
				}
			}
		}
	}

	protected void
	removePeer(
		ExternalSeedPeer	peer )
	{
		Download	download = peer.getDownload();

		try{
			download_mon.enter();

			List	existing_peers = (List)download_map.get( download );

			if ( existing_peers != null ){

				if ( existing_peers.remove( peer )){

					log( download.getName() + " removed seed " + peer.getName());
				}
			}
		}finally{

			download_mon.exit();
		}
	}

	public void
	downloadRemoved(
		Download	download )
	{
		try{
			download_mon.enter();
			download_map.remove( download );

		}finally{
			download_mon.exit();
		}
	}

	public ExternalSeedManualPeer[]
	getManualWebSeeds(
		Download	download )
	{
		try{
			download_mon.enter();

			List	peers = (List)download_map.get( download );

			if ( peers == null ){

				return( new ExternalSeedManualPeer[0] );
			}

			ExternalSeedManualPeer[]	result = new ExternalSeedManualPeer[peers.size()];

			for (int i=0;i<peers.size();i++){

				result[i] = new ExternalSeedManualPeer((ExternalSeedPeer)peers.get(i));
			}

			return( result );

		}finally{

			download_mon.exit();
		}
	}

	public ExternalSeedReader[]
	getManualWebSeeds(
		Torrent	torrent )
	{
		List<ExternalSeedReader>		result = new ArrayList<ExternalSeedReader>();

		for (int i=0;i<factories.length;i++){

			ExternalSeedReader[] peers = factories[i].getSeedReaders( this, torrent );

			result.addAll( Arrays.asList( peers ));
		}

		return( result.toArray( new  ExternalSeedReader[result.size()]));
	}

	public TrackerPeerSource
	getTrackerPeerSource(
		final Download		download )
	{
		return(
			new TrackerPeerSourceAdapter()
			{
				private long	fixup_time;

				private ExternalSeedManualPeer[]	peers;
				private boolean						running;

				public int
				getType()
				{
					return( TP_HTTP_SEED );
				}

				public int
				getStatus()
				{
					fixup();

					if ( running ){

						return( peers.length==0?ST_UNAVAILABLE:ST_AVAILABLE );

					}else{

						return( ST_STOPPED );
					}
				}

				public String
				getName()
				{
					fixup();

					if ( peers.length == 0 ){

						return( "" );
					}

					StringBuffer sb = new StringBuffer();

					for ( ExternalSeedManualPeer peer: peers ){

						if ( sb.length() > 0 ){

							sb.append( ", " );
						}

						String str = peer.getDelegate().getURL().toExternalForm();

						int pos = str.indexOf( '?' );

						if ( pos != -1 ){

							str = str.substring( 0, pos );
						}

						sb.append( str );
					}

					return( sb.toString());
				}

				public int
				getSeedCount()
				{
					fixup();

					if ( running ){

						return( peers.length==0?-1:peers.length );

					}else{

						return( -1 );
					}
				}

				protected void
				fixup()
				{
					long	now = SystemTime.getMonotonousTime();

					if ( peers == null || now - fixup_time > 10*1000 ){

						fixup_time = now;

						peers = getManualWebSeeds(download);

						int	state = download.getState();

						running = state == Download.ST_DOWNLOADING || state == Download.ST_SEEDING;
					}
				}
			});
	}

	public TrackerPeerSource
	getTrackerPeerSource(
		final Torrent		torrent )
	{
		return(
			new TrackerPeerSourceAdapter()
			{
				private ExternalSeedReader[]	peers = getManualWebSeeds( torrent );

				public int
				getType()
				{
					return( TP_HTTP_SEED );
				}

				public int
				getStatus()
				{
					return( peers.length==0?ST_UNAVAILABLE:ST_AVAILABLE );
				}

				public String
				getName()
				{
					if ( peers.length == 0 ){

						return( "" );
					}

					StringBuffer sb = new StringBuffer();

					for ( ExternalSeedReader peer: peers ){

						if ( sb.length() > 0 ){

							sb.append( ", " );
						}

						String str = peer.getURL().toExternalForm();

						int pos = str.indexOf( '?' );

						if ( pos != -1 ){

							str = str.substring( 0, pos );
						}

						sb.append( str );
					}

					return( sb.toString());
				}

				public int
				getSeedCount()
				{
					return( peers.length==0?-1:peers.length );
				}
			});
	}

	public int
	getGlobalDownloadRateBytesPerSec()
	{
		return( dm_stats.getDataAndProtocolReceiveRate());
	}

	public void
	log(
		String		str )
	{
		log.log( str );
	}

	public void
	log(
		String		str,
		Throwable 	e )
	{
		log.log( str, e );
	}

	public PluginInterface
	getPluginInterface()
	{
		return( plugin_interface );
	}

	protected List
	randomiseList(
		List	l )
	{
		if ( l.size() < 2 ){

			return(l);
		}

		List	new_list = new ArrayList();

		for (int i=0;i<l.size();i++){

			new_list.add( random.nextInt(new_list.size()+1), l.get(i));
		}

		return( new_list );
	}
}