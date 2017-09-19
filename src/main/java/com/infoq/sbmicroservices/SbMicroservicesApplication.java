package com.infoq.sbmicroservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.infoq.sbmicroservices.dao.ProductDetailRepository;
import com.infoq.sbmicroservices.model.ProductDetail;

@ComponentScan
@EnableAutoConfiguration
public class SbMicroservicesApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(SbMicroservicesApplication.class, args);
		
		ProductDetail detail = new ProductDetail();
		detail.setProductId("ABCD1234");
		detail.setProductName("O livro de Dan sobre a escrita");
		detail.setShortDescription("Um livro sobre como escrever livros.");
		detail.setLongDescription("Neste livro Dan apresenta ao leitor técnicas sobre como escrever livros.");
		detail.setInventoryId("009178461");
		
		ProductDetailRepository repository = ctx.getBean(ProductDetailRepository.class);
		repository.save(detail);
		
		for (ProductDetail productDetail : repository.findAll()) {
			System.out.println(productDetail.getProductId());
		}
	}
}
