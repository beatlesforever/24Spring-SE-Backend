package com.example.sebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@MapperScan("com.example.sebackend.mapper")
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SeBackendApplication {
    public static void main(String[] args) {
        printHostAddress();
        ConfigurableApplicationContext run = SpringApplication.run(SeBackendApplication.class, args);
    }
    /**
     * 打印主机地址信息。
     * 该方法遍历所有的网络接口（如网卡）和它们的IP地址，打印出非回环（不是127.0.0.1）且处于激活状态的IPv4地址。
     * 不接受任何参数。
     * 无返回值。
     */
    private static void printHostAddress() {
        try {
            // 获取所有网络接口的枚举
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // 过滤掉回环接口（如127.0.0.1）和未激活的接口
                if (networkInterface.isLoopback() || !networkInterface.isUp())
                    continue;

                // 获取当前网络接口的所有IP地址枚举
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // 过滤出IPv4地址
                    if (addr.getHostAddress().indexOf(':') < 0) {
                        String ip = addr.getHostAddress();
                        System.out.println("Interface: " + networkInterface.getDisplayName() + " IP: " + ip);
                        System.out.println("http://" + ip + ":8080/static/customer/login.html");
                        System.out.println("http://" + ip + ":8080/static/admin/index.html");
                    }
                }
            }
        } catch (Exception e) {
            // 捕获并处理可能的异常，如网络接口信息获取失败
            System.out.println("Error retrieving network interface information");
        }
    }

}
