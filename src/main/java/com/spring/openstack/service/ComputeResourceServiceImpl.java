package com.spring.openstack.service;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("ComputeResourceService")
public class ComputeResourceServiceImpl implements ComputeResourceService {
    private final OSClient.OSClientV3 osClient;

    @Autowired
    public ComputeResourceServiceImpl(OSClient.OSClientV3 osClient) {
        this.osClient = osClient;
    }

    public List<? extends Server> getComputeList() {
        return osClient.compute().servers().list();
    }

    public List<? extends Server> getComputeList(Map<String, String> param) {
        return osClient.compute().servers().list(param);
    }

    public List<? extends Server> getComputeDetailList(boolean detail) {
        return osClient.compute().servers().list(detail);
    }

    public List<? extends Server> getAllComputeList(boolean detail) {
        return osClient.compute().servers().listAll(detail);
    }
}
