package app.vcampus.server.controller;

import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.Student;
import app.vcampus.server.entity.User;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Password;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.*;

import java.util.Arrays;
import java.util.List;

/**
 * 管理员控制器。
 * 处理管理员相关的用户管理功能，如添加、搜索、修改和删除用户。
 */
@Slf4j
public class AdminController {
    /**
     * 管理员添加新用户。
     * 如果用户角色包含“学生”，则同时创建学生档案。
     *
     * @param request  包含用户信息的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "admin/user/add", role = "admin")
    public Response addUser(Request request, org.hibernate.Session database) {
        try {
            User user = IEntity.fromJson(request.getParams().get("user"), User.class);
            user.setPassword(Password.hash(user.password));

            Transaction tx = database.beginTransaction();
            database.persist(user);

            if (Arrays.stream(user.getRoles()).toList().contains("student")) {
                Student student = Student.getStudent(user);
                database.persist(student);
            }
            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            return Response.Common.error(e.getMessage());
        }
    }


    /**
     * 管理员搜索用户。
     * 如果提供了关键词，则进行模糊查询；否则返回所有用户。
     *
     * @param request  可能包含搜索关键词的请求。
     * @param database 数据库会话。
     * @return 包含用户列表的响应。
     */
    @RouteMapping(uri = "admin/user/search", role = "admin")
    public Response searchUser(Request request, org.hibernate.Session database) {
        try {
            String keyword = request.getParams().get("keyword");
            List<User> users;
            if (keyword == null || keyword.isBlank()) {
                users = Database.loadAllData(User.class, database);
            } else {
                users = Database.likeQuery(User.class, new String[]{"cardNum", "name", "phone", "email"}, keyword, database);
            }

            return Response.Common.ok(users.stream().peek(user -> user.setPassword(null)).map(User::toJson).toList());
        } catch (Exception e) {
            return Response.Common.error(e.getMessage());
        }
    }

    /**
     * 管理员修改用户信息（密码和角色）。
     *
     * @param request  包含用户卡号、新密码和新角色的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "admin/user/modify", role = "admin")
    public Response modifyUser(Request request, org.hibernate.Session database) {
        try {
            String password = request.getParams().get("password");
            String roleStr = request.getParams().get("roles");
            int cardNum = Integer.parseInt(request.getParams().get("cardNum"));

            User user = database.get(User.class, cardNum);
            if (user == null) {
                return Response.Common.error("User not found");
            }

            if (password != null && !password.isBlank()) {
                user.setPassword(Password.hash(password));
            }

            if (roleStr != null && !roleStr.isBlank()) {
                user.setRoles(roleStr.split(","));
            } else {
                user.setRoles(new String[]{});
            }

            Transaction tx = database.beginTransaction();
            database.merge(user);
            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            return Response.Common.error(e.getMessage());
        }
    }

    /**
     * 管理员删除用户。
     * 如果用户是学生，则同时删除其学生档案。
     *
     * @param request  包含用户卡号的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "admin/user/delete", role = "admin")
    public Response deleteUser(Request request, org.hibernate.Session database) {
        try {
            int cardNum = Integer.parseInt(request.getParams().get("cardNum"));

            User user = database.get(User.class, cardNum);
            if (user == null) {
                return Response.Common.error("User not found");
            }

            Transaction tx = database.beginTransaction();
            if (Arrays.stream(user.getRoles()).toList().contains("student")) {
                Student student = database.get(Student.class, user.getCardNum());
                if (student != null) {
                    database.remove(student);
                }
            }
            database.remove(user);
            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            return Response.Common.error(e.getMessage());
        }
    }
}