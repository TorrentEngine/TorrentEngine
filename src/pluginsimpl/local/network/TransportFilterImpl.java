package pluginsimpl.local.network;

import controller.networkmanager.TransportHelperFilter;
import plugins.network.TransportFilter;

public class TransportFilterImpl implements TransportFilter {
	public TransportHelperFilter filter;
	public TransportFilterImpl(TransportHelperFilter filter) {
		this.filter = filter;
	}
}
