package setting;

/**
 * @Author LYaopei
 */
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DoubanProxySetting {
    // 代理隧道验证信息
    public final static String ProxyUser = "HQ3HYI0NQLNMY1VD";
    public final static String ProxyPass = "1520A4CF467E32F9";

    // 代理服务器
    public final static String ProxyHost = "http-dyn.abuyun.com";
    public final static Integer ProxyPort = 9020;

    // 设置IP切换头
    public final static String ProxyHeadKey = "Proxy-Switch-Ip";
    public final static String ProxyHeadVal = "yes";



    private static class ProxyHolder{
        private static Proxy holder;
        static {
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
            Authenticator.setDefault(new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(ProxyUser, ProxyPass.toCharArray());
                }
            });
            holder = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ProxyHost, ProxyPort));
            System.out.println("Proxy Holder getting proxy");
        }
    }
    public static Proxy getProxy(){
       return ProxyHolder.holder;
    }

}

