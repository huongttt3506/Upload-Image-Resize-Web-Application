package com.example.ImageProcessing;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/views")
public class ViewsController {

    //home
    @GetMapping("")
    public String showHomePage() {return "home";}
}
