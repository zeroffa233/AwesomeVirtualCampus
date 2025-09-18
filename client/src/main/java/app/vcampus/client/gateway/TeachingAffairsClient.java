package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.Course;
import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.TeachingClass;
import app.vcampus.server.utility.Pair;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 教务客户端，提供与教务系统交互的功能，包括学生选课、教师管理课程、成绩导入等。
 */
@Slf4j
public class TeachingAffairsClient extends BaseClient {
    /**
     * 用于获取学生已选课程。
     * @param handler Netty处理器。
     * @return 如果响应成功，返回List<TeachingClass>，否则抛出异常。
     */
    public static List<TeachingClass> getSelectedClasses(NettyHandler handler) {
        Request request = new Request();
        request.setUri("teaching/student/getMyClasses");

        try {
            Response response = BaseClient.sendRequest(handler, request);

            if (response.getStatus().equals("success")) {
                List<String> raw_data = ((Map<String, List<String>>) response.getData()).get("classes");
                return raw_data.stream().map((String s) -> IEntity.fromJson(s, TeachingClass.class)).toList();
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception e) {
            log.warn("获取已选课程失败", e);
            return null;
        }
    }

    /**
     * 用于提交学生对课程的评价结果。
     * @param evaluationResult 包含评价结果的Pair对象。
     * @return 如果响应成功，返回true，否则返回false。
     */
    public static Boolean sendEvaluationResult(NettyHandler handler, Pair<UUID, Pair<List<Integer>, String>> evaluationResult) {
        Request request = new Request();
        request.setUri("teaching/student/submitEvaluation");
        request.setParams(Map.of(
                "evaluation", BaseClient.toJson(evaluationResult)
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);

            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("提交评价失败", e);
            return false;
        }
    }


    /**
     * 用于获取学生可选课程的信息。
     * @param handler Netty处理器。
     * @return 可选课程的列表，如果获取失败则返回null。
     */
    public static List<Course> getSelectableCourses(NettyHandler handler) {
        Request request = new Request();
        request.setUri("teaching/student/getSelectableCourses");

        try {
            Response response = BaseClient.sendRequest(handler, request);

            if (response.getStatus().equals("success")) {
                List<String> raw_data = ((Map<String, List<String>>) response.getData()).get("courses");
                return raw_data.stream().map((String s) -> IEntity.fromJson(s, Course.class)).toList();
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception e) {
            log.warn("获取可选课程失败", e);
            return null;
        }
    }

    /**
     * 用于学生选择课程。
     * @param classUuid 课程的UUID。
     * @return 如果响应成功，返回true，否则返回false。
     */
    public static Boolean chooseClass(NettyHandler handler, UUID classUuid) {
        Request request = new Request();
        request.setUri("teaching/student/chooseClass");
        request.setParams(Map.of(
                "classUuid", classUuid.toString()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);

            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("选课失败", e);
            return false;
        }
    }

    /**
     * 用于学生退选课程。
     * @param  classUuid 课程的UUID。
     * @return 如果响应成功，返回true，否则返回false。
     */
    public static Boolean dropClass(NettyHandler handler, UUID classUuid) {
        Request request = new Request();
        request.setUri("teaching/student/dropClass");
        request.setParams(Map.of(
                "classUuid", classUuid.toString()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);

            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("退课失败", e);
            return false;
        }
    }


    /**
     * 用于教师获取自己所教的课程。
     * @param handler Netty处理器。
     * @return 教师所教课程的列表，如果获取失败则返回null。
     */
    public static List<TeachingClass> getMyTeachingClasses(NettyHandler handler) {
        Request request = new Request();
        request.setUri("teaching/teacher/getMyClasses");

        try {
            Response response = BaseClient.sendRequest(handler, request);

            if (response.getStatus().equals("success")) {
                List<String> raw_data = ((Map<String, List<String>>) response.getData()).get("classes");
                return raw_data.stream().map((String s) -> IEntity.fromJson(s, TeachingClass.class)).toList();
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception e) {
            log.warn("获取所教课程失败", e);
            return null;
        }
    }

    /**
     * 用于导出学生名单。
     * @param handler Netty处理器。
     * @param classUuid 课程的UUID。
     * @return 学生名单的字符串表示，如果导出失败则返回null。
     */
    public static String exportStudentList(NettyHandler handler, UUID classUuid) {
        Request request = new Request();
        request.setUri("teaching/teacher/exportStudentList");
        request.setParams(Map.of(
                "classUuid", classUuid.toString()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);

            if (response.getStatus().equals("success")) {
                return ((Map<String, String>) response.getData()).get("file");
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception e) {
            log.warn("导出学生名单失败", e);
            return null;
        }
    }

    /**
     * 用于导出成绩模板。
     * @param handler Netty处理器。
     * @param classUuid 课程的UUID。
     * @return 成绩模板的字符串表示，如果导出失败则返回null。
     */
    public static String exportGradeTemplate(NettyHandler handler, UUID classUuid) {
        Request request = new Request();
        request.setUri("teaching/teacher/exportGradeTemplate");
        request.setParams(Map.of(
                "classUuid", classUuid.toString()
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);

            if (response.getStatus().equals("success")) {
                return ((Map<String, String>) response.getData()).get("file");
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception e) {
            log.warn("导出成绩模板失败", e);
            return null;
        }
    }

    /**
     * 用于导入成绩。
     * @param handler Netty处理器。
     * @param classUuid 课程的UUID。
     * @param file 文件内容。
     * @return 如果导入成功则返回true，否则返回false。
     */
    public static Boolean importGrade(NettyHandler handler, UUID classUuid, String file) {
        Request request = new Request();
        request.setUri("teaching/teacher/importGrade");
        request.setParams(Map.of(
                "classUuid", classUuid.toString(),
                "file", file
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);

            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("导入成绩失败", e);
            return false;
        }
    }
    // ... existing code ...

    /**
     * 用于管理员添加新课程。
     * @param handler Netty处理器。
     * @param courseId 课程ID。
     * @param courseName 课程名称。
     * @param school 学院名称。
     * @param credit 学分。
     * @return 如果添加成功则返回true，否则返回false。
     */
    public static Boolean addCourse(NettyHandler handler, String courseId, String courseName, String school, float credit) {
        Request request = new Request();
        request.setUri("teaching/admin/addCourse");
        request.setParams(Map.of(
                "courseId", courseId,
                "courseName", courseName,
                "school", school,
                "credit", String.valueOf(credit)
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("添加课程失败", e);
            return false;
        }
    }

    /**
     * 用于管理员添加新教学班。
     * @param handler Netty处理器。
     * @param courseUuid 课程UUID。
     * @param teacherId 教师ID。
     * @param place 教室地点。
     * @param capacity 最大学生容量。
     * @param schedule 课程时间表。
     * @return 如果添加成功则返回true，否则返回false。
     */
    public static Boolean addTeachingClass(NettyHandler handler, UUID courseUuid, int teacherId, String place, int capacity, List<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> schedule) {
        Request request = new Request();
        request.setUri("teaching/admin/addTeachingClass");
        request.setParams(Map.of(
                "courseUuid", courseUuid.toString(),
                "teacherId", String.valueOf(teacherId),
                "place", place,
                "capacity", String.valueOf(capacity),
                "schedule", BaseClient.toJson(schedule)
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("添加教学班失败", e);
            return false;
        }
    }



//    public static Course addCourse(NettyHandler handler, String uuid, String courseName, String courseId, String school, String credit) {
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<Response> response = new AtomicReference();
//        Request request = new Request();
//        request.setUri("course/addCourse");
//        request.setParams(Map.of("uuid", uuid, "courseName", courseName, "courseId", courseId, "school", school, "credit", credit));
//        handler.sendRequest(request, (res) -> {
//            response.set(res);
//            System.out.println(res);
//            latch.countDown();
//        });
//        try {
//            latch.await();
//        } catch (InterruptedException var16) {
//            var16.printStackTrace();
//            return null;
//        }
//
//        if ((response.get()).getStatus().equals("success")) {
//            Map<String, String> data = (Map) ((Map) ((Response) response.get()).getData()).get("course");
//            Course course = Course.fromMap(data);
//            return course;
//        } else {
//            return null;
//        }
//    }
//
//
//    public static Map<String,List<Course>> searchCourse(NettyHandler handler,String keyword){
//        Request request=new Request();
//        request.setUri("course/searchBook");
//        request.setParams(Map.of(
//                "keyword",keyword
//        ));
//
//        try {
//            Response response = BaseClient.sendRequest(handler, request);
//            if (response.getStatus().equals("success")) {
//                Map<String, List<String>> raw_data = (Map<String, List<String>>) response.getData();
//                Map<String, List<Course>> data = new HashMap<>();
//                raw_data.forEach((key, value) -> data.put(key, value.stream().map(json -> IEntity.fromJson(json, Course.class)).toList()));
//                return data;
//            } else {
//                throw new RuntimeException("Failed to get course info");
//            }
//        }catch (InterruptedException e){
//            log.warn("Fail to get course info",e);
//            return null;
//        }
//    }

    //This method is used for student to select teaching class.
//    public static SelectedClass selectClass(NettyHandler handler, String uuid, String classUuid, String cardNumber, String selectTime, String grade) {
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<Response> response = new AtomicReference();
//        Request request = new Request();
//        request.setUri("selectedClass/selectClass");
//        request.setParams(Map.of("uuid", uuid, "classUuid", classUuid, "cardNumber", cardNumber, "selectTime", selectTime, "grade", grade));
//        handler.sendRequest(request, (res) -> {
//            response.set(res);
//            System.out.println(res);
//            latch.countDown();
//        });
//
//        try {
//            latch.await();
//        } catch (InterruptedException var16) {
//            var16.printStackTrace();
//            return null;
//        }
//        if ((response.get()).getStatus().equals("success")) {
//            Map<String, String> data = (Map) ((Map) ((Response) response.get()).getData()).get("selectedClass");
//            SelectedClass selectedClass = SelectedClass.fromMap(data);
//            return selectedClass;
//        } else {
//            return null;
//        }
//    }

    //Used to search course information.
//    public static Course searchInfo(NettyHandler handler, String courseName) {
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<Response> response = new AtomicReference();
//        Request request = new Request();
//        request.setUri("selectedClass/searchInfo");
//        request.setParams(Map.of("courseName", courseName));
//        handler.sendRequest(request, (res) ->
//        {
//            response.set(res);
//            System.out.println(res);
//            latch.countDown();
//        });
//
//        try {
//            latch.await();
//        } catch (InterruptedException var6) {
//            var6.printStackTrace();
//            return null;
//        }
//
//        if ((response.get()).getStatus().equals("success")) {
//            Map<String, String> data = (Map) (response.get()).getData();
//            return Course.fromMap(data);
//        } else {
//            return null;
//        }
//    }
//
//    public static boolean recordGrade(NettyHandler handler, Integer grade) {
//        Request request = new Request();
//        request.setUri("selectedClass/grade");
//        request.setParams(Map.of(
//                "grade", grade.toString()
//        ));
//        try {
//            Response response = BaseClient.sendRequest(handler, request);
//            return response.getStatus().equals("success");
//        } catch (InterruptedException e) {
//            log.warn("Fail to  record grade", e);
//            return false;
//        }
//    }
//
//    public static boolean updateCourse(NettyHandler handler, LibraryBook book) {
//        Request request = new Request();
//        request.setUri("affairs_staff/updateCourse");
//        request.setParams(Map.of(
//                "course", book.toJson()
//        ));
//
//        try {
//            Response response = BaseClient.sendRequest(handler, request);
//            return response.getStatus().equals("success");
//        } catch (InterruptedException e) {
//            log.warn("Fail to update course", e);
//            return false;
//        }
//    }
//
//    public static boolean deleteCourse(NettyHandler handler, UUID uuid)
//    {
//        Request request=new Request();
//        request.setUri("affairs_staff/deleteCourse");
//        request.setParams(Map.of(
//                "uuid",uuid.toString()
//        ));
//
//        try{
//            Response response=BaseClient.sendRequest(handler,request);
//            return response.getStatus().equals("success");
//        }catch(InterruptedException e) {
//            log.warn("Fail to delete course",e);
//            return false;
//    }
//    }
}
