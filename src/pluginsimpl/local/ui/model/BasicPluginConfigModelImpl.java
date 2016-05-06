/*
 * Created on 28-Apr-2004
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

package pluginsimpl.local.ui.model;

/**
 * @author parg
 *
 */

import plugins.ui.model.BasicPluginConfigModel;
import torrentlib.util.platform.win32.PluginInterface;
import pluginsimpl.local.ui.config.ActionParameterImpl;
import pluginsimpl.local.ui.config.ParameterTabFolderImpl;
import pluginsimpl.local.ui.config.IntParameterImpl;
import pluginsimpl.local.ui.config.StringParameterImpl;
import pluginsimpl.local.ui.config.HyperlinkParameterImpl;
import pluginsimpl.local.ui.config.StringListParameterImpl;
import pluginsimpl.local.ui.config.PasswordParameterImpl;
import pluginsimpl.local.ui.config.DirectoryParameterImpl;
import pluginsimpl.local.ui.config.FileParameter;
import pluginsimpl.local.ui.config.BooleanParameterImpl;
import pluginsimpl.local.ui.config.InfoParameterImpl;
import pluginsimpl.local.ui.config.LabelParameterImpl;
import pluginsimpl.local.ui.config.ParameterImpl;
import pluginsimpl.local.ui.config.ParameterGroupImpl;
import pluginsimpl.local.ui.config.UIParameterImpl;
import pluginsimpl.local.ui.config.ColorParameterImpl;
import java.util.*;

import torrentlib.internat.MessageText;
import plugins.ui.config.ActionParameter;
import plugins.ui.config.InfoParameter;
import plugins.ui.config.LabelParameter;
import plugins.ui.config.Parameter;
import plugins.ui.config.ParameterGroup;
import plugins.ui.config.ParameterTabFolder;
import pluginsimpl.local.PluginConfigImpl;
import pluginsimpl.local.ui.UIManagerImpl;

public class
BasicPluginConfigModelImpl
	implements BasicPluginConfigModel
{
	private UIManagerImpl		ui_manager;

	private String					parent_section;
	private String					section;
	private PluginInterface			pi;
	private ArrayList<Parameter>	parameters = new ArrayList<Parameter>();

	private String				key_prefix;

	private PluginConfigImpl    configobj;

	public
	BasicPluginConfigModelImpl(
		UIManagerImpl		_ui_manager,
		String				_parent_section,
		String				_section )
	{
		ui_manager		= _ui_manager;
		parent_section	= _parent_section;
		section			= _section;

		pi				= ui_manager.getPluginInterface();

		key_prefix		= pi.getPluginconfig().getPluginConfigKeyPrefix();
		configobj       = (PluginConfigImpl)pi.getPluginconfig();

		String version = pi.getPluginVersion();

		addLabelParameter2( "!" + MessageText.getString( "ConfigView.pluginlist.column.version" ) + ": " + (version==null?"<local>":version) + "!" );
	}

	public String
	getParentSection()
	{
		return( parent_section );
	}

	public String
	getSection()
	{
		return( section );
	}

	public PluginInterface
	getPluginInterface()
	{
		return( pi );
	}

	public Parameter[]
	getParameters()
	{
		Parameter[] res = new Parameter[parameters.size()];

		parameters.toArray( res );

		return( res );
	}

	public void
	addBooleanParameter(
		String 		key,
		String 		resource_name,
		boolean 	defaultValue )
	{
		addBooleanParameter2( key, resource_name, defaultValue );
	}

	public plugins.ui.config.BooleanParameter
	addBooleanParameter2(
		String 		key,
		String 		resource_name,
		boolean 	defaultValue )
	{
		BooleanParameterImpl res = new BooleanParameterImpl(configobj, resolveKey(key), resource_name, defaultValue );

		parameters.add( res );

		return( res );
	}

	public void
	addStringParameter(
		String 		key,
		String 		resource_name,
		String  	defaultValue )
	{
		addStringParameter2( key, resource_name, defaultValue );
	}

	public plugins.ui.config.StringParameter
	addStringParameter2(
		String 		key,
		String 		resource_name,
		String  	defaultValue )
	{
		StringParameterImpl res = new StringParameterImpl(configobj, resolveKey(key), resource_name, defaultValue );

		parameters.add( res );

		return( res );
	}

	public plugins.ui.config.StringListParameter
	addStringListParameter2(
		String 		key,
		String 		resource_name,
		String[]	values,
		String	 	defaultValue )
	{
		StringListParameterImpl res = new StringListParameterImpl(configobj, resolveKey(key), resource_name, defaultValue, values, values );

		parameters.add( res );

		return( res );
	}

	public plugins.ui.config.StringListParameter
	addStringListParameter2(
		String 		key,
		String 		resource_name,
		String[]	values,
		String[]	labels,
		String	 	defaultValue )
	{
		StringListParameterImpl res = new StringListParameterImpl(configobj,
				resolveKey(key), resource_name, defaultValue,
				values, labels);

		parameters.add(res);

		return (res);
	}

	public plugins.ui.config.PasswordParameter
	addPasswordParameter2(
		String 		key,
		String 		resource_name,
		int			encoding_type,
		byte[]	 	defaultValue )
	{
		PasswordParameterImpl res = new PasswordParameterImpl(configobj, resolveKey(key), resource_name, encoding_type, defaultValue );

		parameters.add( res );

		return( res );
	}

	public plugins.ui.config.IntParameter
	addIntParameter2(
		String 		key,
		String 		resource_name,
		int	 		defaultValue )
	{
		IntParameterImpl res = new IntParameterImpl(configobj, resolveKey(key), resource_name, defaultValue );

		parameters.add( res );

		return( res );
	}

	public plugins.ui.config.IntParameter
	addIntParameter2(
		String 		key,
		String 		resource_name,
		int	 		defaultValue,
		int         min_value,
		int         max_value)
	{
		IntParameterImpl res = new IntParameterImpl(configobj, resolveKey(key), resource_name, defaultValue, min_value, max_value );
		parameters.add( res );
		return( res );
	}

	public plugins.ui.config.DirectoryParameter
	addDirectoryParameter2(
		String 		key,
		String 		resource_name,
		String 		defaultValue )
	{
		DirectoryParameterImpl res = new DirectoryParameterImpl(configobj, resolveKey(key), resource_name, defaultValue );

		parameters.add( res );

		return( res );
	}

	public plugins.ui.config.FileParameter
	addFileParameter2(
			String 		key,
			String 		resource_name,
			String 		defaultValue ) {
		return addFileParameter2(key, resource_name, defaultValue, null);
	}

	public plugins.ui.config.FileParameter
	addFileParameter2(
			String 		key,
			String 		resource_name,
			String 		defaultValue,
		    String[]    file_extensions) {
		FileParameter res = new FileParameter(configobj, resolveKey(key), resource_name, defaultValue, file_extensions);
		parameters.add(res);
		return res;
	}


	public LabelParameter
	addLabelParameter2(
		String		resource_name )
	{
		LabelParameterImpl res = new LabelParameterImpl(configobj, key_prefix, resource_name );

		parameters.add( res );

		return( res );
	}

	public InfoParameter
	addInfoParameter2(
		String		resource_name,
		String		value )
	{
		InfoParameterImpl res = new InfoParameterImpl(configobj, resolveKey(resource_name), resource_name, value );

		parameters.add( res );

		return( res );
	}

	public plugins.ui.config.HyperlinkParameter
	addHyperlinkParameter2(String resource_name, String url_location) {
		HyperlinkParameterImpl res = new HyperlinkParameterImpl(configobj, key_prefix, resource_name, url_location);
		parameters.add(res);
		return res;
	}

	public plugins.ui.config.ColorParameter
	addColorParameter2(String key, String resource_name, int r, int g, int b) {
		ColorParameterImpl res = new ColorParameterImpl(configobj, resolveKey(key), resource_name, r, g, b);
		parameters.add(res);
		return res;
	}

	public plugins.ui.config.UIParameter
	addUIParameter2(plugins.ui.config.UIParameterContext context, String resource_name) {
		UIParameterImpl res = new UIParameterImpl(configobj, context, key_prefix, resource_name);
		parameters.add(res);
		return res;
	}

	public ActionParameter
	addActionParameter2(
		String 		label_resource_name,
		String		action_resource_name )
	{
		ActionParameterImpl res = new ActionParameterImpl(configobj, label_resource_name, action_resource_name );

		parameters.add( res );

		return( res );
	}

	public ParameterGroup
	createGroup(
		String											_resource_name,
		plugins.ui.config.Parameter[]	_parameters )
	{
		ParameterGroupImpl	pg = new ParameterGroupImpl( _resource_name, _parameters );

		return( pg );
	}

	public ParameterTabFolder
	createTabFolder()
	{
		return( new ParameterTabFolderImpl());
	}

	public void
	destroy()
	{
		ui_manager.destroy( this );

		for (int i=0;i<parameters.size();i++){

			((ParameterImpl)parameters.get(i)).destroy();
		}
	}

	public void setLocalizedName(String name) {
		Properties props = new Properties();
		props.put("ConfigView.section." + this.section, name);
		this.pi.getUtilities().getLocaleUtilities().integrateLocalisedMessageBundle(props);
	}

	protected String
	resolveKey(
		String	key )
	{
		if ( key.startsWith("!") && key.endsWith( "!" )){

			return( key.substring(1, key.length()-1 ));
		}

		return( key_prefix + key );
	}
}
