/*
 * Created : 2004/May/26
 *
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
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

package pluginsimpl.local.disk;

import java.io.File;

import torrentlib.Debug;
import plugins.disk.DiskManagerChannel;
import plugins.disk.DiskManagerFileInfo;
import plugins.disk.DiskManagerListener;
import plugins.disk.DiskManagerRandomReadRequest;
import plugins.download.Download;
import plugins.download.DownloadException;
import pluginsimpl.local.download.DownloadImpl;
import pluginsimpl.local.download.DownloadManagerImpl;


/**
 * @author TuxPaper
 *
 */

public class
DiskManagerFileInfoImpl
	implements DiskManagerFileInfo
{
	protected DownloadImpl										download;
	protected torrentlib.disk.DiskManagerFileInfo 	coreObj;

	public
	DiskManagerFileInfoImpl(
		DownloadImpl										_download,
		torrentlib.disk.DiskManagerFileInfo 	coreFileInfo )
	{
	  coreObj 		= coreFileInfo;
	  download	= _download;
	}

	public void setPriority(boolean b) {
	  coreObj.setPriority(b?1:0);
	}

	public void setSkipped(boolean b) {
	  coreObj.setSkipped(b);
	}

	public int
	getNumericPriorty()
	{
		return( coreObj.getPriority());
	}

	public int getNumericPriority() {
		return( coreObj.getPriority());
	}

	public void
	setNumericPriority(
		int priority)
	{
		coreObj.setPriority( priority );
	}

	public void
	setDeleted(boolean b)
	{
		int st = coreObj.getStorageType();

		int	target_st;

		if ( b ){

			if ( st == torrentlib.disk.DiskManagerFileInfo.ST_LINEAR ){

				target_st = torrentlib.disk.DiskManagerFileInfo.ST_COMPACT;

			}else if ( st == torrentlib.disk.DiskManagerFileInfo.ST_REORDER ){

				target_st = torrentlib.disk.DiskManagerFileInfo.ST_REORDER_COMPACT;

			}else{

				return;
			}

		}else{

			if ( st == torrentlib.disk.DiskManagerFileInfo.ST_COMPACT ){

				target_st = torrentlib.disk.DiskManagerFileInfo.ST_LINEAR;

			}else if ( st == torrentlib.disk.DiskManagerFileInfo.ST_REORDER_COMPACT ){

				target_st = torrentlib.disk.DiskManagerFileInfo.ST_REORDER;

			}else{

				return;
			}
		}

		coreObj.setStorageType( target_st );

	}

	public boolean
	isDeleted()
	{
		int st = coreObj.getStorageType();

		return( st ==  torrentlib.disk.DiskManagerFileInfo.ST_COMPACT || st == torrentlib.disk.DiskManagerFileInfo.ST_REORDER_COMPACT );
	}

	public void
	setLink(
		File	link_destination )
	{
		coreObj.setLink( link_destination );
	}

	public File
	getLink()
	{
		return( coreObj.getLink());
	}
	 	// get methods

	public int getAccessMode() {
	  return coreObj.getAccessMode();
	}

	public long getDownloaded() {
	  return coreObj.getDownloaded();
	}

	public long getLength() {
		  return coreObj.getLength();
		}
	public File getFile() {
	  return coreObj.getFile(false);
	}

	public File
	getFile(
		boolean	follow_link )
	{
		return( coreObj.getFile( follow_link ));
	}

	public int getFirstPieceNumber() {
	  return coreObj.getFirstPieceNumber();
	}

	public long getPieceSize(){
		try{
			return getDownload().getTorrent().getPieceSize();
		}catch( Throwable e ){

			Debug.printStackTrace(e);

			return(0);
		}
	}
	public int getNumPieces() {
	  return coreObj.getNbPieces();
	}

	public boolean isPriority() {
	  return coreObj.getPriority() != 0;
	}

	public boolean isSkipped() {
	  return coreObj.isSkipped();
	}

	public int
	getIndex()
	{
		return( coreObj.getIndex());
	}

	public byte[] getDownloadHash()
		throws DownloadException
	{
		return( getDownload().getTorrent().getHash());
	}

	public Download getDownload()
         throws DownloadException
    {
		if ( download != null ){

			return( download );
		}

			// not sure why this code is here as we already have the download - leaving in for the moment just in case...

		return DownloadManagerImpl.getDownloadStatic(coreObj.getDownloadManager());
    }

	public DiskManagerChannel
	createChannel()
	 	throws DownloadException
	{
		return( new DiskManagerChannelImpl( download, this ));
	}

	public DiskManagerRandomReadRequest
	createRandomReadRequest(
		long						file_offset,
		long						length,
		boolean						reverse_order,
		DiskManagerListener			listener )

		throws DownloadException
	{
		return( DiskManagerRandomReadController.createRequest( download, this, file_offset, length, reverse_order, listener ));
	}


	// not visible to plugin interface
	public torrentlib.disk.DiskManagerFileInfo
	getCore()
	{
		return( coreObj );
	}
}
