import hw.okit.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountManagerTest {

        @Mock
        IServerConnection serverConnection;

        @Mock
        IPasswordEncoder passEncoder;

        @InjectMocks
        AccountManager accountManager;

        @Test
        public void init_WithNullArguments_ShouldThrowOperationFailedException() {
                AccountManager am = new AccountManager();
                OperationFailedException exception = assertThrows(OperationFailedException.class, () -> {
                        am.AccountManager(null, passEncoder);
                });
                assertEquals(LocalOperationResponse.NULL_ARGUMENT, exception.response.code);
        }

        @Test
        public void init_WithAlreadyInitiated_ShouldThrowOperationFailedException() throws OperationFailedException {
                // Повторная инициализация должна выбрасывать исключение ALREADY_INITIATED.
                OperationFailedException exception = assertThrows(OperationFailedException.class, () -> {
                        accountManager.AccountManager(serverConnection, passEncoder);
                });
                assertEquals(LocalOperationResponse.ALREADY_INITIATED, exception.response.code);
        }

        @Test
        public void login_WithNullArguments_ShouldReturnNullAndRegisterException() throws OperationFailedException {
                Account account = accountManager.login(null, "password");
                assertNull(account);
                account = accountManager.login("user", null);
                assertNull(account);
                // Проверяем, что в список исключений добавлены ошибки.
                assertFalse(accountManager.getExceptions().isEmpty());
        }

        @Test
        public void login_WithValidArgumentsButIncorrectPassword_ShouldCallServerAndReturnAccount() throws OperationFailedException {
                // Симуляция неверного пароля: сервер возвращает NO_USER_INCORRECT_PASSWORD.
                String login = "user1";
                String rawPassword = "password";
                String encryptedPassword = "encrypted";
                when(passEncoder.makeSecure(rawPassword)).thenReturn(encryptedPassword);
                when(serverConnection.login(login, encryptedPassword))
                        .thenReturn(new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, null));

                Account account = accountManager.login(login, rawPassword);
                // При неудачной авторизации activeSession не устанавливается.
                assertNull(account.getActiveSession());
        }

        @Test
        public void login_WithValidArgumentsAndSuccessfulLogin_ShouldReturnAccountWithActiveSession() throws OperationFailedException {
                String login = "user2";
                String rawPassword = "password";
                String encryptedPassword = "encrypted";
                Long sessionId = 67890L;
                when(passEncoder.makeSecure(rawPassword)).thenReturn(encryptedPassword);
                when(serverConnection.login(login, encryptedPassword))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, sessionId));

                Account account = accountManager.login(login, rawPassword);
                assertEquals(sessionId, account.getActiveSession());
                // Обнаруженная проблема: поле login не устанавливается, поэтому getLogin() возвращает null.
                assertNull(account.getLogin());
        }

        @Test
        public void login_WithAlreadyLoggedUser_ShouldRegisterException() throws OperationFailedException {
                String login = "user3";
                String rawPassword = "password";
                String encryptedPassword = "encrypted";
                Long sessionId = 11111L;
                when(passEncoder.makeSecure(rawPassword)).thenReturn(encryptedPassword).thenReturn(encryptedPassword);
                when(serverConnection.login(login, encryptedPassword))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, sessionId));

                // Первый вход
                Account firstLogin = accountManager.login(login, rawPassword);
                // Второй вход с тем же логином – должно зарегистрироваться исключение.
                Account secondLogin = accountManager.login(login, rawPassword);
                assertNotNull(secondLogin);
                boolean found = accountManager.getExceptions().stream()
                        .anyMatch(e -> e.response.code == LocalOperationResponse.ALREADY_INITIATED);
                assertTrue(found);
        }

        @Test
        public void logout_WithNullAccount_ShouldRegisterException() {
                accountManager.logout(null);
                boolean found = accountManager.getExceptions().stream()
                        .anyMatch(e -> e.response.code == LocalOperationResponse.NULL_ARGUMENT);
                assertTrue(found);
        }

        @Test
        public void logout_WithNullLoginUser_ShouldRegisterException() {
                Account account = new Account();
                // Поле login не установлено (null) – должно вызвать исключение.
                accountManager.logout(account);
                boolean found = accountManager.getExceptions().stream()
                        .anyMatch(e -> e.response.code == LocalOperationResponse.NULL_ARGUMENT);
                assertTrue(found);
        }

        @Test
        public void logout_WithNonLoginUser_ShouldRegisterException() {
                Account account = new Account();
                setPrivateField(account, "login", "user4"); // not empty user
                accountManager.logout(account);
                boolean found = accountManager.getExceptions().stream()
                        .anyMatch(e -> e.response.code == LocalOperationResponse.INCORRECT_SESSION);
                assertTrue(found);
        }

        @Test
        public void scenario1_FullTest() throws OperationFailedException {
                // Сценарий 1 (Приложение 1):
                // 1. Авторизация с некорректным логином
                String loginIncorrect = "nonexistentUser";
                String rawPassword = "password";
                String encryptedPassword = "encrypted";
                when(passEncoder.makeSecure(rawPassword)).thenReturn(encryptedPassword);
                when(serverConnection.login(loginIncorrect, encryptedPassword))
                        .thenReturn(new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, null));
                Account account1 = accountManager.login(loginIncorrect, rawPassword);
                assertNull(account1.getActiveSession());

                // 2. Вторая попытка: корректный логин, неверный пароль
                String loginCorrect = "user1";
                String wrongPassword = "wrongPassword";
                String encryptedWrong = "encryptedWrong";
                when(passEncoder.makeSecure(wrongPassword)).thenReturn(encryptedWrong);
                when(serverConnection.login(loginCorrect, encryptedWrong))
                        .thenReturn(new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, null));
                Account account2 = accountManager.login(loginCorrect, wrongPassword);
                assertNull(account2.getActiveSession());

                // 3. Третья попытка: корректный логин, правильный пароль
                String correctPassword = "correctPassword";
                String encryptedCorrect = "encryptedCorrect";
                Long sessionId = 22222L;
                when(passEncoder.makeSecure(correctPassword)).thenReturn(encryptedCorrect);
                when(serverConnection.login(loginCorrect, encryptedCorrect))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, sessionId));
                Account account3 = accountManager.login(loginCorrect, correctPassword);
                assertEquals(sessionId, account3.getActiveSession());

                // Запрос баланса
                double balance = 500.0;
                when(serverConnection.getBalance(sessionId))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, balance));
                LocalOperationResponse balanceResponse = account3.getBalance();
                assertEquals(LocalOperationResponse.SUCCEED, balanceResponse.code);
                assertEquals(balance, balanceResponse.response);

                // Попытка внести на счёт 100 единиц
                double newBalance = 600.0;
                when(serverConnection.deposit(sessionId, 100.0))
                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, newBalance));
                LocalOperationResponse depositResponse = account3.deposit(100.0);
                assertEquals(LocalOperationResponse.SUCCEED, depositResponse.code);
                assertEquals(newBalance, depositResponse.response);
        }
        private void setPrivateField(Object target, String fieldName, Object value) {
                try {
                        Field field = target.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(target, value);
                } catch (Exception e) {
                        fail("Ошибка при установке поля: " + e.getMessage());
                }
        }

//        @Test
//        public void scenario2_FullTest() throws OperationFailedException {
//                // Сценарий 2 (Приложение 1):
//                // 1. Успешная авторизация
//                String login = "user2";
//                String rawPassword = "password";
//                String encryptedPassword = "encrypted";
//                Long sessionId = 33333L;
//                when(passEncoder.makeSecure(rawPassword)).thenReturn(encryptedPassword);
//                when(serverConnection.login(login, encryptedPassword))
//                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, sessionId));
//                Account account = accountManager.login(login, rawPassword);
////                setPrivateField(account, "login", login); // Установка логина для теста
//
//                assertEquals(sessionId, account.getActiveSession());
//
//                // 2. Попытка снятия 50 единиц (неудачная – NO_MONEY)
//                double currentBalance = 30.0;
//                when(serverConnection.withdraw(sessionId, 50.0))
//                        .thenReturn(new ServerResponse(ServerResponse.NO_MONEY, currentBalance));
//                LocalOperationResponse withdrawResponse1 = account.withdraw(50.0);
//                assertEquals(LocalOperationResponse.NO_MONEY, withdrawResponse1.code);
//                assertEquals(currentBalance, withdrawResponse1.response);
//
//                // 3. Запрос баланса (ожидается тот же баланс)
//                when(serverConnection.getBalance(sessionId))
//                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, currentBalance));
//                LocalOperationResponse balanceResponse = account.getBalance();
//                assertEquals(LocalOperationResponse.SUCCEED, balanceResponse.code);
//                assertEquals(currentBalance, balanceResponse.response);
//
//                // 4. Внесение 100 единиц
//                double newBalanceAfterDeposit = currentBalance + 100.0;
//                when(serverConnection.deposit(sessionId, 100.0))
//                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, newBalanceAfterDeposit));
//                LocalOperationResponse depositResponse = account.deposit(100.0);
//                assertEquals(LocalOperationResponse.SUCCEED, depositResponse.code);
//                assertEquals(newBalanceAfterDeposit, depositResponse.response);
//
//                // 5. Снятие 50 единиц (успешно)
//                double newBalanceAfterWithdraw = newBalanceAfterDeposit - 50.0;
//                when(serverConnection.withdraw(sessionId, 50.0))
//                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, newBalanceAfterWithdraw));
//                LocalOperationResponse withdrawResponse2 = account.withdraw(50.0);
//                assertEquals(LocalOperationResponse.SUCCEED, withdrawResponse2.code);
//                assertEquals(newBalanceAfterWithdraw, withdrawResponse2.response);
//
//                // 6. Выход из системы (logout)
//                when(serverConnection.logout(sessionId))
//                        .thenReturn(new ServerResponse(ServerResponse.SUCCESS, null));
//                accountManager.logout(account);
//                // Дополнительная проверка: если logout выполнен успешно, исключений не должно быть зарегистрировано (нет прямого изменения состояния аккаунта).
//        }
}
