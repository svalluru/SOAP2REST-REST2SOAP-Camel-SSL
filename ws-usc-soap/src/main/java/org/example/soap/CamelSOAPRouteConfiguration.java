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

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.ServletComponent;
import org.apache.camel.component.undertow.UndertowComponent;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.stereotype.Component;

@Component
public class CamelSOAPRouteConfiguration extends RouteBuilder {
	
	//SOAP Calling REST

	@Override
	public void configure() {
		
		
	   KeyStoreParameters tsp = new KeyStoreParameters();
	      tsp.setResource("/Users/svalluru/work/USCellular/ws-usc-soap/src/main/resources/client.truststore");
	      tsp.setPassword("secret");
	   TrustManagersParameters tmp = new TrustManagersParameters();
	      tmp.setKeyStore(tsp);
	      
	      
	   SSLContextParameters sslContextParameters = new SSLContextParameters();
	   sslContextParameters.setTrustManagers(tmp);
	   ModelCamelContext context = getContext();
	   //ServletComponent component = context.getComponent("servlet", ServletComponent.class);
	   //component.getCamelContext().setSSLContextParameters(sslContextParameters);
	   
	   UndertowComponent utc = context.getComponent("undertow", UndertowComponent.class);
	   utc.setSslContextParameters(sslContextParameters);
	   
		from("cxf:{{application.soap2rest.bindAddress}}?serviceClass=org.apache.camel.examples.soap2rest.Order&wsdlURL=OrderService.wsdl")
		.routeId("soap2restConsumerRoute")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				MessageContentsList body = exchange.getIn().getBody(MessageContentsList.class);
				System.out.println("-- Received Order -- ");
				System.out.println("Customer ID : "+ body.get(0));
				System.out.println("Item ID : " +body.get(1));
				System.out.println("Qty : " + body.get(2));
				getContext();
			}
		})
		.setHeader(Exchange.HTTP_METHOD, constant("GET"))
		//.to("cxfrs:https://localhost:7443/rest/orders/order?httpClientAPI=true")
		.to("undertow:https://localhost:7443/rest/orders/order?useGlobalSslContextParameters=true")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				String body = exchange.getIn().getBody(String.class);
				String res = null;
				if(body.contains("101")) {
					res = "101";
				}else {
					res= "Order Creation Failed !!";
				}
				List<Object> resultList = new ArrayList<Object>(); 
				resultList.add(res); 
				exchange.getOut().setBody(resultList);
			}
		});
		;




		//REST Calling SOAP
		from("cxf:{{application.rest2soap.bindAddress}}?serviceClass=org.apache.camel.examples.soap2rest.Order&wsdlURL=OrderService.wsdl&defaultOperationName=shippingDetails")
		.routeId("rest2soapConsumerRoute")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getIn().setBody("{ 'Customer Name' : 'John', 'Order ID' : 101, 'Shipping Estimated Date' : '05/01/2021' }");
			}
		})
		;
	}

}
