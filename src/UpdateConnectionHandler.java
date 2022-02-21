import com.mysql.cj.exceptions.StreamingNotifiable;

import java.util.*;

public class UpdateConnectionHandler extends Thread {
    class UpdConTask extends TimerTask {
        private UpdateConnectionHandler updateConnectionHandler;

        public void setUpdateConnectionHandler(UpdateConnectionHandler updateConnectionHandler) {
            this.updateConnectionHandler = updateConnectionHandler;
        }

        @Override
        public void run() {
            client.handleGetUsersRequest();
            HashMap<String, String> users = updateConnectionHandler.client.getUsersWithStates();
            if (users.get(login) != null) {
                if (users.get(login).equals("offline")) {
                    client.getClientGUIController().setMessageTextAreaText("Собеседник отключился");
                    if (client.getInputSocket() != null) {
                        client.getInputSocket().close();
                    }
                    callToGUIController.cancel();
                    updateConnectionHandler.kill();
                }
            }
        }
    }

    class UpdConTaskFrom extends TimerTask {
        private UpdateConnectionHandler updateConnectionHandler;

        public void setUpdateConnectionHandler(UpdateConnectionHandler updateConnectionHandler) {
            this.updateConnectionHandler = updateConnectionHandler;
        }

        @Override
        public void run() {
            client.handleGetUsersRequest();
            HashMap<String, String> users = updateConnectionHandler.client.getUsersWithStates();
            if (users.get(login) != null) {
                if (users.get(login).equals("offline")) {
                    client.getClientGUIController().setMessageTextAreaText("Вызывающий абонент отключился");
                    if (client.getInputSocket() != null) {
                        client.getInputSocket().close();
                    }
                    callFromGUIController.cancel();
                    updateConnectionHandler.kill();
                }
            }
        }
    }

    private CallFromGUIController callFromGUIController;
    private CallToGUIController callToGUIController;
    private Client client;
    private String login;

    UpdateConnectionHandler(String login, Client client, CallToGUIController callToGUIController) {
        this.callToGUIController = callToGUIController;
        this.login = login;
        this.client = client;
    }

    UpdateConnectionHandler(String login, Client client, CallFromGUIController callFromGUIController) {
        this.callFromGUIController = callFromGUIController;
        this.login = login;
        this.client = client;
    }

    public synchronized void kill() {
        notify();
    }

    @Override
    public synchronized void run() {
        if (callFromGUIController == null) {
            UpdConTask timerTask = new UpdConTask();
            timerTask.setUpdateConnectionHandler(this);
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(timerTask, 0, 3 * 1000);
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timer.cancel();
        } else {
            UpdConTaskFrom timerTask = new UpdConTaskFrom();
            timerTask.setUpdateConnectionHandler(this);
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(timerTask, 0, 3 * 1000);
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timer.cancel();
        }
    }
}
