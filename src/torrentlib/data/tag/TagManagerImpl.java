/*
 * Created on Mar 20, 2013
 * Created by Paul Gardner
 *
 * Copyright 2013 Azureus Software, Inc.  All rights reserved.
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
 */


package torrentlib.data.tag;

import torrentlib.data.tag.TagType;
import torrentlib.data.tag.TaggableResolver;
import torrentlib.data.tag.TagManagerFactory;
import torrentlib.data.tag.TagManagerListener;
import torrentlib.data.tag.TagFeatureListener;
import torrentlib.data.tag.TagFeatureFileLocation;
import torrentlib.data.tag.Tag;
import torrentlib.data.tag.TaggableLifecycleHandler;
import torrentlib.data.tag.TagFeature;
import torrentlib.data.tag.Taggable;
import torrentlib.data.tag.TaggableLifecycleListener;
import torrentlib.data.tag.TagDownload;
import torrentlib.data.tag.TagManager;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;

import controller.config.COConfigurationManager;
import torrentlib.disk.DiskManager;
import xfer.download.DownloadManagerInitialisationAdapter;
import xfer.download.DownloadManagerState;
import torrentlib.internat.MessageText;
import torrentlib.util.logging.LogAlert;
import torrentlib.util.logging.Logger;
import torrentlib.data.torrent.TOTorrent;
import torrentlib.AEDiagnostics;
import torrentlib.AEDiagnosticsEvidenceGenerator;
import torrentlib.AENetworkClassifier;
import torrentlib.AERunnable;
import torrentlib.AEThread2;
import torrentlib.AsyncDispatcher;
import torrentlib.BDecoder;
import torrentlib.BEncoder;
import torrentlib.Base32;
import torrentlib.Debug;
import torrentlib.util.FileUtil;
import torrentlib.FrequencyLimitedDispatcher;
import torrentlib.IndentWriter;
import torrentlib.SimpleTimer;
import torrentlib.SystemTime;
import torrentlib.TimeFormatter;
import torrentlib.TimerEvent;
import torrentlib.TimerEventPerformer;
import torrentlib.util.TorrentUtils;
import torrentlib.UrlUtils;
import stdlib.data.xml.XMLEscapeWriter;
import stdlib.data.xml.XUXmlWriter;
import torrentlib.util.platform.win32.PluginInterface;
import torrentlib.util.platform.win32.PluginManager;
import plugins.disk.DiskManagerFileInfo;
import plugins.download.Download;
import plugins.download.DownloadCompletionListener;
import plugins.download.DownloadScrapeResult;
import plugins.torrent.Torrent;
import plugins.tracker.web.TrackerWebPageRequest;
import plugins.tracker.web.TrackerWebPageResponse;
import plugins.utils.ScriptProvider;
import pluginsimpl.PluginUtils;
import pluginsimpl.local.PluginCoreUtils;

import torrentlib.AzureusCoreComponent;
import torrentlib.AzureusCoreFactory;
import torrentlib.AzureusCoreLifecycleAdapter;
import torrentlib.CopyOnWriteList;
import torrentlib.util.IdentityHashSet;
import torrentlib.util.PlatformTorrentUtils;
import torrentlib.util.MapUtils;
import controller.GlobalManager;
import xfer.download.DownloadManager;
import torrentlib.TorrentEngineCore;

public class
TagManagerImpl
	implements TagManager, DownloadCompletionListener, AEDiagnosticsEvidenceGenerator
{
	private static final String	CONFIG_FILE 				= "tag.config";

		// order is important as 'increases' in effects (see applyConfigUpdates)

	private static final int CU_TAG_CREATE		= 1;
	private static final int CU_TAG_CHANGE		= 2;
	private static final int CU_TAG_CONTENTS	= 3;
	private static final int CU_TAG_REMOVE		= 4;

	private static final boolean	enabled = COConfigurationManager.getBooleanParameter( "tagmanager.enable", true );

	private static TagManagerImpl	singleton;

	public static synchronized TagManagerImpl
	getSingleton()
	{
		if ( singleton == null ){

			singleton = new TagManagerImpl();

			singleton.init();
		}

		return( singleton );
	}

	private CopyOnWriteList<TagTypeBase>	tag_types = new CopyOnWriteList<TagTypeBase>();

	private Map<Integer,TagType>	tag_type_map = new HashMap<Integer, TagType>();

	private static final String RSS_PROVIDER	= "tags";

	private Set<TagBase>	rss_tags = new HashSet<TagBase>();

	private Set<DownloadManager>	active_copy_on_complete = new IdentityHashSet<DownloadManager>();

	private AsyncDispatcher async_dispatcher = new AsyncDispatcher(5000);

	private FrequencyLimitedDispatcher dirty_dispatcher =
		new FrequencyLimitedDispatcher(
			new AERunnable()
			{
				public void
				runSupport()
				{
						// always go async to avoid blocking caller

					new AEThread2( "tag:fld" )
					{
						public void
						run()
						{
							try{
									// just in case there's a bunch of changes coming in together

								Thread.sleep( 1000 );

							}catch( Throwable e ){

							}

							writeConfig();
						}
					}.start();
				}
			},
			30*1000 );


	private Map					config;
	private WeakReference<Map>	config_ref;

	private boolean				config_dirty;

	private List<Object[]>		config_change_queue = new ArrayList<Object[]>();


	private CopyOnWriteList<TagManagerListener>		listeners = new CopyOnWriteList<TagManagerListener>();

	private CopyOnWriteList<Object[]>				feature_listeners = new CopyOnWriteList<Object[]>();

	private Map<Long,LifecycleHandlerImpl>			lifecycle_handlers = new HashMap<Long,LifecycleHandlerImpl>();

	private TagPropertyUntaggedHandler	untagged_handler;

	private boolean		js_plugin_install_tried;

	private
	TagManagerImpl()
	{
		AEDiagnostics.addEvidenceGenerator( this );
	}

	public boolean
	isEnabled()
	{
		return( enabled );
	}

	private void
	init()
	{
		if ( !enabled ){

			return;
		}

		TorrentEngineCore azureus_core = AzureusCoreFactory.getSingleton();

		final TagPropertyTrackerHandler auto_tracker = new TagPropertyTrackerHandler( azureus_core, this );

		untagged_handler = new TagPropertyUntaggedHandler( azureus_core, this );

		new TagPropertyTrackerTemplateHandler( azureus_core, this );

		new TagPropertyConstraintHandler( azureus_core, this );

		azureus_core.addLifecycleListener(new AzureusCoreLifecycleAdapter()
			{
				public void
				started(
					TorrentEngineCore		core )
				{
					core.getPluginManager().getDefaultPluginInterface().getDownloadManager().getGlobalDownloadEventNotifier().addCompletionListener( TagManagerImpl.this);
				}

				public void
				componentCreated(
					TorrentEngineCore 			core,
					AzureusCoreComponent 	component )
				{
					if ( component instanceof GlobalManager ){

						GlobalManager global_manager = (GlobalManager)component;

						global_manager.addDownloadManagerInitialisationAdapter(new DownloadManagerInitialisationAdapter()
								{
									public int
									getActions()
									{
										return( ACT_ASSIGNS_TAGS );
									}

									public void
									initialised(
										DownloadManager 	manager,
										boolean				for_seeding )
									{
										torrentlib.disk.DiskManagerFileInfo[] files = manager.getDiskManagerFileInfoSet().getFiles();

										for ( torrentlib.disk.DiskManagerFileInfo file: files ){

											if ( file.getTorrentFile().getPathComponents().length == 1 ){

												String name = file.getTorrentFile().getRelativePath().toLowerCase( Locale.US );

												if ( name.equals( "index.html" ) || name.equals( "index.htm" )){

													TagType tt = TagManagerFactory.getTagManager().getTagType( TagType.TT_DOWNLOAD_MANUAL );

													String tag_name = "Websites";

													Tag tag = tt.getTag( tag_name, true );

													try{
														if ( tag == null ){

															tag = tt.createTag( tag_name, true );
														}

														if ( !tag.hasTaggable( manager )){

															tag.addTaggable( manager );

															tag.setDescription( MessageText.getString( "tag.website.desc" ));
														}
													}catch( Throwable e ){

														Debug.out( e );
													}

													break;
												}
											}
										}
									}
								});

						global_manager.addDownloadManagerInitialisationAdapter(new DownloadManagerInitialisationAdapter()
							{
								public int
								getActions()
								{
									return( ACT_PROCESSES_TAGS );
								}

								public void
								initialised(
									DownloadManager 	manager,
									boolean				for_seeding )
								{
									if ( for_seeding ){

										return;
									}

										// perform any auto-tagging - note that auto-tags aren't applied to the download
										// yet

									List<Tag> auto_tags = auto_tracker.getTagsForDownload( manager );

									Set<Tag> tags = new HashSet<Tag>( getTagsForTaggable( TagType.TT_DOWNLOAD_MANUAL, manager ));

									tags.addAll( auto_tags );

									if ( tags.size() == 0 ){

											// pick up untagged tags here as they haven't been added yet :(

										tags.addAll( untagged_handler.getUntaggedTags());
									}

									if ( tags.size() > 0 ){

										List<Tag>	sl_tags = new ArrayList<Tag>();

										for ( Tag tag: tags ){

											TagFeatureFileLocation fl = (TagFeatureFileLocation)tag;

											if ( fl.supportsTagInitialSaveFolder()){

												File save_loc = fl.getTagInitialSaveFolder();

												if ( save_loc != null ){

													sl_tags.add( tag );
												}
											}
										}

										if ( sl_tags.size() > 0 ){

											if ( sl_tags.size() > 1 ){

												Collections.sort(
													sl_tags,
													new Comparator<Tag>()
													{
														public int
														compare(
															Tag o1, Tag o2)
														{
															return( o1.getTagID() - o2.getTagID());
														}
													});
											}

											File new_loc = ((TagFeatureFileLocation)sl_tags.get(0)).getTagInitialSaveFolder();

											File old_loc = manager.getSaveLocation();

											if ( !new_loc.equals( old_loc )){

												manager.setTorrentSaveDir( new_loc.getAbsolutePath());
											}
										}
									}
								}
							});
					}
				}

				public void
				stopped(
					TorrentEngineCore		core )
				{
					destroy();
				}
			});

		SimpleTimer.addPeriodicEvent(
			"TM:Sync",
			30*1000,
			new TimerEventPerformer()
			{
				public void
				perform(
					TimerEvent event)
				{
					for ( TagType tt: tag_types ){

						((TagTypeBase)tt).sync();
					}
				}
			});
	}

	public void
	onCompletion(
		Download d )
	{
		final DownloadManager manager = PluginCoreUtils.unwrap( d );

		List<Tag> tags = getTagsForTaggable( manager );

		List<Tag> cc_tags = new ArrayList<Tag>();

		for ( Tag tag: tags ){

			if ( tag.getTagType().hasTagTypeFeature( TagFeature.TF_FILE_LOCATION )){

				TagFeatureFileLocation fl = (TagFeatureFileLocation)tag;

				if ( fl.supportsTagCopyOnComplete()){

					File save_loc = fl.getTagCopyOnCompleteFolder();

					if ( save_loc != null ){

						cc_tags.add( tag );
					}
				}
			}
		}

		if ( cc_tags.size() > 0 ){

			if ( cc_tags.size() > 1 ){

				Collections.sort(
						cc_tags,
					new Comparator<Tag>()
					{
						public int
						compare(
							Tag o1, Tag o2)
						{
							return( o1.getTagID() - o2.getTagID());
						}
					});
			}

			final File new_loc = ((TagFeatureFileLocation)cc_tags.get(0)).getTagCopyOnCompleteFolder();

			File old_loc = manager.getSaveLocation();

			if ( !new_loc.equals( old_loc )){

				boolean do_it;

				synchronized( active_copy_on_complete ){

					if ( active_copy_on_complete.contains( manager )){

						do_it = false;

					}else{

						active_copy_on_complete.add( manager );

						do_it = true;
					}
				}

				if ( do_it ){

					new AEThread2( "tm:copy")
					{
						public void
						run()
						{
							try{
								long stopped_and_incomplete_start 	= 0;
								long looks_good_start 				= 0;

								while( true ){

									if ( manager.isDestroyed()){

										throw( new Exception( "Download has been removed" ));
									}

									DiskManager dm = manager.getDiskManager();

									if ( dm == null ){

										looks_good_start = 0;

										if ( !manager.getAssumedComplete()){

											long	now = SystemTime.getMonotonousTime();

											if ( stopped_and_incomplete_start == 0 ){

												stopped_and_incomplete_start = now;

											}else if ( now - stopped_and_incomplete_start > 30*1000 ){

												throw( new Exception( "Download is stopped and incomplete" ));
											}
										}else{

											break;
										}
									}else{

										stopped_and_incomplete_start = 0;

										if ( manager.getAssumedComplete()){

											if ( dm.getMoveProgress() == -1 && dm.getCompleteRecheckStatus() == -1 ){

												long	now = SystemTime.getMonotonousTime();

												if ( looks_good_start == 0 ){

													looks_good_start = now;

												}else if ( now - looks_good_start > 5*1000 ){

													break;
												}
											}
										}else{

											looks_good_start = 0;
										}
									}

									//System.out.println( "Waiting" );

									Thread.sleep( 1000 );
								}

								manager.copyDataFiles( new_loc );

								Logger.logTextResource(
									new LogAlert(
										manager,
										LogAlert.REPEATABLE,
										LogAlert.AT_INFORMATION,
										"alert.copy.on.comp.done"),
									new String[]{ manager.getDisplayName(), new_loc.toString()});

							}catch( Throwable e ){

								 Logger.logTextResource(
									new LogAlert(
										manager,
										LogAlert.REPEATABLE,
										LogAlert.AT_ERROR,
										"alert.copy.on.comp.fail"),
									new String[]{ manager.getDisplayName(), new_loc.toString(), Debug.getNestedExceptionMessage(e)});

							}finally{

								synchronized( active_copy_on_complete ){

									active_copy_on_complete.remove( manager );
								}

							}
						}
					}.start();
				}
			}
		}
	}

	protected Object
	evalScript(
		Tag					tag,
		String				script,
		DownloadManager		dm,
		String				intent_key )
	{
		String script_type = "";

		if ( script.length() >=10 && script.substring(0,10).toLowerCase( Locale.US ).startsWith( "javascript" )){

			int	p1 = script.indexOf( '(' );

			int	p2 = script.lastIndexOf( ')' );

			if ( p1 != -1 && p2 != -1 ){

				script = script.substring( p1+1, p2 ).trim();

				if ( script.startsWith( "\"" ) && script.endsWith( "\"" )){

					script = script.substring( 1, script.length()-1 );
				}

					// allow people to escape " if it makes them feel better

				script = script.replaceAll( "\\\\\"", "\"" );

				script_type = ScriptProvider.ST_JAVASCRIPT;
			}
		}

		if ( script_type == "" ){

			Debug.out( "Unrecognised script type: " + script );

			return( null );
		}

		boolean	provider_found = false;

		List<ScriptProvider> providers = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface().getUtilities().getScriptProviders();

		for ( ScriptProvider p: providers ){

			if ( p.getScriptType() == script_type ){

				provider_found = true;

				Map<String,Object>	bindings = new HashMap<String, Object>();


				String dm_name = dm.getDisplayName();

				if ( dm_name.length() > 32 ){

					dm_name = dm_name.substring( 0, 29 ) + "...";
				}

				String intent = intent_key + "(\"" + tag.getTagName() + "\",\"" + dm_name + "\")";

				bindings.put( "intent", intent );

				bindings.put( "download", PluginCoreUtils.wrap( dm ));

				bindings.put( "tag", tag );

				try{
					Object result = p.eval( script, bindings );

					return( result );

				}catch( Throwable e ){

					Debug.out( e );

					return( null );
				}
			}
		}

		if ( !provider_found ){

			if ( !js_plugin_install_tried ){

				js_plugin_install_tried = true;

				PluginUtils.installJavaScriptPlugin();
			}
		}

		return( null );
	}

	private void
	resolverInitialized(
		TaggableResolver		resolver )
	{
		TagTypeDownloadManual ttdm = new TagTypeDownloadManual( resolver );

		List<Tag> tags = new ArrayList<Tag>();

		synchronized( this ){

			Map config = getConfig();

			Map<String,Object> tt = (Map<String,Object>)config.get( String.valueOf( ttdm.getTagType()));

			if ( tt != null ){

				for ( Map.Entry<String,Object> entry: tt.entrySet()){

					String key = entry.getKey();

					try{
						if ( Character.isDigit( key.charAt(0))){

							int	tag_id 	= Integer.parseInt( key );
							Map m		= (Map)entry.getValue();

							tags.add( ttdm.createTag( tag_id, m ));
						}
					}catch( Throwable e ){

						Debug.out( e );
					}
				}
			}
		}

		for ( Tag tag: tags ){

			ttdm.addTag( tag );
		}
	}

	private void
	removeTaggable(
		TaggableResolver	resolver,
		Taggable			taggable )
	{
		for ( TagType	tt: tag_types ){

			TagTypeBase	ttb = (TagTypeBase)tt;

			ttb.removeTaggable( resolver, taggable );
		}
	}

	public void
	addTagType(
		TagTypeBase		tag_type )
	{
		if ( !enabled ){

			Debug.out( "Not enabled" );

			return;
		}

		synchronized( tag_type_map ){

			if ( tag_type_map.put( tag_type.getTagType(), tag_type) != null ){

				Debug.out( "Duplicate tag type!" );
			}
		}

		tag_types.add( tag_type );

		for ( TagManagerListener l : listeners ){

			try{
				l.tagTypeAdded(this, tag_type);

			}catch ( Throwable t ){

				Debug.out(t);
			}
		}
	}

	public TagType
	getTagType(
		int 	tag_type)
	{
		synchronized( tag_type_map ){

			return( tag_type_map.get( tag_type ));
		}
	}

	protected void
	removeTagType(
		TagTypeBase		tag_type )
	{
		synchronized( tag_type_map ){

			tag_type_map.remove( tag_type.getTagType());
		}

		tag_types.remove( tag_type );

		for ( TagManagerListener l : listeners ){

			try{
				l.tagTypeRemoved(this, tag_type);

			}catch( Throwable t ){

				Debug.out(t);
			}
		}

		removeConfig( tag_type );
	}

	public List<TagType>
	getTagTypes()
	{
		return((List<TagType>)(Object)tag_types.getList());
	}

	public void
	taggableAdded(
		TagType		tag_type,
		Tag			tag,
		Taggable	tagged )
	{
			// hack to support initial-save-location logic when a user manually assigns a tag and the download
			// hasn't had files allocated yet (most common scenario is user has 'add-torrent-stopped' set up)

		int tt = tag_type.getTagType();

		try{
			if ( tt == TagType.TT_DOWNLOAD_MANUAL && tagged instanceof DownloadManager ){

				TagFeatureFileLocation fl = (TagFeatureFileLocation)tag;

				if ( fl.supportsTagInitialSaveFolder()){

					File save_loc = fl.getTagInitialSaveFolder();

					if ( save_loc != null ){

						DownloadManager dm = (DownloadManager)tagged;

						if ( dm.getState() == DownloadManager.STATE_STOPPED ){

							TOTorrent torrent = dm.getTorrent();

							if ( torrent != null ){

									// This test detects whether or not we are in the process of adding the download
									// If we are then initial save-location stuff will be applied by the init-adapter
									// code above - we're only dealing later assignments here

								if ( dm.getGlobalManager().getDownloadManager( torrent.getHashWrapper()) != null ){

									File existing_save_loc = dm.getSaveLocation();

									if ( ! ( existing_save_loc.equals( save_loc ) || existing_save_loc.exists())){

										dm.setTorrentSaveDir( save_loc.getAbsolutePath());
									}
								}
							}
						}
					}
				}
			}
		}catch( Throwable e ){

			Debug.out(e );
		}

			// hack to limit tagged/untagged callbacks as the auto-dl-state ones generate a lot
			// of traffic and thusfar nobody's interested in it

		if ( tt == TagType.TT_DOWNLOAD_MANUAL ){

			synchronized( lifecycle_handlers ){

				long type = tagged.getTaggableType();

				LifecycleHandlerImpl handler = lifecycle_handlers.get( type );

				if ( handler == null ){

					handler = new LifecycleHandlerImpl();

					lifecycle_handlers.put( type, handler );
				}

				handler.taggableTagged( tag_type, tag, tagged );
			}
		}
	}

	public void
	taggableRemoved(
		TagType		tag_type,
		Tag			tag,
		Taggable	tagged )
	{
		int tt = tag_type.getTagType();

			// as above

		if ( tt == TagType.TT_DOWNLOAD_MANUAL ){

			synchronized( lifecycle_handlers ){

				long type = tagged.getTaggableType();

				LifecycleHandlerImpl handler = lifecycle_handlers.get( type );

				if ( handler == null ){

					handler = new LifecycleHandlerImpl();

					lifecycle_handlers.put( type, handler );
				}

				handler.taggableUntagged( tag_type, tag, tagged );
			}
		}
	}

	public List<Tag>
	getTagsForTaggable(
		Taggable	taggable )
	{
		Set<Tag>	result = new HashSet<Tag>();

		for ( TagType tt: tag_types ){

			result.addAll( tt.getTagsForTaggable( taggable ));
		}

		return( new ArrayList<Tag>( result ));
	}

	public List<Tag>
	getTagsForTaggable(
		int			tts,
		Taggable	taggable )
	{
		Set<Tag>	result = new HashSet<Tag>();

		for ( TagType tt: tag_types ){

			if ( tt.getTagType() == tts ){

				result.addAll( tt.getTagsForTaggable( taggable ));
			}
		}

		return( new ArrayList<Tag>( result ));
	}

	public Tag
	lookupTagByUID(
		long	tag_uid )
	{
		int	tag_type_id = (int)((tag_uid>>32)&0xffffffffL);

		TagType tt;

		synchronized( tag_type_map ){

			tt = tag_type_map.get( tag_type_id );
		}

		if ( tt != null ){

			int	tag_id = (int)(tag_uid&0xffffffffL);

			return( tt.getTag( tag_id ));
		}

		return( null );
	}

	public TaggableLifecycleHandler
	registerTaggableResolver(
		TaggableResolver	resolver )
	{
		if ( !enabled ){

			return(
				new TaggableLifecycleHandler()
				{
					public void
					initialized(
						List<Taggable>	initial_taggables )
					{
					}

					public void
					taggableCreated(
						Taggable	taggable )
					{
					}

					public void
					taggableDestroyed(
						Taggable	taggable )
					{
					}
				});
		}

		LifecycleHandlerImpl handler;

		long type = resolver.getResolverTaggableType();

		synchronized( lifecycle_handlers ){

			handler = lifecycle_handlers.get( type );

			if ( handler == null ){

				handler = new LifecycleHandlerImpl();

				lifecycle_handlers.put( type, handler );
			}

			handler.setResolver( resolver );
		}

		return( handler );
	}

	public void
	setTagPublicDefault(
		boolean	pub )
	{
		COConfigurationManager.setParameter( "tag.manager.pub.default", pub );
	}

	public boolean
	getTagPublicDefault()
	{
		return( COConfigurationManager.getBooleanParameter( "tag.manager.pub.default", true ));
	}

	public void
	addTagManagerListener(
		TagManagerListener		listener,
		boolean					fire_for_existing )
	{
		listeners.add( listener );

		if ( fire_for_existing ){

			for (TagType tt: tag_types ){

				listener.tagTypeAdded( this, tt );
			}
		}
	}

	public void
	removeTagManagerListener(
		TagManagerListener		listener )
	{
		listeners.remove( listener );
	}

	public void
	addTagFeatureListener(
		int						features,
		TagFeatureListener		listener )
	{
		feature_listeners.add( new Object[]{ features, listener });
	}

	public void
	removeTagFeatureListener(
		TagFeatureListener		listener )
	{
		for ( Object[] entry: feature_listeners ){

			if ( entry[1] == listener ){

				feature_listeners.remove( entry );
			}
		}
	}

	protected void
	featureChanged(
		Tag			tag,
		int			feature )
	{
		for ( Object[] entry: feature_listeners ){

			if ((((Integer)entry[0]) & feature ) != 0 ){

				try{
					((TagFeatureListener)entry[1]).tagFeatureChanged( tag, feature );

				}catch( Throwable e ){

					Debug.out( e );
				}
			}
		}
	}

	public void
	addTaggableLifecycleListener(
		long						taggable_type,
		TaggableLifecycleListener	listener )
	{
		synchronized( lifecycle_handlers ){

			LifecycleHandlerImpl handler = lifecycle_handlers.get( taggable_type );

			if ( handler == null ){

				handler = new LifecycleHandlerImpl();

				lifecycle_handlers.put( taggable_type, handler );
			}

			handler.addListener( listener );
		}
	}

	public void
	removeTaggableLifecycleListener(
		long						taggable_type,
		TaggableLifecycleListener	listener )
	{
		synchronized( lifecycle_handlers ){

			LifecycleHandlerImpl handler = lifecycle_handlers.get( taggable_type );

			if ( handler != null ){

				handler.removeListener( listener );
			}
		}
	}

	protected void
	tagCreated(
		TagWithState	tag )
	{
		addConfigUpdate( CU_TAG_CREATE, tag );
	}

	protected void
	tagChanged(
		TagWithState	tag )
	{
		addConfigUpdate( CU_TAG_CHANGE, tag );

	}

	protected void
	tagRemoved(
		TagWithState	tag )
	{
		addConfigUpdate( CU_TAG_REMOVE, tag );
	}

	protected void
	tagContentsChanged(
		TagWithState	tag )
	{
		addConfigUpdate( CU_TAG_CONTENTS, tag );
	}

	private void
	addConfigUpdate(
		int				type,
		TagWithState	tag )
	{
		if ( !tag.getTagType().isTagTypePersistent()){

			return;
		}

		if ( tag.isRemoved() && type != CU_TAG_REMOVE ){

			return;
		}

		synchronized( this ){

			config_change_queue.add( new Object[]{ type, tag });
		}

		setDirty();
	}

	private void
	applyConfigUpdates(
		Map			config )
	{
		Map<TagWithState,Integer>	updates = new HashMap<TagWithState, Integer>();

		for ( Object[] update: config_change_queue ){

			int				type	= (Integer)update[0];
			TagWithState	tag 	= (TagWithState)update[1];

			if ( tag.isRemoved()){

				type = CU_TAG_REMOVE;
			}

			Integer existing = updates.get( tag );

			if ( existing == null ){

				updates.put( tag, type );

			}else{

				if ( existing == CU_TAG_REMOVE ){

				}else if ( type > existing ){

					updates.put( tag, type );
				}
			}
		}

		for ( Map.Entry<TagWithState,Integer> entry: updates.entrySet()){

			TagWithState 	tag = entry.getKey();
			int				type	= entry.getValue();

			TagType	tag_type = tag.getTagType();

			String tt_key = String.valueOf( tag_type.getTagType());

			Map tt = (Map)config.get( tt_key );

			if ( tt == null ){

				if ( type == CU_TAG_REMOVE ){

					continue;
				}

				tt = new HashMap();

				config.put( tt_key, tt );
			}

			String t_key = String.valueOf( tag.getTagID());

			if ( type == CU_TAG_REMOVE ){

				tt.remove( t_key );

				continue;
			}

			Map t = (Map)tt.get( t_key );

			if ( t == null ){

				t = new HashMap();

				tt.put( t_key, t );
			}

			tag.exportDetails( t, type == CU_TAG_CONTENTS );
		}

		config_change_queue.clear();
	}

	private void
	destroy()
	{
		for ( TagType tt: tag_types ){

			((TagTypeBase)tt).closing();
		}

		writeConfig();
	}

	private void
	setDirty()
	{
		synchronized( this ){

			if ( !config_dirty ){

				config_dirty = true;

				dirty_dispatcher.dispatch();
			}
		}
	}

	private Map
	readConfig()
	{
		if ( !enabled ){

			Debug.out( "TagManager is disabled" );;

			return( new HashMap());
		}

		Map map;

		if ( FileUtil.resilientConfigFileExists( CONFIG_FILE )){

			map = FileUtil.readResilientConfigFile( CONFIG_FILE );

		}else{

			map = new HashMap();
		}

		return( map );
	}

	private Map
	getConfig()
	{
		synchronized( this ){

			if ( config != null ){

				return( config );
			}

			if ( config_ref != null ){

				config = config_ref.get();

				if ( config != null ){

					return( config );
				}
			}

			config = readConfig();

			return( config );
		}
	}

	private void
	writeConfig()
	{
		if ( !enabled ){

			Debug.out( "TagManager is disabled" );;
		}

		synchronized( this ){

			if ( !config_dirty ){

				return;
			}

			config_dirty = false;

			if ( config_change_queue.size() > 0 ){

				applyConfigUpdates( getConfig());
			}

			if ( config != null ){

				FileUtil.writeResilientConfigFile( CONFIG_FILE, config );

				config_ref = new WeakReference<Map>( config );

				config = null;
			}
		}
	}

	private Map
	getConf(
		TagTypeBase	tag_type,
		TagBase		tag,
		boolean		create )
	{
		Map m = getConfig();

		String tt_key = String.valueOf( tag_type.getTagType());

		Map tt = (Map)m.get( tt_key );

		if ( tt == null ){

			if ( create ){

				tt = new HashMap();

				m.put( tt_key, tt );

			}else{

				return( null );
			}
		}

		String t_key = String.valueOf( tag.getTagID());

		Map t = (Map)tt.get( t_key );

		if ( t == null ){

			if ( create ){

				t = new HashMap();

				tt.put( t_key, t );

			}else{

				return( null );
			}
		}

		Map conf = (Map)t.get( "c" );

		if ( conf == null && create ){

			conf = new HashMap();

			t.put( "c", conf );
		}

		return( conf );
	}

	protected Boolean
	readBooleanAttribute(
		TagTypeBase	tag_type,
		TagBase		tag,
		String		attr,
		Boolean		def )
	{
		Long result = readLongAttribute(tag_type, tag, attr, def==null?null:(def?1L:0L));

		if ( result == null ){

			return( null );
		}

		return( result == 1 );
	}

	protected boolean
	writeBooleanAttribute(
		TagTypeBase		tag_type,
		TagBase			tag,
		String			attr,
		Boolean			value )
	{
		return( writeLongAttribute( tag_type, tag, attr, value==null?null:(value?1L:0L )));
	}

	protected Long
	readLongAttribute(
		TagTypeBase	tag_type,
		TagBase		tag,
		String		attr,
		Long		def )
	{
		try{
			synchronized( this ){

				Map conf = getConf( tag_type, tag, false );

				if ( conf == null ){

					return( def );
				}

				Long value = (Long)conf.get( attr );

				if ( value == null ){

					return( def );
				}

				return( value );
			}
		}catch( Throwable e ){

			Debug.out( e );

			return( def );
		}
	}

	protected boolean
	writeLongAttribute(
		TagTypeBase		tag_type,
		TagBase			tag,
		String			attr,
		Long			value )
	{
		try{
			synchronized( this ){

				Map conf = getConf( tag_type, tag, true );

				if ( value == null ){

					if ( conf.containsKey( attr )){

						conf.remove( attr );

						setDirty();

						return( true );

					}else{

						return( false );
					}
				}else{

					long old = MapUtils.getMapLong( conf, attr, 0 );

					if ( old == value && conf.containsKey( attr )){

						return( false );
					}

					conf.put( attr, value );

					setDirty();

					return( true );
				}
			}
		}catch( Throwable e ){

			Debug.out( e );

			return( false );
		}
	}

	protected String
	readStringAttribute(
		TagTypeBase	tag_type,
		TagBase		tag,
		String		attr,
		String		def )
	{
		try{
			synchronized( this ){

				Map conf = getConf( tag_type, tag, false );

				if ( conf == null ){

					return( def );
				}

				return( MapUtils.getMapString( conf, attr, def ));
			}
		}catch( Throwable e ){

			Debug.out( e );

			return( def );
		}
	}

	protected void
	writeStringAttribute(
		TagTypeBase		tag_type,
		TagBase			tag,
		String			attr,
		String			value )
	{
		try{
			synchronized( this ){

				Map conf = getConf( tag_type, tag, true );

				String old = MapUtils.getMapString( conf, attr, null );

				if ( old == value ){

					return;

				}else if ( old != null && value != null && old.equals( value )){

					return;
				}

				MapUtils.setMapString( conf, attr, value );

				setDirty();
			}
		}catch( Throwable e ){

			Debug.out( e );
		}
	}

	protected String[]
	readStringListAttribute(
		TagTypeBase		tag_type,
		TagBase			tag,
		String			attr,
		String[]		def )
	{
		try{
			synchronized( this ){

				Map conf = getConf( tag_type, tag, false );

				if ( conf == null ){

					return( def );
				}

				List<String> vals = BDecoder.decodeStrings((List)conf.get( attr ));

				if ( vals == null ){

					return( def );
				}

				return( vals.toArray( new String[ vals.size()]));
			}
		}catch( Throwable e ){

			Debug.out( e );

			return( def );
		}
	}

	protected boolean
	writeStringListAttribute(
		TagTypeBase		tag_type,
		TagBase			tag,
		String			attr,
		String[]		value )
	{
		try{
			synchronized( this ){

				Map conf = getConf( tag_type, tag, true );

				List<String> old = BDecoder.decodeStrings((List)conf.get( attr ));

				if ( old == null && value == null ){

					return( false );

				}else if ( old != null && value != null ){

					if ( value.length == old.size()){

						boolean diff = false;

						for ( int i=0;i<value.length;i++){

							if ( !old.get(i).equals(value[i])){

								diff = true;

								break;
							}
						}

						if ( !diff ){

							return( false );
						}
					}
				}

				if ( value == null ){

					conf.remove( attr );
				}else{

					conf.put( attr, Arrays.asList( value ));
				}

				setDirty();

				return( true );
			}
		}catch( Throwable e ){

			Debug.out( e );

			return( false );
		}
	}

	protected void
	removeConfig(
		TagType	tag_type )
	{
		synchronized( this ){

			Map m = getConfig();

			String tt_key = String.valueOf( tag_type.getTagType());

			Map tt = (Map)m.remove( tt_key );

			if ( tt != null ){

				setDirty();
			}
		}
	}

	protected void
	removeConfig(
		Tag	tag )
	{
		TagType	tag_type = tag.getTagType();

		synchronized( this ){

			Map m = getConfig();

			String tt_key = String.valueOf( tag_type.getTagType());

			Map tt = (Map)m.get( tt_key );

			if ( tt == null ){

				return;
			}

			String t_key = String.valueOf( tag.getTagID());

			Map t = (Map)tt.remove( t_key );

			if ( t != null ){

				setDirty();
			}
		}
	}

	private class
	LifecycleHandlerImpl
		implements TaggableLifecycleHandler
	{
		private TaggableResolver		resolver;
		private boolean					initialised;

		private CopyOnWriteList<TaggableLifecycleListener>	listeners = new CopyOnWriteList<TaggableLifecycleListener>();

		private
		LifecycleHandlerImpl()
		{
		}

		private void
		setResolver(
			TaggableResolver	_resolver )
		{
			resolver = _resolver;
		}

		private void
		addListener(
			final TaggableLifecycleListener	listener )
		{
			synchronized( this ){

				listeners.add( listener );

				if ( initialised ){

					final List<Taggable> taggables = resolver.getResolvedTaggables();

					if ( taggables.size() > 0 ){

						async_dispatcher.dispatch(
							new AERunnable()
							{
								@Override
								public void
								runSupport()
								{
									listener.initialised( taggables );
								}
							});
					}
				}
			}
		}

		private void
		removeListener(
			TaggableLifecycleListener	listener )
		{
			synchronized( this ){

				listeners.remove( listener );
			}
		}

		public void
		initialized(
			final	List<Taggable>	initial_taggables )
		{
			resolverInitialized( resolver );

			synchronized( this ){

				initialised = true;

				if ( listeners.size() > 0 ){

					final List<TaggableLifecycleListener> listeners_ref = listeners.getList();

					async_dispatcher.dispatch(
						new AERunnable()
						{
							@Override
							public void
							runSupport()
							{
								for ( TaggableLifecycleListener listener: listeners_ref ){

									listener.initialised( initial_taggables );
								}
							}
						});
				}
			}
		}

		public void
		taggableCreated(
			final Taggable	t )
		{
			synchronized( this ){

				if ( initialised ){

					final List<TaggableLifecycleListener> listeners_ref = listeners.getList();

					async_dispatcher.dispatch(
						new AERunnable()
						{
							@Override
							public void
							runSupport()
							{
								for ( TaggableLifecycleListener listener: listeners_ref ){

									try{
										listener.taggableCreated( t );

									}catch( Throwable e ){

										Debug.out( e );
									}
								}
							}
						});
				}
			}
		}

		public void
		taggableDestroyed(
			final Taggable	t )
		{
			removeTaggable( resolver, t );

			synchronized( this ){

				if ( initialised ){

					final List<TaggableLifecycleListener> listeners_ref = listeners.getList();

					async_dispatcher.dispatch(
						new AERunnable()
						{
							@Override
							public void
							runSupport()
							{
								for ( TaggableLifecycleListener listener: listeners_ref ){

									try{
										listener.taggableDestroyed( t );

									}catch( Throwable e ){

										Debug.out( e );
									}
								}
							}
						});
				}
			}
		}

		public void
		taggableTagged(
			final TagType	tag_type,
			final Tag		tag,
			final Taggable	taggable )
		{
			synchronized( this ){

				if ( initialised ){

					final List<TaggableLifecycleListener> listeners_ref = listeners.getList();

					async_dispatcher.dispatch(
						new AERunnable()
						{
							@Override
							public void
							runSupport()
							{
								for ( TaggableLifecycleListener listener: listeners_ref ){

									try{
										listener.taggableTagged( tag_type, tag, taggable);

									}catch( Throwable e ){

										Debug.out( e );
									}
								}
							}
						});
				}
			}
		}

		public void
		taggableUntagged(
			final TagType	tag_type,
			final Tag		tag,
			final Taggable	taggable )
		{
			synchronized( this ){

				if ( initialised ){

					final List<TaggableLifecycleListener> listeners_ref = listeners.getList();

					async_dispatcher.dispatch(
						new AERunnable()
						{
							@Override
							public void
							runSupport()
							{
								for ( TaggableLifecycleListener listener: listeners_ref ){

									try{
										listener.taggableUntagged( tag_type, tag, taggable );

									}catch( Throwable e ){

										Debug.out( e );
									}
								}
							}
						});
				}
			}
		}
	}

	public void
	generate(
		IndentWriter		writer )
	{
		writer.println( "Tag Manager" );

		try{
			writer.indent();

			for ( TagTypeBase tag_type: tag_types ){

				tag_type.generate( writer );
			}
		}finally{

			writer.exdent();
		}
	}

	public void
	generate(
		IndentWriter		writer,
		TagTypeBase			tag_type )
	{
	}

	public void
	generate(
		IndentWriter		writer,
		TagTypeBase			tag_type,
		TagBase				tag )
	{
		synchronized( this ){

			Map conf = getConf( tag_type, tag, false );

			if ( conf != null ){

				conf = BDecoder.decodeStrings( BEncoder.cloneMap( conf ));

				writer.println( BEncoder.encodeToJSON( conf ));
			}
		}
	}

}
