package codes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    //不要每次都创建一个Scanner了
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException{
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 10096);
        while(true) {
            Thread.sleep(1000);
            showMenu();
            String choice = sc.nextLine();

            String info = null;
            switch(choice){
                case "1" -> info = logIn();
                case "2" -> info = register();
                default -> {
                    System.out.println("Incorrect action. Please try again");
                    continue;
                }
            }
            OutputStream os = socket.getOutputStream();
            os.write(info.getBytes());
            os.flush();
            //一定要加这一句！否则服务器无法收到数据。

            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            char[] chars = new char[1024];
            int len = isr.read(chars);

            String reply = new String(chars, 0, len);
            if (reply.startsWith("11")){
                System.out.println("Successfully Logged in! You can start chatting now.");
                chatting(reply.substring(2), socket);//可以开始聊天了
                break;
            }else System.out.println(reply);
            Thread.sleep(1000);
            
        }
        System.out.println("Process completed.");
    }

    public static void showMenu(){
        System.out.println("------------------------");
        System.out.println("Welcome to the chat room");
        System.out.println("Please choose the action you would like to make:");
        System.out.println("Enter 1 to log in");
        System.out.println("Enter 2 to register");
        
    }

    public static String logIn() throws IOException, InterruptedException{
        System.out.println("Please enter username:");
        String username = sc.nextLine();
        System.out.println("Please enter password:");
        String password = sc.nextLine();

        return "1=" + username + "=" + password;    
    }

    public static String register(){
        System.out.println("Please enter the username you would like to create:");
        String username = sc.nextLine();
        System.out.println("Please enter your password:");
        String password = sc.nextLine();
        
        return "2=" + username + "=" + password;
    }

    public static void chatting(String username, Socket socket) throws IOException, InterruptedException{
        ReceiveMessageThread rmt = new ReceiveMessageThread(socket);
        rmt.start();
        
        while(true){
            if(!rmt.isAlive()){
                sc.close();
                break;
            }
            System.out.println("------------------------");
            System.out.println("Welcome, " + username);
            System.out.println("Please enter the message you would like to send:");
            //String message = sc.nextLine();
            String message = sc.nextLine();

            if (message == null || message.equals("")){
                System.out.println("Message cannot be empty.");
                continue;
            } //非空判断

            OutputStream os = socket.getOutputStream();
            os.write(("3=" + username + "=" + message).getBytes());
            os.flush();
            //socket.shutdownOutput();//发送数据
            Thread.sleep(1000);
        }
    }
}
