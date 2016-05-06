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

package torrentlib.util.platform.unix;

import torrentlib.Constants;
import torrentlib.Debug;
import torrentlib.SystemProperties;
import torrentlib.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.config.COConfigurationManager;
import torrentlib.internat.MessageText;
import torrentlib.util.platform.PlatformManager;
import torrentlib.util.platform.PlatformManagerCapabilities;
import torrentlib.util.platform.PlatformManagerFactory;
import torrentlib.util.UpdaterUtils;

import torrentlib.util.platform.win32.Plugin;
import torrentlib.util.platform.win32.PluginException;
import torrentlib.util.platform.win32.PluginInterface;
import plugins.ui.UIInstance;
import plugins.ui.UIManagerListener;

/**
 * @author TuxPaper
 * @created Jul 24, 2007
 *
 */
public class PlatformManagerUnixPlugin
	implements Plugin
{
	private PluginInterface plugin_interface;

	// @see plugins.Plugin#initialize(plugins.PluginInterface)
	public void initialize(PluginInterface _plugin_interface)
			throws PluginException {
		plugin_interface = _plugin_interface;

		plugin_interface.getPluginProperties().setProperty("plugin.name",
				"Platform-Specific Support");

		String version = "1.0"; // default version if plugin not present

		PlatformManager platform = PlatformManagerFactory.getPlatformManager();

		if (platform.hasCapability(PlatformManagerCapabilities.GetVersion)) {

			try {
				version = platform.getVersion();

			} catch (Throwable e) {

				Debug.printStackTrace(e);
			}

		} else {

			plugin_interface.getPluginProperties().setProperty("plugin.version.info",
					"Not required for this platform");

		}

		plugin_interface.getPluginProperties().setProperty("plugin.version",
				version);

  		plugin_interface.getUIManager().addUIListener(new UIManagerListener() {
  			boolean done = false;

  			public void UIDetached(UIInstance instance) {
  			}

  			public void UIAttached(UIInstance instance) {
  				if (!done){

  					done = true;

  					if (Constants.compareVersions(UpdaterUtils.getUpdaterPluginVersion(),"1.8.5") >= 0) {

  						checkStartupScript();
  					}
  				}
  			}
  		});
	}

	/**
	 *
	 *
	 * @since 3.0.1.7
	 */
	private void checkStartupScript() {
		COConfigurationManager.setIntDefault("unix.script.lastaskversion", -1);
		int lastAskedVersion = COConfigurationManager.getIntParameter("unix.script.lastaskversion");

		String sVersion = System.getProperty("azureus.script.version", "0");
		int version = 0;
		try {
			version = Integer.parseInt(sVersion);
		} catch (Throwable t) {
		}

		Pattern pat = Pattern.compile("SCRIPT_VERSION=([0-9]+)",
				Pattern.CASE_INSENSITIVE);


		File oldFilePath;
		String sScriptFile = System.getProperty("azureus.script", null);
		if (sScriptFile != null && new File(sScriptFile).exists()) {
			oldFilePath = new File(sScriptFile);
		} else {
			oldFilePath = new File(SystemProperties.getApplicationPath(),
					"azureus");
			if (!oldFilePath.exists()) {
				return;
			}
		}

		final String oldFilePathString = oldFilePath.getAbsolutePath();

		String oldStartupScript;
		try {
			oldStartupScript = FileUtil.readFileAsString(oldFilePath,
					65535, "utf8");
		} catch (IOException e) {
			oldStartupScript = "";
		}

		// Case: Script with no version, we update it, user selects restart.
		//       Restart doesn't include azureus.script.version yet, so we
		//       would normally prompt again.  This fix reads the version
		//       from the file if we don't have a version yet, thus preventing
		//       the second restart message
		if (version == 0) {
			Matcher matcher = pat.matcher(oldStartupScript);
			if (matcher.find()) {
				String sScriptVersion = matcher.group(1);
				try {
					version = Integer.parseInt(sScriptVersion);
				} catch (Throwable t) {
				}
			}
		}

		if (version <= lastAskedVersion) {
			return;
		}

		InputStream stream = getClass().getResourceAsStream("startupScript");
		try {
			String startupScript = FileUtil.readInputStreamAsString(stream, 65535,
					"utf8");
			Matcher matcher = pat.matcher(startupScript);
			if (matcher.find()) {
				String sScriptVersion = matcher.group(1);
				int latestVersion = 0;
				try {
					latestVersion = Integer.parseInt(sScriptVersion);
				} catch (Throwable t) {
				}
				if (latestVersion > version) {
					boolean bNotChanged = oldStartupScript.indexOf("SCRIPT_NOT_CHANGED=0") > 0
							|| oldStartupScript.indexOf("AUTOUPDATE_SCRIPT=1") > 0;
					boolean bInformUserManual = true;

					if (bNotChanged) {
						if (version >= 1) {
							// make the shutdown script copy it
							final String newFilePath = new File(
									SystemProperties.getApplicationPath(), "azureus.new").getAbsolutePath();
							FileUtil.writeBytesAsFile(newFilePath, startupScript.getBytes());

							String s = "cp \"" + newFilePath + "\" \"" + oldFilePathString
									+ "\"; chmod +x \"" + oldFilePathString
									+ "\"; echo \"Script Update successful\"";

							ScriptAfterShutdown.addExtraCommand(s);
							ScriptAfterShutdown.setRequiresExit(true);

							bInformUserManual = false;
						} else {
							// overwrite!
							try {
								FileUtil.writeBytesAsFile(oldFilePathString,
										startupScript.getBytes());
								Runtime.getRuntime().exec(new String[] {
									findCommand( "chmod" ),
									"+x",
									oldStartupScript
								});

								bInformUserManual = false;
							} catch (Throwable t) {
							}
						}
					}

					if (bInformUserManual) {
						final String newFilePath = new File(
								SystemProperties.getApplicationPath(), "azureus.new").getAbsolutePath();
						FileUtil.writeBytesAsFile(newFilePath, startupScript.getBytes());
						showScriptManualUpdateDialog(newFilePath, oldFilePathString,
								latestVersion);
					} else {
						showScriptAutoUpdateDialog();
					}
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}finally{
			try{
				stream.close();
			}catch( Throwable e){
			}
		}
	}

	  private String
	  findCommand(
		String	name )
	  {
		final String[]  locations = { "/bin", "/usr/bin" };

		for ( String s: locations ){

			File f = new File( s, name );

			if ( f.exists() && f.canRead()){

				return( f.getAbsolutePath());
			}
		}

		return( name );
	  }

	private void showScriptManualUpdateDialog(String newFilePath,
			String oldFilePath, final int version) {
			System.out.println("NO UIF");
	}

	private void showScriptAutoUpdateDialog() {
			System.out.println("NO UIF");
	}
}
