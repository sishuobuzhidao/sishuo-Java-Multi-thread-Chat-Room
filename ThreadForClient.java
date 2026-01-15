package codes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ThreadForClient extends Thread {
    Socket socket;
    static HashMap<String,String> users;
    static ArrayList<Socket> clients;
    static LinkedHashMap<String,Socket> onlineUsers = new LinkedHashMap<>();
    String username;

    public ThreadForClient(Socket socket, HashMap<String,String> users, ArrayList<Socket> clients){
        this.socket = socket;
        ThreadForClient.users = users;
        ThreadForClient.clients = clients;
    }
    
    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            System.out.println("Ready to send reply to the client.");
            char[] chars = new char[1024];

            while(true){     
                int len = isr.read(chars);
                String[] arr = new String(chars, 0, len).split("=");
                OutputStream os = socket.getOutputStream();

                //判断发送来的消息
                switch (arr[0]) {
                    case "1" -> {
                        os.write(logIn(arr).getBytes());
                        os.flush();
                    }
                    case "2" -> {
                        String message = register(arr);
                        os.write(message.getBytes());
                        os.flush();
                    }
                    case "3" -> {
                        String message = relayMessage(arr, users);
                        if ((message.startsWith("["))){
                            for (String username : onlineUsers.keySet()){
                                //必须等登录之后才能收到消息，否则未登录用户的缓冲区会有bug
                                //遍历集合发送消息
                                Socket s = onlineUsers.get(username);
                                s.getOutputStream().write(message.getBytes());
                                s.getOutputStream().flush();
                            }
                        } else if (message.startsWith("##")){
                            //用户选择退出
                            os.write("Exiting chat room...".getBytes());
                            os.flush();
                            socket.close();
                            clients.remove(this.socket);
                            onlineUsers.remove(username);
                            break;
                        } else if (message.startsWith("`")){
                            String dest = message.split("`")[1];
                            String realMessage = message.substring(("`" + dest + "`").length());
                            //私信(己方视角)
                            os.write(("You whispers to " + dest + ": " + realMessage).getBytes());
                            os.flush();
                            //私信(对方视角)
                            Socket s = onlineUsers.get(dest);
                            s.getOutputStream().write((username + " whispers to you: " + realMessage).getBytes());
                            s.getOutputStream().flush();
                        }

                        else {
                            //不是普通的消息，私发
                            os.write(message.getBytes());
                            os.flush();
                        }
                    }
                }
            }
        } catch (IOException e){
            System.out.println("Something wrong happened! Could be a connection error.");
        }
        System.out.println("User " + username + " exited the chat room.");
    }

    public String logIn(String[] arr){
        //描述登陆界面，返回要传给用户的字符串的值
        for (String username : users.keySet()) {
            if (arr[1].equals(username)){
                if (arr[2].equals(users.get(username))){
                    //防止重复登陆
                    if (!(onlineUsers.keySet().contains(arr[1]))){
                        System.out.println("User " + arr[1] + " has successfully logged in.");
                        this.username = username;
                        onlineUsers.put(username, socket);
                        return "11" + username;
                    } else {
                        System.out.println("Someone tried to log in on an already logged in account.");
                        return "This user has already logged in. Please try again.";
                    }
                } else {
                    System.out.println("Someone tried to log in with an incorrect password.");
                    return "Incorrect Password";
                }
            }
        }
        System.out.println("Someone tried to log in with an invalid username.");
        return "Username exists not";
    }

    public String register(String[] arr) throws IOException{
        //注册        
        if (users.keySet().contains(arr[1])){
            //用Set集合防止重复的用户名
            System.out.println("Someone tried to register with a duplicated username.");
            return "Duplicate username, please try again";
        }else {
            users.put(arr[1], arr[2]);
            onlineUsers.put(arr[1], socket);
            System.out.println("User " + arr[1] + " has successfully registered.");
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter("files\\users.txt", true));
        bw.write(arr[1] + "=" + arr[2]);
        bw.newLine();
        bw.close(); //写入新注册的数据
        System.out.println("Successfully saved the new user's data.");

        return "11" + arr[1];
    }

    public String relayMessage(String[] arr, HashMap<String,String>users) throws IOException{
        //消息转发，这个方法是生成消息的
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < arr.length; i++) {
            sb.append(arr[i] + "=");
            //防止用户输入的有等号
        }
        sb.deleteCharAt(sb.length() - 1);
        String message = sb.toString();

        //现在增加了命令功能所以要进行判断
        if (!message.startsWith("/")){
            System.out.println("User " + arr[1] + " sends a message: " + message);
            return "[" + arr[1] + "]" + message;
        }else {
            return Server.commands(arr[1], message);
        }
    }
}
