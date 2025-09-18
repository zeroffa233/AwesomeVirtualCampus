package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.TeachingClass;
import app.vcampus.server.utility.Pair;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.List;

/**
 * 我的课表场景控制器。
 * 负责动态构建和渲染学生的周课表视图。
 */
public class MyScheduleController {

    @FXML private Label headingLabel;
    @FXML private Label captionLabel;
    @FXML private ToggleButton prevWeekBtn;
    @FXML private ToggleButton nextWeekBtn;
    @FXML private Label weekLabel;
    @FXML private GridPane timetableGrid;

    private TeachingAffairsViewModel viewModel;

    private int currentWeek = 1;
    private final int MAX_WEEKS = 16;
    private final int TOTAL_SECTIONS = 13;
    private final int WEEKDAYS = 7;

    /**
     * 构造函数。
     */
    public MyScheduleController() {
    }

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        buildGrid();
        prevWeekBtn.setOnAction(e -> {
            if (currentWeek > 1) {
                currentWeek--;
                updateWeekLabel();
                renderTable();
            }
        });
        nextWeekBtn.setOnAction(e -> {
            if (currentWeek < MAX_WEEKS) {
                currentWeek++;
                updateWeekLabel();
                renderTable();
            }
        });

        updateWeekLabel();
    }

    /**
     * 设置视图模型，并初始化数据和监听器。
     *
     * @param vm 教务视图模型。
     */
    public void setViewModel(TeachingAffairsViewModel vm) {
        this.viewModel = vm;
        Platform.runLater(() -> {
            vm.myClasses.init();
            vm.myClasses.selected.addListener((ListChangeListener<? super TeachingClass>) c -> {
                Platform.runLater(this::renderTable);
            });
            renderTable();
        });
    }

    private void updateWeekLabel() {
        weekLabel.setText("第 " + currentWeek + " 周");
        prevWeekBtn.setDisable(currentWeek <= 1);
        nextWeekBtn.setDisable(currentWeek >= MAX_WEEKS);
    }

    private void buildGrid() {
        timetableGrid.getChildren().clear();
        timetableGrid.getColumnConstraints().clear();
        timetableGrid.getRowConstraints().clear();

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(12);
        col0.setHgrow(Priority.NEVER);
        timetableGrid.getColumnConstraints().add(col0);

        for (int i = 0; i < WEEKDAYS; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth((88.0) / WEEKDAYS);
            cc.setHgrow(Priority.SOMETIMES);
            timetableGrid.getColumnConstraints().add(cc);
        }

        RowConstraints header = new RowConstraints();
        header.setPrefHeight(40);
        header.setVgrow(Priority.NEVER);
        timetableGrid.getRowConstraints().add(header);

        for (int r = 0; r < TOTAL_SECTIONS; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setMinHeight(40);
            timetableGrid.getRowConstraints().add(rc);
        }

        Label timeHeader = new Label("");
        timetableGrid.add(timeHeader, 0, 0);
        String[] weekdayNames = {"星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
        for (int i = 0; i < WEEKDAYS; i++) {
            Label l = new Label(weekdayNames[i]);
            l.getStyleClass().add("timetable-weekday");
            timetableGrid.add(l, i + 1, 0);
            GridPane.setHalignment(l, HPos.CENTER);
        }

        for (int s = 1; s <= TOTAL_SECTIONS; s++) {
            Label sec = new Label("第 " + s + " 节");
            sec.getStyleClass().add("timetable-section");
            timetableGrid.add(sec, 0, s);
            GridPane.setValignment(sec, VPos.CENTER);
            GridPane.setHalignment(sec, HPos.CENTER);
        }

        timetableGrid.setPrefHeight(800);
    }

    private void renderTable() {
        if (viewModel == null) return;

        timetableGrid.getChildren().removeIf(node -> {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            int c = (col == null) ? 0 : col;
            int r = (row == null) ? 0 : row;
            return !(r == 0 || (c == 0 && r >= 1));
        });

        List<TeachingClass> classes = viewModel.myClasses.selected;
        if (classes == null || classes.isEmpty()) {
            return;
        }

        for (TeachingClass tc : classes) {
            if (tc == null || tc.getSchedule() == null) continue;
            for (Object raw : tc.getSchedule()) {
                if (!(raw instanceof Pair)) continue;
                Pair<?,?> p = (Pair<?,?>) raw;

                int weekStart = 1, weekEnd = 1;
                Object first = p.getFirst();
                if (first instanceof Pair) {
                    Pair<?,?> wk = (Pair<?,?>) first;
                    if (wk.getFirst() instanceof Number) weekStart = ((Number) wk.getFirst()).intValue();
                    if (wk.getSecond() instanceof Number) weekEnd = ((Number) wk.getSecond()).intValue();
                }

                if (currentWeek < weekStart || currentWeek > weekEnd) continue;

                Object second = p.getSecond();
                if (!(second instanceof Pair)) continue;
                Pair<?,?> dayTime = (Pair<?,?>) second;

                int weekday = -1;
                if (dayTime.getFirst() instanceof Number) weekday = ((Number) dayTime.getFirst()).intValue();

                Object timePairObj = dayTime.getSecond();
                if (!(timePairObj instanceof Pair)) continue;
                Pair<?,?> timePair = (Pair<?,?>) timePairObj;

                int start = -1, end = -1;
                if (timePair.getFirst() instanceof Number) start = ((Number) timePair.getFirst()).intValue();
                if (timePair.getSecond() instanceof Number) end = ((Number) timePair.getSecond()).intValue();

                if (weekday < 1 || weekday > WEEKDAYS || start < 1 || start > TOTAL_SECTIONS || end < start) {
                    continue;
                }

                int colIndex = weekday;
                int rowIndex = start;
                int rowSpan = Math.min(TOTAL_SECTIONS - start + 1, end - start + 1);

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ClassItem.fxml"));
                    Node item = loader.load();
                    Object controller = loader.getController();
                    if (controller instanceof ClassItemController) {
                        ClassItemController cic = (ClassItemController) controller;
                        String courseName = tc.getCourse() == null ? "?" : tc.getCourse().getCourseName();
                        cic.setData(courseName, tc.getTeacherName() == null ? "" : tc.getTeacherName(), tc.getPlace() == null ? "" : tc.getPlace());
                    }

                    timetableGrid.add(item, colIndex, rowIndex, 1, rowSpan);
                    GridPane.setHalignment(item, HPos.CENTER);
                    GridPane.setValignment(item, VPos.CENTER);
                } catch (IOException e) {
                    System.err.println("[MySchedule] failed to load ClassItem.fxml: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception ex) {
                    System.err.println("[MySchedule] render item exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }
}