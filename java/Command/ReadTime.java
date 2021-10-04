package Command;

public class ReadTime implements Command{
    public String answer;
    @Override
    public byte[] makeMessage() {
        byte[] bytesToSend = new byte[8];
        bytesToSend[0] = 0x23;
        for(int i = 1; i < 8; i++){
            bytesToSend[i] = 0x0;
        }
        return bytesToSend;
    }

    @Override
    public void getMessage(byte[] bytes) {
        String year = Byte.toString(bytes[0]).substring(0,Byte.toString(bytes[0]).length() - 1);
        String month = Byte.toString(bytes[0]).substring(Byte.toString(bytes[0]).length() - 1,Byte.toString(bytes[0]).length() - 1) + Byte.toString(bytes[1]).substring(0,2);
        String day = Byte.toString(bytes[1]).substring(3,Byte.toString(bytes[2]).length());
        String hour = Byte.toString(bytes[2]);
        String minutes = Byte.toString(bytes[3]);
        int year_final = Integer.parseInt(year, 16);
        int month_final = Integer.parseInt(month, 16);
        int day_final = Integer.parseInt(day, 16);
        int hour_final = Integer.parseInt(hour, 16);
        int minutes_final = Integer.parseInt(minutes, 16);
        this.answer = Integer.toString(day_final) + "." + Integer.toString(month_final) + "." + Integer.toString(year_final) + " " + Integer.toString(hour_final) + ":" + Integer.toString(minutes_final);
    }
}
