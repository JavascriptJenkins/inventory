//package com.techvvs.inventory.viewcontroller
//
//import com.techvvs.inventory.constants.MessageConstants
//import com.techvvs.inventory.jparepo.BatchRepo
//import com.techvvs.inventory.jparepo.TransferRepo
//import com.techvvs.inventory.model.TransferVO
//import com.techvvs.inventory.service.auth.TechvvsAuthService
//import com.techvvs.inventory.service.metrc.dispensary.MetrcDispensaryService
//import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.stereotype.Controller
//import org.springframework.ui.Model
//import org.springframework.web.bind.annotation.*
//
//@RequestMapping("/metrc/dispensary/transfers")
//@Controller
//public class MetrcDispensaryTransfersViewController {
//
//
//    @Autowired
//    RewardConfigHelper rewardConfigHelper
//
//    @Autowired
//    TechvvsAuthService techvvsAuthService
//
//    @Autowired
//    MetrcDispensaryService metrcDispensaryService
//
//    @Autowired
//    BatchRepo batchRepo
//
//    @Autowired
//    TransferRepo transferRepo
//
//    // log expenses to a batch
//    @GetMapping
//    String viewNewForm(
//            Model model,
//
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size,
//            @RequestParam("transferid") Optional<Integer> transferid
//
//    ){
//
//        techvvsAuthService.checkuserauth(model)
//
//        // attach a blank object to the model
//        if(transferid.isPresent()){
//            metrcDispensaryService.getTransfer(transferid.get(), model)
//        } else {
//            metrcDispensaryService.loadBlankTransfer(model)
//        }
//
//        metrcDispensaryService.addPaginatedData(model, page, size)
//
//        // load the values for dropdowns here
//        bindStaticValues(model)
//
//        return "metrc/admin.html";
//    }
//
//    void bindStaticValues(Model model) {
////        model.addAttribute("paymentmethods", PaymentMethod.values());
////        model.addAttribute("expensetypes", ExpenseType.values());
////        model.addAttribute("batches", batchRepo.findAll())
////        model.addAttribute("transfers", transferRepo.findAll())
//    }
//
//
//    @PostMapping("/create")
//    String createTransfer(
//            @ModelAttribute( "transfer" ) TransferVO transferVO,
//            Model model,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//    ){
//        techvvsAuthService.checkuserauth(model)
//
//        transferVO = metrcDispensaryService.validateTransfer(transferVO, model)
//
//        // only proceed if there is no error
//        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
//            // create the customer
//            transferVO = metrcDispensaryService.createTransfer(transferVO)
//            model.addAttribute("successMessage", "Transfer created successfully!")
//        }
//
//        model.addAttribute("transfer", transferVO)
//        metrcDispensaryService.addPaginatedData(model, page, size)
//
//        return "transfer/admin.html";
//    }
//
//    @PostMapping("/edit")
//    String editTransfer(
//            @ModelAttribute( "transfer" ) TransferVO transferVO,
//            Model model,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//    ) {
//        techvvsAuthService.checkuserauth(model)
//        metrcDispensaryService.updateTransfer(transferVO, model)
//        metrcDispensaryService.addPaginatedData(model, page, size)
//        return "transfer/admin.html"
//    }
//
//    @GetMapping("/get")
//    String getRewardConfig(
//            @RequestParam("customerid") Integer customerid,
//            Model model,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//    ) {
//        rewardConfigHelper.getRewardConfig(customerid, model)
//        rewardConfigHelper.addPaginatedData(model, page, size)
//        techvvsAuthService.checkuserauth(model)
//        return "customer/customer.html"
//    }
//
//    @PostMapping("/delete")
//    String deleteRewardConfig(
//            @RequestParam("customerid") Integer customerid,
//            Model model,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//    ) {
//        rewardConfigHelper.deleteRewardConfig(customerid, model)
//        rewardConfigHelper.addPaginatedData(model, page, size)
//        techvvsAuthService.checkuserauth(model)
//        return "customer/customer.html"
//    }
//}
