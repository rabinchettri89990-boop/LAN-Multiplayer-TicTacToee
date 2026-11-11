import java.net.*;
import java.io.*;
import java.util.function.Consumer;

public class GameServer {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;

    public void startServer(String hostName, Consumer<String> callback) {

        new Thread(() -> {

            try {
                serverSocket = new ServerSocket(5000);

                callback.accept("Server started. Port: 5000");
                callback.accept("Waiting for opponent...");

                clientSocket = serverSocket.accept();

                output = new PrintWriter(
                        clientSocket.getOutputStream(), true);

                input = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));

                output.println("NAME," + hostName);

                callback.accept("Opponent connected!");

                String message;

                while ((message = input.readLine()) != null) {

                    callback.accept("Opponent: " + message);
                }

            } catch (IOException e) {

                callback.accept("Server error: " + e.getMessage());
            }

        }).start();
    }

    public void sendMessage(String message) {

        if (output != null) {
            output.println(message);
        }
    }

    public void stopServer() {

        try {
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}