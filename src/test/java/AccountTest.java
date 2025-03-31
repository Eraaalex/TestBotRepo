

import hw.okit.Account;
import hw.okit.IServerConnection;
import hw.okit.LocalOperationResponse;
import hw.okit.ServerResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class AccountTest {

        @Mock
        IServerConnection serverConnection;

        @Test
        public void callLogin_WithAlreadyLoggedResponse_ShouldReturnAccountManagerResponse() {
                // При серверном ответе ALREADY_LOGGED должен возвращаться ACCOUNT_MANAGER_RESPONSE.
                when(serverConnection.login(anyString(), anyString()))
                        .thenReturn(new ServerResponse(ServerResponse.ALREADY_LOGGED, null));

                Account account = new Account();
                LocalOperationResponse response = account.callLogin(serverConnection, "testUser", "encryptedPassword");

                assertEquals(LocalOperationResponse.ACCOUNT_MANAGER_RESPONSE.code, response.code);
        }

        @Test
        public void callLogin_WithIncorrectPasswordResponse_ShouldReturnNoUserIncorrectPasswordResponse() {
                // При серверном ответе NO_USER_INCORRECT_PASSWORD должен возвращаться соответствующий ответ.
                when(serverConnection.login(anyString(), anyString()))
                        .thenReturn(new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, null));

                Account account = new Account();
                LocalOperationResponse response = account.callLogin(serverConnection, "testUser", "encryptedPassword");

                assertEquals(LocalOperationResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE.code, response.code);
        }

        @Test
        public void callLogin_WithSuccessAndValidSession_ShouldReturnSucceedResponseAndSetActiveSession() {
                // При успешном ответе с корректным sessionId (типа Long) должно установиться activeSession.
                Long sessionId = 12345L;
                when(serverConnection.login(anyString(), anyString()))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, sessionId));

                Account account = new Account();
                LocalOperationResponse response = account.callLogin(serverConnection, "testUser", "encryptedPassword");

                assertEquals(LocalOperationResponse.SUCCEED, response.code);
                assertEquals(sessionId, response.response);
                assertEquals(sessionId, account.getActiveSession());
        }

        @Test
        public void callLogin_WithSuccessAndInvalidSessionData_ShouldReturnIncorrectResponse() {
                // Если данные ответа не являются Long, должен вернуться INCORRECT_RESPONSE.
                when(serverConnection.login(anyString(), anyString()))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, "invalidSession"));

                Account account = new Account();
                LocalOperationResponse response = account.callLogin(serverConnection, "testUser", "encryptedPassword");

                assertEquals(LocalOperationResponse.INCORRECT_RESPONSE, response.code);
        }

        @Test
        public void callLogout_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
                // Если activeSession не установлен, callLogout должен вернуть NOT_LOGGED_RESPONSE.
                Account account = new Account();
                LocalOperationResponse response = account.callLogout();
                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void callLogout_WithNotLoggedResponseFromServer_ShouldReturnNotLoggedResponse() {
                // При серверном ответе NOT_LOGGED от logout.
                Account account = new Account();
                account.activeSession = 12345L;
                when(serverConnection.logout(12345L))
                        .thenReturn(new ServerResponse(ServerResponse.NOT_LOGGED, null));
                account.serverConnection = serverConnection;
                LocalOperationResponse response = account.callLogout();
                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void callLogout_WithSuccessResponseFromServer_ShouldReturnSucceedResponse() {
                // При успешном завершении logout.
                Account account = new Account();
                account.activeSession = 12345L;
                when(serverConnection.logout(12345L))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, null));
                account.serverConnection = serverConnection;
                LocalOperationResponse response = account.callLogout();
                assertEquals(LocalOperationResponse.SUCCEED_RESPONSE.code, response.code);
        }

        @Test
        public void withdraw_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
                // Если нет activeSession, операция снятия должна вернуть NOT_LOGGED_RESPONSE.
                Account account = new Account();
                LocalOperationResponse response = account.withdraw(50.0);
                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void withdraw_WithNoMoneyResponse_ShouldReturnNoMoneyResponse() {
                // При попытке снять сумму, превышающую баланс, сервер возвращает NO_MONEY.
                Account account = new Account();
                account.activeSession = 12345L;
                double currentBalance = 20.0;
                when(serverConnection.withdraw(12345L, 50.0))
                        .thenReturn(new ServerResponse(ServerResponse.NO_MONEY, currentBalance));
                account.serverConnection = serverConnection;
                LocalOperationResponse response = account.withdraw(50.0);
                assertEquals(LocalOperationResponse.NO_MONEY, response.code);
                assertEquals(currentBalance, response.response);
        }

        @Test
        public void withdraw_WithSuccessResponse_ShouldReturnSucceedResponse() {
                // Успешное снятие средств.
                Account account = new Account();
                account.activeSession = 12345L;
                double newBalance = 100.0;
                when(serverConnection.withdraw(12345L, 50.0))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, newBalance));
                account.serverConnection = serverConnection;
                LocalOperationResponse response = account.withdraw(50.0);
                assertEquals(LocalOperationResponse.SUCCEED, response.code);
                assertEquals(newBalance, response.response);
        }

        @Test
        public void deposit_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
                // Если не выполнена авторизация, deposit должен вернуть NOT_LOGGED_RESPONSE.
                Account account = new Account();
                LocalOperationResponse response = account.deposit(100.0);
                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void deposit_WithSuccessResponse_ShouldReturnSucceedResponse() {
                // Успешное внесение средств.
                Account account = new Account();
                account.activeSession = 12345L;
                double newBalance = 150.0;
                when(serverConnection.deposit(12345L, 100.0))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, newBalance));
                account.serverConnection = serverConnection;
                LocalOperationResponse response = account.deposit(100.0);
                assertEquals(LocalOperationResponse.SUCCEED, response.code);
                assertEquals(newBalance, response.response);
        }

        @Test
        public void deposit_WithUnexpectedNoMoneyResponse_ShouldReturnNoMoneyResponse() {
                // Тест демонстрирует дисрептанцию: сервер возвращает NO_MONEY для deposit,
                // хотя по спецификации для внесения средств такого кода быть не должно.
                Account account = new Account();
                account.activeSession = 12345L;
                double balance = 200.0;
                when(serverConnection.deposit(12345L, 100.0))
                        .thenReturn(new ServerResponse(ServerResponse.NO_MONEY, balance));
                account.serverConnection = serverConnection;
                LocalOperationResponse response = account.deposit(100.0);
                assertEquals(LocalOperationResponse.NO_MONEY, response.code);
                assertEquals(balance, response.response);
        }

        @Test
        public void getBalance_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
                // Если не выполнена авторизация, getBalance должен вернуть NOT_LOGGED_RESPONSE.
                Account account = new Account();
                LocalOperationResponse response = account.getBalance();
                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void getBalance_WithSuccessResponse_ShouldReturnSucceedResponse() {
                // Успешный запрос баланса.
                Account account = new Account();
                account.activeSession = 12345L;
                double balance = 300.0;
                when(serverConnection.getBalance(12345L))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, balance));
                account.serverConnection = serverConnection;
                LocalOperationResponse response = account.getBalance();
                assertEquals(LocalOperationResponse.SUCCEED, response.code);
                assertEquals(balance, response.response);
        }
}
