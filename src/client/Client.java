package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;


public class Client {
    private Socket socket;
    private JFrame conForm;
    private JPanel conPanel;
    private JButton conButton;
    private JLabel conText;
    private MainForm mainForm;

    public static void main(String[] args) {
        new Client();
    }

    private Client(){
        init();
    }

    private void init(){
        conForm = new JFrame("客户端");
        conForm.setBounds((1920-200)/2,(1080-180)/2,200,180);
        conForm.setLayout(null);

        conPanel = new JPanel();
        conPanel.setBounds(0,0,200,180);
        conPanel.setBackground(Color.WHITE);
        conPanel.setLayout(null);

        conText = new JLabel("连接状态：未连接");
        conText.setBounds((200-150)/2,30,150,25);
        conText.setFont(new Font("微软雅黑",Font.BOLD,18));
        conText.setHorizontalAlignment(SwingConstants.CENTER);
        conText.setVerticalAlignment(SwingConstants.CENTER);
        conPanel.add(conText);

        conButton = new JButton("连接服务器");
        conButton.setBounds((200-125)/2,80,125,45);
        conButton.setFont(new Font("黑体",Font.PLAIN,18));
        conButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if(conButton.getText().equals("连接服务器")){
                    try {
                        socket = new Socket("10.8.24.98",2020);
                    } catch (Exception e){
                        conText.setForeground(Color.BLACK);
                        conText.setText("连接服务器失败");
                        conPanel.setBackground(Color.RED);
                        return;
                    }
                    conText.setForeground(Color.BLACK);
                    conText.setText("已连接服务器");
                    conButton.setText("断开连接");
                    conPanel.setBackground(Color.GREEN);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }else{
                    conButton.setText("连接服务器");
                    conText.setForeground(Color.red);

                    conText.setText("连接状态：未连接");
                    conPanel.setBackground(Color.WHITE);
                    socket = null;
                }
            }
        });
        conPanel.add(conButton);
        conForm.add(conPanel);
        conForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        conForm.setResizable(false);
        conForm.setVisible(true);

        //客户端
//        try {
//
//
//
////                OutputStream output = socket.getOutputStream();//字节输出流
////                for(int i = 0;i<10;i++){
////                    output.write(("第"+i+"次发送").getBytes());
////                    Thread.sleep(1000);
////                }
////                socket.shutdownOutput();
//
//            InputStream input = socket.getInputStream();
//            byte[] dataRec = new byte[1024];
//            int len;
//            while((len = input.read(dataRec))!=-1){
//                System.out.println(new String(dataRec,0,len));
//            }
//            socket.shutdownInput();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private void formLoad(){

    }
    class MainForm extends JFrame{

    }
}

