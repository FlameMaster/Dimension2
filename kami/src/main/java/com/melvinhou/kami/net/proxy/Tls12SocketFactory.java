package com.melvinhou.kami.net.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/13 0013 14:10
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class Tls12SocketFactory extends SSLSocketFactory{

    private SSLContext context;
    private SSLSocketFactory internalSSLSocketFactory;

    Tls12SocketFactory(KeyManager[] km, TrustManager[] tm, SecureRandom sr){
        try {
            context = SSLContext.getInstance("TLSv1.2");
            context.init(km, tm, sr);
            internalSSLSocketFactory = context.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        SSLSocket sslSocket = (SSLSocket) context.getSocketFactory().createSocket(socket, host, port, autoClose);
        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        SSLSocket sslSocket = (SSLSocket) context.getSocketFactory().createSocket( host, port);
        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        SSLSocket sslSocket = (SSLSocket) context.getSocketFactory().createSocket(host, port, localHost, localPort);
        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        return sslSocket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocket sslSocket = (SSLSocket) context.getSocketFactory().createSocket(host, port);
        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        return sslSocket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        SSLSocket sslSocket = (SSLSocket) context.getSocketFactory().createSocket(address, port, localAddress, localPort);
        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        return sslSocket;
    }
}
