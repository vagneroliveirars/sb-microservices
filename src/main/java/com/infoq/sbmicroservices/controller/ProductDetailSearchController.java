package com.infoq.sbmicroservices.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infoq.sbmicroservices.dao.ProductDetailRepository;

@RestController
@RequestMapping("/search")
public class ProductDetailSearchController {
	
	private final ProductDetailRepository repository;
	
    @Autowired
    public ProductDetailSearchController(ProductDetailRepository repository) {
        this.repository = repository;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public List search(@RequestParam("q") String queryTerm) {
    	List productDetails = repository.search("%"+queryTerm+"%");
        return productDetails == null ? new ArrayList<>() : productDetails;
    }

}
