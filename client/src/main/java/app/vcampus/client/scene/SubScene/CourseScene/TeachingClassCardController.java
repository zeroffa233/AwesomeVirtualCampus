package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.Course;
import app.vcampus.server.entity.TeachingClass;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

import java.util.List;
import java.util.UUID;

public class TeachingClassCardController {

    @FXML private Label teacherLabel;
    @FXML private Label statusLabel;
    @FXML private Label scheduleLabel;
    @FXML private Label placeLabel;
    @FXML private Label capacityLabel;
    @FXML private Label selectedCountLabel;
    @FXML private ToggleButton actionBtn;

    private TeachingClass teachingClass;
    private TeachingAffairsViewModel vm;
    private boolean listenersAttached = false;

    public void bind(TeachingClass tc, TeachingAffairsViewModel vm) {
        this.teachingClass = tc;
        this.vm = vm;

        teacherLabel.setText(tc.getTeacherName());
        scheduleLabel.setText(tc.humanReadableSchedule());
        placeLabel.setText(tc.getPlace());
        capacityLabel.setText(String.valueOf(tc.getCapacity()));
        selectedCountLabel.setText(String.valueOf(tc.getSelectedCount()));

        // attach listeners once per card to react to VM changes
        if (!listenersAttached && vm != null && vm.myClasses != null) {
            try {
                vm.myClasses.selected.addListener((ListChangeListener<TeachingClass>) change -> {
                    Platform.runLater(this::refreshState);
                });
                vm.myClasses.allCourses.addListener((ListChangeListener<Course>) change -> {
                    Platform.runLater(this::refreshState);
                });
                listenersAttached = true;
            } catch (Exception ignored) {
                // 如果 vm.myClasses.selected 不是 ObservableList，会抛异常（但你已定义为 ObservableList）
            }
        }

        refreshState();
    }

    /** 刷新状态 + 按钮行为 */
    private void refreshState() {
        if (teachingClass == null || vm == null || vm.myClasses == null) return;

        boolean isChosen = vm.myClasses.selected.stream()
                .anyMatch(it -> it.getUuid() != null && it.getUuid().equals(teachingClass.getUuid()));

        // 【新增】同一门课程是否已经有别的班级被选中
        boolean sameCourseAlreadyChosen = vm.myClasses.selected.stream()
                .anyMatch(it -> it.getCourseUuid() != null
                        && it.getCourseUuid().equals(teachingClass.getCourseUuid())
                        && !it.getUuid().equals(teachingClass.getUuid()));

        boolean isConflict = checkConflict(teachingClass) || sameCourseAlreadyChosen;

        // 更新 selectedCount
        int visibleSelectedCount = findSelectedCountFromVM(teachingClass.getUuid());
        selectedCountLabel.setText(String.valueOf(visibleSelectedCount));

        // 状态显示
        if (sameCourseAlreadyChosen && !isChosen) {
            statusLabel.setText("已选该课程的其他班级");
            statusLabel.getStyleClass().setAll("tc-status", "conflict-badge");
        } else if (isConflict && !isChosen) {
            statusLabel.setText("课程冲突");
            statusLabel.getStyleClass().setAll("tc-status", "conflict-badge");
        } else if (isChosen) {
            statusLabel.setText("已选");
            statusLabel.getStyleClass().setAll("tc-status", "ok-badge");
        } else {
            statusLabel.setText("");
            statusLabel.getStyleClass().setAll("tc-status");
        }

        // 按钮逻辑
        actionBtn.setOnAction(null);
        if (isChosen) {
            actionBtn.setText("退选");
            boolean cannotDrop = (teachingClass.getSelectRecord() != null && teachingClass.getSelectRecord().getGrade() != null);
            actionBtn.setDisable(cannotDrop);
            actionBtn.setOnAction(e -> {
                actionBtn.setDisable(true);
                vm.myClasses.dropClass(teachingClass.getUuid()).whenComplete((success, ex) -> {
                    Platform.runLater(() -> {
                        if (ex != null || !Boolean.TRUE.equals(success)) {
                            actionBtn.setDisable(false);
                        }
                        refreshState();
                    });
                });
            });
        } else {
            actionBtn.setText("选择");
            boolean cannotChoose = (visibleSelectedCount >= teachingClass.getCapacity() || isConflict);
            actionBtn.setDisable(cannotChoose);
            actionBtn.setOnAction(e -> {
                actionBtn.setDisable(true);
                vm.myClasses.chooseClass(teachingClass.getUuid()).whenComplete((success, ex) -> {
                    Platform.runLater(() -> {
                        if (ex != null || !Boolean.TRUE.equals(success)) {
                            actionBtn.setDisable(false);
                        }
                        refreshState();
                    });
                });
            });
        }

    }

    /** 从 vm.myClasses.allCourses 中查找最新的 selectedCount；没有找到时回退到 teachingClass.getSelectedCount() */
    private int findSelectedCountFromVM(UUID uuid) {
        if (vm == null || vm.myClasses == null) return teachingClass.getSelectedCount();
        for (Course c : vm.myClasses.allCourses) {
            if (c.getTeachingClasses() == null) continue;
            for (TeachingClass t : c.getTeachingClasses()) {
                if (t != null && t.getUuid() != null && t.getUuid().equals(uuid)) {
                    return t.getSelectedCount();
                }
            }
        }
        // 回退
        return teachingClass.getSelectedCount();
    }

    /** 检查是否冲突（改进过的重叠判断 + 跳过自身） */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean checkConflict(TeachingClass tc) {
        if (tc == null || tc.getSchedule() == null) return false;

        for (TeachingClass chosen : vm.myClasses.selected) {
            if (chosen == null || chosen.getSchedule() == null) continue;
            // 跳过同一教学班（避免自己与自己比较）
            if (chosen.getUuid() != null && chosen.getUuid().equals(tc.getUuid())) continue;

            List scheduleThis = tc.getSchedule();
            List scheduleIt = chosen.getSchedule();

            for (Object thisObj : scheduleThis) {
                if (!(thisObj instanceof app.vcampus.server.utility.Pair)) continue;
                app.vcampus.server.utility.Pair thisPair = (app.vcampus.server.utility.Pair) thisObj;

                Object thisSecond = thisPair.getSecond();
                if (!(thisSecond instanceof app.vcampus.server.utility.Pair)) continue;
                app.vcampus.server.utility.Pair thisDayTime = (app.vcampus.server.utility.Pair) thisSecond;

                int thisDay = ((Number) thisDayTime.getFirst()).intValue();
                app.vcampus.server.utility.Pair thisTimePair = (app.vcampus.server.utility.Pair) thisDayTime.getSecond();
                int thisStart = ((Number) thisTimePair.getFirst()).intValue();
                int thisEnd = ((Number) thisTimePair.getSecond()).intValue();

                for (Object itObj : scheduleIt) {
                    if (!(itObj instanceof app.vcampus.server.utility.Pair)) continue;
                    app.vcampus.server.utility.Pair itPair = (app.vcampus.server.utility.Pair) itObj;

                    Object itSecond = itPair.getSecond();
                    if (!(itSecond instanceof app.vcampus.server.utility.Pair)) continue;
                    app.vcampus.server.utility.Pair itDayTime = (app.vcampus.server.utility.Pair) itSecond;

                    int itDay = ((Number) itDayTime.getFirst()).intValue();
                    app.vcampus.server.utility.Pair itTimePair = (app.vcampus.server.utility.Pair) itDayTime.getSecond();
                    int itStart = ((Number) itTimePair.getFirst()).intValue();
                    int itEnd = ((Number) itTimePair.getSecond()).intValue();

                    // 修正：使用 aStart < bEnd && bStart < aEnd 判定重叠（避免端点误判）
                    if (thisDay == itDay && thisStart < itEnd && itStart < thisEnd) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
