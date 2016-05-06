/*
 * Created on Feb 15, 2005
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

package plugins.utils;

import plugins.ui.UIInstance;
import java.io.InputStream;
import java.net.URL;

import torrentlib.AESemaphore;
import torrentlib.Debug;
import plugins.utils.resourcedownloader.ResourceDownloaderFactory;
import plugins.utils.resourceuploader.ResourceUploaderFactory;
import plugins.utils.xml.rss.RSSFeed;
import plugins.utils.xml.simpleparser.SimpleXMLParserDocumentException;
import pluginsimpl.local.PluginInitializer;
import pluginsimpl.local.utils.resourcedownloader.ResourceDownloaderFactoryImpl;
import pluginsimpl.local.utils.resourceuploader.ResourceUploaderFactoryImpl;

/**
 * Plugin utility class for easy access to static helper methods,
 * without the need for a plugin interface instance.
 */
public class StaticUtilities {

  private static Formatters formatters;

  static {
    try {
      Class c = Class.forName("pluginsimpl.local.utils.FormattersImpl");
      formatters = (Formatters) c.newInstance();
    } catch (Exception e) {
     e.printStackTrace();
    }
  }

  /**
   * Get display and byte format utilities.
   * @return formatters
   */
  public static Formatters getFormatters() {  return formatters;  }

  public static ResourceDownloaderFactory
  getResourceDownloaderFactory()
  {
	  return( ResourceDownloaderFactoryImpl.getSingleton());
  }

  public static ResourceUploaderFactory
  getResourceUploaderFactory()
  {
	  return( ResourceUploaderFactoryImpl.getSingleton());
  }

  	/**
  	 * See UIInstance.promptUser
  	 * @param title
  	 * @param desc
  	 * @param options
  	 * @param default_option
  	 * @return
  	 */

   /**
   * gets the default UI manager and also waits for up to a specified time for a UI instance to
   * attach. useful when doing things during initialisation
   * @param millis_to_wait_for_attach
   * @return
   */
}
