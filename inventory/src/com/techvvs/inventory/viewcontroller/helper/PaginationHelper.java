package com.techvvs.inventory.viewcontroller.helper;


import com.techvvs.inventory.model.MenuVO;
import com.techvvs.inventory.model.ProductVO;
import com.techvvs.inventory.modelnonpersist.FileVO;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class PaginationHelper {

//    void addPaginatedListOfProducts(
//            MenuVO menuVO,
//            Model model,
//            Optional<Integer> productpage,
//            Optional<Integer> productsize){
//
//        int page = productpage.orElse(0);
//
//        if(page != 0){
//            page = page - 1;
//        }
//
//        List<ProductVO> productList = menuVO.menu_product_list;
//        Pageable pageable = PageRequest.of(page, productsize.orElse(5), Sort.by(Sort.Direction.ASC, "createTimeStamp"));
//        // Apply sorting manually to the fileList
//
//        int start = Math.min((int) pageable.getOffset(), productList.size());
//        int end = Math.min((start + pageable.getPageSize()), productList.size());
//        List<ProductVO> pagedProducts = productList.subList(start, end);
//
//        model.addAttribute("productPage", new PageImpl<>(pagedProducts, pageable, productList.size()));
//    }



}
