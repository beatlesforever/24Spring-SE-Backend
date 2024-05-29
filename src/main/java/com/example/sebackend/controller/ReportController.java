package com.example.sebackend.controller;

import com.example.sebackend.entity.ControlLog;
import com.example.sebackend.entity.Report;
import com.example.sebackend.entity.Room;
import com.example.sebackend.entity.TempLog;
import com.example.sebackend.service.IControlLogService;
import com.example.sebackend.service.IReportService;
import com.example.sebackend.service.IRoomService;
import com.example.sebackend.service.IUsageRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private IControlLogService controlLogService;

    @Autowired
    private IUsageRecordService usageRecordService;

    @Autowired
    private IRoomService roomService;

    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    /**
     * 获取报表
     * 该接口接收时间参数queryTime、房间号roomId和报告类型reportType，
     * 规则如下：
     * 1.报表中不含正在进行的温控请求，
     * 2.查询时间范围内的温控请求时，以请求开始的时间为准，即若结束时间在查询时间范围外，也包含在本报表内。
     *
     * @param roomId 房间ID
     * //@param queryTime 查询时间
     * @param reportType 报告类型
     * @return ResponseEntity<Map<String, Object>> 包含更新后的房间信息的HTTP响应实体，
     * 其中状态码为OK（200），信息为"实时房间能耗费用查询成功"，数据部分为累积能耗和累积费用。
     */

    @GetMapping("/getReport")
    public ResponseEntity<Map<String, Object>> getReport(@RequestParam int roomId, @RequestParam String reportType) {
        log.info("room id: " + roomId + " report type: " + reportType );
        Report report = new Report();
        // 通过roomId查询房间信息
        Room room = roomService.getById(roomId);
        LocalDateTime startTime = null;
        // 获取当前时间
        LocalDateTime queryTime = LocalDateTime.now();
        // 确定查找类型
        if(reportType.equals("daily")){
            // 如果是日报表，查询开始时间为当天0点
            startTime = queryTime.toLocalDate().atStartOfDay();
        }
        else if(reportType.equals("weekly")){
            // 如果是周报表，查询开始时间为该周的周一0点
            startTime = queryTime.minusDays(queryTime.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        }
        else if(reportType.equals("monthly")){
            // 如果是月报表，查询时间为该月的1号0点
            startTime = queryTime.withDayOfMonth(1).toLocalDate().atStartOfDay();
        }
        // 查询结束时间为当前请求时间

        // 查询时间范围内的该房间内的已经完成的温控请求
        List<ControlLog> controlLogs = controlLogService.getFinishedLogs(roomId, startTime, queryTime);
        // 查询时间范围内的该房间内的从控机使用记录次数
        int count = usageRecordService.getUsageRecordCount(roomId, startTime, queryTime);
        // 空处理
        if(controlLogs == null || controlLogs.isEmpty()){
            return createResponse(HttpStatus.OK, "未查询到相关信息", null);
        }
        // 累加报告期间总能量消耗和总费用
        float totalEnergyConsumed = 0.0f;
        float totalCost = 0.0f;
        for(ControlLog controlLog : controlLogs){
            totalEnergyConsumed += controlLog.getEnergyConsumed();
            totalCost += controlLog.getCost();
        }

        List<TempLog> logs = new ArrayList<>();
        for (ControlLog controlLog : controlLogs) {
            TempLog tempLog = new TempLog();
            tempLog.setCost(controlLog.getCost());
            tempLog.setDuration(controlLog.getDuration());
            tempLog.setEnergyConsumed(controlLog.getEnergyConsumed());
            tempLog.setActualTemp(controlLog.getActualTemp());
            tempLog.setEndTemp(controlLog.getEndTemp());
            tempLog.setEndTime(controlLog.getEndTime());
            tempLog.setRequestedFanSpeed(controlLog.getRequestedFanSpeed());
            tempLog.setRequestTime(controlLog.getRequestTime());
            logs.add(tempLog);
        }

        //构建报告
        report.setRoomId(roomId);
        report.setType(reportType);
        report.setGenerationDate(queryTime);
        report.setTotalEnergyConsumed(totalEnergyConsumed);
        report.setTotalCost(totalCost);
        report.setUsageTime(count);
        report.setLog(logs);
        report.setCreator("admin");

        // 返回一个包含报表详细信息的响应实体
        return createResponse(HttpStatus.OK, "成功获取房间详细信息", report);
    }

}
