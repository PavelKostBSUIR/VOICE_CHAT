import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client {
    Queue<Message> messages = new ArrayDeque<>();
    private DatagramSocket inputSocket;
    VoiceSession voiceSession;
    private OutputHandler outputHandler;
    private InputHandler inputHandler;
    private AuthorizationGUIController authorizationGUIController;
    private CreateNewAccGUIController createNewAccGUIController;
    private ClientGUIController clientGUIController;
    private CallToGUIController callToGUIController;
    private CallFromGUIController callFromGUIController;
    private HashMap<String, String> usersWithStates = new HashMap<>();

    public void setUsersWithStates(HashMap<String, String> usersWithStates) {
        this.usersWithStates = usersWithStates;
    }

    public HashMap<String, String> getUsersWithStates() {
        return usersWithStates;
    }

    public ClientGUIController getClientGUIController() {
        return clientGUIController;
    }

    public void setCallFromGUIController(CallFromGUIController callFromGUIController) {
        this.callFromGUIController = callFromGUIController;
    }

    public void setCallToGUIController(CallToGUIController callToGUIController) {
        this.callToGUIController = callToGUIController;
    }

    public void setClientGUIController(ClientGUIController clientGUIController) {
        this.clientGUIController = clientGUIController;
    }

    public void setCreateNewAccGUIController(CreateNewAccGUIController createNewAccGUIController) {
        this.createNewAccGUIController = createNewAccGUIController;
    }

    public void setAuthorizationGUIController(AuthorizationGUIController authorizationGUIController) {
        this.authorizationGUIController = authorizationGUIController;
    }

    public void kill() {
        if (voiceSession != null) {
            voiceSession.kill();
        }
        outputHandler.kill();
        inputHandler.kill();
        outputHandler.wakeUp();
    }

    Client(String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        outputHandler = new OutputHandler(socket.getOutputStream(), messages);
        inputHandler = new InputHandler(socket.getInputStream(), new ClientInpHandlerMethod(this));
    }

    public void handleLogOutRequest() {

        messages.add(new Message(Method.LOG_OUT, Status.OK, null));
        outputHandler.wakeUp();
    }

    public void handleLogOutResponse(Message message) {
        if (clientGUIController != null) {
            clientGUIController.handleLogOutResponse(message);
        }
    }

    public void setInputSocket(DatagramSocket inputSocket) {
        this.inputSocket = inputSocket;
    }

    public DatagramSocket getInputSocket() {
        return inputSocket;
    }

    public void handleGetUsersRequest() {
        messages.add(new Message(Method.GET_USERS, Status.OK, null));
        outputHandler.wakeUp();
    }

    public void handleGetUsersResponse(Message response) {
        clientGUIController.handleUpdateUsersResponse(response);

    }

    public void handleCreateAccountRequest(String login, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("login", login);
        params.put("password", password);
        messages.add(new Message(Method.NEW_ACC, Status.OK, params));
        outputHandler.wakeUp();
    }

    public void handleCreateAccountResponse(Message response) {
        if (createNewAccGUIController != null) {
            createNewAccGUIController.handleCreateAccountResponse(response);
        }
    }

    public void handleAuthorizeRequest(String login, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("login", login);
        params.put("password", password);
        messages.add(new Message(Method.AUTHORIZE, Status.OK, params));
        outputHandler.wakeUp();
    }

    public void handleAuthorizeResponse(Message response) {
        if (authorizationGUIController != null) {
            authorizationGUIController.handleAuthorizationResponse(response);
        }
    }

    public void handleCallRequest(String login) throws SocketException, UnknownHostException {
        HashMap<String, String> params = new HashMap<>();
        params.put("login", login);
        int port;
        while (true) {
            try {

                port = (int) (Math.random() * 1000 + 1);
                inputSocket = new DatagramSocket(port);
                break;
            } catch (SocketException e) {
            }

        }
        params.put("ip", InetAddress.getLocalHost().getHostAddress());
        params.put("port", String.valueOf(port));

        messages.add(new Message(Method.CALL, Status.OK, params));
        //  playMusic("skype.wav");
        outputHandler.wakeUp();
    }

    public void handleCallResponse(Message message) {
        if (message.status == Status.OK) {
            String ip = message.params.get("ip");

            int port = Integer.parseInt(message.params.get("port"));
            if (clientGUIController != null) {
                try {
                    voiceSession = new VoiceSession(port, InetAddress.getByName(ip), inputSocket);

                    voiceSession.start();
                    clientGUIController.handleCallToResponse(message);
                } catch (IOException | LineUnavailableException e) {
                    e.printStackTrace();
                }
            }

        } else if (message.status == Status.REJECTED) {
            clientGUIController.setMessageTextAreaText("Вызов отклонен");
            callToGUIController.cancel();
            inputSocket.close();
            setCallToGUIController(null);
        }
    }

    private void playMusic(String file) {
        Clip clipSound = null;
        try {
            File f = new File(file);
            AudioFileFormat aff = AudioSystem.getAudioFileFormat(f);
            AudioFormat af = aff.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, af);
            if (AudioSystem.isLineSupported(info)) {
                clipSound = (Clip) AudioSystem.getLine(info);
                AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                clipSound.open(ais);
                clipSound.start();
                clipSound.loop(2);
                //clipSound.stop();
                // clipSound.close();
            } else {
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    public void handleInputCallRequest(Message message) {
        //playMusic("skype.wav");
        clientGUIController.handleInputCallRequest(message);
    }

    public void handleInputCallResponse(Message message) {
        if (message.status == Status.OK) {
            try {
                int port;
                while (true) {
                    try {

                        port = (int) (Math.random() * 1000 + 1);
                        inputSocket = new DatagramSocket(port);
                        break;
                    } catch (SocketException e) {
                        System.out.println("Error");
                    }

                }
                int clPort = Integer.parseInt(message.params.get("port"));
                voiceSession = new VoiceSession(clPort, InetAddress.getByName(message.params.get("ip")), inputSocket);
                voiceSession.start();
                HashMap<String, String> params = message.params;
                params.put("port", String.valueOf(port));
                params.put("ip", InetAddress.getLocalHost().getHostAddress());
                messages.add(message);
                outputHandler.wakeUp();
            } catch (IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        } else if (message.status == Status.REJECTED) {
            messages.add(message);
            outputHandler.wakeUp();
        }
    }

    public void handleCallEndingRequest(String login) {
        HashMap<String, String> params = new HashMap<>();
        params.put("login", login);
        messages.add(new Message(Method.CALL_END, Status.OK, params));
        outputHandler.wakeUp();
    }

    public void handleCallEndingResponse(Message message) {
        if (callToGUIController != null)
            callToGUIController.handleCallEndingResponse(message);
    }
}