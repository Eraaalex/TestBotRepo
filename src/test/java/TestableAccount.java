import hw.okit.Account;
import hw.okit.IServerConnection;
import hw.okit.LocalOperationResponse;

class TestableAccount extends Account {
        public LocalOperationResponse callLogin(IServerConnection conn, String user, String pass) {
                return super.callLogin(conn, user, pass);
        }

        public LocalOperationResponse callLogout() {
                return super.callLogout();
        }

        public LocalOperationResponse callWithdraw(double amount) {
                return super.withdraw(amount);
        }

        // Установка serverConnection для тестов
        public void setServerConnection(IServerConnection serverConnection) {
                this.serverConnection = serverConnection;
        }

        public IServerConnection getServerConnection() {
                return this.serverConnection;
        }

        // Установка login для тестов
        public void setLogin(String login) {
                this.login = login;
        }

        // Получение login уже есть в суперклассе через getLogin()

        // Установка activeSession для тестов
        public void setActiveSession(Long sessionId) {
                this.activeSession = sessionId;
        }


}