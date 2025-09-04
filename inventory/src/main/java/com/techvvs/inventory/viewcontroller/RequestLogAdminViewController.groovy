package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.RequestLogRepo
import com.techvvs.inventory.model.nonpersist.graphs.RequestLogDataPoint
import com.techvvs.inventory.service.auth.TechvvsAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import java.time.LocalDateTime
import java.util.stream.Collectors

@RequestMapping("/requestlog/admin")
@Controller
public class RequestLogAdminViewController {

    @Autowired
    RequestLogRepo requestLogRepo

    @Autowired
    TechvvsAuthService techvvsAuthService

    @GetMapping('/dashboard')
    String viewRequestLogDashboard(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("deviceType") Optional<String> deviceTypeFilter,
            @RequestParam("browser") Optional<String> browserFilter,
            @RequestParam("operatingSystem") Optional<String> osFilter,
            @RequestParam("responseStatus") Optional<Integer> responseStatusFilter,
            @RequestParam("clientIp") Optional<String> clientIpFilter,
            @RequestParam("locale") Optional<String> localeFilter,
            @RequestParam("origin") Optional<String> originFilter,
            @RequestParam("minDuration") Optional<Long> minDurationFilter,
            @RequestParam("maxDuration") Optional<Long> maxDurationFilter,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> endDate
    ) {
        // Define default range using LocalDateTime (last 7 days)
        LocalDateTime start = startDate.orElse(LocalDateTime.now().minusDays(7)).withHour(0).withMinute(0)
        LocalDateTime end = endDate.orElse(LocalDateTime.now()).withHour(23).withMinute(59)

        // Auth check
        techvvsAuthService.checkuserauth(model)
        
        // Log the request for debugging
        System.out.println("RequestLogAdminViewController: Processing dashboard request for date range: ${start} to ${end}")

        // Get filter values
        String deviceType = deviceTypeFilter.orElse(null)
        String browser = browserFilter.orElse(null)
        String operatingSystem = osFilter.orElse(null)
        Integer responseStatus = responseStatusFilter.orElse(null)
        String clientIp = clientIpFilter.orElse(null)
        String locale = localeFilter.orElse(null)
        String origin = originFilter.orElse(null)
        Long minDuration = minDurationFilter.orElse(null)
        Long maxDuration = maxDurationFilter.orElse(null)

        // Get daily request statistics
        List<RequestLogDataPoint> dailyData = []
        try {
            List<Object[]> dailyStats = requestLogRepo.getDailyRequestStatistics(start, end) ?: []
            dailyData = dailyStats.collect { row ->
                new RequestLogDataPoint(
                    row[0]?.toString() ?: "Unknown", // date
                    row[1] ? ((Number) row[1]).longValue() : 0L, // count
                    row[2] ? ((Number) row[2]).doubleValue() : 0.0 // avg duration
                )
            }
            System.out.println("RequestLogAdminViewController: Processed ${dailyData.size()} daily data points")
        } catch (Exception e) {
            System.err.println("RequestLogAdminViewController: Error processing daily statistics: ${e.message}")
            e.printStackTrace()
        }

        // Get hourly request statistics
        List<RequestLogDataPoint> hourlyData = []
        try {
            List<Object[]> hourlyStats = requestLogRepo.getHourlyRequestStatistics(start, end) ?: []
            hourlyData = hourlyStats.collect { row ->
                new RequestLogDataPoint(
                    (row[0]?.toString() ?: "0") + ":00", // hour
                    row[1] ? ((Number) row[1]).longValue() : 0L, // count
                    row[2] ? ((Number) row[2]).doubleValue() : 0.0 // avg duration
                )
            }
            System.out.println("RequestLogAdminViewController: Processed ${hourlyData.size()} hourly data points")
        } catch (Exception e) {
            System.err.println("RequestLogAdminViewController: Error processing hourly statistics: ${e.message}")
            e.printStackTrace()
        }

        // Get device type distribution
        List<RequestLogDataPoint> deviceData = []
        try {
            List<Object[]> deviceStats = requestLogRepo.getDeviceTypeDistribution(start, end) ?: []
            deviceData = deviceStats.collect { row ->
                new RequestLogDataPoint(
                    row[0] ?: "Unknown", // device type
                    row[1] ? ((Number) row[1]).longValue() : 0L, // count
                    0.0 // not used for pie charts
                )
            }
            System.out.println("RequestLogAdminViewController: Processed ${deviceData.size()} device data points")
        } catch (Exception e) {
            System.err.println("RequestLogAdminViewController: Error processing device statistics: ${e.message}")
            e.printStackTrace()
        }

        // Get browser distribution
        List<RequestLogDataPoint> browserData = []
        try {
            List<Object[]> browserStats = requestLogRepo.getBrowserDistribution(start, end) ?: []
            browserData = browserStats.collect { row ->
                new RequestLogDataPoint(
                    row[0] ?: "Unknown", // browser
                    row[1] ? ((Number) row[1]).longValue() : 0L, // count
                    0.0 // not used for pie charts
                )
            }
            System.out.println("RequestLogAdminViewController: Processed ${browserData.size()} browser data points")
        } catch (Exception e) {
            System.err.println("RequestLogAdminViewController: Error processing browser statistics: ${e.message}")
            e.printStackTrace()
        }

        // Get response status distribution
        List<RequestLogDataPoint> statusData = []
        try {
            List<Object[]> statusStats = requestLogRepo.getResponseStatusDistribution(start, end) ?: []
            statusData = statusStats.collect { row ->
                new RequestLogDataPoint(
                    row[0]?.toString() ?: "Unknown", // status code
                    row[1] ? ((Number) row[1]).longValue() : 0L, // count
                    0.0 // not used for pie charts
                )
            }
            System.out.println("RequestLogAdminViewController: Processed ${statusData.size()} status data points")
        } catch (Exception e) {
            System.err.println("RequestLogAdminViewController: Error processing status statistics: ${e.message}")
            e.printStackTrace()
        }

        // Get average response time by endpoint
        List<RequestLogDataPoint> endpointData = []
        try {
            List<Object[]> endpointStats = requestLogRepo.getAverageResponseTimeByEndpoint(start, end) ?: []
            endpointData = endpointStats.collect { row ->
                new RequestLogDataPoint(
                    row[0] ?: "Unknown", // endpoint
                    row[1] ? ((Number) row[1]).longValue() : 0L, // count
                    row[2] ? ((Number) row[2]).doubleValue() : 0.0 // avg duration
                )
            }
            System.out.println("RequestLogAdminViewController: Processed ${endpointData.size()} endpoint data points")
        } catch (Exception e) {
            System.err.println("RequestLogAdminViewController: Error processing endpoint statistics: ${e.message}")
            e.printStackTrace()
        }

        // Get unique values for filter dropdowns
        List<String> deviceTypes = requestLogRepo.findDistinctDeviceTypes() ?: []
        List<String> browsers = requestLogRepo.findDistinctBrowsers() ?: []
        List<String> operatingSystems = requestLogRepo.findDistinctOperatingSystems() ?: []
        List<Integer> responseStatuses = requestLogRepo.findDistinctResponseStatuses() ?: []
        List<String> locales = requestLogRepo.findDistinctLocales() ?: []
        List<String> origins = requestLogRepo.findDistinctOrigins() ?: []

        // Bind data to model
        model.addAttribute("dailyData", dailyData)
        model.addAttribute("hourlyData", hourlyData)
        model.addAttribute("deviceData", deviceData)
        model.addAttribute("browserData", browserData)
        model.addAttribute("statusData", statusData)
        model.addAttribute("endpointData", endpointData)

        // Bind filter values
        model.addAttribute("startDate", start)
        model.addAttribute("endDate", end)
        model.addAttribute("deviceTypeFilter", deviceType)
        model.addAttribute("browserFilter", browser)
        model.addAttribute("osFilter", operatingSystem)
        model.addAttribute("responseStatusFilter", responseStatus)
        model.addAttribute("clientIpFilter", clientIp)
        model.addAttribute("localeFilter", locale)
        model.addAttribute("originFilter", origin)
        model.addAttribute("minDurationFilter", minDuration)
        model.addAttribute("maxDurationFilter", maxDuration)

        // Bind filter options
        model.addAttribute("deviceTypes", deviceTypes)
        model.addAttribute("browsers", browsers)
        model.addAttribute("operatingSystems", operatingSystems)
        model.addAttribute("responseStatuses", responseStatuses)
        model.addAttribute("locales", locales)
        model.addAttribute("origins", origins)

        return "requestlog/dashboard.html"
    }
}
