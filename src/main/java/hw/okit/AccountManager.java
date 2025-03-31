package hw.okit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AccountManager {
  private static String[] storedAccounts = new String[]{"user1", "user2", "user3"};
  private IServerConnection serverConnection;
  private IPasswordEncoder passEncoder;
  private ConcurrentLinkedDeque<OperationFailedException> exceptionsList = new ConcurrentLinkedDeque<>();
  private Lock accountsLock = new ReentrantLock();
  private Lock exceptionsLock = new ReentrantLock();
  private HashMap<String, Account> activeAccounts = new HashMap<>();
  public void AccountManager(IServerConnection s, IPasswordEncoder encoder) throws OperationFailedException {
    init(s, encoder);
  }
  private void init (IServerConnection s, IPasswordEncoder encoder) throws OperationFailedException {
    if(serverConnection!=null)
      throw new OperationFailedException(LocalOperationResponse.ALREADY_INITIATED_RESPONSE);
    if(s == null || encoder == null)
      throw new OperationFailedException(LocalOperationResponse.NULL_ARGUMENT_EXCEPTION);
    serverConnection = s;
    Arrays.stream(storedAccounts).parallel().forEach(
            one -> activeAccounts.put(one, new Account())
    );
  }
  public Account login (String login, String password) {
    if(login == null || password == null) {
      registerException(new OperationFailedException(LocalOperationResponse.NULL_ARGUMENT_EXCEPTION));
      return null;
    }
    accountsLock.lock();
    Account session = activeAccounts.get(login);
    if(session != null)
      registerException(new OperationFailedException(LocalOperationResponse.ALREADY_INITIATED_RESPONSE));
    Account a = new Account();
    activeAccounts.put(login, a);
    a.callLogin(serverConnection, login, passEncoder.makeSecure(password));
    accountsLock.unlock();
    return a;
  }
  public void logout (Account account) {
    if (account == null || account.getLogin() == null) {
      registerException(new OperationFailedException(LocalOperationResponse.NULL_ARGUMENT_EXCEPTION));
      return;
    }
    Account b = activeAccounts.remove(account.getLogin());
    if (b == null)
    {
      registerException(new OperationFailedException(LocalOperationResponse.INCORRECT_SESSION_RESPONSE));
      return;
    }
    b.callLogout();
  }
  private void registerException(OperationFailedException exception){
    exceptionsLock.lock();
    accountsLock.lock();
    try {
      exceptionsList.add(exception);
    }finally{
      accountsLock.unlock();
      exceptionsLock.unlock();
    }
  }
  public Collection<OperationFailedException> getExceptions(){
    exceptionsLock.lock();
    accountsLock.lock();
    try {
      return Collections.unmodifiableCollection(exceptionsList);
    }finally{
      accountsLock.unlock();
      exceptionsLock.unlock();
    }
  }
}
