package com.techvvs.inventory.validation


import com.techvvs.inventory.model.TaskVO
import org.springframework.stereotype.Component

@Component
class ValidateTask {


    String validateNewFormInfo(TaskVO taskVO){

        if(taskVO.getName() != null &&
                (taskVO.getName().length() > 250
                        || taskVO.getName().length() < 1)
        ){
            return "first name must be between 1-250 characters. ";
        }



        if(taskVO.getNotes() != null && (taskVO.getNotes().length() > 1000)
        ){
            return "Notes must be less than 1000 characters";
        }

        return "success";
    }




}
