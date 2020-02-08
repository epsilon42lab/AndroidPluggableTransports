package info.pluggabletransports.dispatch.transports;

import android.content.Context;
import android.util.Log;

import java.util.Properties;
import java.util.HashMap;

import info.pluggabletransports.dispatch.Connection;
import info.pluggabletransports.dispatch.Dispatcher;
import info.pluggabletransports.dispatch.Listener;
import info.pluggabletransports.dispatch.Transport;
import info.pluggabletransports.dispatch.util.TransportListener;
import info.pluggabletransports.dispatch.util.TransportManager;

//import static info.pluggabletransports.dispatch.DispatchConstants.PT_TRANSPORTS_STEGOTORUS;
import static info.pluggabletransports.dispatch.DispatchConstants.TAG;

public class StegotorusTransport implements Transport {

    private final static String ASSET_KEY = "stegotorus";
    private final static String ASSET_CONF = "chop-nosteg-client.yaml";
    private TransportManager mTransportManager;

  private int mLocalSocksPort = -1;

    @Override
    public void register() {
        Dispatcher.get().register("stegotorus", getClass());
    } //PT_TRANSPORTS_STEGOTORUS

    @Override
    public void init(Context context, Properties options) {

        mTransportManager = new TransportManager() {
            public void startTransportSync(TransportListener listener) {
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

                    HashMap<String, String> env = new HashMap<>(); //we don't have environmental variable
                    //we just need it to be able to call exec
                    
                    exec(cmd.toString(), false, env, listener);

                } catch (Exception ioe) {
                    debug("Couldn't install transport: " + ioe);
                }
            }

        };

        mTransportManager.installTransport(context, ASSET_KEY);
    }

    @Override
    public Connection connect(String addr) {

        mTransportManager.startTransport(new TransportListener() {
            @Override
            public void transportStarted(int localPort) {
                mLocalSocksPort = localPort;
            }

            @Override
            public void transportFailed(String err) {
                Log.d(TAG,"error starting transport: " + err);
            }
        });

        return null;
    }

    @Override
    public Listener listen(String addr) {
        return null;
    }
}
