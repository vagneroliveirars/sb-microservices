package com.infoq.sbmicroservices.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

@Service
public class ProductService {

	private static final String GROUP = "products";
	private static final int TIMEOUT = 60000;
	private final ProductDetailService productDetailService;
	private final ProductPricingService productPricingService;
	private final ProductRatingService productRatingService;
	private final ProductReviewService productReviewService;

	@Autowired
	public ProductService(ProductDetailService productDetailService, ProductPricingService productPricingService,
			ProductRatingService productRatingService, ProductReviewService productReviewService) {
		this.productDetailService = productDetailService;
		this.productPricingService = productPricingService;
		this.productRatingService = productRatingService;
		this.productReviewService = productReviewService;
	}

	public Map getProductSummary(String productId) {
		List callables = new ArrayList<>();
		callables.add(new BackendServiceCallable("details", getProductDetails(productId)));
		callables.add(new BackendServiceCallable("pricing", getProductPricing(productId)));
		return doBackendAsyncServiceCall(callables);
	}

	public Map getProduct(String productId) {
		List callables = new ArrayList<>();
		callables.add(new BackendServiceCallable("details", getProductDetails(productId)));
		callables.add(new BackendServiceCallable("pricing", getProductPricing(productId)));
		callables.add(new BackendServiceCallable("ratings", getProductRatings(productId)));
		callables.add(new BackendServiceCallable("reviews", getProductReviews(productId)));
		return doBackendAsyncServiceCall(callables);
	}

	private static Map doBackendAsyncServiceCall(List callables) {
		ExecutorService executorService = Executors.newFixedThreadPool(4);
		try {
			List<Future> futures = executorService.invokeAll(callables);
			executorService.shutdown();
			executorService.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
			Map result = new HashMap<>();
			for (Future future : futures) {
				AsyncResponse response = (AsyncResponse) future.get();
				result.put(response.serviceKey, response.response);
			}
			return result;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Cacheable
	private HystrixCommand getProductDetails(String productId) {
		return new HystrixCommand(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(GROUP))
				.andCommandKey(HystrixCommandKey.Factory.asKey("getProductDetails")).andCommandPropertiesDefaults(
						HystrixCommandProperties.Setter().withExecutionIsolationThreadTimeoutInMilliseconds(TIMEOUT))) {

			@Override
			protected Map run() throws Exception {
				return productDetailService.getDetails(productId);
			}

			@Override
			protected Map getFallback() {
				return new HashMap<>();
			}

		};
	}

	private HystrixCommand getProductPricing(String productId) {
		// ... veja getProductDetails() ...
		return null;
	}

	private HystrixCommand getProductRatings(String productId) {
		// ... veja getProductDetails() ...
		return null;
	}

	private HystrixCommand getProductReviews(String productId) {
		// ... veja getProductDetails() ...
		return null;
	}

	private static class AsyncResponse {
		private final String serviceKey;
		private final Map response;

		AsyncResponse(String serviceKey, Map response) {
			this.serviceKey = serviceKey;
			this.response = response;
		}
	}

	private static class BackendServiceCallable implements Callable {
		private final String serviceKey;

		private final HystrixCommand hystrixCommand;

		public BackendServiceCallable(String serviceKey, HystrixCommand hystrixCommand) {
			this.serviceKey = serviceKey;
			this.hystrixCommand = hystrixCommand;
		}

		@Override
		public AsyncResponse call() throws Exception {
			return new AsyncResponse(serviceKey, (Map) hystrixCommand.execute());
		}
	}

}
