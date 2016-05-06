/*
 * Created on Mar 6, 2015
 * Created by Paul Gardner
 *
 * Copyright 2015 Azureus Software, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */


package plugins;

import torrentlib.internat.MessageText;
import torrentlib.AENetworkClassifier;
import torrentlib.Debug;
import torrentlib.util.platform.win32.PluginManager;

import torrentlib.AzureusCoreFactory;

public class
I2PHelpers
{
	private static final Object i2p_install_lock = new Object();

	private static boolean i2p_installing = false;

	public static boolean
	isI2PInstalled()
	{
		if ( isInstallingI2PHelper()){

			return( true );
		}

		PluginManager pm = AzureusCoreFactory.getSingleton().getPluginManager();

		return( pm.getPluginInterfaceByID( "azneti2phelper" ) != null );
	}

	public static boolean
	isInstallingI2PHelper()
	{
		synchronized( i2p_install_lock ){

			return( i2p_installing );
		}
	}

	public static boolean
	installI2PHelper(
		String				remember_id,
		final boolean[]		install_outcome,
		final Runnable		callback )
	{
		return installI2PHelper(null, remember_id, install_outcome, callback);
	}

	public static boolean
	installI2PHelper(
		String extra_text,
		String				remember_id,
		final boolean[]		install_outcome,
		final Runnable		callback )
	{
		synchronized( i2p_install_lock ){

			if ( i2p_installing ){

				Debug.out( "I2P Helper already installing" );

				return( false );
			}

			i2p_installing = true;
		}

		boolean	installing = false;

		try{

			Debug.out( "UIFunctions unavailable - can't install plugin" );
			return false;

		}finally{

			if ( !installing ){

				synchronized( i2p_install_lock ){

					i2p_installing = false;
				}
			}
		}
	}
}
