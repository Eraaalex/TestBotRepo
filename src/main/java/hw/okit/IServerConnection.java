package hw.okit;

public interface IServerConnection {
  public ServerResponse login(String userName, String mdPass);
  public ServerResponse logout(long session);
  public ServerResponse withdraw(long session, double balance);
  public ServerResponse deposit(long session, double balance);
  public ServerResponse getBalance(long session);
}
