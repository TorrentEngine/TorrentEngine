/*
 * File    : DownloadManagerFactory.java
 * Created : 19-Oct-2003
 * By      : stuff
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

package xfer.download;

/**
 * @author parg
 *
 */

import xfer.download.DownloadManager;
import xfer.download.DownloadManagerAvailability;
import java.util.List;

import torrentlib.data.torrent.TOTorrent;
import controller.GlobalManager;

public class
DownloadManagerFactory
{
		// new downloads

	public static DownloadManager
	create(
		GlobalManager 							gm,
		byte[]									torrent_hash,
		String 									torrentFileName,
		String 									savePath,
		String									saveFile,
		int      								initialState,
		boolean									persistent,
		boolean									for_seeding,
		List									file_priorities,
		DownloadManagerInitialisationAdapter 	adapter )
	{
		return( new DownloadManager( gm, torrent_hash, torrentFileName, savePath, saveFile, initialState, persistent, false, for_seeding, false, file_priorities, adapter ));
	}

		// recovery method

	public static DownloadManager
	create(
		GlobalManager 	gm,
		byte[]			torrent_hash,
		String 			torrentFileName,
		String 			torrent_save_dir,
		String			torrent_save_file,
		int      		initialState,
		boolean			persistent,
		boolean			recovered,
		boolean			has_ever_been_started,
		List			file_priorities )
	{
		return( new DownloadManager( gm, torrent_hash, torrentFileName, torrent_save_dir, torrent_save_file, initialState, persistent, recovered, false, has_ever_been_started, file_priorities, null ));
	}

	public static DownloadManagerAvailability
	getAvailability(
		TOTorrent				torrent,
		List<List<String>>		updated_trackers,
		String[]				enabled_peer_sources,
		String[]				enabled_networks )
	{
		return( new DownloadManagerAvailability( torrent, updated_trackers, enabled_peer_sources, enabled_networks ));
	}
}
