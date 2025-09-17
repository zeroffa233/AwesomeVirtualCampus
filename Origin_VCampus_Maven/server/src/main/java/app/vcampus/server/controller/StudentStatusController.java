package app.vcampus.server.controller;

import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.Student;
import app.vcampus.server.entity.User;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class StudentStatusController {
    /**
     *    Solve client to update student status information and update to the database
     *     The constraint is cardNumber  != null and when no such student throw exception
     *     Test passed on 2023/08/27
     * @param request  from client with role and uri
     * @param database database
     * @return  it returns an “OK” response with a map containing a JSON string representing the student information
     */
    @RouteMapping(uri = "student/updateInfo", role = "affairs_staff")
    public Response updateInfo(Request request, org.hibernate.Session database) {
        Student newStudent = IEntity.fromJson(request.getParams().get("student"), Student.class);

        if (newStudent == null) {
            return Response.Common.badRequest();
        }

        Transaction tx = database.beginTransaction();
        database.merge(newStudent);
        tx.commit();

        return Response.Common.ok(Map.of("student", newStudent.toJson()));
    }

    /**
     * Solve client to get now student status information
     *     The constraint are cardNumber  != null and when no such student throw exception
     *     Test passed on 2023/08/27
     * @param request  from client with role and uri
     * @param database database
     * @return  it returns an “OK” response with a map containing a JSON string representing the student information
     */
    @RouteMapping(uri = "student/getSelf", role = "student")
    public Response getSelf(Request request, org.hibernate.Session database) {
        Integer cardNumber = request.getSession().getCardNum();

        Student student = database.get(Student.class, cardNumber);

        if (student == null) {
            if (request.getSession().permission("student")) {
                User user = database.get(User.class, cardNumber);
                student = Student.getStudent(user);
                Transaction tx = database.beginTransaction();
                database.persist(student);
                tx.commit();
            } else {
                return Response.Common.error("no such card number");
            }
        }

        return Response.Common.ok(Map.of("student", student.toJson()));
    }


    /**
     *    Solve client to search student status information and update to the database
     *     The constraint is that students obtained by keyword search are not empty
     * @param request  from client with role and uri
     * @param database database
     * @return  it returns an “OK” response with a map list containing a JSON string representing the student information
     */
    @RouteMapping(uri = "student/filter", role = "affairs_staff")
    public Response filter(Request request, org.hibernate.Session database) {
        try {
            String keyword = request.getParams().get("keyword");
            List<Student> students;
            if (keyword == null) {
                students = Database.loadAllData(Student.class, database);
            } else {
                // 使用自定义查询以支持完整姓名搜索
                students = searchStudentWithFullName(keyword, database);
            }

            return Response.Common.ok(Map.of("students", students.stream().map(Student::toJson).collect(Collectors.toList())));
        } catch (Exception e) {
            log.warn("Failed to filter students", e);
            return Response.Common.error("Failed to filter students");
        }
    }

    /**
     * 支持完整姓名搜索的学生查询方法
     * @param keyword 搜索关键词
     * @param database 数据库会话
     * @return 匹配的学生列表
     */
    private List<Student> searchStudentWithFullName(String keyword, org.hibernate.Session database) {
        jakarta.persistence.criteria.CriteriaBuilder builder = database.getCriteriaBuilder();
        jakarta.persistence.criteria.CriteriaQuery<Student> criteria = builder.createQuery(Student.class);
        jakarta.persistence.criteria.Root<Student> studentRoot = criteria.from(Student.class);

        // 构建搜索条件
        java.util.ArrayList<jakarta.persistence.criteria.Predicate> conditions = new java.util.ArrayList<>();

        // 原有的字段搜索
        String[] searchFields = {"cardNumber", "studentNumber", "givenName", "familyName", "birthDate", "major", "school", "birthPlace"};
        for (String field : searchFields) {
            conditions.add(builder.like(studentRoot.get(field).as(String.class), "%" + keyword + "%"));
        }

        // 添加完整姓名搜索（familyName + givenName）
        jakarta.persistence.criteria.Expression<String> fullName = builder.concat(
                builder.concat(studentRoot.get("familyName").as(String.class), " "),
                studentRoot.get("givenName").as(String.class)
        );
        conditions.add(builder.like(fullName, "%" + keyword + "%"));

        // 添加无空格完整姓名搜索（familyName + givenName）
        jakarta.persistence.criteria.Expression<String> fullNameNoSpace = builder.concat(
                studentRoot.get("familyName").as(String.class),
                studentRoot.get("givenName").as(String.class)
        );
        conditions.add(builder.like(fullNameNoSpace, "%" + keyword + "%"));

        criteria.where(builder.or(conditions.toArray(new jakarta.persistence.criteria.Predicate[0])));
        return database.createQuery(criteria).getResultList();
    }
}

