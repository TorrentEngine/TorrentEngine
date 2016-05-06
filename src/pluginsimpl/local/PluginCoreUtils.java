/*
 * Created on Apr 17, 2007
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


package pluginsimpl.local;

import java.io.File;
import java.io.IOException;

import torrentlib.disk.DiskManagerFileInfo;
import torrentlib.disk.DiskManagerFileInfoListener;
import connect.peer.PEPeer;
import connect.peer.PEPeerManager;
import torrentlib.data.torrent.TOTorrent;
import torrentlib.data.torrent.TOTorrentFile;
import connect.tracker.host.TRHostTorrent;
import connect.tracker.server.TRTrackerServerTorrent;
import torrentlib.Debug;
import torrentlib.DirectByteBuffer;
import plugins.disk.DiskManager;
import plugins.download.Download;
import plugins.download.DownloadException;
import plugins.network.Connection;
import plugins.peers.Peer;
import plugins.peers.PeerManager;
import plugins.torrent.Torrent;
import plugins.tracker.TrackerTorrent;

import pluginsimpl.local.disk.DiskManagerFileInfoImpl;
import pluginsimpl.local.disk.DiskManagerImpl;
import pluginsimpl.local.download.DownloadImpl;
import pluginsimpl.local.download.DownloadManagerImpl;
import pluginsimpl.local.network.ConnectionImpl;
import pluginsimpl.local.peers.PeerImpl;
import pluginsimpl.local.peers.PeerManagerImpl;
import pluginsimpl.local.torrent.TorrentImpl;
import pluginsimpl.local.tracker.TrackerTorrentImpl;

import controller.networkmanager.NetworkConnection;
import xfer.download.DownloadManager;

public class 
PluginCoreUtils 
{
	public static Torrent
	wrap(
		TOTorrent	t )
	{
		return( new TorrentImpl( t ));
	}
	
	public static TOTorrent
	unwrap(
		Torrent		t )
	{
		return(((TorrentImpl)t).getTorrent());
	}
	
	public static DiskManager
	wrap(
		torrentlib.disk.DiskManager	dm )
	{
		return( new DiskManagerImpl( dm ));
	}
	
	public static torrentlib.disk.DiskManager
	unwrap(
		DiskManager		dm )
	{
		return(((DiskManagerImpl)dm).getDiskmanager());
	}
	
	/**
	 * May return NULL if download not found (e.g. has been removed)
	 * @param dm
	 * @return may be null
	 */
	
	public static Download
	wrap(
		xfer.download.DownloadManager	dm )
	{
		try{
			return( DownloadManagerImpl.getDownloadStatic( dm ));
			
		}catch( Throwable e ){
			
			// Debug.printStackTrace( e );
			
			return( null );
		}
	}
	
	public static NetworkConnection
	unwrap(
		Connection		connection )
	{
		if ( connection instanceof ConnectionImpl ){
			
			return(((ConnectionImpl)connection).getCoreConnection());
		}
		
		return( null );
	}
	
	public static Connection
	wrap(
		NetworkConnection			connection )
	{
		return( new ConnectionImpl( connection, connection.isIncoming()));
	}
	
	public static plugins.disk.DiskManagerFileInfo
	wrap(
		DiskManagerFileInfo		info )
	
		throws DownloadException
	{
		if ( info == null ){
			
			return( null );
		}
		
		return( new DiskManagerFileInfoImpl( DownloadManagerImpl.getDownloadStatic( info.getDownloadManager()), info ));
	}
	
	public static DiskManagerFileInfo
	unwrap(
		final plugins.disk.DiskManagerFileInfo		info )
	
		throws DownloadException
	{
		if ( info instanceof DiskManagerFileInfoImpl ){
			
			return(((DiskManagerFileInfoImpl)info).getCore());
		}
	
		if ( info == null ){
			
			return( null );
		}

		try{
			Download dl = info.getDownload();
			
			if ( dl != null ){
				
				xfer.download.DownloadManager dm = unwrap( dl );
				
				return( dm.getDiskManagerFileInfo()[ info.getIndex()]);
			}
		}catch( Throwable e ){
		}
		
			// no underlying download, lash something up
		
		return(
			new DiskManagerFileInfo()
			{
				public void 
				setPriority(
					int b )
				{
					info.setNumericPriority(b);
				}
				
				public void 
				setSkipped(
					boolean b)
				{
					info.setSkipped(b);
				}

				public boolean
				setLink(
					File	link_destination )
				{	
					info.setLink(link_destination);
					
					return( true );
				}
				
				public boolean 
				setLinkAtomic(
					File link_destination )
				{	
					info.setLink(link_destination);
					
					return( true );
				}
				
				public File
				getLink()
				{
					return( info.getLink());
				}
				
				public boolean 
				setStorageType(
					int type )
				{
					return( false );
				}
				
				public int
				getStorageType()
				{
					return( ST_LINEAR );
				}
				
				public int 
				getAccessMode()
				{
					return( info.getAccessMode());
				}
				
				public long 
				getDownloaded()
				{
					return( info.getDownloaded());
				}
				
				public String 
				getExtension()
				{
					return( "" );
				}
					
				public int 
				getFirstPieceNumber()
				{
					return( info.getFirstPieceNumber());
				}
			  
				public int 
				getLastPieceNumber()
				{
					return((int)(( info.getLength() + info.getPieceSize()-1 )/info.getPieceSize()));
				}
				
				public long 
				getLength()
				{
					return( info.getLength());
				}
					
				public int 
				getNbPieces()
				{
					return( info.getNumPieces());
				}
						
				public int 
				getPriority()
				{
					return( info.getNumericPriorty());
				}
				
				public boolean 
				isSkipped()
				{
					return( info.isSkipped());
				}
				
				public int	
				getIndex()
				{
					return( info.getIndex());
				}
				
				public DownloadManager	
				getDownloadManager()
				{
					return( null );
				}
				
				public torrentlib.disk.DiskManager 
				getDiskManager()
				{
					return( null );
				}
				
				public File 
				getFile( 
					boolean follow_link )
				{
					if ( follow_link ){
						
						return( info.getLink());
						
					}else{
						
						return( info.getFile());
					}
				}
				
				public TOTorrentFile
				getTorrentFile()
				{
					return( null );
				}
				
				public DirectByteBuffer
				read(
					long	offset,
					int		length )
				
					throws IOException
				{
					throw( new IOException( "unsupported" ));
				}
				
				public void
				flushCache()
				
					throws	Exception
				{	
				}
				
				public int 
				getReadBytesPerSecond() 
				{
					return( 0 );
				}
				
				public int 
				getWriteBytesPerSecond() 
				{
					return( 0 );
				}
				
				public long
				getETA()
				{
					return( -1 );
				}
				
				public void
				close()
				{
				}
				
				public void
				addListener(
					DiskManagerFileInfoListener	listener )
				{
				}
				
				public void
				removeListener(
					DiskManagerFileInfoListener	listener )
				{
				}
			});
	}
	
	public static Object
	convert(
		Object datasource,
		boolean toCore)
	{
		if (datasource instanceof Object[]) {
			Object[] array = (Object[]) datasource;
			Object[] newArray = new Object[array.length];
			for (int i = 0; i < array.length; i++) {
				Object o = array[i];
				newArray[i] = convert(o, toCore);
			}
			return newArray;
		}

		try {
			if (toCore) {
				if (datasource instanceof xfer.download.DownloadManager) {
					return datasource;
				}
				if (datasource instanceof DownloadImpl) {
					return ((DownloadImpl) datasource).getDownload();
				}

				if (datasource instanceof torrentlib.disk.DiskManager) {
					return datasource;
				}
				if (datasource instanceof DiskManagerImpl) {
					return ((DiskManagerImpl) datasource).getDiskmanager();
				}

				if (datasource instanceof PEPeerManager) {
					return datasource;
				}
				if (datasource instanceof PeerManagerImpl) {
					return ((PeerManagerImpl) datasource).getDelegate();
				}
				
				if (datasource instanceof PEPeer) {
					return datasource;
				}
				if (datasource instanceof PeerImpl) {
					return ((PeerImpl)datasource).getPEPeer();
				}

				if (datasource instanceof torrentlib.disk.DiskManagerFileInfo) {
					return datasource;
				}
				if (datasource instanceof pluginsimpl.local.disk.DiskManagerFileInfoImpl) {
					return ((pluginsimpl.local.disk.DiskManagerFileInfoImpl) datasource).getCore();
				}

				if (datasource instanceof TRHostTorrent) {
					return datasource;
				}
				if (datasource instanceof TrackerTorrentImpl) {
					((TrackerTorrentImpl) datasource).getHostTorrent();
				}
			} else { // to PI
				if (datasource instanceof xfer.download.DownloadManager) {
					return wrap((xfer.download.DownloadManager) datasource);
				}
				if (datasource instanceof DownloadImpl) {
					return datasource;
				}

				if (datasource instanceof torrentlib.disk.DiskManager) {
					return wrap((torrentlib.disk.DiskManager) datasource);
				}
				if (datasource instanceof DiskManagerImpl) {
					return datasource;
				}

				if (datasource instanceof PEPeerManager) {
					return wrap((PEPeerManager) datasource);
				}
				if (datasource instanceof PeerManagerImpl) {
					return datasource;
				}

				if (datasource instanceof PEPeer) {
					return PeerManagerImpl.getPeerForPEPeer((PEPeer) datasource);
				}
				if (datasource instanceof Peer) {
					return datasource;
				}
				
				if (datasource instanceof torrentlib.disk.DiskManagerFileInfo) {
					DiskManagerFileInfo fileInfo = (torrentlib.disk.DiskManagerFileInfo) datasource;
					if (fileInfo != null) {
						try {
							return new pluginsimpl.local.disk.DiskManagerFileInfoImpl(
									DownloadManagerImpl.getDownloadStatic(fileInfo.getDownloadManager()),
									fileInfo);
						} catch (DownloadException e) { /* Ignore */
						}
					}
				}
				if (datasource instanceof pluginsimpl.local.disk.DiskManagerFileInfoImpl) {
					return datasource;
				}

				if (datasource instanceof TRHostTorrent) {
					TRHostTorrent item = (TRHostTorrent) datasource;
					return new TrackerTorrentImpl(item);
				}
				if (datasource instanceof TrackerTorrentImpl) {
					return datasource;
				}
			}
		} catch (Throwable t) {
			Debug.out(t);
		}

		return datasource;
	}
	
	public static xfer.download.DownloadManager
	unwrapIfPossible(
		Download		dm )
	{
			// might be a LWSDownload
		
		if ( dm instanceof DownloadImpl ){
			
			return(((DownloadImpl)dm).getDownload());
			
		}else{
			
			return( null );
		}
	}
	
	public static xfer.download.DownloadManager
	unwrap(
		Download		dm )
	{
		if ( dm instanceof DownloadImpl ){
			
			return(((DownloadImpl)dm).getDownload());
			
		}else{
			
			Debug.out( "Can't unwrap " + dm );
			
			return( null );
		}
	}
	
	public static PeerManager
	wrap(
		PEPeerManager	pm )
	{
		return( PeerManagerImpl.getPeerManager( pm ));
	}
	
	public static PEPeerManager
	unwrap(
		PeerManager		pm )
	{
		return(((PeerManagerImpl)pm).getDelegate());
	}
	
	public static TRTrackerServerTorrent
	unwrap(
		TrackerTorrent		torrent )
	{
		return( ((TrackerTorrentImpl)torrent).getHostTorrent().getTrackerTorrent());
	}
	
	public static PEPeer
	unwrap(
		Peer		peer )
	{
		return(((PeerImpl)peer).getDelegate());
	}
	
	public static boolean
	isInitialisationComplete()
	{
		return( PluginInitializer.getDefaultInterface().getPluginState().isInitialisationComplete());
	}
}
