package com.example.demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}

@Configuration
@LoadBalancerClient(value = "fraud-verifier")
class RestTemplateConfig {

	@Bean
	@LoadBalanced
	RestTemplate loadBalancedRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Qualifier("restTemplate")
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

@RestController
@RequestMapping("/users")
class UserController {
	Set<User> userSet = new HashSet<>();

	private final VerificationClient verificationClient;

	UserController(VerificationClient verificationClient) {
		this.verificationClient = verificationClient;
	}


	@GetMapping
	public Set<User> userSet() {
		if(userSet.size() == 0) {
			userSet.add(new User(1L, "Bob", 19L));
			userSet.add(new User(2L, "Josh", 19L));
			userSet.add(new User(3L, "Sam", 19L));
		}

		return userSet;
	}

	@PostMapping(path = "/register")
	public ResponseEntity<String> registerNewUser(@RequestBody User user) {
		System.out.println("USER Object received in request");
		//System.out.println(user);
		if(verificationClient.verifyNewUser(user.getId(), user.getAge()).equalsIgnoreCase("ok")) {
			userSet.add(new User(user.getId(), user.getName(), user.getAge()));
			return ResponseEntity.ok("OK");
		}

		return ResponseEntity.ok("NOT-OK");
	}
}

/*@RestController
@RequestMapping("/register")
class NewUserRegister {

	NewUserRegister(VerificationClient verificationClient) {
		this.verificationClient = verificationClient;
	}


}*/


@Service
class VerificationClient {
	private final RestTemplate restTemplate;
	private final DiscoveryClient discoveryClient;

	VerificationClient(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
		this.restTemplate = restTemplate;
		this.discoveryClient = discoveryClient;
	}

	public String verifyNewUser(Long id, Long age) {
		List<ServiceInstance> instances = discoveryClient.getInstances("proxy");
		//System.out.println("Printing instances");
		instances.forEach(System.out::println);
		ServiceInstance instance = instances.stream().findAny()
						.orElseThrow(() -> new IllegalStateException("No proxy instance available"));

		//System.out.println("Creating URI Component builder");
		//System.out.println(instance.getUri().toString());
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
						.fromHttpUrl(instance.getUri()
										.toString() + "/fraud-verifier/users/verify")
						.queryParam("id", id)
						.queryParam("age", age);

		//System.out.println(uriComponentsBuilder.toUriString());

		return restTemplate.getForObject(uriComponentsBuilder.toUriString(), String.class);

	}

}
