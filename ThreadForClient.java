package codes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ThreadForClient extends Thread {
    Socket socket;
    static HashMap<String,String> users;
    static ArrayList<Socket> clients;

    public ThreadForClient(Socket socket, HashMap<String,String> users, ArrayList<Socket> clients){
        this.socket = socket;
        ThreadForClient.users = users;
        ThreadForClient.clients = clients;
    }
    
    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            System.out.println("aaa");
            char[] chars = new char[1024];

            while(true){     
                int len = isr.read(chars);
                String[] arr = new String(chars, 0, len).split("=");
                OutputStream os = socket.getOutputStream();

                //String reply = "";
                switch (arr[0]) {
                    case "1" -> {
                        os.write(logIn(arr, users).getBytes());
                        os.flush();
                    }
                    case "2" -> {
                        String message = register(arr, users);
                        os.write(message.getBytes());
                        os.flush();
                    }
                    case "3" -> {
                        String message = relayMessage(arr, users);
                        for (Socket s : clients) {
                            //遍历集合发送数据（转发）
                            s.getOutputStream().write(message.getBytes());
                            s.getOutputStream().flush();
                        }
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String logIn(String[] arr, HashMap<String,String> users){
        //描述登陆界面，返回要传给用户的字符串的值
        for (String username : users.keySet()) {
            if (arr[1].equals(username)){
                if (arr[2].equals(users.get(username))){
                    System.out.println(arr[1] + " has successfully logged in");
                    return "11" + username;
                } else {
                    System.out.println("Incorrect password");
                    return "Incorrect Password";
                }
            }
        }
        System.out.println("Username exists not");
        return "Username exists not";
    }

    public static String register(String[] arr, HashMap<String,String> users) throws IOException{
        //注册        
        if (users.keySet().contains(arr[1])){
            //用Set集合防止重复的用户名
            return "Duplicate username, please try again";
        }else {
            users.put(arr[1], arr[2]);
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter("files\\users.txt", true));
        bw.newLine();
        bw.write(arr[1] + "=" + arr[2]);
        bw.newLine();
        bw.close(); //写入新注册的数据
        return "11" + arr[1];
    }

    public static String relayMessage(String[] arr, HashMap<String,String>users){
        //消息转发，这个方法是生成消息的
        StringBuilder sb = new StringBuilder("[");
        sb.append(arr[1]);
        sb.append("] ");
        for (int i = 2; i < arr.length; i++) {
            sb.append(arr[i] + "=");
            //防止用户输入的有等号
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
