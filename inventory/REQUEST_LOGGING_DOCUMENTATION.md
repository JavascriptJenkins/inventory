# Request Logging Documentation

## Overview
The request logging system automatically captures and logs comprehensive metadata for all incoming HTTP requests, including HTTP method, device information, headers, session data, and more. This provides valuable insights for monitoring, debugging, and analytics.

## Components

### 1. **RequestLoggingInterceptor**
**Location**: `inventory/src/main/java/com/techvvs/inventory/interceptor/RequestLoggingInterceptor.java`

**Purpose**: Spring MVC interceptor that captures request metadata before and after request processing.

**Key Features**:
- **Pre-request Logging**: Captures incoming request metadata
- **Post-request Logging**: Captures response metadata
- **Security Filtering**: Excludes sensitive headers and information
- **JSON Formatting**: Logs data in structured JSON format
- **Error Handling**: Graceful error handling to prevent request blocking

### 2. **DeviceDetectionUtil**
**Location**: `inventory/src/main/java/com/techvvs/inventory/util/DeviceDetectionUtil.java`

**Purpose**: Advanced device detection utility that parses User-Agent strings to extract detailed device information.

**Capabilities**:
- **Browser Detection**: Identifies browser type and version
- **OS Detection**: Identifies operating system and version
- **Device Type**: Determines if device is mobile, tablet, or desktop
- **Bot Detection**: Identifies web crawlers and bots
- **Touch Device Detection**: Determines if device supports touch
- **Architecture Detection**: Identifies 64-bit systems

### 3. **RequestLoggingConfig**
**Location**: `inventory/src/main/java/com/techvvs/inventory/config/RequestLoggingConfig.java`

**Purpose**: Configuration class for customizing request logging behavior.

**Configuration Options**:
- **Enable/Disable Logging**: Toggle request logging on/off
- **Header Logging**: Control whether headers are logged
- **Device Info Logging**: Control device information capture
- **Session Info Logging**: Control session data capture
- **Security Info Logging**: Control security-related information
- **Exclude Paths**: Define paths to exclude from logging
- **Sensitive Headers**: Define headers to exclude for security

### 4. **WebConfig Integration**
**Location**: `inventory/src/main/java/com/techvvs/inventory/config/WebConfig.java`

**Purpose**: Registers the request logging interceptor with Spring MVC.

**Configuration**:
- **Path Patterns**: Applies to all paths (`/**`)
- **Exclusions**: Excludes static resources and actuator endpoints
- **Order**: Executes before request processing

## Logged Information

### 1. **Request Metadata**
```json
{
  "timestamp": "2024-01-15T10:30:45.123",
  "method": "GET",
  "uri": "/tenant/admin",
  "queryString": "page=1&size=10",
  "protocol": "HTTP/1.1",
  "remoteAddr": "192.168.1.100",
  "remoteHost": "client.example.com",
  "remotePort": 54321,
  "serverName": "localhost",
  "serverPort": 8080,
  "contextPath": "",
  "servletPath": "/tenant/admin",
  "pathInfo": null
}
```

### 2. **Device Information**
```json
{
  "deviceInfo": {
    "browser": "Chrome",
    "browserVersion": "120.0.6099.109",
    "os": "Windows",
    "osVersion": "Windows 10/11",
    "device": "Desktop",
    "deviceModel": "Unknown",
    "isBot": false,
    "isTouchDevice": false,
    "is64Bit": true
  }
}
```

### 3. **Request Headers** (Filtered)
```json
{
  "headers": {
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Encoding": "gzip, deflate, br",
    "Accept-Language": "en-US,en;q=0.5",
    "Cache-Control": "no-cache",
    "Connection": "keep-alive",
    "Host": "localhost:8080",
    "Upgrade-Insecure-Requests": "1"
  }
}
```

### 4. **Session Information**
```json
{
  "sessionId": "A1B2C3D4E5F6G7H8",
  "requestedSessionId": "A1B2C3D4E5F6G7H8",
  "requestedSessionIdFromCookie": true,
  "requestedSessionIdFromURL": false,
  "requestedSessionIdValid": true
}
```

### 5. **Content Information**
```json
{
  "contentType": "application/x-www-form-urlencoded",
  "contentLength": 256,
  "characterEncoding": "UTF-8"
}
```

### 6. **Security Information**
```json
{
  "secure": false,
  "authType": null,
  "remoteUser": null,
  "referer": "https://example.com/previous-page",
  "origin": "https://example.com"
}
```

### 7. **Response Metadata**
```json
{
  "timestamp": "2024-01-15T10:30:45.456",
  "status": 200,
  "contentType": "text/html;charset=UTF-8",
  "characterEncoding": "UTF-8",
  "method": "GET",
  "uri": "/tenant/admin"
}
```

## Configuration

### 1. **Application Properties**
```properties
## Request Logging Configuration
request.logging.enabled=true
request.logging.log-headers=true
request.logging.log-device-info=true
request.logging.log-session-info=true
request.logging.log-security-info=false
```

### 2. **Environment Variables**
```bash
# Enable/disable request logging
export REQUEST_LOGGING_ENABLED=true

# Control what information is logged
export REQUEST_LOGGING_HEADERS=true
export REQUEST_LOGGING_DEVICE_INFO=true
export REQUEST_LOGGING_SESSION_INFO=true
export REQUEST_LOGGING_SECURITY_INFO=false
```

### 3. **Excluded Paths**
The following paths are automatically excluded from logging:
- `/static/**` - Static resources
- `/css/**` - CSS files
- `/js/**` - JavaScript files
- `/images/**` - Image files
- `/favicon.ico` - Favicon requests
- `/actuator/**` - Spring Boot actuator endpoints

### 4. **Sensitive Headers**
The following headers are automatically excluded for security:
- `Authorization`
- `Cookie`
- `X-API-Key`
- `X-Auth-Token`
- `X-Access-Token`
- `X-CSRF-Token`
- `X-Forwarded-For`
- `X-Real-IP`

## Log Output Examples

### 1. **Incoming Request Log**
```
2024-01-15 10:30:45.123 INFO  [http-nio-8080-exec-1] c.t.i.i.RequestLoggingInterceptor : INCOMING_REQUEST: {"timestamp":"2024-01-15T10:30:45.123","method":"GET","uri":"/tenant/admin","queryString":"page=1&size=10","protocol":"HTTP/1.1","remoteAddr":"192.168.1.100","remoteHost":"client.example.com","remotePort":54321,"serverName":"localhost","serverPort":8080,"contextPath":"","servletPath":"/tenant/admin","pathInfo":null,"userAgent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.109 Safari/537.36","deviceInfo":{"browser":"Chrome","browserVersion":"120.0.6099.109","os":"Windows","osVersion":"Windows 10/11","device":"Desktop","deviceModel":"Unknown","isBot":false,"isTouchDevice":false,"is64Bit":true},"headers":{"Accept":"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8","Accept-Encoding":"gzip, deflate, br","Accept-Language":"en-US,en;q=0.5","Cache-Control":"no-cache","Connection":"keep-alive","Host":"localhost:8080","Upgrade-Insecure-Requests":"1"},"contentType":null,"contentLength":-1,"characterEncoding":"UTF-8","sessionId":"A1B2C3D4E5F6G7H8","requestedSessionId":"A1B2C3D4E5F6G7H8","requestedSessionIdFromCookie":true,"requestedSessionIdFromURL":false,"requestedSessionIdValid":true,"locale":"en_US","acceptLanguage":"en-US,en;q=0.5","secure":false,"authType":null,"remoteUser":null,"referer":"https://example.com/previous-page","origin":"https://example.com"}
```

### 2. **Response Log**
```
2024-01-15 10:30:45.456 INFO  [http-nio-8080-exec-1] c.t.i.i.RequestLoggingInterceptor : RESPONSE_SENT: {"timestamp":"2024-01-15T10:30:45.456","status":200,"contentType":"text/html;charset=UTF-8","characterEncoding":"UTF-8","method":"GET","uri":"/tenant/admin"}
```

## Device Detection Examples

### 1. **Chrome on Windows**
```
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.109 Safari/537.36
Device Info: {
  "browser": "Chrome",
  "browserVersion": "120.0.6099.109",
  "os": "Windows",
  "osVersion": "Windows 10/11",
  "device": "Desktop",
  "deviceModel": "Unknown",
  "isBot": false,
  "isTouchDevice": false,
  "is64Bit": true
}
```

### 2. **Safari on iPhone**
```
User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 17_1_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1.2 Mobile/15E148 Safari/604.1
Device Info: {
  "browser": "Safari",
  "browserVersion": "17.1.2",
  "os": "iOS",
  "osVersion": "17.1.2",
  "device": "Mobile",
  "deviceModel": "iPhone",
  "isBot": false,
  "isTouchDevice": true,
  "is64Bit": false
}
```

### 3. **Google Bot**
```
User-Agent: Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)
Device Info: {
  "browser": "Unknown",
  "browserVersion": "Unknown",
  "os": "Unknown",
  "osVersion": "Unknown",
  "device": "Unknown",
  "deviceModel": "Unknown",
  "isBot": true,
  "isTouchDevice": false,
  "is64Bit": false
}
```

## Performance Considerations

### 1. **Logging Overhead**
- **Minimal Impact**: Interceptor adds minimal overhead to requests
- **Asynchronous Logging**: Consider using async logging for high-traffic applications
- **Selective Logging**: Use configuration to disable logging for specific paths

### 2. **Memory Usage**
- **Header Filtering**: Sensitive headers are excluded to reduce memory usage
- **JSON Serialization**: ObjectMapper is reused to minimize object creation
- **Error Handling**: Prevents memory leaks from logging errors

### 3. **Storage Considerations**
- **Log Volume**: Request logging can generate significant log volume
- **Log Rotation**: Ensure proper log rotation is configured
- **Log Aggregation**: Consider using log aggregation tools for production

## Security Considerations

### 1. **Sensitive Data Protection**
- **Header Filtering**: Sensitive headers are automatically excluded
- **Session Data**: Session IDs are logged but not session content
- **Authentication**: Authentication tokens are not logged

### 2. **Privacy Compliance**
- **IP Addresses**: Client IP addresses are logged (consider GDPR implications)
- **User Agents**: Full user agent strings are logged
- **Referrers**: Referrer URLs are logged

### 3. **Configuration Security**
- **Environment Variables**: Use environment variables for sensitive configuration
- **Access Control**: Restrict access to log files
- **Log Monitoring**: Monitor logs for suspicious activity

## Monitoring and Analytics

### 1. **Request Patterns**
- **Popular Endpoints**: Identify most frequently accessed endpoints
- **User Behavior**: Track user navigation patterns
- **Performance Metrics**: Monitor request/response times

### 2. **Device Analytics**
- **Browser Usage**: Track browser market share
- **OS Distribution**: Monitor operating system usage
- **Mobile vs Desktop**: Track device type distribution

### 3. **Security Monitoring**
- **Bot Detection**: Identify and monitor bot traffic
- **Suspicious Activity**: Detect unusual request patterns
- **Attack Vectors**: Monitor for potential security threats

## Troubleshooting

### 1. **Common Issues**
- **Missing Logs**: Check if logging is enabled in configuration
- **Performance Impact**: Monitor application performance with logging enabled
- **Log Volume**: Adjust logging configuration for high-traffic applications

### 2. **Configuration Issues**
- **Path Exclusions**: Verify excluded paths are working correctly
- **Header Filtering**: Check that sensitive headers are properly excluded
- **Environment Variables**: Ensure environment variables are set correctly

### 3. **Debugging**
- **Log Level**: Set log level to DEBUG for detailed interceptor information
- **Error Logs**: Check for interceptor errors in application logs
- **Configuration Validation**: Verify configuration properties are loaded correctly

## Future Enhancements

### 1. **Advanced Analytics**
- **Request Correlation**: Correlate requests with user sessions
- **Performance Metrics**: Add request timing and performance data
- **Geolocation**: Add IP-based geolocation information

### 2. **Integration Features**
- **Log Aggregation**: Integration with ELK stack or similar
- **Real-time Monitoring**: Real-time request monitoring dashboard
- **Alerting**: Automated alerts for suspicious activity

### 3. **Customization Options**
- **Custom Fields**: Allow custom fields to be logged
- **Conditional Logging**: Log based on request conditions
- **Sampling**: Configurable request sampling for high-traffic applications

## Conclusion

The request logging system provides comprehensive visibility into application usage patterns, user behavior, and system performance. With configurable options and security considerations, it offers a powerful tool for monitoring, debugging, and analytics while maintaining performance and security standards.





