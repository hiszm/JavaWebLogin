

### 案例：用户登录
* 用户登录案例需求：
```
1.编写login.html登录页面 username & password 两个输入框
2.使用Druid数据库连接池技术,操作mysql，day14数据库中user表
3.使用JdbcTemplate技术封装JDBC
4.登录成功跳转到SuccessServlet展示：登录成功！用户名,欢迎您
5.登录失败跳转到FailServlet展示：登录失败，用户名或密码错误
```

![流程](https://img-blog.csdnimg.cn/20201108141157739.png)


	* 分析
	
	
	* 开发步骤
	
	
> 1. 创建项目，导入html页面，配置文件，jar包

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    <form action="/JavaWeb_login_war_exploded/loginServlet" method="post">
        用户名:<input type="text" name="username"> <br>
        密码:<input type="password" name="password"><br>

        <input type="submit" value="登录">

    </form>
</body>
</html>
```

> 2. 创建数据库环境

```sql
CREATE DATABASE login_test;
USE login_test;
CREATE TABLE USER(
	id INT PRIMARY KEY AUTO_INCREMENT,
	username VARCHAR(32) UNIQUE NOT NULL,
	PASSWORD VARCHAR(32) NOT NULL
);
```
> 3. 创建包login.domain,创建类User

```java
package login.domain;
/**
 * 用户的实体类
 */
public class User {

    private int id;
    private String username;
    private String password;


​			
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
```


4. 创建包login.util,编写工具类JDBCUtils
```java
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


```
> 5. 创建包logint.dao,创建类UserDao,提供login方法

```java
package login.dao;

import login.domain.User;
import login.util.JDBCUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDao {

    //声明JDBCTemplate对象共用
    private JdbcTemplate template = new JdbcTemplate(JDBCUtils.getDataSource());

    /**
     * 登录方法
     * @param loginUser 只有用户名和密码
     * @return user包含用户全部数据,没有查询到，返回null
     */
    public User login(User loginUser){
        try {
            //1.编写sql
            String sql = "select * from user where username = ? and password = ?";
            //2.调用query方法
            User user = template.queryForObject(sql,
                    new BeanPropertyRowMapper<User>(User.class),
                    loginUser.getUsername(), loginUser.getPassword());


            return user;
        } catch (DataAccessException e) {
            e.printStackTrace();//记录日志
            return null;
        }
    }

}

> 6. 编写cn.web.servlet.LoginServlet类


```java
package login.servlet;

import login.dao.UserDao;
import login.domain.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/loginServlet")
public class LoginServlet extends HttpServlet{


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.设置编码
        req.setCharacterEncoding("utf-8");
        //2.获取请求参数
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        //3.封装user对象
        User loginUser = new User();
        loginUser.setUsername(username);
        loginUser.setPassword(password);

        //4.调用UserDao的login方法
        UserDao dao = new UserDao();
        User user = dao.login(loginUser);

        //5.判断user
        if(user == null){
            //登录失败
            req.getRequestDispatcher("/failServlet").forward(req,resp);
        }else{
            //登录成功
            //存储数据
            req.setAttribute("user",user);
            //转发
            req.getRequestDispatcher("/successServlet").forward(req,resp);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req,resp);
    }
}


```


> 7. 编写FailServlet和SuccessServlet类


```java

package login.servlet;

import login.domain.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/successServlet")
public class SuccessServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //获取request域中共享的user对象
        User user = (User) request.getAttribute("user");

        if (user != null) {
            //给页面写一句话

            //设置编码
            response.setContentType("text/html;charset=utf-8");
            //输出
            response.getWriter().write("登录成功！" + user.getUsername() + ",欢迎您");
        }


    }
}	


package login.servlet;

import login.domain.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet("/failServlet")
public class FailServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //给页面写一句话

        //设置编码
        response.setContentType("text/html;charset=utf-8");
        //输出
        response.getWriter().write("登录失败，用户名或密码错误");

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request,response);
    }
}

```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20201109093623772.png#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20201109093638778.png#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/2020110909371247.png#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20201109093656887.png#pic_center)

> 8. login.html中form表单的action路径的写法
* 虚拟目录+Servlet的资源路径
	

```
        //2.获取请求参数
//        String username = req.getParameter("username");
//        String password = req.getParameter("password");
//        //3.封装user对象
//        User loginUser = new User();
//        loginUser.setUsername(username);
//        loginUser.setPassword(password);

        Map<String,String[]> map = req.getParameterMap()
        User loginUser = new User();
        try {
            BeanUtils.populate(loginUser,map);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
```
> 9. BeanUtils工具类，**简化数据封装**

* 用于封装JavaBean的,`可以用于接受多个参数请求`**封装数据**


-  JavaBean：标准的Java类
```
	1. 类必须被public修饰
	2. 必须提供空参的构造器
	3. 成员变量必须使用private修饰
	4. 提供公共setter和getter方法

```
```

-  概念：
		成员变量：
		属性：setter和getter方法截取后的产物
			例如：getUsername() --> Username--> username


-  方法：
		1. setProperty()
		2. getProperty()
		3. `populate(Object obj , Map map)`:将map集合的键值对信息，封装到对应的JavaBean对象中
```
