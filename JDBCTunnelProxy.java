import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class JDBCTunnelProxy {

    public static void main(String[] args) {
        int proxyPort = 9090;
        String databaseServer = "localhost";
        int databasePort = 3306;

        try (ServerSocket serverSocket = new ServerSocket(proxyPort)) {
            System.out.println("JDBC Proxy Server started on port " + proxyPort);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                Socket databaseSocket = new Socket(databaseServer, databasePort);

                Thread clientToDatabase = new Thread(new ProxyThread(clientSocket, databaseSocket));
                Thread databaseToClient = new Thread(new ProxyThread(databaseSocket, clientSocket));

                clientToDatabase.start();
                databaseToClient.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ProxyThread implements Runnable {
        private final Socket sourceSocket;
        private final Socket destinationSocket;

        public ProxyThread(Socket sourceSocket, Socket destinationSocket) {
            this.sourceSocket = sourceSocket;
            this.destinationSocket = destinationSocket;
        }

        @Override
        public void run() {
            try {
                InputStream sourceInput = sourceSocket.getInputStream();
                OutputStream destinationOutput = destinationSocket.getOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = sourceInput.read(buffer)) != -1) {
                    destinationOutput.write(buffer, 0, bytesRead);
                    destinationOutput.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    sourceSocket.close();
                    destinationSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}