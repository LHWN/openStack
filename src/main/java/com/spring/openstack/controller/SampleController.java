package com.spring.openstack.controller;

import com.spring.openstack.service.ComputeResourceService;
import lombok.extern.slf4j.Slf4j;
import org.openstack4j.openstack.compute.domain.NovaServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
public class SampleController {
    private final ComputeResourceService computeResourceService;

    @Autowired
    public SampleController(ComputeResourceService computeResourceService) {
        this.computeResourceService = computeResourceService;
    }

    @GetMapping(value = {"", "/", "/login"})
    public String loginPage() {
        return "login";
    }

    @GetMapping("/server/list")
    public String serverList(Model model) {
        List<NovaServer> serverList = computeResourceService.getAllComputeList(true);
        model.addAttribute("serverList", serverList);
        return "server/list";
    }
}
