package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.service.paging.FilePagingService
import org.apache.catalina.connector.ClientAbortException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRange
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.DigestUtils
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import javax.servlet.http.HttpServletResponse
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@RequestMapping("/video")
@Controller
class VideoController {

    @Autowired
    AppConstants appConstants

    @Autowired
    FilePagingService filePagingService

    @Autowired
    ProductRepo productRepo

    @GetMapping("/videos/{productid}/{filename}")
    void streamVideo(
            @PathVariable String productid,
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatchHeader,

            HttpServletResponse response) throws IOException {

        // Resolve the file path
        Path videoPath = Paths.get(appConstants.UPLOAD_DIR + "media/product/" + productid + "/videos").resolve(filename)

        if (!Files.exists(videoPath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        RandomAccessFile videoFile = null
        try {
            videoFile = new RandomAccessFile(videoPath.toFile(), "r")
            long fileLength = videoFile.length()
            long lastModifiedTime = Files.getLastModifiedTime(videoPath).toMillis()
            String contentType = filename.endsWith(".mov") ? "video/quicktime" : "video/mp4"
            response.setContentType(contentType)

            // Generate an ETag using file properties
            String eTag = "\"" + DigestUtils.md5DigestAsHex((filename + fileLength + lastModifiedTime).getBytes()) + "\""
            response.setHeader("ETag", eTag)

            // Check If-None-Match header for cache validation
            if (ifNoneMatchHeader && ifNoneMatchHeader == eTag) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
                return
            }

            // Add caching headers
            response.setHeader("Cache-Control", "public, max-age=2592000") // Cache for 30 days
            response.setDateHeader("Last-Modified", lastModifiedTime)

            if (rangeHeader == null) {
                // Serve the entire file
                response.setContentLengthLong(fileLength)

                // Stream file using a buffer
                InputStream inputStream = new FileInputStream(videoPath.toFile())
                byte[] buffer = new byte[8192] // 8 KB buffer
                int bytesRead
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead)
                }
                inputStream.close()
            } else {
                // Parse the Range header for partial content
                HttpRange range = HttpRange.parseRanges(rangeHeader).get(0)
                long start = range.getRangeStart(fileLength)
                long end = range.getRangeEnd(fileLength)
                long chunkSize = end - start + 1

                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)
                response.setHeader("Content-Range", "bytes ${start}-${end}/${fileLength}")
                response.setContentLengthLong(chunkSize)

                // Serve the requested range
                videoFile.seek(start)
                byte[] buffer = new byte[8192] // 8 KB buffer
                long bytesRemaining = chunkSize
                while (bytesRemaining > 0) {
                    int bytesRead = videoFile.read(buffer, 0, Math.min(buffer.length, (int) bytesRemaining))
                    if (bytesRead == -1) {
                        break
                    }
                    response.getOutputStream().write(buffer, 0, bytesRead)
                    bytesRemaining -= bytesRead
                }
            }
        } catch (ClientAbortException e) {
            // Log and ignore client abort exceptions
            println("Client aborted the connection: ${e.message}")
        } catch (IOException e) {
            // Handle other IOExceptions
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            println("Error streaming video: ${e.message}")
        } finally {
            if (videoFile != null) {
                videoFile.close()
            }
        }
    }




    // this serves the default menu for the batch
    @GetMapping("/product")
    String getVideoListForProduct(
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("productid") Optional<String> productid,
            @RequestParam("page") Optional<String> page,
            @RequestParam("size") Optional<String> size
    ){

        // this needs to get a list of FileVO's that are populated with info from the video folder
        ProductVO productVO = productRepo.findById(Integer.valueOf(productid.get())).get(

        )

        Page<FileVO> filePage = filePagingService.getFilePageForProductUploadMedia(productVO, Integer.valueOf(page.orElse("0")), Integer.valueOf(size.orElse("5")), Paths.get(appConstants.UPLOAD_DIR_MEDIA+appConstants.UPLOAD_DIR_PRODUCT+productid.get()+appConstants.UPLOAD_DIR_PRODUCT_VIDEOS).toString())

        if(filePage.size() > 0){
            model.addAttribute("filePage", filePage);
        } else {
            model.addAttribute("filePage", null);
        }

        model.addAttribute("menuid", menuid.orElse("0"));// bind this for the back button
        model.addAttribute("productid", productid.orElse("0"));// bind this for the back button

        // fetch all customers from database and bind them to model
        //checkoutHelper.getAllCustomers(model)
        //techvvsAuthService.checkuserauth(model)
        return "menu/videoplayer.html";
    }


}
