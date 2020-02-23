package info.pluggabletransports.dispatch.transports;

import android.content.Context;
import android.util.Log;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.HashMap;
import java.io.File;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.net.UnknownHostException;

import com.runjva.sourceforge.jsocks.protocol.Socks5Proxy;
import com.runjva.sourceforge.jsocks.protocol.SocksException;
import com.runjva.sourceforge.jsocks.protocol.SocksSocket;
import com.runjva.sourceforge.jsocks.protocol.UserPasswordAuthentication;

import info.pluggabletransports.dispatch.Connection;
import info.pluggabletransports.dispatch.Dispatcher;
import info.pluggabletransports.dispatch.Listener;
import info.pluggabletransports.dispatch.Transport;
import info.pluggabletransports.dispatch.util.ResourceInstaller;
import info.pluggabletransports.dispatch.util.TransportListener;
import info.pluggabletransports.dispatch.util.TransportManager;

import static info.pluggabletransports.dispatch.DispatchConstants.PT_TRANSPORTS_STEGOTORUS;
import static info.pluggabletransports.dispatch.DispatchConstants.TAG;

public class StegotorusTransport implements Transport {

    private final static String ASSET_KEY = "stegotorus";
    private final static String ASSET_CONF = "chop-nosteg-client.yaml";
    private TransportManager mTransportManager;

    private int mLocalSocksPort = -1;
    private int mLocalPort = -1;

    private File mTransportConfigFile = null;

    @Override
    public void register() {
        Dispatcher.get().register(PT_TRANSPORTS_STEGOTORUS, getClass());
    }

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
                    String localPort = "4999";
                    mLocalPort = 4999;

                    StringBuffer cmd = new StringBuffer();
                    cmd.append(mFileTransport.getCanonicalPath()).append(' ');
                    cmd.append("--config-file=").append(mTransportConfigFile);
                    // cmd.append("-p ").append(serverPort).append(' ');
                    // cmd.append("-k ").append(serverPassword).append(' ');
                    // cmd.append("-m ").append(serverCipher).append(' ');
                    // cmd.append("-b ").append(localAddress).append(' ');
                    // cmd.append("-l ").append(localPort).append(' ');

                    HashMap<String, String> env = new HashMap<>(); //we don't have environmental variable
                    //we just need it to be able to call exec

                    exec(cmd.toString(), false, env, listener);

                } catch (Exception ioe) {
                    debug("Couldn't initiate transport: " + ioe);
                }

            }
        };

        mTransportManager.installTransport(context, ASSET_KEY);
        installTransportConfiguration(context, ASSET_CONF);
    };


    public File installTransportConfiguration(Context context, String assetKey) {
        ResourceInstaller configInstaller = new ResourceInstaller(context, context.getFilesDir());
        try {
             configInstaller.installConfig(assetKey, true);
             mTransportConfigFile = new File(context.getFilesDir(), assetKey);
        } catch (Exception ioe) {
            Log.d(TAG, "Couldn't install transport: " + ioe);
        }

        return mTransportConfigFile;
    }

    // @Override
    // public Connection connect(String addr) {

    //     mTransportManager.startTransport(new TransportListener() {
    //         @Override
    //         public void transportStarted(int localPort) {
    //             mLocalSocksPort = localPort;
    //         }

    //         @Override
    //         public void transportFailed(String err) {
    //             Log.d(TAG, "error starting transport: " + err);
    //         }
    //     });

    //     return null;
    // }

    // @Override
    // public Listener listen(String addr) {
    //     return null;
    // }

    public void startTransport() {

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

    }

    @Override
    public Connection connect(String addr) {

        startTransport();

        if (addr == "")
            return null;

        while (!isPortOpen("127.0.0.1", mLocalPort, 5*1000))
        {
             try { Thread.sleep(500);}catch(Exception e){}
        }

        try {
            return new StegotorusConnection(addr, InetAddress.getLocalHost(), mLocalSocksPort);
        } catch (IOException e) {
            Log.e(getClass().getName(),"Error making connection",e);
            return null;
        }
    }

    @Override
    public Listener listen(String addr) {
        return null;
    }


    public static boolean isPortOpen(final String ip, final int port, final int timeout) {

      try {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), timeout);
        socket.close();
        return true;
      } 

      catch(ConnectException ce){
        ce.printStackTrace();
        return false;
      }

      catch (Exception ex) {
        ex.printStackTrace();
        return false;
      }
    }     
    class StegotorusConnection implements Connection {

        private InetAddress mLocalAddress;
        private int mLocalPort;
        private String mRemoteAddress;
        private int mRemotePort;

        private InputStream mInputStream;
        private OutputStream mOutputStream;

        public StegotorusConnection(String bridgeAddr, InetAddress localSocks, int port) throws IOException {

            String[] addressparts = bridgeAddr.split(":");
            mRemoteAddress = addressparts[0];
            mRemotePort = Integer.parseInt(addressparts[1]);
            mLocalAddress = localSocks;
            mLocalPort = port;

        }

        private void initBridgeViaSocks() throws IOException {
            Socket s = getSocket(mRemoteAddress, mRemotePort);

            mInputStream = new BufferedInputStream(s.getInputStream());
            mOutputStream = new BufferedOutputStream(s.getOutputStream());

        }

        /**
         * Read from socks socket
         *
         * @param b
         * @param offset
         * @param length
         * @return
         * @throws IOException
         */
        @Override
        public int read(byte[] b, int offset, int length) throws IOException {
            if (mInputStream == null)
                initBridgeViaSocks();

            return mInputStream.read(b,offset,length);
        }

        /**
         * Write to socks socket
         *
         * @param b
         * @throws IOException
         */
        @Override
        public void write(byte[] b) throws IOException {

            if (mOutputStream == null)
                initBridgeViaSocks();

            mOutputStream.write(b);
            mOutputStream.flush();
        }

        /**
         * Close socks socket
         */
        @Override
        public void close() {

            if (mOutputStream != null && mInputStream != null) {
                try {
                    mOutputStream.close();
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public InetAddress getLocalAddress() {
            return mLocalAddress;
        }

        @Override
        public int getLocalPort() {
            return mLocalPort;
        }

        @Override
        public InetAddress getRemoteAddress() {
            try {
                return InetAddress.getByName(mRemoteAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getRemotePort() {
            return mRemotePort;
        }

        @Override
        public void setDeadline(Date deadlineTime) {

        }

        @Override
        public void setReadDeadline(Date deadlineTime) {

        }

        @Override
        public void setWriteDeadline(Date deadlineTime) {

        }

        @Override
        public String getProxyUsername ()
        {
            return null;
        }

        @Override
        public String getProxyPassword ()
        {
          return null;
        }

        @Override
        public Socket getSocket (String remoteAddress, int remotePort) throws SocksException, UnknownHostException {

          //   Socks5Proxy proxy = new Socks5Proxy(mLocalAddress,mLocalPort);

          //   UserPasswordAuthentication auth = new UserPasswordAuthentication(getProxyUsername(),getProxyPassword());
          // //  proxy.setAuthenticationMethod(0,null);
          //   proxy.setAuthenticationMethod(UserPasswordAuthentication.METHOD_ID, auth);

          //SocksSocket s = new SocksSocket(proxy, remoteAddress, remotePort);
          try {
            Socket socket = new Socket(remoteAddress, remotePort);
            
            return socket;
          } catch (Exception ex) {
              ex.printStackTrace();
              return null;
          }

        }
    }

}
