package app.vcampus.server.controller;

import app.vcampus.server.entity.*;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Pair;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import com.alibaba.excel.EasyExcel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 教务控制器。
 * 处理与学生、教师和管理员相关的教务操作。
 */
@Slf4j
public class TeachingAffairsController {
    /**
     * 获取学生已选课程列表。
     *
     * @param request  请求对象，包含会话信息。
     * @param database 数据库会话。
     * @return 包含学生已选课程列表的响应。
     */
    @RouteMapping(uri = "teaching/student/getMyClasses", role = "student")
    public Response getSelectedClasses(Request request, org.hibernate.Session database) {
        int cardNumber = request.getSession().getCardNum();

        List<SelectRecord> selectRecords = Database.getWhereString(SelectRecord.class, "cardNumber", Integer.toString(cardNumber), database);
        List<TeachingClass> teachingClasses = selectRecords.stream().map((SelectRecord sc) -> {
            Grades grades = sc.getGrade();
            if (grades != null) {
                List<SelectRecord> classmates = Database.getWhereUuid(SelectRecord.class, "classUuid", sc.getClassUuid(), database);
                List<Grades> gradesList = classmates.stream().map(SelectRecord::getGrade).toList();
                grades.classAvg = gradesList.stream().mapToInt(Grades::getTotal).average().orElse(0);
                grades.classMax = gradesList.stream().mapToInt(Grades::getTotal).max().orElse(0);
                grades.classMin = gradesList.stream().mapToInt(Grades::getTotal).min().orElse(0);
                sc.setGrade(grades);
            }

            TeachingClass teachingClass = database.get(TeachingClass.class, sc.getClassUuid());
            teachingClass.setSelectRecord(sc);
            return teachingClass;
        }).toList();

        List<TeachingEvaluation> evaluations = Database.getWhereString(TeachingEvaluation.class, "studentId", Integer.toString(cardNumber), database);

        teachingClasses = teachingClasses.stream().peek((TeachingClass tc) -> {
            User teacher = database.get(User.class, tc.getTeacherId());
            tc.setTeacherName(teacher.getName());

            Course course = database.get(Course.class, tc.getCourseUuid());
            tc.setCourse(course);

            tc.setIsEvaluated(evaluations.stream().anyMatch((TeachingEvaluation te) -> te.classUuid.equals(tc.getUuid())));
        }).toList();

        return Response.Common.ok(Map.of("classes", teachingClasses.stream().map(TeachingClass::toJson).toList()));
    }

    /**
     * 提交教学评估。
     *
     * @param request  请求对象，包含评估信息。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "teaching/student/submitEvaluation", role = "student")
    public Response submitEvaluation(Request request, org.hibernate.Session database) {
        int cardNumber = request.getSession().getCardNum();
        Type type = new TypeToken<Pair<UUID, Pair<List<Integer>, String>>>() {
        }.getType();
        Pair<UUID, Pair<List<Integer>, String>> evaluationResult = new Gson().fromJson(request.getParams().get("evaluation"), type);

        try {
            Transaction tx = database.beginTransaction();
            TeachingEvaluation newTeachingEvaluation = new TeachingEvaluation();
            newTeachingEvaluation.setUuid(UUID.randomUUID());
            newTeachingEvaluation.setClassUuid(evaluationResult.getFirst());
            newTeachingEvaluation.setStudentId(cardNumber);
            newTeachingEvaluation.setResult(evaluationResult.getSecond().getFirst());
            newTeachingEvaluation.setComment(evaluationResult.getSecond().getSecond());
            database.persist(newTeachingEvaluation);
            tx.commit();
            return Response.Common.ok();
        } catch (Exception e) {
            log.warn("Failed to add evaluation", e);
            return Response.Common.badRequest();
        }
    }

    /**
     * 获取所有可选课程列表。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含可选课程列表的响应。
     */
    @RouteMapping(uri = "teaching/student/getSelectableCourses", role = "student")
    public Response getSelectableCourses(Request request, org.hibernate.Session database) {
        List<Course> courses = Database.loadAllData(Course.class, database);
        courses = courses.stream().peek((Course course) -> {
            List<TeachingClass> teachingClasses = Database.getWhereUuid(TeachingClass.class, "courseUuid", course.getUuid(), database);
            teachingClasses = teachingClasses.stream().peek((TeachingClass tc) -> {
                User teacher = database.get(User.class, tc.getTeacherId());
                tc.setTeacherName(teacher.getName());

                tc.setSelectedCount(Database.getWhereUuid(SelectRecord.class, "classUuid", tc.getUuid(), database).size());

                SelectRecord selectRecord = Database.getWhereUuid(SelectRecord.class, "classUuid", tc.getUuid(), database).stream()
                        .filter((SelectRecord sr) -> sr.getCardNumber().equals(request.getSession().getCardNum()))
                        .findFirst().orElse(null);
                tc.setSelectRecord(selectRecord);
            }).toList();
            course.setTeachingClasses(teachingClasses);
        }).toList();

        return Response.Common.ok(Map.of("courses", courses.stream().map(Course::toJson).toList()));
    }

    /**
     * 评价课程。
     *
     * @param request  请求对象，包含评价信息。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "teaching/student/evaluate", role = "student")
    public Response evaluateClass(Request request, org.hibernate.Session database) {
        int cardNumber = request.getSession().getCardNum();
        TeachingEvaluation newTeachingEvaluation = IEntity.fromJson(request.getParams().get("evaluation"), TeachingEvaluation.class);

        newTeachingEvaluation.studentId = cardNumber;

        try {
            Transaction tx = database.beginTransaction();
            database.persist(newTeachingEvaluation);
            tx.commit();
            return Response.Common.ok();
        } catch (Exception e) {
            log.warn("Failed to add evaluation", e);
            return Response.Common.badRequest();
        }
    }

    /**
     * 学生选课。
     *
     * @param request  请求对象，包含课程UUID。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "teaching/student/chooseClass", role = "student")
    public Response selectClass(Request request, org.hibernate.Session database) {
        int cardNumber = request.getSession().getCardNum();
        UUID classUuid = UUID.fromString(request.getParams().get("classUuid"));

        SelectRecord selectRecord = new SelectRecord();
        selectRecord.setUuid(UUID.randomUUID());
        selectRecord.setCardNumber(cardNumber);
        selectRecord.setClassUuid(classUuid);
        selectRecord.setSelectTime(new Date());

        try {
            Transaction tx = database.beginTransaction();
            database.persist(selectRecord);
            tx.commit();
            return Response.Common.ok();
        } catch (Exception e) {
            log.warn("Failed to select class", e);
            return Response.Common.badRequest();
        }
    }

    /**
     * 学生退课。
     *
     * @param request  请求对象，包含课程UUID。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "teaching/student/dropClass", role = "student")
    public Response dropClass(Request request, org.hibernate.Session database) {
        int cardNumber = request.getSession().getCardNum();
        UUID classUuid = UUID.fromString(request.getParams().get("classUuid"));

        SelectRecord selectRecord = Database.getWhereUuid(SelectRecord.class, "classUuid", classUuid, database).stream()
                .filter((SelectRecord sr) -> sr.getCardNumber().equals(cardNumber))
                .findFirst().orElse(null);

        if (selectRecord == null) {
            return Response.Common.error("No such class");
        }

        if (selectRecord.getGrade() != null) {
            return Response.Common.error("Cannot drop class after grades are recorded");
        }

        try {
            Transaction tx = database.beginTransaction();
            database.remove(selectRecord);
            tx.commit();
            return Response.Common.ok();
        } catch (Exception e) {
            log.warn("Failed to drop class", e);
            return Response.Common.badRequest();
        }
    }

    /**
     * 教师获取其教授的课程列表。
     *
     * @param request  请求对象，包含会话信息。
     * @param database 数据库会话。
     * @return 包含教师课程列表的响应。
     */
    @RouteMapping(uri = "teaching/teacher/getMyClasses", role = "teacher")
    public Response getMyClasses(Request request, org.hibernate.Session database) {
        int cardNumber = request.getSession().getCardNum();
        List<TeachingClass> teachingClasses = Database.getWhereString(TeachingClass.class, "teacherId", Integer.toString(cardNumber), database);
        teachingClasses = teachingClasses.stream().peek((TeachingClass tc) -> {
            Course course = database.get(Course.class, tc.getCourseUuid());
            tc.setCourse(course);

            List<TeachingEvaluation> evaluations = Database.getWhereUuid(TeachingEvaluation.class, "classUuid", tc.getUuid(), database);
            List<List<Integer>> evaluationRatings = new ArrayList<>();
            List<String> evaluationComments = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                evaluationRatings.add(new ArrayList<>());
                for (int j = 0; j < 10; j++) {
                    evaluationRatings.get(i).add(0);
                }
            }
            evaluations.forEach((TeachingEvaluation te) -> {
                final int[] index = {0};
                te.result.forEach((Integer score) -> {
                    evaluationRatings.get(index[0]).set(score - 1, evaluationRatings.get(index[0]).get(score - 1) + 1);
                    index[0]++;
                });

                if (!te.comment.isBlank()) evaluationComments.add(te.comment);
            });
            tc.setEvaluationResult(new Pair<>(evaluationRatings, evaluationComments));
        }).toList();

        return Response.Common.ok(Map.of("classes", teachingClasses.stream().map(TeachingClass::toJson).toList()));
    }

    /**
     * 导出学生名单为 Excel 文件。
     *
     * @param request  请求对象，包含课程UUID。
     * @param database 数据库会话。
     * @return 包含Base64编码的Excel文件的响应。
     */
    @RouteMapping(uri = "teaching/teacher/exportStudentList", role = "teacher")
    public Response exportStudentList(Request request, org.hibernate.Session database) {
        try {
            UUID classUuid = UUID.fromString(request.getParams().get("classUuid"));
            List<SelectRecord> selectRecords = Database.getWhereUuid(SelectRecord.class, "classUuid", classUuid, database);

            Path tmpFile = Files.createTempFile("studentList", ".xlsx");
            tmpFile.toFile().deleteOnExit();
            EasyExcel.write(tmpFile.toFile(), ExcelStudentList.class)
                    .sheet()
                    .doWrite(selectRecords.stream().map((SelectRecord sr) -> {
                        Student student = database.get(Student.class, sr.getCardNumber());
                        return new ExcelStudentList(student.getStudentNumber(), student.getFamilyName() + student.getGivenName());
                    }).toList());

            return Response.Common.ok(Map.of("file", Base64.getEncoder().encodeToString(Files.readAllBytes(tmpFile))));
        } catch (Exception e) {
            log.warn("Failed to create Excel", e);
            return Response.Common.error("Failed to export");
        }
    }

    /**
     * 导出成绩模板为 Excel 文件。
     *
     * @param request  请求对象，包含课程UUID。
     * @param database 数据库会话。
     * @return 包含Base64编码的Excel文件的响应。
     */
    @RouteMapping(uri = "teaching/teacher/exportGradeTemplate", role = "teacher")
    public Response getGradeTemplate(Request request, org.hibernate.Session database) {
        try {
            UUID classUuid = UUID.fromString(request.getParams().get("classUuid"));
            List<SelectRecord> selectRecords = Database.getWhereUuid(SelectRecord.class, "classUuid", classUuid, database);

            Path tmpFile = Files.createTempFile("gradeTemplate", ".xlsx");
            tmpFile.toFile().deleteOnExit();
            EasyExcel.write(tmpFile.toFile(), ExcelStudentGrade.class)
                    .sheet()
                    .doWrite(selectRecords.stream().map((SelectRecord sr) -> {
                        Student student = database.get(Student.class, sr.getCardNumber());
                        ExcelStudentGrade e = new ExcelStudentGrade(student.getStudentNumber(), student.getFamilyName() + student.getGivenName());

                        if (sr.getGrade() != null) {
                            e.setGeneral(sr.getGrade().getGeneral());
                            e.setMidterm(sr.getGrade().getMidterm());
                            e.setFinalExam(sr.getGrade().getFinalExam());
                            e.setTotal(sr.getGrade().getTotal());
                        }

                        return e;
                    }).toList());

            return Response.Common.ok(Map.of("file", Base64.getEncoder().encodeToString(Files.readAllBytes(tmpFile))));
        } catch (Exception e) {
            log.warn("Failed to create Excel", e);
            return Response.Common.error("Failed to export");
        }
    }

    /**
     * 从 Excel 文件导入学生成绩。
     *
     * @param request  请求对象，包含课程UUID和Base64编码的Excel文件。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "teaching/teacher/importGrade", role = "teacher")
    public Response importGrade(Request request, org.hibernate.Session database) {
        try {
            UUID classUuid = UUID.fromString(request.getParams().get("classUuid"));
            String file = request.getParams().get("file");
            byte[] fileBytes = Base64.getDecoder().decode(file);
            Path tmpFile = Files.createTempFile("gradeTemplate", ".xlsx");
            tmpFile.toFile().deleteOnExit();
            Files.write(tmpFile, fileBytes);

            List<ExcelStudentGrade> studentGrades = EasyExcel.read(tmpFile.toFile()).head(ExcelStudentGrade.class).sheet().doReadSync();

            Transaction tx = database.beginTransaction();
            for (ExcelStudentGrade studentGrade : studentGrades) {
                Student student = Database.getWhereString(Student.class, "studentNumber", studentGrade.getStudentNumber(), database).stream().findFirst().orElse(null);
                if (student == null) {
                    return Response.Common.error("No such student");
                }
                SelectRecord selectRecord = Database.getWhereUuid(SelectRecord.class, "classUuid", classUuid, database).stream()
                        .filter((SelectRecord sr) -> sr.getCardNumber().equals(student.getCardNumber()))
                        .findFirst().orElse(null);

                if (selectRecord == null) {
                    return Response.Common.error("No such student");
                }

                Grades grades = new Grades();
                grades.setGeneral(studentGrade.getGeneral());
                grades.setMidterm(studentGrade.getMidterm());
                grades.setFinalExam(studentGrade.getFinalExam());
                grades.setTotal(studentGrade.getTotal());

                selectRecord.setGrade(grades);
                database.merge(selectRecord);
            }
            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            log.warn("Failed to create Excel", e);
            return Response.Common.error("Failed to import");
        }
    }

    /**
     * 管理员添加新课程。
     *
     * @param request  请求对象，包含课程信息。
     * @param database 数据库会话。
     * @return 包含新课程信息的响应。
     */
    @RouteMapping(uri = "teaching/admin/addCourse", role = "admin")
    public Response addCourse(Request request, org.hibernate.Session database) {
        try {
            Transaction tx = database.beginTransaction();

            Course course = new Course();
            course.setUuid(UUID.randomUUID());
            course.setCourseId(request.getParams().get("courseId"));
            course.setCourseName(request.getParams().get("courseName"));
            course.setSchool(request.getParams().get("school"));
            course.setCredit(Float.parseFloat(request.getParams().get("credit")));

            database.persist(course);
            tx.commit();

            return Response.Common.ok(Map.of("course", course.toJson()));
        } catch (Exception e) {
            log.warn("Failed to add course", e);
            return Response.Common.badRequest();
        }
    }

    /**
     * 管理员添加新教学班。
     *
     * @param request  请求对象，包含教学班信息。
     * @param database 数据库会话。
     * @return 包含新教学班信息的响应。
     */
    @RouteMapping(uri = "teaching/admin/addTeachingClass", role = "admin")
    public Response addTeachingClass(Request request, org.hibernate.Session database) {
        try {
            Transaction tx = database.beginTransaction();

            TeachingClass teachingClass = new TeachingClass();
            teachingClass.setUuid(UUID.randomUUID());
            teachingClass.setCourseUuid(UUID.fromString(request.getParams().get("courseUuid")));
            teachingClass.setTeacherId(Integer.parseInt(request.getParams().get("teacherId")));
            teachingClass.setPlace(request.getParams().get("place"));
            teachingClass.setCapacity(Integer.parseInt(request.getParams().get("capacity")));

            Type scheduleType = new TypeToken<List<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>>>() {}.getType();
            List<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> schedule =
                new Gson().fromJson(request.getParams().get("schedule"), scheduleType);
            teachingClass.setSchedule(schedule);

            database.persist(teachingClass);
            tx.commit();

            return Response.Common.ok(Map.of("teachingClass", teachingClass.toJson()));
        } catch (Exception e) {
            log.warn("Failed to add teaching class", e);
            return Response.Common.badRequest();
        }
    }


}