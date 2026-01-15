package codes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReceiveMessageThread extends Thread{
    Socket socket;

    public ReceiveMessageThread(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStreamReader isr;//转换流为了能调用放在这
        char[] chars = new char[1024];

        try {
            isr = new InputStreamReader(socket.getInputStream());
            while(true){
                int len;
                len = isr.read(chars);
                System.out.println(new String(chars, 0, len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
