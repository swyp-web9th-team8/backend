package com.swyp.plogging.backend.controller;

import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.print.Pageable;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @GetMapping("")
    public String getListOfNotices(@PageableDefault(size = 10, sort = "createdDt",  direction = Sort.Direction.DESC) Pageable pageable){
        return "";
    }
}
