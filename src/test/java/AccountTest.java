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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@ExtendWith(MockitoExtension.class)
public class AccountTest {

        @Mock
        IServerConnection serverConnection;

        private void setPrivateField(Object target, String fieldName, Object value) {
                try {
                        Field field = target.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(target, value);
                } catch (Exception e) {
                        fail("Ошибка при установке поля: " + e.getMessage());
                }
        }

        private Object callProtectedMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
                try {
                        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
                        method.setAccessible(true);
                        return method.invoke(target, args);
                } catch (Exception e) {
                        fail("Ошибка при вызове метода " + methodName + ": " + e.getMessage());
                        return null;
                }
        }

        @Test
        public void callLogin_WithAlreadyLoggedResponse_ShouldReturnAccountManagerResponse() {
                when(serverConnection.login(anyString(), anyString()))
                        .thenReturn(new ServerResponse(ServerResponse.ALREADY_LOGGED, null));

                Account account = new Account();
                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogin",
                        new Class[]{IServerConnection.class, String.class, String.class},
                        serverConnection, "testUser", "encryptedPassword"
                );

                assertEquals(LocalOperationResponse.ACCOUNT_MANAGER_RESPONSE.code, response.code);
        }

        @Test
        public void callLogin_WithIncorrectPasswordResponse_ShouldReturnNoUserIncorrectPasswordResponse() {
                when(serverConnection.login(anyString(), anyString()))
                        .thenReturn(new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, null));

                Account account = new Account();
                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogin",
                        new Class[]{IServerConnection.class, String.class, String.class},
                        serverConnection, "testUser", "encryptedPassword"
                );

                assertEquals(LocalOperationResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE.code, response.code);
        }

        @Test
        public void callLogin_WithSuccessAndValidSession_ShouldReturnSucceedResponseAndSetActiveSession() {
                Long sessionId = 12345L;
                when(serverConnection.login(anyString(), anyString()))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, sessionId));

                Account account = new Account();
                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogin",
                        new Class[]{IServerConnection.class, String.class, String.class},
                        serverConnection, "testUser", "encryptedPassword"
                );

                assertEquals(LocalOperationResponse.SUCCEED, response.code);
                assertEquals(sessionId, response.response);
                assertEquals(sessionId, account.getActiveSession());
        }

        @Test
        public void callLogin_WithSuccessAndInvalidSessionData_ShouldReturnIncorrectResponse() {
                when(serverConnection.login(anyString(), anyString()))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, "invalidSession"));

                Account account = new Account();
                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogin",
                        new Class[]{IServerConnection.class, String.class, String.class},
                        serverConnection, "testUser", "encryptedPassword"
                );

                assertEquals(LocalOperationResponse.INCORRECT_RESPONSE, response.code);
        }

        @Test
        public void callLogout_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogout", new Class[]{}
                );

                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void callLogout_WithNotLoggedResponseFromServer_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                setPrivateField(account, "serverConnection", serverConnection);

                when(serverConnection.logout(12345L))
                        .thenReturn(new ServerResponse(ServerResponse.NOT_LOGGED, null));

                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogout", new Class[]{}
                );

                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void callLogout_WithSuccessResponseFromServer_ShouldReturnSucceedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                setPrivateField(account, "serverConnection", serverConnection);

                when(serverConnection.logout(12345L))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, null));

                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogout", new Class[]{}
                );

                assertEquals(LocalOperationResponse.SUCCEED_RESPONSE.code, response.code);
        }

        @Test
        public void withdraw_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                LocalOperationResponse response = account.withdraw(50.0);
                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void withdraw_WithNoMoneyResponse_ShouldReturnNoMoneyResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                setPrivateField(account, "serverConnection", serverConnection);

                double currentBalance = 20.0;
                when(serverConnection.withdraw(12345L, 50.0))
                        .thenReturn(new ServerResponse(ServerResponse.NO_MONEY, currentBalance));

                LocalOperationResponse response = account.withdraw(50.0);
                assertEquals(LocalOperationResponse.NO_MONEY, response.code);
                assertEquals(currentBalance, response.response);
        }

        @Test
        public void withdraw_WithSuccessResponse_ShouldReturnSucceedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                setPrivateField(account, "serverConnection", serverConnection);

                double newBalance = 100.0;
                when(serverConnection.withdraw(12345L, 50.0))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, newBalance));

                LocalOperationResponse response = account.withdraw(50.0);
                assertEquals(LocalOperationResponse.SUCCEED, response.code);
                assertEquals(newBalance, response.response);
        }

        @Test
        public void deposit_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                LocalOperationResponse response = account.deposit(100.0);
                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void deposit_WithSuccessResponse_ShouldReturnSucceedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                setPrivateField(account, "serverConnection", serverConnection);

                double newBalance = 150.0;
                when(serverConnection.deposit(12345L, 100.0))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, newBalance));

                LocalOperationResponse response = account.deposit(100.0);
                assertEquals(LocalOperationResponse.SUCCEED, response.code);
                assertEquals(newBalance, response.response);
        }

        @Test
        public void deposit_WithUnexpectedNoMoneyResponse_ShouldReturnNoMoneyResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                setPrivateField(account, "serverConnection", serverConnection);

                double balance = 200.0;
                when(serverConnection.deposit(12345L, 100.0))
                        .thenReturn(new ServerResponse(ServerResponse.NO_MONEY, balance));

                LocalOperationResponse response = account.deposit(100.0);
                assertEquals(LocalOperationResponse.NO_MONEY, response.code);
                assertEquals(balance, response.response);
        }

        @Test
        public void getBalance_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                LocalOperationResponse response = account.getBalance();
                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }

        @Test
        public void getBalance_WithSuccessResponse_ShouldReturnSucceedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                setPrivateField(account, "serverConnection", serverConnection);

                double balance = 300.0;
                when(serverConnection.getBalance(12345L))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, balance));

                LocalOperationResponse response = account.getBalance();
                assertEquals(LocalOperationResponse.SUCCEED, response.code);
                assertEquals(balance, response.response);
        }

        @Test
        public void callLogout_WithIncorrectResponse_ShouldReturnIncorrectResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                // Возвращаем код, не обрабатываемый в switch (например, UNDEFINED_ERROR)
                ServerResponse fakeResponse = new ServerResponse(ServerResponse.UNDEFINED_ERROR, "errorData");
                when(serverConnection.logout(12345L)).thenReturn(fakeResponse);
                setPrivateField(account, "serverConnection", serverConnection);

                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogout", new Class[]{}
                );;

                assertEquals(LocalOperationResponse.INCORRECT_RESPONSE, response.code);
                assertEquals(fakeResponse, response.response);
        }

        // 2) Тесты для withdraw()

//        // 2.1) Если activeSession == null, должен возвращаться NOT_LOGGED_RESPONSE.
//        @Test
//        public void withdraw_WithoutActiveSession_ShouldReturnNotLoggedResponse() {
//                Account account = new Account();
//                LocalOperationResponse response = account.withdraw(50.0);
//                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
//        }

        // 2.2) Если сервер возвращает неожиданный код, должен возвращаться INCORRECT_RESPONSE.
        @Test
        public void withdraw_WithIncorrectResponse_ShouldReturnIncorrectResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                // Используем код ALREADY_LOGGED (2), который не обрабатывается в withdraw
                ServerResponse fakeResponse = new ServerResponse(ServerResponse.ALREADY_LOGGED, "errorData");
                when(serverConnection.withdraw(12345L, 50.0)).thenReturn(fakeResponse);
                setPrivateField(account, "serverConnection", serverConnection);

                LocalOperationResponse response = account.withdraw(50.0);

                assertEquals(LocalOperationResponse.INCORRECT_RESPONSE, response.code);
                assertEquals(fakeResponse, response.response);
        }

        // 3.2) Если сервер возвращает неожиданный код, должен возвращаться INCORRECT_RESPONSE.
        @Test
        public void deposit_WithIncorrectResponse_ShouldReturnIncorrectResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                // Используем код ALREADY_LOGGED (2), который не обрабатывается в deposit
                ServerResponse fakeResponse = new ServerResponse(ServerResponse.ALREADY_LOGGED, "errorData");
                when(serverConnection.deposit(12345L, 100.0)).thenReturn(fakeResponse);
                setPrivateField(account, "serverConnection", serverConnection);
                LocalOperationResponse response = account.deposit(100.0);

                assertEquals(LocalOperationResponse.INCORRECT_RESPONSE, response.code);
                assertEquals(fakeResponse, response.response);
        }
        
        

        // 4.2) Если сервер возвращает неожиданный код, должен возвращаться INCORRECT_RESPONSE.
        @Test
        public void getBalance_WithIncorrectResponse_ShouldReturnIncorrectResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                // Используем код ALREADY_LOGGED (2) для тестирования default-ветки
                ServerResponse fakeResponse = new ServerResponse(ServerResponse.ALREADY_LOGGED, "errorData");
                when(serverConnection.getBalance(12345L)).thenReturn(fakeResponse);
                setPrivateField(account, "serverConnection", serverConnection);

                LocalOperationResponse response = account.getBalance();

                assertEquals(LocalOperationResponse.INCORRECT_RESPONSE, response.code);
                assertEquals(fakeResponse, response.response);
        }

        @Test
        public void withdraw_WithServerReturningNotLogged_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                when(serverConnection.withdraw(12345L, 100.0))
                        .thenReturn(new ServerResponse(ServerResponse.NOT_LOGGED, null)); // сервер говорит "не залогинен"
                setPrivateField(account, "serverConnection", serverConnection);

                LocalOperationResponse response = account.withdraw(100.0);

                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }
        @Test
        public void deposit_WithServerReturningNotLogged_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                when(serverConnection.deposit(12345L, 50.0))
                        .thenReturn(new ServerResponse(ServerResponse.NOT_LOGGED, null));
                setPrivateField(account, "serverConnection", serverConnection);

                LocalOperationResponse response = account.deposit(50.0);

                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }
        @Test
        public void getBalance_WithServerReturningNotLogged_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                when(serverConnection.getBalance(12345L))
                        .thenReturn(new ServerResponse(ServerResponse.NOT_LOGGED, null));
                setPrivateField(account, "serverConnection", serverConnection);

                LocalOperationResponse response = account.getBalance();

                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }
        @Test
        public void callLogout_WithServerReturningNotLogged_ShouldReturnNotLoggedResponse() {
                Account account = new Account();
                setPrivateField(account, "activeSession", 12345L);
                when(serverConnection.logout(12345L))
                        .thenReturn(new ServerResponse(ServerResponse.NOT_LOGGED, null));
                setPrivateField(account, "serverConnection", serverConnection);

                LocalOperationResponse response = (LocalOperationResponse) callProtectedMethod(
                        account, "callLogout", new Class[]{}
                );

                assertEquals(LocalOperationResponse.NOT_LOGGED_RESPONSE.code, response.code);
        }


}
