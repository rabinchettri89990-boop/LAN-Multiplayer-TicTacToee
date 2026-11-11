import java.net.*;
import java.io.*;
import java.util.function.Consumer;

public class GameClient {

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    public void connect(String ip, String playerName,
                        Consumer<String> callback) {

        new Thread(() -> {

            try {

                socket = new Socket(ip, 5000);

                output = new PrintWriter(
                        socket.getOutputStream(), true);

                input = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));

                callback.accept("Connected successfully!");

                output.println("NAME," + playerName);

                String message;

                while ((message = input.readLine()) != null) {

                    callback.accept("Server: " + message);
                }

            } catch (IOException e) {

                callback.accept(
                        "Connection failed: " + e.getMessage());
            }

        }).start();
    }

    public void sendMessage(String message) {

        if (output != null) {
            output.println(message);
        }
    }

    public void disconnect() {

        try {
            if (socket != null) socket.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}