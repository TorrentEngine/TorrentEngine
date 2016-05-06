/**
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package torrentlib.util.platform;

import java.util.Properties;

import torrentlib.util.platform.unix.PlatformManagerUnixPlugin;

import torrentlib.util.platform.win32.Plugin;
import torrentlib.util.platform.win32.PluginException;
import torrentlib.util.platform.win32.PluginInterface;

/**
 * @author TuxPaper
 * @created Jul 24, 2007
 *
 */
public class PlatformManagerPluginDelegate
	implements Plugin
{

	public static void
	load(
		PluginInterface		plugin_interface )
	{
			// name it during initialisation

		plugin_interface.getPluginProperties().setProperty( "plugin.name", 	"Platform-Specific Support" );
	}

	// @see plugins.Plugin#initialize(plugins.PluginInterface)
	public void initialize(PluginInterface pluginInterface)
			throws PluginException {
		PlatformManager platform = PlatformManagerFactory.getPlatformManager();

      Properties pluginProperties = pluginInterface.getPluginProperties();
      pluginProperties.setProperty("plugin.name", "Platform-Specific Support");
      pluginProperties.setProperty("plugin.version", "1.0");
      pluginProperties.setProperty("plugin.version.info",
                                   "Not required for this platform");
	}

	public String
	getName()
	{
		return( "Mixin only" );
	}


	public int
	getMaximumCheckTime()
	{
		return( 0 );
	}
}
