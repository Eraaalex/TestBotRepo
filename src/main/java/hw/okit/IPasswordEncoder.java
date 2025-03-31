package hw.okit;

public interface IPasswordEncoder {
  public String makeSecure(String password) throws OperationFailedException;
}
