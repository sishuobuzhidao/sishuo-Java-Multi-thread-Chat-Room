package codes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server{
    public static void main(String[] args) throws IOException{
        HashMap<String,String> users = new HashMap<>();
        //防止用户名重复所以使用HashMap
        BufferedReader br = new BufferedReader(new FileReader("files\\users.txt"));
        String str;
        while((str = br.readLine()) != null){
            users.put(str.split("=")[0], str.split("=")[1]);
        } //读取数据IO
        br.close();

        ArrayList<Socket> clients = new ArrayList<>();

        ServerSocket ss = new ServerSocket(10096);
        System.out.println("Waiting for clients to connect...");
        
        while(true){
            Socket socket = ss.accept();
            clients.add(socket);
            System.out.println("Accepted new connection from a client.");

            //一有用户连接就创建线程
            ThreadForClient tfc = new ThreadForClient(socket, users, clients);
            tfc.start();
            //处理用户发送的数据的代码都在ThreadForClient当中。
            //方法我都转移到ThreadForClient当中了，Server这里的我都删除了。
        }
        //ss.close();
    }

    public final static String getHelpString() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("files\\help.txt"));
        StringBuilder sb = new StringBuilder();
        String str;
        while((str = br.readLine()) != null){
            sb.append(str + "\r\n");
        }
        sb.delete(sb.length() - 2, sb.length() - 1);
        br.close();
        return sb.toString();
    }

    public static String commands(String username, String message) throws IOException{
        //现在增加了命令功能所以要进行判断
        if (message.equals("/help")){
            System.out.println("User " + username + " uses the /help command.");
            return Server.getHelpString();
        }else if (message.equals("/exit")){
            System.out.println("User " + username + " uses the /exit command.");
            return "##";
        }else if (message.equals("/list")){
            System.out.println("User " + username + " uses the /list command.");
            //运用onlineUsers的keySet直接生成list
            StringBuilder sb = new StringBuilder("Here is the list of online users in the chat room.\r\n");
            sb.append(ThreadForClient.onlineUsers.keySet().toString());
            return sb.toString();
        }else if (message.startsWith("/message ")){
            //不能/message然后不加空格，否则我不知道你要跟谁说话
            String dest = message.split(" ")[1];
            if (message.length() < ("/message " + dest + " ").length()){
                System.out.println("User " + username + " tries to whisper without a message.");
                return "Please enter a message to be whispered.";
            }
            String realMessage = message.substring(("/message " + dest + " ").length());
            //如果对方不在线就不要发了
            if (!ThreadForClient.onlineUsers.keySet().contains(dest)){
                System.out.println("User " + username + " tries to whisper to a user that is offline or exists not.");
                return "The user you tried to message to is offline or exists not.";
            } else if (dest.equals(username)){
                System.out.println("User " + username + " tries to whisper to himself.");
                return "You cannot whisper to yourself!";
            }
            
            System.out.println("User " + username + " whispers to " + dest + ": " + realMessage);
            return "`" + dest + "`" + realMessage;
        }
        else {
            System.out.println("User " + username + " sent an invalid command.");
            return "Invalid command. Please try again.";
        }
        //return null;
    }
}