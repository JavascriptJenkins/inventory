package com.techvvs.inventory.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing device information from User-Agent strings
 */
public class DeviceDetectionUtil {

    // Browser patterns
    private static final Pattern CHROME_PATTERN = Pattern.compile("Chrome/([0-9.]+)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("Firefox/([0-9.]+)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("Version/([0-9.]+).*Safari");
    private static final Pattern EDGE_PATTERN = Pattern.compile("Edg/([0-9.]+)");
    private static final Pattern OPERA_PATTERN = Pattern.compile("OPR/([0-9.]+)|Opera/([0-9.]+)");
    private static final Pattern IE_PATTERN = Pattern.compile("MSIE ([0-9.]+)|Trident.*rv:([0-9.]+)");

    // OS patterns
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("Windows NT ([0-9.]+)");
    private static final Pattern MACOS_PATTERN = Pattern.compile("Mac OS X ([0-9_]+)");
    private static final Pattern LINUX_PATTERN = Pattern.compile("Linux");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("Android ([0-9.]+)");
    private static final Pattern IOS_PATTERN = Pattern.compile("OS ([0-9_]+)");

    // Device patterns
    private static final Pattern IPHONE_PATTERN = Pattern.compile("iPhone");
    private static final Pattern IPAD_PATTERN = Pattern.compile("iPad");
    private static final Pattern IPOD_PATTERN = Pattern.compile("iPod");

    /**
     * Parse comprehensive device information from User-Agent string
     */
    public static Map<String, Object> parseDeviceInfo(String userAgent) {
        Map<String, Object> deviceInfo = new HashMap<>();
        
        if (userAgent == null || userAgent.isEmpty()) {
            setUnknownDeviceInfo(deviceInfo);
            return deviceInfo;
        }
        
        String ua = userAgent;
        
        // Parse browser information
        parseBrowserInfo(ua, deviceInfo);
        
        // Parse operating system information
        parseOSInfo(ua, deviceInfo);
        
        // Parse device type information
        parseDeviceType(ua, deviceInfo);
        
        // Parse additional metadata
        parseAdditionalInfo(ua, deviceInfo);
        
        return deviceInfo;
    }

    private static void parseBrowserInfo(String userAgent, Map<String, Object> deviceInfo) {
        // Chrome
        Matcher chromeMatcher = CHROME_PATTERN.matcher(userAgent);
        if (chromeMatcher.find() && !userAgent.contains("Edg")) {
            deviceInfo.put("browser", "Chrome");
            deviceInfo.put("browserVersion", chromeMatcher.group(1));
            return;
        }
        
        // Edge
        Matcher edgeMatcher = EDGE_PATTERN.matcher(userAgent);
        if (edgeMatcher.find()) {
            deviceInfo.put("browser", "Edge");
            deviceInfo.put("browserVersion", edgeMatcher.group(1));
            return;
        }
        
        // Firefox
        Matcher firefoxMatcher = FIREFOX_PATTERN.matcher(userAgent);
        if (firefoxMatcher.find()) {
            deviceInfo.put("browser", "Firefox");
            deviceInfo.put("browserVersion", firefoxMatcher.group(1));
            return;
        }
        
        // Safari
        Matcher safariMatcher = SAFARI_PATTERN.matcher(userAgent);
        if (safariMatcher.find() && !userAgent.contains("Chrome")) {
            deviceInfo.put("browser", "Safari");
            deviceInfo.put("browserVersion", safariMatcher.group(1));
            return;
        }
        
        // Opera
        Matcher operaMatcher = OPERA_PATTERN.matcher(userAgent);
        if (operaMatcher.find()) {
            deviceInfo.put("browser", "Opera");
            deviceInfo.put("browserVersion", operaMatcher.group(1) != null ? operaMatcher.group(1) : operaMatcher.group(2));
            return;
        }
        
        // Internet Explorer
        Matcher ieMatcher = IE_PATTERN.matcher(userAgent);
        if (ieMatcher.find()) {
            deviceInfo.put("browser", "Internet Explorer");
            deviceInfo.put("browserVersion", ieMatcher.group(1) != null ? ieMatcher.group(1) : ieMatcher.group(2));
            return;
        }
        
        deviceInfo.put("browser", "Unknown");
        deviceInfo.put("browserVersion", "Unknown");
    }

    private static void parseOSInfo(String userAgent, Map<String, Object> deviceInfo) {
        // Windows
        Matcher windowsMatcher = WINDOWS_PATTERN.matcher(userAgent);
        if (windowsMatcher.find()) {
            deviceInfo.put("os", "Windows");
            deviceInfo.put("osVersion", getWindowsVersion(windowsMatcher.group(1)));
            return;
        }
        
        // macOS
        Matcher macosMatcher = MACOS_PATTERN.matcher(userAgent);
        if (macosMatcher.find()) {
            deviceInfo.put("os", "macOS");
            deviceInfo.put("osVersion", macosMatcher.group(1).replace("_", "."));
            return;
        }
        
        // iOS
        Matcher iosMatcher = IOS_PATTERN.matcher(userAgent);
        if (iosMatcher.find()) {
            deviceInfo.put("os", "iOS");
            deviceInfo.put("osVersion", iosMatcher.group(1).replace("_", "."));
            return;
        }
        
        // Android
        Matcher androidMatcher = ANDROID_PATTERN.matcher(userAgent);
        if (androidMatcher.find()) {
            deviceInfo.put("os", "Android");
            deviceInfo.put("osVersion", androidMatcher.group(1));
            return;
        }
        
        // Linux
        if (LINUX_PATTERN.matcher(userAgent).find()) {
            deviceInfo.put("os", "Linux");
            deviceInfo.put("osVersion", "Unknown");
            return;
        }
        
        deviceInfo.put("os", "Unknown");
        deviceInfo.put("osVersion", "Unknown");
    }

    private static void parseDeviceType(String userAgent, Map<String, Object> deviceInfo) {
        String ua = userAgent.toLowerCase();
        
        // Check for specific devices first
        if (IPAD_PATTERN.matcher(userAgent).find()) {
            deviceInfo.put("device", "Tablet");
            deviceInfo.put("deviceModel", "iPad");
        } else if (IPHONE_PATTERN.matcher(userAgent).find()) {
            deviceInfo.put("device", "Mobile");
            deviceInfo.put("deviceModel", "iPhone");
        } else if (IPOD_PATTERN.matcher(userAgent).find()) {
            deviceInfo.put("device", "Mobile");
            deviceInfo.put("deviceModel", "iPod");
        } else if (ua.contains("android")) {
            deviceInfo.put("device", "Mobile");
            deviceInfo.put("deviceModel", "Android");
        } else if (ua.contains("tablet")) {
            deviceInfo.put("device", "Tablet");
            deviceInfo.put("deviceModel", "Unknown");
        } else if (ua.contains("mobile")) {
            deviceInfo.put("device", "Mobile");
            deviceInfo.put("deviceModel", "Unknown");
        } else {
            deviceInfo.put("device", "Desktop");
            deviceInfo.put("deviceModel", "Unknown");
        }
    }

    private static void parseAdditionalInfo(String userAgent, Map<String, Object> deviceInfo) {
        String ua = userAgent.toLowerCase();
        
        // Bot detection
        boolean isBot = ua.contains("bot") || ua.contains("crawler") || ua.contains("spider") || 
                       ua.contains("scraper") || ua.contains("facebookexternalhit") || 
                       ua.contains("twitterbot") || ua.contains("linkedinbot") ||
                       ua.contains("googlebot") || ua.contains("bingbot") ||
                       ua.contains("slurp") || ua.contains("duckduckbot");
        
        deviceInfo.put("isBot", isBot);
        
        // Touch device detection
        boolean isTouchDevice = ua.contains("touch") || ua.contains("mobile") || 
                               ua.contains("android") || ua.contains("iphone") || 
                               ua.contains("ipad") || ua.contains("tablet");
        deviceInfo.put("isTouchDevice", isTouchDevice);
        
        // 64-bit detection (limited)
        boolean is64Bit = ua.contains("wow64") || ua.contains("x64") || ua.contains("amd64");
        deviceInfo.put("is64Bit", is64Bit);
    }

    private static String getWindowsVersion(String version) {
        switch (version) {
            case "10.0": return "Windows 10/11";
            case "6.3": return "Windows 8.1";
            case "6.2": return "Windows 8";
            case "6.1": return "Windows 7";
            case "6.0": return "Windows Vista";
            case "5.1": return "Windows XP";
            case "5.0": return "Windows 2000";
            default: return "Windows " + version;
        }
    }

    private static void setUnknownDeviceInfo(Map<String, Object> deviceInfo) {
        deviceInfo.put("browser", "Unknown");
        deviceInfo.put("browserVersion", "Unknown");
        deviceInfo.put("os", "Unknown");
        deviceInfo.put("osVersion", "Unknown");
        deviceInfo.put("device", "Unknown");
        deviceInfo.put("deviceModel", "Unknown");
        deviceInfo.put("isBot", false);
        deviceInfo.put("isTouchDevice", false);
        deviceInfo.put("is64Bit", false);
    }
}



