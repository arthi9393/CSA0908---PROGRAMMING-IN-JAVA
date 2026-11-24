package com.capstone.scheduler.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("appName", "Distributed Task Scheduler");
        return "dashboard";
    }
}
