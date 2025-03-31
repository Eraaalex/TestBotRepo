package hw.okit;

public class Account {
  protected IServerConnection serverConnection;
  protected String login;
  protected Long activeSession;
  public String getLogin() {
    return login;
  }
  public Long getActiveSession() {
    return activeSession;
  }
  protected LocalOperationResponse callLogin(IServerConnection serverConnection, String login, String password) {
    this.serverConnection = serverConnection;
    ServerResponse response = serverConnection.login(login, password);
    switch(response.code) {
      case ServerResponse.ALREADY_LOGGED:
        return LocalOperationResponse.ACCOUNT_MANAGER_RESPONSE;
      case ServerResponse.NO_USER_INCORRECT_PASSWORD:
        return LocalOperationResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE;
      case ServerResponse.SUCCESS:{
        Object answer = response.data;
        if(answer instanceof Long) {
          activeSession = (Long) answer;
          return new LocalOperationResponse(LocalOperationResponse.SUCCEED, answer);
        }
        break;
      }
    }
    return new LocalOperationResponse(LocalOperationResponse.INCORRECT_RESPONSE, response);
  }
  protected LocalOperationResponse callLogout() {
    if(activeSession == null)
      return LocalOperationResponse.NOT_LOGGED_RESPONSE;
    ServerResponse response = serverConnection.logout(activeSession);
    switch(response.code){
      case ServerResponse.NOT_LOGGED:
        return LocalOperationResponse.NOT_LOGGED_RESPONSE;
      case ServerResponse.SUCCESS:
        return LocalOperationResponse.SUCCEED_RESPONSE;
    }
    return new LocalOperationResponse(LocalOperationResponse.INCORRECT_RESPONSE, response);
    
  }
  public LocalOperationResponse withdraw(double amount){
    if(activeSession == null)
      return LocalOperationResponse.NOT_LOGGED_RESPONSE;
    ServerResponse response = serverConnection.withdraw(activeSession, amount);
    switch(response.code){
      case ServerResponse.NOT_LOGGED:
        return LocalOperationResponse.NOT_LOGGED_RESPONSE;
      case ServerResponse.NO_MONEY:
        Object r = response.data;
        if(r!=null && r instanceof Double)
          return new LocalOperationResponse(LocalOperationResponse.NO_MONEY, (Double)r);
        break;
      case ServerResponse.SUCCESS:
        r = response.data;
        if(r!=null && r instanceof Double)
          return new LocalOperationResponse(LocalOperationResponse.SUCCEED, (Double)r);
        break;
    }
    return new LocalOperationResponse(LocalOperationResponse.INCORRECT_RESPONSE, response);
  }  
  public LocalOperationResponse deposit(double amount){
    if(activeSession == null)
      return LocalOperationResponse.NOT_LOGGED_RESPONSE;
    ServerResponse response = serverConnection.deposit(activeSession, amount);
    switch(response.code){
      case ServerResponse.NOT_LOGGED:
        return LocalOperationResponse.NOT_LOGGED_RESPONSE;
      case ServerResponse.NO_MONEY:
        Object r = response.data;
        if(r!=null && r instanceof Double)
          return new LocalOperationResponse(LocalOperationResponse.NO_MONEY, (Double)r);
        break;
      case ServerResponse.SUCCESS:
        r = response.data;
        if(r!=null && r instanceof Double)
          return new LocalOperationResponse(LocalOperationResponse.SUCCEED, (Double)r);
        break;
    }
    return new LocalOperationResponse(LocalOperationResponse.INCORRECT_RESPONSE, response);
  }  
  public LocalOperationResponse getBalance(){
    if(activeSession == null)
      return LocalOperationResponse.NOT_LOGGED_RESPONSE;
    ServerResponse response = serverConnection.getBalance(activeSession);
    switch(response.code){
      case ServerResponse.NOT_LOGGED:
        return LocalOperationResponse.NOT_LOGGED_RESPONSE;
      case ServerResponse.SUCCESS:
        Object r = response.data;
        if(r!=null && r instanceof Double)
          return new LocalOperationResponse(LocalOperationResponse.SUCCEED, (Double)r);
        break;
    }
    return new LocalOperationResponse(LocalOperationResponse.INCORRECT_RESPONSE, response);
  }

}
