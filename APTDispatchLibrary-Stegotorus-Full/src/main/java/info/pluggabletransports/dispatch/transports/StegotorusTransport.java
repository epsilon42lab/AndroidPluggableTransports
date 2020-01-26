package info.pluggabletransports.dispatch.transports;

import android.content.Context;

import java.util.Properties;

import info.pluggabletransports.dispatch.Connection;
import info.pluggabletransports.dispatch.Dispatcher;
import info.pluggabletransports.dispatch.Listener;
import info.pluggabletransports.dispatch.Transport;
import info.pluggabletransports.dispatch.util.TransportManager;

import static info.pluggabletransports.dispatch.DispatchConstants.PT_TRANSPORTS_SHADOWSOCKS;

public class StegotorusTransport implements Transport {

    private final static String ASSET_KEY = "stegotorus";
    private final static String ASSET_CONF = "chop-nosteg-client.yaml";
    private TransportManager mTransportManager;

    @Override
    public void register() {
        Dispatcher.get().register(PT_TRANSPORTS_SHADOWSOCKS, getClass());
    }

    @Override
    public void init(Context context, Properties options) {

        mTransportManager = new TransportManager() {
            public  void startTransportSync ()
            {
                try {

                        // String serverAddress = "172.104.48.102";
                        // String serverPort = "443";
                        // String serverPassword = "zomzom123";
                        // String serverCipher = "aes-128-cfb";
                        // String localAddress = "127.0.0.1";
                        // String localPort = "31059";
                  
                        String serverAddress = "127.0.0.1";
                        String serverPort = "5000";
                        String serverPassword = "zomzom123";
                        String serverCipher = "aes-128-cfb";
                        String localAddress = "127.0.0.1";
                        String localPort = "4449";

                        StringBuffer cmd = new StringBuffer();
                        cmd.append(mFileTransport.getCanonicalPath()).append(' ');
                        cmd.append("--config-file=").append(mFileTransport.getParent()).append("/chop-nosteg-rr-client.yaml").append(' ');
                        // cmd.append("-p ").append(serverPort).append(' ');
                        // cmd.append("-k ").append(serverPassword).append(' ');
                        // cmd.append("-m ").append(serverCipher).append(' ');
                        // cmd.append("-b ").append(localAddress).append(' ');
                        // cmd.append("-l ").append(localPort).append(' ');

                        exec(cmd.toString(), false);

                }
                catch (Exception ioe)
                {
                    debug("Couldn't install transport: " + ioe);
                }
            }

        };

        mTransportManager.installTransport(context, ASSET_KEY);
    }

    @Override
    public Connection connect(String addr) {

        mTransportManager.startTransport();

        return null;
    }

    @Override
    public Listener listen(String addr) {
        return null;
    }
}
