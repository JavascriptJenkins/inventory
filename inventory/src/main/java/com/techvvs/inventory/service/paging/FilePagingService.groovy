package com.techvvs.inventory.service.paging

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.util.TechvvsFileHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class FilePagingService {

    @Autowired
    TechvvsFileHelper techvvsFileHelper

    @Autowired
    AppConstants appConstants

    Page<FileVO> getFilePage(ProductVO productVO, Integer page, Integer size, String selected) {
        Page<FileVO> filePage = techvvsFileHelper.getPagedFilesByDirectory(
                appConstants.PARENT_LEVEL_DIR+productVO.productnumber+selected,
                page,
                size
        );
        return filePage;
    }


    Page<FileVO> getFilePage(BatchVO batchVO, Integer page, Integer size, String selected) {
        Page<FileVO> filePage = techvvsFileHelper.getPagedFilesByDirectory(
                appConstants.PARENT_LEVEL_DIR+batchVO.batchnumber+selected,
                page,
                size
        );
        return filePage;
    }

    Page<FileVO> getFilePageFromDirectory(Integer page, Integer size, String dir) {

        // attach the filelist to the model
        Page<FileVO> filePage = techvvsFileHelper.getPagedFilesByDirectory(
                dir,
                page,
                size
        );
        return filePage
    }

    void bindPageAttributesToModel(Model model, Page<FileVO> filePage, Optional<Integer> page, Optional<Integer> size) {


        int currentPage = page.orElse(0);
        int totalPages = filePage.getTotalPages(); // for some reason there needs to be a -1 here ... dont ask ...

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", filePage.getTotalPages());
        model.addAttribute("filePage", filePage);
    }


}
