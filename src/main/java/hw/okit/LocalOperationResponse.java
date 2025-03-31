package hw.okit;

public class LocalOperationResponse {
  public static int
    SUCCEED = 0,
    ALREADY_LOGGED = 1,
    NOT_LOGGED = 2,
    NO_USER_INCORRECT_PASSWORD = 3,
    INCORRECT_RESPONSE = 4,
    UNDEFINED_ERROR = 5,
    INCORRECT_SESSION = 6,
    NO_MONEY = 7,
    ENCODING_ERROR = 8,
    ALREADY_INITIATED = 9,
    NULL_ARGUMENT = 10;
  public static final LocalOperationResponse ACCOUNT_MANAGER_RESPONSE =
    new LocalOperationResponse(ALREADY_LOGGED, null);
  public static final LocalOperationResponse NO_USER_INCORRECT_PASSWORD_RESPONSE =
    new LocalOperationResponse(NO_USER_INCORRECT_PASSWORD, null);
  public static final LocalOperationResponse UNDEFINED_ERROR_RESPONSE =
    new LocalOperationResponse(UNDEFINED_ERROR, null);
  public static final LocalOperationResponse NOT_LOGGED_RESPONSE =
    new LocalOperationResponse(NOT_LOGGED, null);
  public static final LocalOperationResponse INCORRECT_SESSION_RESPONSE =
    new LocalOperationResponse(INCORRECT_SESSION, null);
  public static final LocalOperationResponse SUCCEED_RESPONSE =
    new LocalOperationResponse(SUCCEED, null);
  public static final LocalOperationResponse NO_MONEY_RESPONSE =
    new LocalOperationResponse(NO_MONEY, null);
  public static final LocalOperationResponse ENCODING_ERROR_RESPONSE =
    new LocalOperationResponse(ENCODING_ERROR, null);
  public static final LocalOperationResponse ALREADY_INITIATED_RESPONSE =
    new LocalOperationResponse(ALREADY_INITIATED, null);
  public static final LocalOperationResponse NULL_ARGUMENT_EXCEPTION =
    new LocalOperationResponse(NULL_ARGUMENT, null);
  public int code;
  public Object response;
  public LocalOperationResponse(int code, Object obj) {
    this.code = code;
    this.response = obj;
  }
}
