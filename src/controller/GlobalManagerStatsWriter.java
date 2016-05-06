/*
 * Created on 12-Jul-2004
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

package controller;

import torrentlib.stats.StatsWriterPeriodic;
import torrentlib.stats.StatsWriterFactory;
import controller.GlobalManagerStats;
import torrentlib.stats.StatsFactory;

import torrentlib.AzureusCoreLifecycleAdapter;
import torrentlib.TorrentEngineCore;

/**
 * @author parg
 *
 */
public class 
GlobalManagerStatsWriter
{
	protected StatsWriterPeriodic	stats_writer;
	
	protected
	GlobalManagerStatsWriter(
		TorrentEngineCore				core,
		GlobalManagerStats		stats )
	{
	    StatsFactory.initialize( core, stats );
		
	    stats_writer = StatsWriterFactory.createPeriodicDumper( core );
	    
	    core.addLifecycleListener(new AzureusCoreLifecycleAdapter()
	    	{
	    		public void
	    		started(
	    			TorrentEngineCore		core )
	    		{
	    			stats_writer.start();
	    			
	    			core.removeLifecycleListener( this );
	    		}
	    	});
	}
	
	
	protected void
	destroy()
	{
		if ( stats_writer != null ){
	     
			stats_writer.stop();
		}
	}
}
