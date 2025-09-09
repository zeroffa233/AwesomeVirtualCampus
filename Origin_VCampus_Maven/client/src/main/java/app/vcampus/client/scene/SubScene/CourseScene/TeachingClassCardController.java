package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.TeachingClass;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.List;

public class TeachingClassCardController {

    @FXML private Label teacherLabel;
    @FXML private Label statusLabel;
    @FXML private Label scheduleLabel;
    @FXML private Label placeLabel;
    @FXML private Label capacityLabel;
    @FXML private Label selectedCountLabel;
    @FXML private Button actionBtn;

    private TeachingClass teachingClass;
    private TeachingAffairsViewModel vm;

    public void bind(TeachingClass tc, TeachingAffairsViewModel vm) {
        this.teachingClass = tc;
        this.vm = vm;

        teacherLabel.setText(tc.getTeacherName());
        scheduleLabel.setText(tc.humanReadableSchedule());
        placeLabel.setText(tc.getPlace());
        capacityLabel.setText(String.valueOf(tc.getCapacity()));
        selectedCountLabel.setText(String.valueOf(tc.getSelectedCount()));

        refreshState();
    }

    /** 刷新状态 + 按钮行为 */
    private void refreshState() {
        boolean isChosen = vm.myClasses.selected.stream().anyMatch(it -> it.getUuid().equals(teachingClass.getUuid()));
        boolean isConflict = checkConflict(teachingClass);

        // 状态显示
        if (isConflict && !isChosen) {
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
        if (isChosen) {
            actionBtn.setText("退选");
            boolean cannotDrop = (teachingClass.getSelectRecord() != null && teachingClass.getSelectRecord().getGrade() != null);
            actionBtn.setDisable(cannotDrop);
            actionBtn.setOnAction(e -> {
                vm.myClasses.dropClass(teachingClass.getUuid());
                Platform.runLater(() -> {
                    selectedCountLabel.setText(String.valueOf(Math.max(0, teachingClass.getSelectedCount() - 1)));
                    refreshState();
                });
            });
        } else {
            actionBtn.setText("选择");
            boolean cannotChoose = (teachingClass.getSelectedCount() >= teachingClass.getCapacity() || isConflict);
            actionBtn.setDisable(cannotChoose);
            actionBtn.setOnAction(e -> {
                vm.myClasses.chooseClass(teachingClass.getUuid());
                Platform.runLater(() -> {
                    selectedCountLabel.setText(String.valueOf(teachingClass.getSelectedCount() + 1));
                    refreshState();
                });
            });
        }
    }

    /** 检查是否冲突（原逻辑不变） */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean checkConflict(TeachingClass tc) {
        if (tc == null || tc.getSchedule() == null) return false;

        for (TeachingClass chosen : vm.myClasses.selected) {
            if (chosen == null || chosen.getSchedule() == null) continue;

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

                    app.vcampus.server.utility.Pair itDayTime = (app.vcampus.server.utility.Pair) itPair.getSecond();
                    int itDay = ((Number) itDayTime.getFirst()).intValue();
                    app.vcampus.server.utility.Pair itTimePair = (app.vcampus.server.utility.Pair) itDayTime.getSecond();
                    int itStart = ((Number) itTimePair.getFirst()).intValue();
                    int itEnd = ((Number) itTimePair.getSecond()).intValue();

                    if (thisDay == itDay && !(thisStart > itEnd || thisEnd < itStart)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}


