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

/**
 * 学籍信息控制器。
 * 处理学生学籍信息的获取、更新和搜索。
 */
@Slf4j
public class StudentStatusController {
    /**
     * 更新学生学籍信息。
     *
     * @param request  包含学生信息的请求。
     * @param database 数据库会话。
     * @return 包含更新后学生信息的响应。
     */
    @RouteMapping(uri = "student/updateInfo", role = "admin")
    public Response updateInfo(Request request, org.hibernate.Session database) {
        Student newStudent = IEntity.fromJson(request.getParams().get("student"), Student.class);

        if (newStudent == null || newStudent.getCardNumber() == null) {
            return Response.Common.badRequest();
        }

        try {
            // 先从数据库获取现有学生信息
            Student existingStudent = database.get(Student.class, newStudent.getCardNumber());
            if (existingStudent == null) {
                return Response.Common.notFound();
            }

            // 只更新客户端提供的非null字段
            if (newStudent.getStudentNumber() != null) {
                existingStudent.setStudentNumber(newStudent.getStudentNumber());
            }
            if (newStudent.getFamilyName() != null) {
                existingStudent.setFamilyName(newStudent.getFamilyName());
            }
            if (newStudent.getGivenName() != null) {
                existingStudent.setGivenName(newStudent.getGivenName());
            }
            // 注意：这里不更新gender字段，因为客户端没提供，保留原有值
            if (newStudent.getBirthDate() != null) {
                existingStudent.setBirthDate(newStudent.getBirthDate());
            }
            if (newStudent.getMajor() != null) {
                existingStudent.setMajor(newStudent.getMajor());
            }
            if (newStudent.getSchool() != null) {
                existingStudent.setSchool(newStudent.getSchool());
            }
            if (newStudent.getStatus() != null) {
                existingStudent.setStatus(newStudent.getStatus());
            }
            if (newStudent.getBirthPlace() != null) {
                existingStudent.setBirthPlace(newStudent.getBirthPlace());
            }
            if (newStudent.getPoliticalStatus() != null) {
                existingStudent.setPoliticalStatus(newStudent.getPoliticalStatus());
            }

            Transaction tx = database.beginTransaction();
            database.merge(existingStudent);
            tx.commit();
            return Response.Common.ok(Map.of("student", existingStudent.toJson()));
        } catch (Exception e) {
            log.error("更新学生信息失败: {}", e.getMessage());
            return Response.Common.error("更新学生信息失败: " + e.getMessage());
        }
    }
    /**
     * 获取当前登录学生自己的学籍信息。
     *
     * @param request  请求对象，包含会话信息。
     * @param database 数据库会话。
     * @return 包含学生信息的响应。
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
     * 根据关键词筛选学生信息。
     *
     * @param request  包含搜索关键词的请求。
     * @param database 数据库会话。
     * @return 包含匹配学生列表的响应。
     */
    @RouteMapping(uri = "student/filter", role = "admin")
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
     * 使用支持完整姓名搜索的自定义查询方法来查找学生。
     *
     * @param keyword  搜索关键词。
     * @param database 数据库会话。
     * @return 匹配的学生列表。
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