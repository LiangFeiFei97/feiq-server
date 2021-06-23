package server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Server extends JFrame {
    private static final int maxConnect = 100;
    private static final int maxFileCount = 100;
    private final ThreadPool threadPool;
    private SystemTray systemTray;
    private final ChatServer chatServer;
    private final FileServer fileServer;
    private int ConnectCount = 0;

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        this.setVisible(false);
        PopupMenu popup = new PopupMenu();
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        popup.add(exitMenuItem);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setUndecorated(true);
        this.setType(Window.Type.UTILITY);
        Image icon = new ImageIcon(Server.class.getResource("/icon.png")).getImage();
        TrayIcon trayIcon;
        trayIcon = new TrayIcon(icon, "运行中", popup);
        try {
            if (systemTray == null) {
                systemTray = SystemTray.getSystemTray();
                systemTray.remove(trayIcon);
            }
            systemTray.add(trayIcon);
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
        threadPool = new ThreadPool();
        chatServer = new ChatServer();
        chatServer.start();
        fileServer = new FileServer();
        fileServer.start();
    }

    private class FileServer extends Thread {
        private String[] clientList;

        private FileServer() {
            super();
        }

        @Override
        public void run() {
            try {
                clientList = new String[maxConnect];
                for (int i = 0; i < maxConnect; i++) {
                    clientList[i] = "";
                }
                ServerSocket serverSocket = new ServerSocket(2048);
                while (true) {
                    Socket socket = serverSocket.accept();
                    FileThread thread = threadPool.createFileThread(socket);
                    new Thread(thread).start();
                    for (int i = 0; i < maxConnect; i++) {
                        if (clientList[i].equals("")) {
                            clientList[i] = socket.getInetAddress().getHostAddress();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    private class ChatServer extends Thread {
        private String[] clientList;

        @Override
        public void run() {
            try {
                clientList = new String[maxConnect];
                for (int i = 0; i < maxConnect; i++) {
                    clientList[i] = "";
                }
                ServerSocket serverSocket = new ServerSocket(2020);
                while (true) {
                    Socket socket = serverSocket.accept();
                    ChatThread thread = threadPool.createChatThread(socket);
                    new Thread(thread).start();
                    for (int i = 0; i < maxConnect; i++) {
                        if (clientList[i].equals("")) {
                            clientList[i] = socket.getInetAddress().getHostAddress();
                            break;
                        }
                    }
                    Thread.sleep(1000);
                    notice("OnConnect", socket.getInetAddress().getHostAddress());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("ServerError");
            }
        }

        private ChatServer() {
            super();
        }

        private void fileTrans(String ipSend, String ipRec) {
            if (threadPool.getFileThread(ipRec) != null)
                Objects.requireNonNull(threadPool.getFileThread(ipRec)).append(ipSend);
        }

        private void notice(String ip) {
            try {
                StringBuilder str = new StringBuilder("Refresh@");
                for (int i = 0; i < maxConnect; i++) {
                    if (!clientList[i].equals(""))
                        str.append(clientList[i]).append(",");
                }
                for (int i = 0; i < maxConnect; i++) {
                    if (clientList[i].equals(ip)) {
                        if (threadPool.getChatThread(clientList[i]) != null) {
                            Objects.requireNonNull(threadPool.getChatThread(clientList[i])).msgSend(str.toString());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("NoticeError01");
            }
        }

        private void notice(String type, String ip) {
            try {
                String str = type + "@" + ip;
                for (int i = 0; i < maxConnect; i++) {
                    if (!clientList[i].equals("") && !clientList[i].equals(ip))
                        if (threadPool.getChatThread(clientList[i]) != null)
                            Objects.requireNonNull(threadPool.getChatThread(clientList[i])).msgSend(str);
                }
            } catch (Exception e) {
                //System.out.println("NoticeError02");
            }
        }
    }

    private class FileThread extends MyThread {
        private boolean isBusy;
        private String[] tranList;
        private int fileCount;
        private String ipSend;

        private FileThread() {
            super();
        }

        private FileThread(Socket socket) {
            super(socket);
            this.isBusy = false;
            this.tranList = new String[maxFileCount];
            for (int i = 0; i < maxConnect; i++) {
                this.tranList[i] = "";
            }
            this.fileCount = 0;
            this.ipSend = "";
        }

        @Override
        public void run() {
            fileRec();
        }

        private void fileRec() {
            while (isEnable()) {
                try {
                    byte[] bytes = new byte[1024];
                    int len;
                    String str;
                    while ((len = getSocket().getInputStream().read(bytes)) != -1) {
                        str = new String(bytes, 0, len);
                        if (str.contains("@END")) {
                            if (str.indexOf("@END") != 0) {
                                str = str.substring(0, str.indexOf("@END"));
                                Objects.requireNonNull(threadPool.getFileThread(ipSend)).fileSend(str);
                                break;
                            }
                        }
                        if (threadPool.getFileThread(ipSend) != null) {
                            Objects.requireNonNull(threadPool.getFileThread(ipSend)).fileSend(str);
                        }
                    }
                } catch (Exception e) {
                    for (int i = 0; i < maxConnect; i++) {
                        if (fileServer.clientList[i].equals(getIp())) {
                            String ip = getIp();
                            threadPool.returnFileThread(ip);
                            fileServer.clientList[i] = "";
                            System.out.println(ip + "停止文件传输");
                            break;
                        }
                    }
                }
            }
        }

        private void fileSend(String bytes) {
            try {
                OutputStream output = getSocket().getOutputStream();
                output.write(bytes.getBytes());
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendOver(String ip) {
            for (int i = 0; i < maxFileCount; i++) {
                if (tranList[i].equals(ip)) {
                    tranList[i] = "";
                    break;
                }
            }
            fileCount--;
            isBusy = false;
            recReady();
        }

        private void recReady() {
            if (!isBusy) {
                if (fileCount <= 0) {
                    return;
                }
                for (String s : tranList) {
                    if (!s.equals("")) {
                        Objects.requireNonNull(threadPool.getChatThread(s)).msgSend("FileRecReady@" + getIp());
                        ipSend = s;
                        isBusy = true;
                        break;
                    }
                }
            }
        }

        private void append(String ip) {
            for (int i = 0; i < maxFileCount; i++) {
                if (tranList[i].equals("")) {
                    tranList[i] = ip;
                    fileCount++;
                    recReady();
                    break;
                }
            }
        }
    }

    private class ChatThread extends MyThread {
        private ChatThread() {
            super();
        }

        private ChatThread(Socket socket) {
            super(socket);
        }

        @Override
        public void run() {
            msgRec();
        }

        private void msgRec() {
            while (isEnable()) {
                try {
                    byte[] byteRec = new byte[1024];
                    int len;
                    StringBuilder strB = new StringBuilder();
                    String strEnd = "";
                    while ((len = getSocket().getInputStream().read(byteRec)) != -1) {
                        strEnd = new String(byteRec, 0, len);
                        if (strEnd.contains("@END")) break;
                        strB.append(strEnd);
                    }
                    if (strEnd.indexOf("@END") != 0)
                        strB.append(strEnd, 0, strEnd.indexOf("@END"));
                    //System.out.println(strB.toString());
                    String strRec = strB.toString();
                    int pos = strRec.indexOf("@");
                    String head = strRec.substring(0, pos);
                    String body = "";
                    if (pos < strRec.length())
                        body = strRec.substring(pos + 1);
                    if (head.equals("connect")) {//用户上线
                        ConnectCount++;
                        System.out.println("上线通知：" + getIp() + " 上线！");
                        System.out.println("当前客户端连接数量：" + ConnectCount + "台");
                        chatServer.notice(getIp());
                    } else if (head.equals("FileSend")) {//请求发送文件
                        chatServer.fileTrans(getIp(), body);
                    } else if (head.equals("SendOver")) {//文件发送完毕
                        Objects.requireNonNull(threadPool.getFileThread(body)).sendOver(getIp());
                    } else if (threadPool.getChatThread(head) != null) {//传输聊天消息
                        Objects.requireNonNull(threadPool.getChatThread(head)).msgSend(getIp() + strRec);
                    }
                } catch (IOException e) {
                    for (int i = 0; i < maxConnect; i++) {
                        if (chatServer.clientList[i].equals(getIp())) {
                            String ip = getIp();
                            threadPool.returnChatThread(ip);
                            chatServer.clientList[i] = "";
                            ConnectCount--;
                            System.out.println("下线通知：" + ip + " 已下线！");
                            System.out.println("当前客户端连接数量：" + ConnectCount + "台");
                            chatServer.notice("DisConnect", ip);
                            break;
                        }
                    }
                }
            }
        }

        private synchronized void msgSend(String str) {
            try {
                OutputStream output = getSocket().getOutputStream();
                output.write(str.getBytes());
                output.write("@END".getBytes());
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("msgSendError");
            }
        }
    }

    private static class MyThread implements Runnable {
        private Socket socket;
        private String ip;
        private boolean enable;

        void init() {
            this.socket = null;
            this.ip = "";
            this.enable = false;
        }

        String getIp() {
            return ip;
        }

        boolean isEnable() {
            return enable;
        }

        Socket getSocket() {
            return socket;
        }

        private MyThread() {
            init();
        }

        private MyThread(Socket socket) {
            this.socket = socket;
            this.ip = socket.getInetAddress().getHostAddress();
            this.enable = true;
        }

        @Override
        public void run() {

        }
    }

    private class ThreadPool {
        private final ChatThread[] chatThreads;
        private final FileThread[] fileThreads;
        private final int tCount;

        private ThreadPool() {
            tCount = Server.maxConnect;
            chatThreads = new ChatThread[tCount];
            fileThreads = new FileThread[tCount];
            for (int i = 0; i < tCount; i++) {
                chatThreads[i] = new ChatThread();
                fileThreads[i] = new FileThread();
            }
        }

        private ChatThread createChatThread(Socket socket) {
            if (getChatThread(socket.getInetAddress().getHostAddress()) == null) {
                for (int i = 0; i < tCount; i++) {
                    if (!chatThreads[i].isEnable()) {
                        chatThreads[i] = new ChatThread(socket);
                        return chatThreads[i];
                    }
                }
            }
            return getChatThread(socket.getInetAddress().getHostAddress());
        }

        private FileThread createFileThread(Socket socket) {
            if (getFileThread(socket.getInetAddress().getHostAddress()) == null) {
                for (int i = 0; i < tCount; i++) {
                    if (!fileThreads[i].isEnable()) {
                        fileThreads[i] = new FileThread(socket);
                        return fileThreads[i];
                    }
                }
            }
            return getFileThread(socket.getInetAddress().getHostAddress());
        }

        private ChatThread getChatThread(String ip) {
            for (int i = 0; i < tCount; i++) {
                if (chatThreads[i].getIp().equals(ip))
                    return chatThreads[i];
            }
            return null;
        }

        private FileThread getFileThread(String ip) {
            for (int i = 0; i < tCount; i++) {
                if (fileThreads[i].getIp().equals(ip))
                    return fileThreads[i];
            }
            return null;
        }

        private void returnChatThread(String ip) {
            ChatThread thread = getChatThread(ip);
            if (thread != null) {
                thread.init();
            }
        }

        private void returnFileThread(String ip) {
            FileThread thread = getFileThread(ip);
            if (thread != null) {
                thread.init();
            }
        }
    }
}
