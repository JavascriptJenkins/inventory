package com.techvvs.inventory.service.expense

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.ExpenseRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.ExpenseVO
import com.techvvs.inventory.model.RewardConfigVO
import com.techvvs.inventory.model.ExpenseVO
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import java.time.LocalDateTime

@Service
class ExpenseService {

    @Autowired
    BatchRepo batchRepo

    @Autowired
    ExpenseRepo expenseRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    void logExpenseToBatch(ExpenseVO expenseVO, int batchid) {

        // validate the expenseVO


        // fetch the batchvo from database based on batchid
        BatchVO batchVO = batchRepo.findById(batchid).get()

        // fill in missing expenseVO fields
        expenseVO.batch = batchVO

        // save the expenseVO


    }


    ExpenseVO validateExpense(ExpenseVO expenseVO, Model model) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(expenseVO, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(expenseVO, model)

        // third - do any business logic / page specific validation below

        return expenseVO
    }


    void getExpense(Integer expenseid, Model model) {
        ExpenseVO expenseVO = findExpenseById(expenseid)
        if (expenseVO != null) {
            model.addAttribute("expense", expenseVO)
        } else {
            loadBlankExpense(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "Expense not found.")
        }
    }

    ExpenseVO findExpenseById(Integer expenseid) {
        Optional<ExpenseVO> expenseVO = expenseRepo.findById(expenseid)
        if (expenseVO.isPresent()) {
            return expenseVO.get()
        }
        return null
    }


    void loadBlankExpense(Model model) {
        model.addAttribute("expense", new ExpenseVO(expenseid: 0))
    }


    void addPaginatedData(Model model, Optional<Integer> page, Optional<Integer> size) {

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(100);
        Pageable pageable;
        if (currentPage == 0) {
            pageable = PageRequest.of(0, pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<ExpenseVO> pageOfExpense = expenseRepo.findAll(pageable)

//        //filter out soft deleted rewardconfig records
//        pageOfRewardConfig.filter { it.deleted == 0 }

        int totalPages = pageOfExpense.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while (totalPages > 0) {
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfExpense.getTotalPages());
        model.addAttribute("expensePage", pageOfExpense);
    }


    ExpenseVO createExpense(ExpenseVO expense) {
        expense.createTimeStamp = LocalDateTime.now()
        expense.updateTimeStamp = LocalDateTime.now()
        expense = expenseRepo.save(expense)
        return expense
    }


    void updateExpense(ExpenseVO expenseVO, Model model) {
        validateExpense(expenseVO, model)


        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {

            // check for existing rewards config with existing region
            expenseVO  = checkForExistingExpense(expenseVO)


            if (expenseVO.expenseid > 0) {

                expenseVO.updateTimeStamp = LocalDateTime.now()
                try {
                    expenseRepo.save(expenseVO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "Expense updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed")
                }
            } else {
                // If it's a new expense, use create method
                expenseVO = createExpense(expenseVO)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Expense created successfully!")
            }
        }
        model.addAttribute("expense", expenseVO)
    }


    // This ensures that if user submits an existing region, it will not be overwritten
    ExpenseVO checkForExistingExpense(ExpenseVO expenseVO){

        Optional<ExpenseVO> existingExpense = expenseRepo.findById(expenseVO.expenseid)
        if(existingExpense.isPresent()){
            // make sure the id is set, because user could have submitted it with id of 0 thinking they were going to create a new entry for same region.....
            expenseVO.expenseid = existingExpense.get().expenseid
            expenseVO.updateTimeStamp = existingExpense.get().updateTimeStamp
            expenseVO.createTimeStamp = existingExpense.get().createTimeStamp
            return expenseVO
        } else {
            expenseVO
        }
    }

}
