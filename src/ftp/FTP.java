package ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class FTP {
    public static void main(String[] args) throws IOException {
        //使用URL读取网页内容
//创建一个URL实例
        URL url =new URL("http://www.baidu.com");
        InputStream is = url.openStream();//通过openStream方法获取资源的字节输入流
        InputStreamReader isr =new InputStreamReader(is,"UTF-8");//将字节输入流转换为字符输入流,如果不指定编码，中文可能会出现乱码
        BufferedReader br =new BufferedReader(isr);//为字符输入流添加缓冲，提高读取效率
        String data = br.readLine();//读取数据
        while(data!=null){
            System.out.println(data);//输出数据
            data = br.readLine();
        }
        br.close();
        isr.close();
        is.close();
        //获取本机的InetAddress实例
/*        InetAddress address =InetAddress.getLocalHost();
        String hostName = address.getHostName();//获取计算机名
        String hostAddress = address.getHostAddress();//获取IP地址

        byte[] bytes = address.getAddress();//获取字节数组形式的IP地址,以点分隔的四部分

        //获取其他主机的InetAddress实例
        //InetAddress address2 =InetAddress.getByName("其他主机名");
        InetAddress address3;
        for(int i=2;i<255;i++){
             address = InetAddress.getByName("10.8.24."+i);
            System.out.println(address.getHostName()+":"+address.getHostAddress());
        }*/
    }
}
