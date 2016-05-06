/**
* Created on Apr 17, 2007
* Created by Alan Snyder
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


package controller.networkmanager.admin;

import controller.networkmanager.admin.NetworkAdminException;
import controller.networkmanager.admin.NetworkAdminSpeedTestScheduler;
import controller.networkmanager.admin.NetworkAdminSpeedTestScheduledTestListener;
import controller.networkmanager.admin.NetworkAdminSpeedTesterResult;
import controller.networkmanager.admin.NetworkAdminSpeedTestScheduledTest;
import torrentlib.Debug;
import torrentlib.util.platform.win32.PluginInterface;
import pluginsimpl.local.PluginInitializer;



public class NetworkAdminSpeedTestSchedulerImpl
        implements NetworkAdminSpeedTestScheduler
{
    private static NetworkAdminSpeedTestSchedulerImpl instance = null;
    private NetworkAdminSpeedTestScheduledTestImpl currentTest = null;

     public static synchronized NetworkAdminSpeedTestScheduler getInstance(){
        if(instance==null){
            instance = new NetworkAdminSpeedTestSchedulerImpl();
        }
        return instance;
    }

    private 
    NetworkAdminSpeedTestSchedulerImpl()
    {
    	NetworkAdminSpeedTesterBTImpl.initialise();
    }

    public void
    initialise()
    {
    	NetworkAdminSpeedTesterBTImpl.startUp();
    }
    
    public synchronized NetworkAdminSpeedTestScheduledTest
    getCurrentTest()
    {
    	return( currentTest );
    }
    
    public synchronized NetworkAdminSpeedTestScheduledTest 
    scheduleTest(int type)
    	throws NetworkAdminException
    {
    	if ( currentTest != null ){

    		throw( new NetworkAdminException( "Test already scheduled" ));
    	}

    	if ( type == TEST_TYPE_BT ){

        PluginInterface plugin = PluginInitializer.getDefaultInterface(); 

    		currentTest = new NetworkAdminSpeedTestScheduledTestImpl(plugin, new NetworkAdminSpeedTesterBTImpl(plugin) );
            currentTest.getTester().setMode(type);

            currentTest.addListener(
    			new NetworkAdminSpeedTestScheduledTestListener()
    			{
    				public void stage(NetworkAdminSpeedTestScheduledTest test, String step){}

    				public void 
    				complete(NetworkAdminSpeedTestScheduledTest test )
    				{
    					synchronized( NetworkAdminSpeedTestSchedulerImpl.this ){
    						
    						currentTest = null;
    					}
    				}
  				
    			});
    	}else{

    		throw( new NetworkAdminException( "Unknown test type" ));
    	}

    	return( currentTest );
    }
    
    /**
     * Get the most recent result for the test.
     *
     * @return - Result
     */
    public NetworkAdminSpeedTesterResult getLastResult(int type) {
    	
        if ( type == TEST_TYPE_BT ){
        	
        	return( NetworkAdminSpeedTesterBTImpl.getLastResult());
        	
        }else{
        	
        	Debug.out( "Unknown test type" );
        	
        	return( null );
        }
    }


}
