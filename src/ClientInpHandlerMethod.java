public class ClientInpHandlerMethod extends InputHandlerMethod {
    Client client;

    ClientInpHandlerMethod(Client client) {
        this.client = client;
    }

    @Override
    void handle(Message message) {
        switch (message.method) {
            case AUTHORIZE: {
                client.handleAuthorizeResponse(message);
                break;
            }
            case CALL: {
                client.handleCallResponse(message);
                break;
            }
            case GET_USERS: {
                client.handleGetUsersResponse(message);
                break;
            }
            case NEW_ACC: {
                client.handleCreateAccountResponse(message);
                break;
            }
            case LOG_OUT: {
                client.handleLogOutResponse(message);
                break;
            }
            case INP_CALL: {
                client.handleInputCallRequest(message);
                break;
            }
            case CALL_END: {
                client.handleCallEndingResponse(message);
                break;
            }
        }
    }
}
