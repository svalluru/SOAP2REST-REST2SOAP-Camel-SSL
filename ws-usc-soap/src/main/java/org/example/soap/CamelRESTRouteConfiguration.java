/*
 * Copyright (C) Red Hat, Inc.
 * http://www.redhat.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example.soap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CamelRESTRouteConfiguration extends RouteBuilder {

	
	//REST Calling SOAP

	@Override
	public void configure() {

		restConfiguration().component("servlet")
		.bindingMode(RestBindingMode.json)
		.producerComponent("http4")
		.contextPath("/service");

		rest("/orders")
		.get("/shipping/{id}")
		.produces("application/json")
		.to("direct:processShippingDetails");
		

		from("direct:processShippingDetails")
		   .setBody().simple("${headers.id}")
           .unmarshal().json(JsonLibrary.Jackson)
           .process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				ProducerTemplate template = exchange.getContext().createProducerTemplate();
				MessageContentsList msg = (MessageContentsList)template.requestBody("cxf://http://0.0.0.0:7443/soap/rest2soap/OrderService?serviceClass=org.apache.camel.examples.soap2rest.Order&defaultOperationName=shippingDetails&skipPayloadMessagePartCheck=true",exchange.getIn().getBody());
				Object object = msg.get(0);
				System.out.println(object.toString());
				exchange.getIn().setBody(object);
			}
		});
		
		//SOAP Calling REST

		rest("/orders")
		.get("/order")
		.produces("application/json")
		.to("direct:processOrders");
		
		from("direct:processOrders")
		.log("ordered")
		.setBody().simple("{\n"
				+ "	\"Order ID\" : 101\n"
				+ "}");

	}
	
	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), "/rest/*");
		registration.setName("CamelServlet");
		return registration;
	}

}
