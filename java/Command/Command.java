package Command;

public interface Command {
    public byte[] makeMessage();
    public void getMessage(byte[] bytes);
}
