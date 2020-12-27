package login.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCUtils {
    private  static DataSource ds;

   static {
       try {
       //1加载配置文件
       Properties properties = new Properties();
       //2使用classloader加载配置文件
       InputStream inputSteam = JDBCUtils.class.getClassLoader().getResourceAsStream("druid.properties");
       properties.load(inputSteam);
       //3初始化连接池对象
       ds = DruidDataSourceFactory.createDataSource(properties);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   //获取连接Connecttion
    public static Connection getConntion() throws SQLException{
       return ds.getConnection();
    }


    //获取连接池对象
    public static DataSource getDataSource(){
        return ds;
    }

}
