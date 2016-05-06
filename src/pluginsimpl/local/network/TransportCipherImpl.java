package pluginsimpl.local.network;

import plugins.network.TransportCipher;

public class TransportCipherImpl implements TransportCipher {
	controller.networkmanager.TransportCipher cipher;
	public TransportCipherImpl(controller.networkmanager.TransportCipher cipher) {
		this.cipher = cipher;
	}
}
