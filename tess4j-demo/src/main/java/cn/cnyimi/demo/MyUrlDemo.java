package cn.cnyimi.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Charles
 * @package cn.cnyimi.demo
 * @classname Test
 * @description TODO
 * @date 2019-5-13 18:04
 */
public class MyUrlDemo {

    public static void main(String[] args) {
        MyUrlDemo muDemo = new MyUrlDemo();
        try {
            muDemo.showURL();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void showURL() throws IOException {

        // 第一种: 获取类加载的根路径   D:\ProjectDemo\test\target\classes
        File f1 = new File(this.getClass().getResource("/").getPath());
        System.out.println(f1);

        // 如果不加"/",则获取当前类的绝对路径   D:\ProjectDemo\test\target\classes\cn\cnyimi\demo
        File f2 = new File(this.getClass().getResource("").getPath());
        System.out.println(f2);


        // 第二种: 获取当前类的所在项目路径   D:\ProjectDemo
        File directory = new File("");// 参数为空
        String courseFile = directory.getCanonicalPath();
        System.out.println(courseFile);


        // 第三种: 获取当前工程资源文件目录下文件的路径 file:/D:/ProjectDemo/test/target/classes/
        URL xmlpath = this.getClass().getClassLoader().getResource("");
        URL xmlpath1 = this.getClass().getClassLoader().getResource("test.txt");
        System.out.println(xmlpath); //    file:/D:/ProjectDemo/test/target/classes/
        System.out.println(xmlpath1);//    file:/D:/ProjectDemo/tesst/target/classes/test.txt


        // 第四种:获取当前工程路径 D:\ProjectDemo
        System.out.println(System.getProperty("user.dir"));

        // 第五种: 获取所有的类路径 包括jar包的路径
        System.out.println(System.getProperty("java.class.path"));

    }
}
